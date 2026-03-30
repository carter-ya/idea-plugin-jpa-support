package com.ifengxue.plugin.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assume;
import org.junit.Test;

public class PostgreSqlMetadataSmokeTest {

  private static final String JDBC_URL =
      "jdbc:postgresql://127.0.0.1:35432/jpa_support_edge?user=postgres&password=postgres";

  @Test
  public void postgresMetadataMatchesDatabaseSettingsDialogAssumptions() throws Exception {
    Assume.assumeTrue("PostgreSQL test container is not available", canConnect());

    try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
      DatabaseMetaData metadata = connection.getMetaData();

      Set<String> tableNames = new HashSet<>();
      try (ResultSet resultSet = metadata.getTables(connection.getCatalog(), "public", "%", new String[]{"TABLE"})) {
        while (resultSet.next()) {
          tableNames.add(resultSet.getString("TABLE_NAME"));
        }
      }
      assertTrue(tableNames.contains("account_profile"));
      assertTrue(tableNames.contains("data_type_showcase"));
      assertTrue(tableNames.contains("composite_pk_example"));

      Set<String> primaryKeys = new HashSet<>();
      try (ResultSet resultSet = metadata.getPrimaryKeys(connection.getCatalog(), "public", "account_profile")) {
        while (resultSet.next()) {
          primaryKeys.add(resultSet.getString("COLUMN_NAME"));
        }
      }
      assertEquals(Set.of("id"), primaryKeys);

      Map<String, ColumnSnapshot> columns = new HashMap<>();
      try (ResultSet resultSet = metadata.getColumns(connection.getCatalog(), "public", "data_type_showcase", "%")) {
        while (resultSet.next()) {
          columns.put(resultSet.getString("COLUMN_NAME"), new ColumnSnapshot(
              resultSet.getInt("DATA_TYPE"),
              resultSet.getString("TYPE_NAME"),
              resultSet.getInt("NULLABLE"),
              getOptionalString(resultSet, "IS_AUTOINCREMENT"),
              getOptionalString(resultSet, "IS_GENERATEDCOLUMN")));
        }
      }

      assertFalse(columns.isEmpty());
      assertColumn(columns, "id", "YES", Types.BIGINT);
      assertColumn(columns, "c_numeric_value", "NO", Types.NUMERIC);
      assertColumn(columns, "c_boolean_flag", "NO", Types.BIT, Types.BOOLEAN);
      assertColumn(columns, "c_jsonb_value", "NO", Types.OTHER);
      assertColumn(columns, "c_bytea_value", "NO", Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY);
      assertColumn(columns, "c_timestamptz_value", "NO", Types.TIMESTAMP_WITH_TIMEZONE, Types.TIMESTAMP);
      assertColumn(columns, "c_uuid_value", "NO", Types.OTHER);
      assertColumn(columns, "c_xml_value", "NO", Types.SQLXML, Types.LONGVARCHAR);
    }
  }

  private static void assertColumn(
      Map<String, ColumnSnapshot> columns,
      String columnName,
      String autoIncrement,
      int... jdbcTypes) {
    ColumnSnapshot column = columns.get(columnName);
    assertNotNull(columnName, column);
    System.out.println(columnName
        + " jdbcType=" + column.jdbcType
        + " typeName=" + column.typeName
        + " nullable=" + column.nullable
        + " autoIncrement=" + column.autoIncrement
        + " generatedColumn=" + column.generatedColumn);
    assertTrue(
        "Unexpected JDBC type for " + columnName + ": " + column.jdbcType,
        java.util.Arrays.stream(jdbcTypes).anyMatch(value -> value == column.jdbcType));
    assertFalse(column.typeName == null || column.typeName.isBlank());
    assertNotNull(column.generatedColumn);
    assertEquals(autoIncrement, column.autoIncrement);
  }

  private static boolean canConnect() {
    try (Connection ignored = DriverManager.getConnection(JDBC_URL)) {
      return true;
    } catch (SQLException e) {
      return false;
    }
  }

  private static String getOptionalString(ResultSet resultSet, String columnLabel) throws SQLException {
    try {
      String value = resultSet.getString(columnLabel);
      return value == null ? "" : value;
    } catch (SQLException ignored) {
      return "";
    }
  }

  private static final class ColumnSnapshot {
    private final int jdbcType;
    private final String typeName;
    private final int nullable;
    private final String autoIncrement;
    private final String generatedColumn;

    private ColumnSnapshot(int jdbcType, String typeName, int nullable, String autoIncrement, String generatedColumn) {
      this.jdbcType = jdbcType;
      this.typeName = typeName;
      this.nullable = nullable;
      this.autoIncrement = autoIncrement;
      this.generatedColumn = generatedColumn;
    }
  }
}
