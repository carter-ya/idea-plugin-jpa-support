package com.ifengxue.plugin.entity;

import java.util.List;

public class Table {

  /**
   * 是否被选择，选择后才可生成类
   */
  private boolean selected;

  /**
   * 表名
   */
  private String tableName;

  /**
   * 表注释
   */
  private String tableComment;

  /**
   * 数据库名称
   */
  private String tableSchema;

  /**
   * 实体名称
   */
  private String entityName;

  /**
   * 包名称
   */
  private String packageName;

  /**
   * 主键类型
   */
  private Class<?> primaryKeyClassType;

  private List<Column> columns;

  public static Table from(TableSchema tableSchema, String entityName, boolean selected) {
    Table table = new Table();
    table.setTableName(tableSchema.getTableName());
    table.setTableComment(tableSchema.getTableComment());
    table.setTableSchema(tableSchema.getTableSchema());
    table.setEntityName(entityName);
    table.setSelected(selected);
    return table;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getTableComment() {
    return tableComment;
  }

  public void setTableComment(String tableComment) {
    this.tableComment = tableComment;
  }

  public String getTableSchema() {
    return tableSchema;
  }

  public void setTableSchema(String tableSchema) {
    this.tableSchema = tableSchema;
  }

  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public List<Column> getColumns() {
    return columns;
  }

  public void setColumns(List<Column> columns) {
    this.columns = columns;
  }

  public Class<?> getPrimaryKeyClassType() {
    return primaryKeyClassType;
  }

  public void setPrimaryKeyClassType(Class<?> primaryKeyClassType) {
    this.primaryKeyClassType = primaryKeyClassType;
  }

  @Override
  public String toString() {
    return "Table{" +
        "selected=" + selected +
        ", tableName='" + tableName + '\'' +
        ", tableComment='" + tableComment + '\'' +
        ", tableSchema='" + tableSchema + '\'' +
        ", entityName='" + entityName + '\'' +
        ", packageName='" + packageName + '\'' +
        ", columns=" + columns +
        '}';
  }
}
