package com.ifengxue.plugin.entity;

import com.ifengxue.fastjdbc.annotations.Column;
import com.ifengxue.fastjdbc.annotations.Id;
import com.ifengxue.fastjdbc.annotations.Table;
import java.io.Serializable;

@Table(name = "information_schema.TABLES")
public class TableSchema implements Serializable {

  private static final long serialVersionUID = 1853575310189734827L;
  @Id
  @Column(name = "TABLE_NAME")
  private String tableName;

  @Column(name = "TABLE_COMMENT")
  private String tableComment;

  @Column(name = "TABLE_SCHEMA")
  private String tableSchema;

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

  @Override
  public String toString() {
    return "TableSchema{" +
        "tableName='" + tableName + '\'' +
        ", tableComment='" + tableComment + '\'' +
        ", tableSchema='" + tableSchema + '\'' +
        '}';
  }
}
