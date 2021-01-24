package com.ifengxue.plugin.adapter;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.entity.MybatisGeneratorTableSchema;
import com.ifengxue.plugin.entity.TableSchema;
import fastjdbc.SimpleFastJdbc;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.db.DatabaseIntrospector;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

public abstract class AbstractDriverAdapter implements DriverAdapter {

  protected static Pattern connectionUrlPattern;

  static {
    connectionUrlPattern = Pattern.compile(".*\\?(.*)");
  }

  @Override
  public List<TableSchema> findDatabaseSchemas(String database) throws SQLException {
    DataSource datasource = ((SimpleFastJdbc) Holder.getFastJdbc()).getDatasource();
    try (Connection connection = datasource.getConnection()) {
      List<String> warnings = new ArrayList<>();
      Context context = new Context(ModelType.FLAT);
      DatabaseIntrospector introspector = new DatabaseIntrospector(
          context, connection.getMetaData(), new JavaTypeResolverDefaultImpl(), warnings);
      TableConfiguration tc = new TableConfiguration(context);
      tc.setCatalog(database);
      return introspector.introspectTables(tc)
          .stream()
          .map(MybatisGeneratorTableSchema::new)
          .collect(Collectors.toList());
    }
  }

  @Override
  public String toConnectionUrl(String oldConnectionUrl, String host, String port, String username, String database) {
    Matcher matcher = connectionUrlPattern.matcher(oldConnectionUrl);
    String params = "";
    if (matcher.matches()) {
      params = matcher.group(1);
    }
    return toConnectionUrl(oldConnectionUrl, host, port, username, database, params);
  }

  /**
   * @param params url query params.
   */
  protected abstract String toConnectionUrl(String oldConnectionUrl, String host, String port, String username,
      String database, String params);
}
