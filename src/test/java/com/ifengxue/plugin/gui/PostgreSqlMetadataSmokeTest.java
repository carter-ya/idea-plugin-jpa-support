package com.ifengxue.plugin.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fastjdbc.NoPoolDataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.ClassRule;
import org.junit.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.db.DatabaseIntrospector;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSqlMetadataSmokeTest {

  @ClassRule
  public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
      .withInitScript("init-postgresql-edge.sql");

  @Test
  public void postgresMetadataMatchesDatabaseSettingsDialogAssumptions() throws Exception {
    DataSource dataSource = new NoPoolDataSource(
        postgres.getJdbcUrl(),
        postgres.getUsername(), postgres.getPassword());
    try (Connection connection = dataSource.getConnection()) {
      List<String> warnings = new ArrayList<>();
      Context context = new Context(ModelType.FLAT);
      DatabaseIntrospector introspector = new DatabaseIntrospector(
          context, connection.getMetaData(), new JavaTypeResolverDefaultImpl(), warnings);
      TableConfiguration tc = new TableConfiguration(context);
      tc.setSchema("public");
      List<IntrospectedTable> tables = introspector.introspectTables(tc);
      assertFalse("Should introspect at least one table", tables.isEmpty());

      // Verify account_profile — BIGSERIAL PK must have sequenceColumn=true
      IntrospectedTable accountTable = tables.stream()
          .filter(t -> t.getFullyQualifiedTable().getIntrospectedTableName()
              .equals("account_profile"))
          .findFirst().orElse(null);
      assertNotNull("account_profile should exist", accountTable);
      IntrospectedColumn idCol = accountTable.getColumn("id").orElse(null);
      assertNotNull("id column should exist", idCol);
      assertTrue("BIGSERIAL id should be a sequence column", idCol.isSequenceColumn());

      // Verify data_type_showcase exists and has expected columns
      IntrospectedTable showcaseTable = tables.stream()
          .filter(t -> t.getFullyQualifiedTable().getIntrospectedTableName()
              .equals("data_type_showcase"))
          .findFirst().orElse(null);
      assertNotNull("data_type_showcase should exist", showcaseTable);
      assertNotNull("c_numeric_value column should exist",
          showcaseTable.getColumn("c_numeric_value").orElse(null));
      assertNotNull("c_jsonb_value column should exist",
          showcaseTable.getColumn("c_jsonb_value").orElse(null));

      // Verify composite_pk_example — composite primary key
      IntrospectedTable compositeTable = tables.stream()
          .filter(t -> t.getFullyQualifiedTable().getIntrospectedTableName()
              .equals("composite_pk_example"))
          .findFirst().orElse(null);
      assertNotNull("composite_pk_example should exist", compositeTable);
      assertEquals("Should have 2 primary key columns",
          2, compositeTable.getPrimaryKeyColumns().size());
    }
  }
}
