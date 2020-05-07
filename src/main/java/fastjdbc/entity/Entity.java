package fastjdbc.entity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Entity {

  private String tableName;
  private Class<?> beanClass;
  /**
   * 主键字段
   *
   * @see javax.persistence.Id
   */
  private EntityField idField;
  /**
   * 乐观锁版本字段
   *
   * @see javax.persistence.Version
   */
  private EntityField versionField;
  /**
   * 创建日期字段
   */
  private EntityField createDateField;
  /**
   * 更新日期字段
   */
  private EntityField updateDateField;
  /**
   * 不包含主键字段的其它字段，包括乐观锁版本字段，创建日期，更新日期字段
   */
  private Map<String, EntityField> nonePrimaryKeyFieldMap;
  /**
   * 不包含主键字段的其它字段，包括乐观锁子弹，创建日期，更新日期字段
   */
  private Map<String, EntityField> nonePrimaryKeyColumnMap;
  /**
   * 不包含主键字段的其它有序字段，包括乐观锁版本字段，创建日期，更新日期字段
   */
  private Set<EntityField> nonePrimaryKeyOrderedFields;

  public Optional<EntityField> getVersionField() {
    return Optional.ofNullable(versionField);
  }

  public Optional<EntityField> getCreateDateField() {
    return Optional.ofNullable(createDateField);
  }

  public Optional<EntityField> getUpdateDateField() {
    return Optional.ofNullable(updateDateField);
  }
}
