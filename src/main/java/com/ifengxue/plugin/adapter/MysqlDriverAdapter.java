package com.ifengxue.plugin.adapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class MysqlDriverAdapter implements DriverAdapter {

  private static Pattern connectionUrlPattern;

  static {
    connectionUrlPattern = Pattern.compile(".*\\?(.*)");
  }

  @Override
  public String toConnectionUrl(String oldConnectionUrl, String host, String port, String username, String database) {
    Matcher matcher = connectionUrlPattern.matcher(oldConnectionUrl);
    String params = "";
    if (matcher.matches()) {
      params = matcher.group(1);
    }
    String connectionUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;
    if (StringUtils.isNotBlank(params)) {
      connectionUrl += "?" + params;
    }
    return connectionUrl;
  }

}
