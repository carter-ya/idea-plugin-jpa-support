package com.ifengxue.plugin.util;

import com.ifengxue.plugin.state.SettingsState;
import com.ifengxue.plugin.state.wrapper.ClassWrapper;
import com.intellij.openapi.components.ServiceManager;
import java.beans.Introspector;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * 字符串工具类
 */
public class StringHelper {

  private static final Map<Class<?>, Class<?>> WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE = new HashMap<>();
  private static final Set<Class<?>> DATETIME_CLASSES = new HashSet<>();
  private static final Set<String> JAVA_KEYWORDS = new HashSet<>();

  static {
    WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.put(Boolean.class, boolean.class);
    WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.put(Integer.class, int.class);
    WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.put(Float.class, float.class);
    WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.put(Long.class, long.class);
    WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.put(Double.class, double.class);

    DATETIME_CLASSES.add(Date.class);
    DATETIME_CLASSES.add(java.sql.Date.class);
    DATETIME_CLASSES.add(Timestamp.class);
    DATETIME_CLASSES.add(LocalDate.class);
    DATETIME_CLASSES.add(LocalTime.class);
    DATETIME_CLASSES.add(LocalDateTime.class);
    DATETIME_CLASSES.add(ZonedDateTime.class);

    JAVA_KEYWORDS.add("abstract");
    JAVA_KEYWORDS.add("continue");
    JAVA_KEYWORDS.add("for");
    JAVA_KEYWORDS.add("new");
    JAVA_KEYWORDS.add("switch");
    JAVA_KEYWORDS.add("assert");
    JAVA_KEYWORDS.add("default");
    JAVA_KEYWORDS.add("goto");
    JAVA_KEYWORDS.add("package");
    JAVA_KEYWORDS.add("synchronized");
    JAVA_KEYWORDS.add("boolean");
    JAVA_KEYWORDS.add("do");
    JAVA_KEYWORDS.add("if");
    JAVA_KEYWORDS.add("private");
    JAVA_KEYWORDS.add("this");
    JAVA_KEYWORDS.add("break");
    JAVA_KEYWORDS.add("double");
    JAVA_KEYWORDS.add("implements");
    JAVA_KEYWORDS.add("protected");
    JAVA_KEYWORDS.add("throw");
    JAVA_KEYWORDS.add("byte");
    JAVA_KEYWORDS.add("else");
    JAVA_KEYWORDS.add("import");
    JAVA_KEYWORDS.add("public");
    JAVA_KEYWORDS.add("throws");
    JAVA_KEYWORDS.add("case");
    JAVA_KEYWORDS.add("enum");
    JAVA_KEYWORDS.add("instanceof");
    JAVA_KEYWORDS.add("return");
    JAVA_KEYWORDS.add("transient");
    JAVA_KEYWORDS.add("catch");
    JAVA_KEYWORDS.add("extends");
    JAVA_KEYWORDS.add("int");
    JAVA_KEYWORDS.add("short");
    JAVA_KEYWORDS.add("try");
    JAVA_KEYWORDS.add("char");
    JAVA_KEYWORDS.add("final");
    JAVA_KEYWORDS.add("interface");
    JAVA_KEYWORDS.add("static");
    JAVA_KEYWORDS.add("void");
    JAVA_KEYWORDS.add("class");
    JAVA_KEYWORDS.add("finally");
    JAVA_KEYWORDS.add("long");
    JAVA_KEYWORDS.add("strictfp");
    JAVA_KEYWORDS.add("volatile");
    JAVA_KEYWORDS.add("const");
    JAVA_KEYWORDS.add("float");
    JAVA_KEYWORDS.add("native");
    JAVA_KEYWORDS.add("super");
    JAVA_KEYWORDS.add("while");
  }

  /**
   * 解析基本类型的封装类型
   */
  public static Class<?> getWrapperClass(Class<?> clazz) {
    Map<String, Class<?>> clazzWrapper = new HashMap<>();
    clazzWrapper.put("clazz", clazz);
    WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.forEach((key, value) -> {
      if (value.equals(clazz)) {
        clazzWrapper.put("clazz", key);
      }
    });
    return clazzWrapper.get("clazz");
  }

  /**
   * 是否是日期时间类型
   */
  public static boolean isDatetimeType(Class<?> clazz) {
    return DATETIME_CLASSES.contains(clazz);
  }

  /**
   * 获取封装类型的基本数据类型
   */
  public static Class<?> getPrimitiveClass(Class<?> clazz) {
    return WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.getOrDefault(clazz, clazz);
  }

