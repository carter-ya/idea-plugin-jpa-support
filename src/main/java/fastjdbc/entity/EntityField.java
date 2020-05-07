package fastjdbc.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EntityField implements Comparable<EntityField> {

  private final Field field;
  private final String columnName;
  private final Method readMethod;
  private final Method writeMethod;
  private final int order;

  /**
   * 获取实体字段名称
   */
  public String getFieldName() {
    return field.getName();
  }

  /**
   * 获取实体类型
   */
  public Class<?> getJavaType() {
    return field.getType();
  }

  @Override
  public int compareTo(@Nonnull EntityField o) {
    return Integer.compare(order, o.getOrder());
  }
}
