package com.ifengxue.plugin.adapter;

import com.ifengxue.plugin.entity.ColumnSchema;
import java.sql.SQLException;
import java.util.List;

/**
 * 驱动适配器
 */
public interface DriverAdapter {

  /**
   * 查询指定数据库指定表格的schema
   *
   * @param database database name
   * @param table table name
   */
  @Deprecated
  default List<ColumnSchema> findTableSchemas(String database, String table) throws SQLException {
    throw new UnsupportedOperationException();
  }

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
