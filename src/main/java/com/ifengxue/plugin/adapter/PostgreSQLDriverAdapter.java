package com.ifengxue.plugin.adapter;

import com.ifengxue.plugin.entity.ColumnSchema;
import com.ifengxue.plugin.entity.TableSchema;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class PostgreSQLDriverAdapter extends AbstractDriverAdapter {

  @Override
  protected String toConnectionUrl(String oldConnectionUrl, String host, String port, String username, String database,
      String params) {
    String connectionUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
    if (StringUtils.isNotBlank(params)) {
      connectionUrl += "?" + params;
    }
    return connectionUrl;
  }

  @Override
  public List<TableSchema> findDatabaseSchemas(String database) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ColumnSchema> findTableSchemas(String database, String table) throws SQLException {
    throw new UnsupportedOperationException();
  }
}
