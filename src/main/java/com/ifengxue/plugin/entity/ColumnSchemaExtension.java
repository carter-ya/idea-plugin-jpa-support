package com.ifengxue.plugin.entity;

import javax.annotation.Nullable;

public interface ColumnSchemaExtension<T> {

  T origin();

  boolean nullable();

  boolean primary();

  boolean autoIncrement();

  /**
   * @see java.sql.Types
   */
  int jdbcType();

  /**
   * @see java.sql.Types
   * @see org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl
   */
  @Nullable
  String jdbcTypeName();

  /**
   * @see org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl
   */
  @Nullable
  Class<?> javaTypeClass();
}