  public static Class<?> parseJavaDataType(
      @Nullable Class<?> fallbackJavaDataType, String jdbcTypeName,
      String dbDataType, String columnName, boolean useWrapper, boolean useJava8DataType) {
    Class<?> javaDataType = parseJavaDataType(fallbackJavaDataType, jdbcTypeName, dbDataType, columnName, useWrapper);
    if (!useJava8DataType) {
      return javaDataType;
    }
    switch (javaDataType.getName()) {
      case "java.sql.Date":
        return LocalDate.class;
      case "java.sql.Time":
        return LocalTime.class;
      case "java.sql.Timestamp":
        return LocalDateTime.class;
      default:
        return javaDataType;
    }
  }

  private static Class<?> parseJavaDataType(@Nullable Class<?> fallbackJavaDataType, String jdbcTypeName,
      String dbDataType, String columnName, boolean useWrapper) {
    SettingsState settingsState = ServiceManager.getService(SettingsState.class);
    ClassWrapper classWrapper = settingsState.getOrResetDbTypeToJavaType().get(dbDataType);
    if (classWrapper == null) {
      classWrapper = settingsState.getOrResetDbTypeToJavaType().get(jdbcTypeName);
    }
    if (classWrapper == null && fallbackJavaDataType == null) {
      if (settingsState.isThrowException()) {
        throw new NoMatchTypeException(dbDataType);
      } else {
        return settingsState.getFallbackTypeClass();
      }
    }
    Class<?> javaDataType = classWrapper == null ? fallbackJavaDataType : classWrapper.getClazz();
    if (columnName.startsWith("is_")) {
      javaDataType = Boolean.class;
    }
    if (useWrapper) {
      return javaDataType;
    }
    return WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.getOrDefault(javaDataType, javaDataType);
  }

  /**
   * 移除数组所有维度，获取原始的数据类型
   */
  public static Class<?> expandArray(Class<?> clazz) {
    Class<?> target = clazz;
    while (target.isArray()) {
      target = target.getComponentType();
    }
    return target;
  }

  /**
   * 解析实体名称
   */
  public static String parseEntityName(String className) {
    className = parseSimpleName(className);
    String fieldName = parseFieldName(className);
    return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
  }

  /**
   * 与StringHelper.class.getSimpleName()效果一致
   */
  public static String parseSimpleName(String className) {
    if (className.contains(".")) {
      return className.substring(className.lastIndexOf('.') + 1);
    }
    return className;
  }

  /**
   * 解析列名 <br>
   * column_name -> columnName <br>
   * _column_name_ -> columnName <br>
   * COLUMN_NAME -> columnName <br>
   * ColumnName -> columnName <br>
   */
  public static String parseFieldName(String columnName) {
    columnName = columnName.replaceAll("\\s+", "_");
    if (!columnName.contains("_")) {
      return Introspector.decapitalize(columnName);
    }
    columnName = columnName.toLowerCase();
    if (columnName.startsWith("is_")) {
      columnName = columnName.substring("is_".length());
    }
    if (!columnName.contains("_")) {
      return columnName;
    } else {
      int length = columnName.length();
      StringBuilder sb = new StringBuilder(length);
      for (int i = 0; i < length; i++) {
        if ('_' == columnName.charAt(i) && i < length - 1) {
          sb.append(String.valueOf(columnName.charAt(++i)).toUpperCase());
        } else {
          sb.append(columnName.charAt(i));
        }
      }
      String fieldName = sb.toString().replace("_", "");
      char first = fieldName.charAt(0);
      if (first < 'a') {
        fieldName = fieldName.replaceFirst(String.valueOf(first), String.valueOf(first).toLowerCase());
      }
      return fieldName;
    }
  }

  /**
   * 解析列名<br> prefix_column_name -> columnName <br> prefix_column_name_ -> columnName <br>
   * prefix__column_name_ -> columnName <br> ColumnName -> columnName <br>
   *
   * @param columnName 字段名称
   * @param removePrefixes 要移除的前缀，多个前缀用,分割
   * @param ifJavaKeywordAddSuffix 当解析完成后的字段名是Java关键词，则添加该后缀
   */
  public static String parseFieldName(String columnName, String removePrefixes,
      String ifJavaKeywordAddSuffix) {
    String fieldName = null;
    if (removePrefixes == null || removePrefixes.isEmpty()) {
      fieldName = parseFieldName(columnName);
    } else {
      for (String removePrefix : removePrefixes.split(",")) {
        if (columnName.startsWith(removePrefix)) {
          fieldName = parseFieldName(columnName.substring(removePrefix.length()));
          break;
        }
      }
      if (fieldName == null) {
        fieldName = parseFieldName(columnName);
      }
    }
    if (JAVA_KEYWORDS.contains(fieldName)) {
      fieldName += ifJavaKeywordAddSuffix;
    }
    return fieldName;
  }

