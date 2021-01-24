package com.ifengxue.plugin.adapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractDriverAdapter implements DriverAdapter {

  protected static Pattern connectionUrlPattern;

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
    return toConnectionUrl(oldConnectionUrl, host, port, username, database, params);
  }

  /**
   * @param params url query params.
   */
  protected abstract String toConnectionUrl(String oldConnectionUrl, String host, String port, String username,
      String database, String params);
}
