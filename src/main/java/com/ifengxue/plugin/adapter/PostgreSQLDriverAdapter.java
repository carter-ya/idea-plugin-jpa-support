package com.ifengxue.plugin.adapter;

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
}
