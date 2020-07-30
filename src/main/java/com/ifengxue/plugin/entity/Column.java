package com.ifengxue.plugin.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
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

}
