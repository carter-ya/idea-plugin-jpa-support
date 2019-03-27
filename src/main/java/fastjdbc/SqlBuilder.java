package fastjdbc;

import static java.util.stream.Collectors.joining;

import fastjdbc.entity.Entity;
import fastjdbc.entity.EntityCacheManager;
import fastjdbc.entity.EntityField;
import fastjdbc.entity.EntityUtil;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class SqlBuilder {

  protected List<Object> args = new LinkedList<>();
  protected StringBuilder sqlBuilder = new StringBuilder(100);
  protected Entity entity;
  protected String as = "";
  private String sql;

  private SqlBuilder(Class<?> entityClass) {
    this.entity = EntityCacheManager.getEntity(entityClass);
  }

  public static <T> InsertSqlBuilder newInsertBuilder(T saveEntity) {
    return newInsertBuilder(saveEntity.getClass());
  }

  public static <T> InsertSqlBuilder newInsertBuilder(Class<T> entityClass) {
    return new InsertSqlBuilder(entityClass);
  }

  public static <T> UpdateSqlBuilder newUpdateBuilder(Class<T> entityClass) {
    return new UpdateSqlBuilder(entityClass);
  }

  public static <T> UpdateSqlBuilder newUpdateBuilder(T updateEntity) {
    return newUpdateBuilder(updateEntity.getClass());
  }

  public static <T> DeleteSqlBuilder newDeleteBuilder(T entity) {
    return newDeleteBuilder(entity.getClass());
  }

  public static <T> DeleteSqlBuilder newDeleteBuilder(Class<T> entityClass) {
    return new DeleteSqlBuilder(entityClass);
  }

  public static <T> SelectSqlBuilder newSelectBuilder(Class<T> entityClass) {
    return new SelectSqlBuilder(entityClass);
  }

  public SqlBuilder equal(String field, Object arg) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" = ?");
    args.add(arg);
    return this;
  }

  public SqlBuilder notEqual(String field, Object arg) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" <> ?");
    args.add(arg);
    return this;
  }

  public SqlBuilder isNull(String field) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" is null");
    return this;
  }

  public SqlBuilder isNotNull(String field) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" is not null");
    return this;
  }

  public SqlBuilder less(String field, Object arg) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" < ?");
    args.add(arg);
    return this;
  }

  public SqlBuilder lessOrEqual(String field, Object arg) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" <= ?");
    args.add(arg);
    return this;
  }

  public SqlBuilder greater(String field, Object arg) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" > ?");
    args.add(arg);
    return this;
  }

  public SqlBuilder greaterOrEqual(String field, Object arg) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" >= ?");
    args.add(arg);
    return this;
  }

  public SqlBuilder between(String field, Object leftArg, Object rightArg) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" between ? and ?");
    args.add(leftArg);
    args.add(rightArg);
    return this;
  }

  public <I> SqlBuilder in(String field, List<I> inList) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" in (")
        .append(inList.stream().map(obj -> "?").collect(joining(","))).append(")");
    args.addAll(inList);
    return this;
  }

  public <I> SqlBuilder notIn(String field, List<I> notInList) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" not in (")
        .append(notInList.stream().map(obj -> "?").collect(joining(","))).append(")");
    args.addAll(notInList);
    return this;
  }

  public SqlBuilder like(String field, String like) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" like ?");
    args.add(like);
    return this;
  }

  public SqlBuilder notLike(String field, String notLike) {
    sqlBuilder.append(" ").append(as).append(EntityUtil.findColumnName(field, entity)).append(" not like ?");
    args.add(notLike);
    return this;
  }

  public SqlBuilder distinct(String field) {
    sqlBuilder.append(" distinct(").append(as).append(EntityUtil.findColumnName(field, entity)).append(")");
    return this;
  }

  public SqlBuilder where() {
    sqlBuilder.append(" where");
    return this;
  }

  public SqlBuilder and() {
    sqlBuilder.append(" and");
    return this;
  }

  public SqlBuilder or() {
    sqlBuilder.append(" or");
    return this;
  }

  public SqlBuilder leftBracket() {
    sqlBuilder.append(" (");
    return this;
  }

  public SqlBuilder rightBracket() {
    sqlBuilder.append(" )");
    return this;
  }

  /**
   * 构建sql
   */
  public Sql build() {
    if (sqlBuilder == null) {
      throw new FastJdbcException("不能重复构建SQL");
    }
    sql = sqlBuilder.toString();
    sqlBuilder = null;
    return new Sql(sql, args);
  }

  public static class InsertSqlBuilder extends SqlBuilder {

    private boolean firstInsert = true;

    public <T> InsertSqlBuilder(Class<T> entityClass) {
      super(entityClass);
      sqlBuilder.append("insert into ").append(entity.getTableName()).append("(")
          .append(entity.getNonePrimaryKeyOrderedFields().stream().map(
              EntityField::getColumnName).collect(joining(","))).append(") values");
    }

    public <T> InsertSqlBuilder insert(T saveEntity) {
      if (!firstInsert) {
        sqlBuilder.append(',');
      }
      sqlBuilder.append("(")
          .append(entity.getNonePrimaryKeyOrderedFields().stream().map(obj -> "?").collect(joining(",")))
          .append(")");
      entity.getVersionField().ifPresent(vf -> EntityUtil.setVersion(saveEntity, vf));
      entity.getCreateDateField().ifPresent(cdf -> EntityUtil.setDate(saveEntity, cdf));
      entity.getUpdateDateField().ifPresent(udf -> EntityUtil.setDate(saveEntity, udf));
      entity.getNonePrimaryKeyOrderedFields().forEach(ef -> args.add(EntityUtil.readValue(saveEntity, ef)));

      firstInsert = false;
      return this;
    }

    public <T> InsertSqlBuilder insert(List<T> saveEntities) {
      saveEntities.forEach(this::insert);
      return this;
    }
  }

  public static class UpdateSqlBuilder extends SqlBuilder {

    private <T> UpdateSqlBuilder(Class<T> entityClass) {
      super(entityClass);
      sqlBuilder.append("update ").append(entity.getTableName()).append(" set ");
      entity.getVersionField().ifPresent(
          vf -> sqlBuilder.append(vf.getColumnName()).append(" = ").append(vf.getColumnName()).append(" + 1,"));
    }

    public <T> UpdateSqlBuilder update(T updateEntity) {
      Object primaryKey = EntityUtil.readValue(updateEntity, entity.getIdField());
      if (EntityUtil.readValue(updateEntity, entity.getIdField()) == null) {
        throw new FastJdbcException(
            "实体" + entity.getBeanClass().getName() + "主键字段" + entity.getIdField().getFieldName() + "不能为null");
      }
      entity.getUpdateDateField().ifPresent(udf -> {
        EntityUtil.setDate(updateEntity, udf);
        equal(udf.getFieldName(), EntityUtil.readValue(updateEntity, udf));
        sqlBuilder.append(",");
      });

      for (EntityField entityField : entity.getNonePrimaryKeyOrderedFields()) {
        if ((entity.getVersionField().isPresent() && entity.getVersionField().get() == entityField) ||
            (entity.getUpdateDateField().isPresent() && entity.getUpdateDateField().get() == entityField) ||
            (entity.getCreateDateField().isPresent() && entity.getCreateDateField().get() == entityField)) {
          continue;
        }
        equal(entityField.getFieldName(), EntityUtil.readValue(updateEntity, entityField));
        sqlBuilder.append(",");
      }
      int lastIndex = sqlBuilder.length() - 1;
      if (sqlBuilder.charAt(lastIndex) == ',') {
        sqlBuilder.deleteCharAt(lastIndex);
      }
      where().equal(entity.getIdField().getFieldName(), primaryKey);
      entity.getVersionField().ifPresent(vf -> and().equal(vf.getFieldName(), EntityUtil.readValue(updateEntity, vf)));
      return this;
    }
  }

  public static class DeleteSqlBuilder extends SqlBuilder {

    private <T> DeleteSqlBuilder(Class<T> entityClass) {
      super(entityClass);
      sqlBuilder.append("delete from ").append(entity.getTableName());
    }
  }

  public static class SelectSqlBuilder extends SqlBuilder {

    private SelectSqlBuilder(Class<?> entityClass) {
      super(entityClass);
    }

    public SelectSqlBuilder select() {
      return select(as);
    }

    public SelectSqlBuilder select(String as) {
      this.as = as.isEmpty() ? as : (as + ".");
      sqlBuilder.append("select ").append(this.as).append(entity.getIdField().getColumnName()).append(",")
          .append(entity.getNonePrimaryKeyOrderedFields().stream().map(ef -> this.as + ef.getColumnName())
              .collect(joining(",")));
      return this;
    }

    public SelectSqlBuilder select(String... fields) {
      return select(as, fields);
    }

    public SelectSqlBuilder select(String as, String... fields) {
      this.as = as.isEmpty() ? as : (as + ".");
      sqlBuilder.append("select ")
          .append(Arrays.stream(fields).map(field -> this.as + EntityUtil.findColumnName(field, entity))
              .collect(joining(",")));
      return this;
    }

    public SelectSqlBuilder count() {
      sqlBuilder.append("select count(").append(as).append("*)");
      return this;
    }

    public SelectSqlBuilder count(String field) {
      sqlBuilder.append("select count(").append(as).append(EntityUtil.findColumnName(field, entity)).append(")");
      return this;
    }

    public SelectSqlBuilder countDistinct(String field) {
      sqlBuilder.append("select count(distinct(").append(as).append(EntityUtil.findColumnName(field, entity))
          .append("))");
      return this;
    }

    public SelectSqlBuilder from() {
      sqlBuilder.append(" from ").append(entity.getTableName());
      if (!as.isEmpty()) {
        sqlBuilder.append(" as ").append(as.substring(0, as.length() - 1));
      }
      return this;
    }
  }
}
