package com.ifengxue.plugin.entity;

import com.ifengxue.fastjdbc.annotations.Column;
import com.ifengxue.fastjdbc.annotations.Id;
import com.ifengxue.fastjdbc.annotations.Table;
import java.io.Serializable;

@Table(name = "information_schema.COLUMNS")
public class ColumnSchema implements Serializable {

  private static final long serialVersionUID = -7523969607822355567L;
  @Id
  @Column(name = "COLUMN_NAME")
  private String columnName;

  @Column(name = "TABLE_SCHEMA")
  private String tableSchema;

  @Column(name = "TABLE_NAME")
  private String tableName;

  @Column(name = "ORDINAL_POSITION")
  private int ordinalPosition;

  @Column(name = "DATA_TYPE")
  private String dataType;

  @Column(name = "COLUMN_TYPE")
  private String columnType;

  @Column(name = "EXTRA")
  private String extra;

  @Column(name = "COLUMN_COMMENT")
  private String columnComment;

  @Column(name = "IS_NULLABLE")
  private String isNullable;

  @Column(name = "COLUMN_DEFAULT")
  private String columnDefault;

  @Column(name = "COLUMN_KEY")
  private String columnKey;

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public int getOrdinalPosition() {
    return ordinalPosition;
  }

  public void setOrdinalPosition(int ordinalPosition) {
    this.ordinalPosition = ordinalPosition;
  }

  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  public String getColumnType() {
    return columnType;
  }

  public void setColumnType(String columnType) {
    this.columnType = columnType;
  }

  public String getExtra() {
    return extra;
  }

  public void setExtra(String extra) {
    this.extra = extra;
  }

  public String getColumnComment() {
    return columnComment;
  }

  public void setColumnComment(String columnComment) {
    this.columnComment = columnComment;
  }

  public String getIsNullable() {
    return isNullable;
  }

  public void setIsNullable(String isNullable) {
    this.isNullable = isNullable;
  }

  public String getColumnDefault() {
    return columnDefault;
  }

  public void setColumnDefault(String columnDefault) {
    this.columnDefault = columnDefault;
  }

  public String getTableSchema() {
    return tableSchema;
  }

  public void setTableSchema(String tableSchema) {
    this.tableSchema = tableSchema;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getColumnKey() {
    return columnKey;
  }

  public void setColumnKey(String columnKey) {
    this.columnKey = columnKey;
  }

  @Override
  public String toString() {
    return "ColumnSchema{" +
        "columnName='" + columnName + '\'' +
        ", tableSchema='" + tableSchema + '\'' +
        ", tableName='" + tableName + '\'' +
        ", ordinalPosition=" + ordinalPosition +
        ", dataType='" + dataType + '\'' +
        ", columnType='" + columnType + '\'' +
        ", extra='" + extra + '\'' +
        ", columnComment='" + columnComment + '\'' +
        ", isNullable='" + isNullable + '\'' +
        ", columnDefault='" + columnDefault + '\'' +
        ", columnKey='" + columnKey + '\'' +
        '}';
  }
}
