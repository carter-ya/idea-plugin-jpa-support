package com.ifengxue.plugin.entity;

public interface ColumnSchemaExtension<T> {

  T origin();

  boolean nullable();

  boolean primary();

  boolean autoIncrement();
}
