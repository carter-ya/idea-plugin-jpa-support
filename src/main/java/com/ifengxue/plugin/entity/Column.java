package com.ifengxue.plugin.entity;

public class Column {

  /**
   * 数据库字段名
   */
  private String columnName;
  /**
   * 实体字段名
   */
  private String fieldName;
  /**
   * 字段顺序
   */
  private int sort;
  /**
   * 数据库数据类型
   */
  private String dbDataType;
  /**
   * Java数据类型
   */
  private Class<?> javaDataType;
  /**
   * 是否是主键
   */
  private boolean primary;
  /**
   * 是否允许为null
   */
  private boolean nullable;
  /**
   * 是否是自增字段
   */
  private boolean autoIncrement;
  /**
   * 是否有默认值
   */
  private boolean hasDefaultValue;
  /**
   * 默认值，如果是字符串则默认值是"默认值"
   */
  private String defaultValue;
  /**
   * 字段注释
   */
  private String columnComment;

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public int getSort() {
    return sort;
  }

  public void setSort(int sort) {
    this.sort = sort;
  }

  public String getDbDataType() {
    return dbDataType;
  }

  public void setDbDataType(String dbDataType) {
    this.dbDataType = dbDataType;
  }

  public Class<?> getJavaDataType() {
    return javaDataType;
  }

  public void setJavaDataType(Class<?> javaDataType) {
    this.javaDataType = javaDataType;
  }

  public boolean isPrimary() {
    return primary;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public boolean isAutoIncrement() {
    return autoIncrement;
  }

  public void setAutoIncrement(boolean autoIncrement) {
    this.autoIncrement = autoIncrement;
  }

  public boolean isHasDefaultValue() {
    return hasDefaultValue;
  }

  public void setHasDefaultValue(boolean hasDefaultValue) {
    this.hasDefaultValue = hasDefaultValue;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public String getColumnComment() {
    return columnComment;
  }

  public void setColumnComment(String columnComment) {
    this.columnComment = columnComment;
  }

  @Override
  public String toString() {
    return "Column{" +
        "columnName='" + columnName + '\'' +
        ", fieldName='" + fieldName + '\'' +
        ", sort=" + sort +
        ", dbDataType='" + dbDataType + '\'' +
        ", javaDataType=" + javaDataType +
        ", primary=" + primary +
        ", nullable=" + nullable +
        ", autoIncrement=" + autoIncrement +
        ", hasDefaultValue=" + hasDefaultValue +
        ", defaultValue='" + defaultValue + '\'' +
        ", columnComment='" + columnComment + '\'' +
        '}';
  }
}
