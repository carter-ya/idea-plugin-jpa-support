package fastjdbc.entity;

import fastjdbc.FastJdbcException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

public class EntityUtil {

  private EntityUtil() {
  }

  public static String findColumnName(String field, Entity entity) {
    if (Optional.ofNullable(entity.getIdField()).map(EntityField::getFieldName).orElse("")
        .equals(field)) {
      return entity.getIdField().getColumnName();
    }
    return Optional.ofNullable(entity.getNonePrimaryKeyFieldMap().get(field))
        .map(EntityField::getColumnName)
        .orElseThrow(
            () -> new FastJdbcException(
                "实体" + entity.getBeanClass() + "中没有字段:" + field + "，或该字段非数据库字段"));
  }

  public static EntityField findByColumnName(String columnName, Entity entity) {
    if (entity.getIdField() != null && entity.getIdField().getColumnName()
        .equals(columnName)) {
      return entity.getIdField();
    }
    return Optional.ofNullable(entity.getNonePrimaryKeyColumnMap().get(columnName))
        .orElseThrow(
            () -> new FastJdbcException("实体" + entity.getBeanClass() + "中没有数据库列:" + columnName));
  }

  public static Object readValue(Object obj, EntityField ef) {
    try {
      return ef.getReadMethod().invoke(obj);
    } catch (ReflectiveOperationException e) {
      throw new FastJdbcException(
          "读取实体" + obj.getClass().getName() + "的字段" + ef.getFieldName() + "失败", e);
    }
  }

  public static void setVersion(Object obj, EntityField ef) {
    try {
      Class<?> type = ef.getJavaType();
      if (type == int.class || type == Integer.class) {
        ef.getWriteMethod().invoke(obj, 0);
      } else {
        ef.getWriteMethod().invoke(obj, 0L);
      }
    } catch (ReflectiveOperationException e) {
      throw new FastJdbcException(
          "设置实体" + obj.getClass().getName() + "的版本字段" + ef.getFieldName() + "失败", e);
    }
  }

  /**
   * 设置日期字段，支持日期格式为long,Long,Date,Timestamp
   *
   * @param obj 被设置的对象
   * @param ef 被设置的字段
   */
  public static void setDate(Object obj, EntityField ef) {
    Class<?> type = ef.getJavaType();
    try {
      if (type == Long.class || type == long.class) {
        ef.getWriteMethod().invoke(obj, System.currentTimeMillis());
      } else if (type == Date.class) {
        ef.getWriteMethod().invoke(obj, new Date());
      } else {
        ef.getWriteMethod().invoke(obj, new Timestamp(System.currentTimeMillis()));
      }
    } catch (ReflectiveOperationException e) {
      throw new FastJdbcException(
          "设置实体" + obj.getClass().getName() + "的创建/更新日期字段" + ef.getFieldName() + "失败", e);
    }
  }

}
