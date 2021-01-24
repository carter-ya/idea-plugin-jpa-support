package com.ifengxue.plugin.adapter;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.entity.ColumnSchema;
import fastjdbc.Sql;
import fastjdbc.SqlBuilder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;

public class MysqlDriverAdapter extends AbstractDriverAdapter {

  @Override
  protected String toConnectionUrl(String oldConnectionUrl, String host, String port, String username, String database,
      String params) {
    String connectionUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;
    try {
      Map<String, String> shouldAddParams = new HashMap<>();
      // retrieve table remark
      shouldAddParams.put("useInformationSchema", "true");
      // disable ssl connection
      shouldAddParams.put("useSSL", "false");
      if (params == null) {
        params = "";
      }
      for (Entry<String, String> entry : shouldAddParams.entrySet()) {
        if (!StringUtils.contains(params, entry.getKey())) {
          params += "&" + entry.getKey() + "=" + entry.getValue();
        }
      }
      if (params.startsWith("&")) {
        params = params.substring(1);
      }
    } catch (Exception ignore) {
    }
    if (StringUtils.isNotBlank(params)) {
      connectionUrl += "?" + params;
    }
    return connectionUrl;
  }

  @Override
  public List<ColumnSchema> findTableSchemas(String database, String table) throws SQLException {
    Sql sql = SqlBuilder.newSelectBuilder(ColumnSchema.class)
        .select()
        .from()
        .where()
        .equal("tableSchema", database)
        .and().equal("tableName", table)
        .build();
    return Holder.getFastJdbc().find(sql.getSql(), ColumnSchema.class, sql.getArgs().toArray());
  }
}
