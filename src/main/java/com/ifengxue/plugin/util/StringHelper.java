package com.ifengxue.plugin.util;

import com.ifengxue.plugin.adapter.DriverAdapter;
import com.ifengxue.plugin.adapter.MysqlDriverAdapter;
import com.ifengxue.plugin.adapter.PostgreSQLDriverAdapter;
import com.intellij.openapi.diagnostic.Logger;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 */
public class StringHelper {

  private static Logger log = Logger.getInstance(StringHelper.class);
  private static final Map<Class<?>, Class<?>> WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE = new HashMap<>();

  static {
    WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.put(Boolean.class, boolean.class);
    WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.put(Integer.class, int.class);
    WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.put(Float.class, float.class);
    WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.put(Long.class, long.class);
    WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.put(Double.class, double.class);
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
   * 获取封装类型的基本数据类型
   */
  public static Class<?> getPrimitiveClass(Class<?> clazz) {
    return WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.getOrDefault(clazz, clazz);
  }

  public static Class<?> parseJavaDataType(DriverAdapter driverAdapter, String dbDataType, String columnName,
      boolean useWrapper, boolean useJava8DataType) {
    Class<?> javaDataType = null;
    if (driverAdapter instanceof MysqlDriverAdapter) {
      javaDataType = parseJavaDataType((MysqlDriverAdapter) driverAdapter, dbDataType, columnName, useWrapper);
    }
    if (driverAdapter instanceof PostgreSQLDriverAdapter) {
      javaDataType = parseJavaDataType((PostgreSQLDriverAdapter) driverAdapter, dbDataType, columnName, useWrapper);
    }
    if (javaDataType == null) {
      throw new IllegalStateException("不支持的类型:" + driverAdapter.getClass().getName());
    }
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

  private static Class<?> parseJavaDataType(MysqlDriverAdapter driverAdapter, String dbDataType,
      String columnName, boolean useWrapper) {
    dbDataType = dbDataType.toUpperCase();
    Class<?> javaDataType;
    switch (dbDataType) {
      case "CLOB":
      case "TEXT":
      case "VARCHAR":
      case "CHAR":
        javaDataType = String.class;
        break;
      case "BLOB":
        javaDataType = byte[].class;
        break;
      case "ID":
      case "BIGINT":
      case "INTEGER":
        javaDataType = Long.class;
        break;
      case "TINYINT":
      case "SMALLINT":
      case "INT":
      case "MEDIUMINT":
        javaDataType = Integer.class;
        break;
      case "BIT":
        javaDataType = Boolean.class;
        break;
      case "FLOAT":
        javaDataType = Float.class;
        break;
      case "DOUBLE":
        javaDataType = Double.class;
        break;
      case "DECIMAL":
        javaDataType = BigDecimal.class;
        break;
      case "YEAR":
        javaDataType = Short.class;
        break;
      case "DATE":
        javaDataType = java.sql.Date.class;
        break;
      case "TIME":
        javaDataType = Time.class;
        break;
      case "DATETIME":
      case "TIMESTAMP":
        javaDataType = Timestamp.class;
        break;
      default:
        javaDataType = String.class;
        log.warn("不支持的数据库类型:" + dbDataType + "，用String替代");
        break;
    }
    if (columnName.startsWith("is_")) {
      javaDataType = Boolean.class;
    }
    if (useWrapper) {
      return javaDataType;
    }
    return WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.getOrDefault(javaDataType, javaDataType);
  }

  private static Class<?> parseJavaDataType(PostgreSQLDriverAdapter driverAdapter, String dbDataType,
      String columnName, boolean useWrapper) {
    dbDataType = dbDataType.toUpperCase();
    Class<?> javaDataType;
    switch (dbDataType) {
      case "CHARACTER VARYING":
      case "TEXT":
      case "VARCHAR":
      case "CHAR":
        javaDataType = String.class;
        break;
      case "BYTEA":
        javaDataType = byte[].class;
        break;
      case "BIGINT":
      case "BIGSERIAL":
        javaDataType = Long.class;
        break;
      case "INT":
      case "SMALLINT":
      case "INTEGER":
        javaDataType = Integer.class;
        break;
      case "BIT":
      case "BOOLEAN":
        javaDataType = Boolean.class;
        break;
      case "REAL":
        javaDataType = Float.class;
        break;
      case "DOUBLE PRECISION":
        javaDataType = Double.class;
        break;
      case "DECIMAL":
      case "NUMERIC":
        javaDataType = BigDecimal.class;
        break;
      case "YEAR":
        javaDataType = Short.class;
        break;
      case "DATE":
        javaDataType = java.sql.Date.class;
        break;
      case "TIME":
        javaDataType = Time.class;
        break;
      case "DATETIME":
      case "TIMESTAMP":
        javaDataType = Timestamp.class;
        break;
      default:
        javaDataType = String.class;
        log.warn("不支持的数据库类型:" + dbDataType + "，用String替代");
        break;
    }
    if (columnName.startsWith("is_")) {
      javaDataType = Boolean.class;
    }
    if (useWrapper) {
      return javaDataType;
    }
    return WRAPPER_DATA_TYPE_AND_PRIMITIVE_DATA_TYPE.getOrDefault(javaDataType, javaDataType);
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
   * column_name_ -> columnName <br>
   * _column_name_ -> columnName <br>
   * COLUMN_NAME -> columnName <br>
   */
  public static String parseFieldName(String columnName) {
    if (!columnName.contains("_")) {
      return columnName;
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
   * 解析列名<br>
   * prefix_column_name -> columnName <br>
   * prefix_column_name_ -> columnName <br>
   * prefix__column_name_ -> columnName <br>
   *
   * @param columnName 字段名称
   * @param removePrefix 要移除的前缀
   */
  public static String parseFieldName(String columnName, String removePrefix) {
    if (removePrefix == null || removePrefix.isEmpty() || !columnName.startsWith(removePrefix)) {
      return parseFieldName(columnName);
    }
    return parseFieldName(columnName.substring(removePrefix.length()));
  }

  /**
   * 解析字段的set方法名称
   */
  public static String parseSetMethodName(String fieldName) {
    return "set" + firstLetterUpper(fieldName);
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