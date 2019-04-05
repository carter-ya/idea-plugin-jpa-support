package com.ifengxue.plugin.adapter;

/**
 * 驱动适配器
 */
public interface DriverAdapter {

  /**
   * 转换为 connection url
   *
   * @param oldConnectionUrl 原始的 connection url
   * @param host host
   * @param port port
   * @param username username
   * @param database database
   */
  String toConnectionUrl(String oldConnectionUrl, String host, String port, String username, String database);
}
