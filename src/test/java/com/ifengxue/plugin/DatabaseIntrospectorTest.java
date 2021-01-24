package com.ifengxue.plugin;

import fastjdbc.NoPoolDataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.db.DatabaseIntrospector;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

public class DatabaseIntrospectorTest {

  @BeforeClass
  public static void beforeClass() throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
  }

  @Test
  public void introspectTables() throws Exception {
    DataSource dataSource = new NoPoolDataSource(
        "jdbc:mysql://localhost:3306/test?useInformationSchema=true&useSSL=false",
        "user_rw", "");
    try (Connection connection = dataSource.getConnection()) {
      List<String> warnings = new ArrayList<>();
      Context context = new Context(ModelType.FLAT);
      DatabaseIntrospector introspector = new DatabaseIntrospector(
          context, connection.getMetaData(), new JavaTypeResolverDefaultImpl(), warnings);
      TableConfiguration tc = new TableConfiguration(context);
      tc.setCatalog("test");
      List<IntrospectedTable> tables = introspector.introspectTables(tc);
      for (IntrospectedTable table : tables) {
        if (table.getFullyQualifiedTable().getIntrospectedTableName().equals("t_complex")) {
          System.out.println();
        }
        List<IntrospectedColumn> columns = table.getAllColumns();
        for (IntrospectedColumn column : columns) {
          System.out.println(column);
        }
      }
    }
  }
}