  /**
   * 解析字段的set方法名称
   */
  public static String parseSetMethodName(String fieldName) {
    return "set" + firstLetterUpper(fieldName);
  }

  /**
   * 解析字段的set方法名称
   *
   * @param fieldName 字段名称
   * @param type 字段数据类型
   */
  public static String parseGetMethodName(String fieldName, Class<?> type) {
    if (type == boolean.class || type == Boolean.class) {
      return parseIsMethodName(fieldName);
    } else {
      return parseGetMethodName(fieldName);
    }
  }

  /**
   * 解析字段的get方法名称
   */
  public static String parseGetMethodName(String fieldName) {
    return "get" + firstLetterUpper(fieldName);
  }

  /**
   * 解析字段的is方法名称
   */
  public static String parseIsMethodName(String fieldName) {
    return "is" + firstLetterUpper(fieldName);
  }

  /**
   * 解析字段的update方法名称
   */
  public static String parseUpdateMethodName(String fieldName) {
    return "update" + firstLetterUpper(fieldName);
  }

  /**
   * 解析字段转枚举的方法名称
   */
  public static String parseGetEnumByMethodName(String fieldName) {
    return "getEnumBy" + firstLetterUpper(fieldName);
  }

  /**
   * 解析枚举转字段的方法名称
   */
  public static String parseSetEnumMethodName(String fieldName) {
    return "set" + firstLetterUpper(fieldName) + "Enum";
  }

  /**
   * 首字母大写
   */
  public static String firstLetterUpper(String name) {
    return name.replaceFirst(String.valueOf(name.charAt(0)), String.valueOf(name.charAt(0)).toUpperCase());
  }

  /**
   * 首字母小写
   */
  public static String firstLetterLower(String name) {
    return name.replaceFirst(String.valueOf(name.charAt(0)), String.valueOf(name.charAt(0)).toLowerCase());
  }

  /**
   * 包路径转换为父路径
   * 如包路径为 <code>/path/to/package/parent/path/com/foo</code>，包名称为<code>com.foo</code>，则输出路径为<code>/path/to/package/parent/path</code>
   *
   * @param packagePath 包路径
   * @param packageName 包名称
   */
  public static String packagePathTrimToParentFolder(String packagePath, String packageName) {
    Path path = Paths.get(packagePath);
    String packageFolder = packageNameToFolder(packageName);
    Path packageFolderPath = Paths.get(packageFolder);
    if (path.endsWith(packageFolderPath)) {
      for (String subPackage : packageName.split("\\.")) {
        path = path.getParent();
      }
      return path.toString();
    }
    return packagePath;
  }

  /**
   * 包名转文件夹名称
   */
  public static String packageNameToFolder(String packageName) {
    if (packageName.contains(".")) {
      return packageName.replace('.', '/');
    } else {
      return packageName;
    }
  }

  /**
   * 实体包名转接口包名
   */
  public static String entityPackageNameToServicePackageName(String entityPackageName) {
    if (entityPackageName.contains(".")) {
      return entityPackageName.substring(0, entityPackageName.lastIndexOf('.')) + ".service";
    } else {
      return "service";
    }
  }

  /**
   * 实体包名转dao包名
   */
  public static String entityPackageNameToDAOPackageName(String entityPackageName) {
    if (entityPackageName.contains(".")) {
      return entityPackageName.substring(0, entityPackageName.lastIndexOf('.')) + ".dao";
    } else {
      return "dao";
    }
  }

  /**
   * 实体名转mapper包名
   */
  public static String entityPackageNameToMapperPackageName(String entityPackageName) {
    String daoPackageName = entityPackageNameToDAOPackageName(entityPackageName);
    return daoPackageName.substring(0, daoPackageName.length() - "dao".length()) + "mapper";
  }

  /**
   * 对<code>repeat</code>重复拼接<code>times</code>次
   *
   * @param repeat 要进行重复的字符串
   * @param times 重复次数
   */
  public static String repeat(String repeat, int times) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < times; i++) {
      builder.append(repeat);
    }
    return builder.toString();
  }

  /**
   * 将当前字符串按<code>lineSeparator</code>拆分，在每个拆分字符串前添加<code>indent</code>，之后添加<code>lineSeparator</code>
   */
  public static String insertIndentBefore(String string, String lineSeparator, String indent) {
    return Arrays.stream(string.split(lineSeparator))
        .map(line -> indent + line)
        .collect(Collectors.joining(lineSeparator));
  }

}
