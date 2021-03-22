package com.ifengxue.plugin.entity;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;

public class MybatisGeneratorColumnSchema extends ColumnSchema implements ColumnSchemaExtension<IntrospectedColumn> {

  private static final long serialVersionUID = 7541057281001869806L;
  private final IntrospectedColumn introspectedColumn;

  public MybatisGeneratorColumnSchema(IntrospectedColumn introspectedColumn, int index) {
    this.introspectedColumn = introspectedColumn;

    IntrospectedTable introspectedTable = introspectedColumn.getIntrospectedTable();
    setColumnName(introspectedColumn.getActualColumnName());
    setTableSchema(introspectedTable.getFullyQualifiedTable().getIntrospectedCatalog());
    setTableName(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName());
    setOrdinalPosition(index);
    setDataType(introspectedColumn.getActualTypeName());
    setColumnType(getDataType());
    setColumnComment(StringUtils.trimToEmpty(introspectedColumn.getRemarks()));
    setColumnDefault(introspectedColumn.getDefaultValue());
  }

  @Override
  public IntrospectedColumn origin() {
    return introspectedColumn;
  }

  @Override
  public boolean nullable() {
    return introspectedColumn.isNullable();
  }

  @Override
  public boolean primary() {
    return introspectedColumn.getIntrospectedTable().getPrimaryKeyColumns().contains(origin());
  }

  @Override
  public boolean autoIncrement() {
    return introspectedColumn.isAutoIncrement();
  }

  @Override
  public boolean sequenceColumn() {
    return introspectedColumn.isSequenceColumn();
  }

  @Override
  public boolean generateColumn() {
    return introspectedColumn.isGeneratedColumn();
  }

  @Override
  public int jdbcType() {
    return introspectedColumn.getJdbcType();
  }

  @Nullable
  @Override
  public String jdbcTypeName() {
    return introspectedColumn.getJdbcTypeName();
  }

  @Nullable
  @Override
  public Class<?> javaTypeClass() {
    return Optional.ofNullable(introspectedColumn.getFullyQualifiedJavaType())
        .map(type -> {
          try {
            return Class.forName(type.getFullyQualifiedName());
          } catch (ClassNotFoundException ignored) {
            return null;
          }
        })
        .filter(clazz -> clazz != Object.class) // skip Object
        .orElse(null);
  }
}
