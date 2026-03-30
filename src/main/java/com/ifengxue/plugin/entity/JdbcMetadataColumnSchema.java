package com.ifengxue.plugin.entity;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.jetbrains.annotations.Nullable;

public class JdbcMetadataColumnSchema extends ColumnSchema implements ColumnSchemaExtension<ColumnSchema> {

  private static final long serialVersionUID = 123234258218982686L;

  private boolean nullable;
  private boolean primary;
  private boolean autoIncrement;
  private boolean sequenceColumn;
  private boolean generatedColumn;
  private int jdbcType;
  private String jdbcTypeName;
  private Class<?> javaTypeClass;

  @Override
  public ColumnSchema origin() {
    return this;
  }

  @Override
  public boolean nullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  @Override
  public boolean primary() {
    return primary;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  @Override
  public boolean autoIncrement() {
    return autoIncrement;
  }

  public void setAutoIncrement(boolean autoIncrement) {
    this.autoIncrement = autoIncrement;
  }

  @Override
  public boolean sequenceColumn() {
    return sequenceColumn;
  }

  public void setSequenceColumn(boolean sequenceColumn) {
    this.sequenceColumn = sequenceColumn;
  }

  @Override
  public boolean generateColumn() {
    return generatedColumn;
  }

  public void setGeneratedColumn(boolean generatedColumn) {
    this.generatedColumn = generatedColumn;
  }

  @Override
  public int jdbcType() {
    return jdbcType;
  }

  public void setJdbcType(int jdbcType) {
    this.jdbcType = jdbcType;
    this.javaTypeClass = resolveJavaTypeClass(jdbcType);
  }

  @Nullable
  @Override
  public String jdbcTypeName() {
    return jdbcTypeName;
  }

  public void setJdbcTypeName(String jdbcTypeName) {
    this.jdbcTypeName = jdbcTypeName;
  }

  @Nullable
  @Override
  public Class<?> javaTypeClass() {
    return javaTypeClass;
  }

  public void setJavaTypeClass(@Nullable Class<?> javaTypeClass) {
    this.javaTypeClass = javaTypeClass;
  }

  @Nullable
  private static Class<?> resolveJavaTypeClass(int jdbcType) {
    switch (jdbcType) {
      case Types.BIT:
      case Types.BOOLEAN:
        return Boolean.class;
      case Types.TINYINT:
        return Byte.class;
      case Types.SMALLINT:
        return Short.class;
      case Types.INTEGER:
        return Integer.class;
      case Types.BIGINT:
        return Long.class;
      case Types.FLOAT:
      case Types.REAL:
        return Float.class;
      case Types.DOUBLE:
        return Double.class;
      case Types.NUMERIC:
      case Types.DECIMAL:
        return BigDecimal.class;
      case Types.CHAR:
      case Types.NCHAR:
      case Types.VARCHAR:
      case Types.NVARCHAR:
      case Types.LONGVARCHAR:
      case Types.LONGNVARCHAR:
      case Types.CLOB:
      case Types.NCLOB:
      case Types.SQLXML:
        return String.class;
      case Types.DATE:
        return LocalDate.class;
      case Types.TIME:
      case Types.TIME_WITH_TIMEZONE:
        return LocalTime.class;
      case Types.TIMESTAMP:
      case Types.TIMESTAMP_WITH_TIMEZONE:
        return LocalDateTime.class;
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
      case Types.BLOB:
        return byte[].class;
      default:
        return String.class;
    }
  }
}
