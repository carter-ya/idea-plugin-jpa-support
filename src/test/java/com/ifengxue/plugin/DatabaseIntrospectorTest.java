package com.ifengxue.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fastjdbc.NoPoolDataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.db.DatabaseIntrospector;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;
import org.testcontainers.containers.MySQLContainer;

public class DatabaseIntrospectorTest {

  @ClassRule
  public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withInitScript("init-mysql-edge.sql");

  @BeforeClass
  public static void beforeClass() throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
  }

  @Test
  public void introspectTables() throws Exception {
    DataSource dataSource = new NoPoolDataSource(
        mysql.getJdbcUrl() + "?useInformationSchema=true&useSSL=false",
        mysql.getUsername(), mysql.getPassword());
    try (Connection connection = dataSource.getConnection()) {
      List<String> warnings = new ArrayList<>();
      Context context = new Context(ModelType.FLAT);
      DatabaseIntrospector introspector = new DatabaseIntrospector(
          context, connection.getMetaData(), new JavaTypeResolverDefaultImpl(), warnings);
      TableConfiguration tc = new TableConfiguration(context);
      tc.setCatalog(mysql.getDatabaseName());
      List<IntrospectedTable> tables = introspector.introspectTables(tc);
      assertFalse("Should introspect at least one table", tables.isEmpty());

      // Verify data_type_showcase table — NUMERIC(10,0) must resolve to Long
      IntrospectedTable showcaseTable = tables.stream()
          .filter(t -> t.getFullyQualifiedTable().getIntrospectedTableName()
              .equals("data_type_showcase"))
          .findFirst().orElse(null);
      assertNotNull("data_type_showcase should exist", showcaseTable);

      IntrospectedColumn numericCol = showcaseTable.getColumn("c_numeric_int").orElse(null);
      assertNotNull("c_numeric_int column should exist", numericCol);
      assertEquals("NUMERIC(10,0) should resolve to Long",
          Long.class.getName(),
          numericCol.getFullyQualifiedJavaType().getFullyQualifiedName());

      // Verify account_profile table — auto-increment PK
      IntrospectedTable accountTable = tables.stream()
          .filter(t -> t.getFullyQualifiedTable().getIntrospectedTableName()
              .equals("account_profile"))
          .findFirst().orElse(null);
      assertNotNull("account_profile should exist", accountTable);
      IntrospectedColumn idCol = accountTable.getColumn("id").orElse(null);
      assertNotNull("id column should exist", idCol);
      assertTrue("id should be auto increment", idCol.isAutoIncrement());
      assertTrue("id should be primary key",
          accountTable.getPrimaryKeyColumns().contains(idCol));

      // Verify composite_pk_example table
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
