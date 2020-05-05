package fastjdbc.entity;

import fastjdbc.BeanUtil;
import fastjdbc.FastJdbcException;
import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * 实体缓存管理器
 */
public class EntityCacheManager {

  private static final ConcurrentHashMap<String, Entity> BEAN_CACHE_MAP = new ConcurrentHashMap<>();

  private EntityCacheManager() {
  }

  public static <T> Entity getEntity(T entity) {
    return getEntity(entity.getClass());
  }

  /**
   * 获取实体
   */
  public static Entity getEntity(Class<?> entityClass) {
    return BEAN_CACHE_MAP.computeIfAbsent(entityClass.getName(), className -> parseEntity(entityClass));
  }

  /**
   * 解析实体类
   */
  private static Entity parseEntity(Class<?> entityClass) {
    Table table = entityClass.getAnnotation(Table.class);
    String tableName = formatName(entityClass.getSimpleName(),
        Optional.ofNullable(table).map(Table::name).orElse(""));

    Map<String, EntityField> entityFieldMap = new HashMap<>();
    Map<String, EntityField> entityColumnMap = new HashMap<>();
    Set<EntityField> orderedFields = new TreeSet<>();
    Entity.EntityBuilder entityBuilder = Entity.builder()
        .beanClass(entityClass).tableName(tableName);
    int primaryKeyCount = 0;
    int versionCount = 0;

    // 字段顺序
    int fieldOrderSequence = 0;
    // 解析属性
    for (Field field : BeanUtil.findOrderedFields(entityClass)) {
      // 跳过static/final修饰的属性
      if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
        continue;
      }
      // 跳过非数据库字段
      if (field.getAnnotation(Transient.class) != null) {
        continue;
      }
      Class<?> typeClass = field.getType();
      String fieldName = field.getName();
      String firstUpperName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
      String readMethodName = "get" + firstUpperName;
      String writeMethodName = "set" + firstUpperName;
      // 解析getter/setter方法
      Method writeMethod = BeanUtil.findMethod(writeMethodName, entityClass, typeClass);
      if (writeMethod == null) {
        continue;
      }
      Method readMethod = BeanUtil.findMethod(readMethodName, entityClass);
      if (readMethod == null && (typeClass == Boolean.class || typeClass == boolean.class)) {
        if (!fieldName.startsWith("is")) {
          readMethodName = "is" + firstUpperName;
        }
        readMethod = BeanUtil.findMethod(readMethodName, entityClass);
      }
      if (readMethod == null) {
        continue;
      }
      // 解析字段
      Column column = field.getAnnotation(Column.class);
      String columnName = formatName(fieldName, Optional.ofNullable(column).map(Column::name).orElse(""));
      boolean isPrimaryKey = field.getAnnotation(Id.class) != null;
      EntityField entityField = EntityField.builder().columnName(columnName).field(field)
          .order(++fieldOrderSequence)
          .readMethod(readMethod)
          .writeMethod(writeMethod).build();
      if (isPrimaryKey) {
        entityBuilder.idField(entityField);
        primaryKeyCount++;
        if (primaryKeyCount > 1) {
          throw new FastJdbcException("实体" + entityClass.getName() + "有超过1个的主键字段");
        }
      } else {
        entityFieldMap.put(fieldName, entityField);
        entityColumnMap.put(columnName, entityField);
        orderedFields.add(entityField);
      }
      // 解析乐观锁字段
      boolean isVersion = field.getAnnotation(Version.class) != null;
      if (isVersion) {
        entityBuilder.versionField(entityField);
        versionCount++;
        if (versionCount > 1) {
          throw new FastJdbcException("实体" + entityClass.getName() + "有超过1个乐观锁字段");
        }
      }
    }
    entityBuilder.nonePrimaryKeyFieldMap(entityFieldMap);
    entityBuilder.nonePrimaryKeyOrderedFields(orderedFields);
    entityBuilder.nonePrimaryKeyColumnMap(entityColumnMap);
    if (primaryKeyCount == 0) {
      throw new FastJdbcException("实体" + entityClass.getName() + "没有主键字段");
    }
    return entityBuilder.build();
  }

  private static String formatName(String rawName, String specifyName) {
    if (!specifyName.isEmpty()) {
      return specifyName;
    }
    return Introspector.decapitalize(rawName);
  }

  /**
   * 字符串转 camelCase
   *
   * @param prefix 字符串前缀
   */
  private static String formatCamelCaseName(String prefix, String rawName) {
    StringBuilder builder = new StringBuilder(prefix.length() + rawName.length());
    builder.append(prefix);
    char[] charArray = rawName.toCharArray();
    boolean preIsUnderline = false;
    for (int index = 0; index < charArray.length; index++) {
      char ch = charArray[index];
      if (ch == '_') {
        preIsUnderline = true;
        continue;
      }
      if (ch >= 'a' && ch <= 'z' && preIsUnderline) {
        builder.append((char) (ch - 32));
        preIsUnderline = false;
      } else {
        builder.append(ch);
        preIsUnderline = false;
      }
    }
    return builder.toString();
  }

  /**
   * 字符串转 snakeCase
   *
   * @param prefix 字符串前缀
   */
  private static String formatSnakeCaseName(String prefix, String rawName) {
    StringBuilder builder = new StringBuilder(prefix.length() + rawName.length() + 10);
    builder.append(prefix);
    char[] charArray = rawName.toCharArray();
    for (char ch : charArray) {
      if (ch >= 'A' && ch <= 'Z') {
        char lastChar = builder.charAt(builder.length() - 1);
        if (lastChar != '_') {
          builder.append('_');
        }
        builder.append((char) (ch + 32));
      } else {
        builder.append(ch);
      }
    }
    return builder.toString();
  }
}
