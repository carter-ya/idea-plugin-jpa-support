package com.ifengxue.plugin.util;

import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.ColumnSchema;
import com.ifengxue.plugin.entity.ColumnSchemaExtension;
import com.intellij.openapi.diagnostic.Logger;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ColumnUtil {

  private static final Logger logger = Logger.getInstance(ColumnUtil.class);

  public static final Map<String, Integer> jdbcTypeNameToCode = new HashMap<>();

  static {
    // MySQL
    jdbcTypeNameToCode.put("INT", Types.INTEGER);
    jdbcTypeNameToCode.put("DATETIME", Types.TIMESTAMP);
    jdbcTypeNameToCode.put("MEDIUMINT", Types.INTEGER);
    jdbcTypeNameToCode.put("YEAR", Types.DATE);
    jdbcTypeNameToCode.put("TINYBLOB", Types.BLOB);
    jdbcTypeNameToCode.put("TINYTEXT", Types.VARCHAR);
    jdbcTypeNameToCode.put("TEXT", Types.VARCHAR);
    jdbcTypeNameToCode.put("MEDIUMBLOB", Types.BLOB);
    jdbcTypeNameToCode.put("MEDIUMTEXT", Types.VARCHAR);
    jdbcTypeNameToCode.put("LONGBLOB", Types.LONGVARBINARY);
    jdbcTypeNameToCode.put("LONGTEXT", Types.LONGVARCHAR);
    jdbcTypeNameToCode.put("ENUM", Types.VARCHAR);
    jdbcTypeNameToCode.put("SET", Types.VARCHAR);

    // Postgre SQL
    jdbcTypeNameToCode.put("INT8", Types.BIGINT);
    jdbcTypeNameToCode.put("BIGSERIAL", Types.BIGINT);
    jdbcTypeNameToCode.put("OID", Types.BIGINT);
    jdbcTypeNameToCode.put("BYTEA", Types.BLOB);
    jdbcTypeNameToCode.put("BPCHAR", Types.CHAR);
    jdbcTypeNameToCode.put("CHARACTER", Types.VARCHAR);
    jdbcTypeNameToCode.put("CHARACTER VARYING", Types.VARCHAR);
    jdbcTypeNameToCode.put("DOUBLE PRECISION", Types.DOUBLE);
    jdbcTypeNameToCode.put("FLOAT8", Types.DOUBLE);
    jdbcTypeNameToCode.put("INT4", Types.INTEGER);
    jdbcTypeNameToCode.put("SERIAL", Types.INTEGER);
    jdbcTypeNameToCode.put("JSON", Types.VARCHAR);
    jdbcTypeNameToCode.put("JSONB", Types.VARCHAR);
    jdbcTypeNameToCode.put("FLOAT4", Types.FLOAT);
    jdbcTypeNameToCode.put("INT2", Types.SMALLINT);
    jdbcTypeNameToCode.put("SMALLSERIAL", Types.SMALLINT);
    jdbcTypeNameToCode.put("MONEY", Types.DOUBLE);
    jdbcTypeNameToCode.put("NAME", Types.VARCHAR);
    jdbcTypeNameToCode.put("TIME WITH TIME ZONE", Types.TIME_WITH_TIMEZONE);
    jdbcTypeNameToCode.put("TIMETZ", Types.TIME_WITH_TIMEZONE);
    jdbcTypeNameToCode.put("TIMESTAMPTZ", Types.TIMESTAMP);
    jdbcTypeNameToCode.put("TIMESTAMP WITH TIME ZONE", Types.TIMESTAMP_WITH_TIMEZONE);
    jdbcTypeNameToCode.put("UUID", Types.VARCHAR);
    jdbcTypeNameToCode.put("XML", Types.VARCHAR);

    for (Field field : Types.class.getFields()) {
      if (Modifier.isStatic(field.getModifiers())
          && Modifier.isFinal(field.getModifiers())
          && (field.getType() == int.class || field.getType() == Integer.class)) {
        try {
          jdbcTypeNameToCode.put(field.getName().toUpperCase(), field.getInt(Types.class));
        } catch (ReflectiveOperationException ignore) {
        }
      }
    }
  }

  public static Column columnSchemaToColumn(ColumnSchema columnSchema, String removePrefixes,
      String ifJavaKeywordAddSuffix, boolean useWrapper, boolean useJava8DateType) {
    if (!(columnSchema instanceof ColumnSchemaExtension)) {
      throw new IllegalStateException(
          columnSchema.getClass().getName() + " is not instance of " + ColumnSchemaExtension.class
              .getName());
    }
    ColumnSchemaExtension<?> extension = (ColumnSchemaExtension<?>) columnSchema;
    Column column = new Column();
    column.setColumnName(columnSchema.getColumnName());
    column.setSort(columnSchema.getOrdinalPosition());
    column.setDbDataType(columnSchema.getDataType());
    column.setPrimary(extension.primary());
    column.setNullable(extension.nullable());
    column.setAutoIncrement(extension.autoIncrement());
    column.setColumnComment(columnSchema.getColumnComment());
    column.setDefaultValue(columnSchema.getColumnDefault());
    column.setJavaDataType(extension.javaTypeClass());
    column.setJdbcType(extension.jdbcType());
    column.setJdbcTypeName(extension.jdbcTypeName());
    column.setSequenceColumn(extension.sequenceColumn());
    ColumnUtil
        .parseColumn(column, removePrefixes, ifJavaKeywordAddSuffix, useWrapper, useJava8DateType);
    return column;
  }

  public static void parseColumn(Column column, String removePrefixes,
      String ifJavaKeywordAddSuffix, boolean useWrapper, boolean useJava8DateType) {
    column.setFieldName(StringHelper
        .parseFieldName(column.getColumnName(), removePrefixes, ifJavaKeywordAddSuffix));
    Class<?> javaDataType = StringHelper.parseJavaDataType(column.getJavaDataType(),
        column.getJdbcTypeName(), column.getDbDataType(), column.getColumnName(), useWrapper,
        useJava8DateType);
    if ((javaDataType == Integer.class || javaDataType == int.class)
        && (column.getColumnComment().contains("true") || column.getColumnComment()
        .contains("false"))) {
      if (useWrapper) {
        javaDataType = Boolean.class;
      } else {
        javaDataType = boolean.class;
      }
    }
    column.setJavaDataType(javaDataType);
    if (column.getDefaultValue() != null) {
      if (javaDataType == String.class) {
        column.setDefaultValue("\"" + column.getDefaultValue() + "\"");
      }
      Class<?> primitiveClass = StringHelper.getPrimitiveClass(javaDataType);
      if (primitiveClass == long.class) {
        column.setDefaultValue(column.getDefaultValue() + "L");
      }
      if (primitiveClass == float.class) {
        column.setDefaultValue(column.getDefaultValue() + "F");
      }
      if (primitiveClass == double.class) {
        column.setDefaultValue(column.getDefaultValue() + "D");
      }
      if (primitiveClass == boolean.class) {
        if (column.getDefaultValue().equals("1")) {
          if (useWrapper) {
            column.setDefaultValue("Boolean.TRUE");
          } else {
            column.setDefaultValue("true");
          }
        } else {
          if (useWrapper) {
            column.setDefaultValue("Boolean.FALSE");
          } else {
            column.setDefaultValue("false");
          }
        }
      }
      if (javaDataType == BigDecimal.class) {
        BigDecimal amount;
        try {
          amount = new BigDecimal(column.getDefaultValue());
        } catch (Exception e) {
          amount = BigDecimal.ZERO;
          logger.warn("can't parse '" + column.getDefaultValue() + "' to BigDecimal", e);
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
          column.setDefaultValue("BigDecimal.ZERO");
        } else if (amount.compareTo(BigDecimal.ONE) == 0) {
          column.setDefaultValue("BigDecimal.ONE");
        } else if (amount.compareTo(BigDecimal.TEN) == 0) {
          column.setDefaultValue("BigDecimal.TEN");
        } else {
          column.setDefaultValue("new BigDecimal(\"" + column.getDefaultValue() + "\")");
        }
      }
      column.setHasDefaultValue(true);
      if (javaDataType == java.util.Date.class ||
          javaDataType == java.sql.Date.class ||
          javaDataType == java.sql.Timestamp.class ||
          javaDataType == LocalDateTime.class) {
        if (isNow(column.getDefaultValue())) {
          if (javaDataType == java.util.Date.class) {
            column.setDefaultValue("new Date()");
          } else if (javaDataType == java.sql.Date.class) {
            column.setDefaultValue("new Date(System.currentTimeMillis())");
          } else if (javaDataType == Timestamp.class) {
            column.setDefaultValue("new Timestamp(System.currentTimeMillis())");
          } else {
            column.setDefaultValue("LocalDateTime.now()");
          }
        } else {
          LocalDateTime dateTime = tryParseDateTime(column.getDefaultValue());
          if (dateTime == null) {
            column.setDefaultValue(null);
            column.setHasDefaultValue(false);
          } else {
            Timestamp timestamp = Timestamp.valueOf(dateTime);
            if (javaDataType == java.util.Date.class || javaDataType == java.sql.Date.class) {
              column.setDefaultValue("new Date(" + timestamp.getTime() + "L)");
            } else if (javaDataType == Timestamp.class) {
              column.setDefaultValue("new Timestamp(" + timestamp.getTime() + "L)");
            } else {
              column.setDefaultValue("new Timestamp(" + timestamp.getTime() + "L).toLocalDateTime()");
            }
          }
        }
      }

      if (javaDataType == LocalDate.class || javaDataType == LocalTime.class) {
        column.setDefaultValue(null);
        column.setHasDefaultValue(false);
      }
    }
  }

  private static boolean isNow(String value) {
    return "CURRENT_TIMESTAMP".equals(value);
  }

  private static LocalDateTime tryParseDateTime(String value) {
    try {
      return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    } catch (Exception ex) {
      return null;
    }
  }
}
