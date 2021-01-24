package com.ifengxue.plugin.entity;

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
    setColumnComment(introspectedColumn.getRemarks());
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
}
