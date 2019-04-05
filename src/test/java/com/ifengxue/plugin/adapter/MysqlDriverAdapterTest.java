package com.ifengxue.plugin.adapter;

import org.junit.Assert;
import org.junit.Test;

public class MysqlDriverAdapterTest {

  private MysqlDriverAdapter driverAdapter = new MysqlDriverAdapter();

  @Test
  public void toConnectionUrl() {
    String connectionUrl = driverAdapter.toConnectionUrl("", "localhost", "3306", "root", "example");
    Assert.assertEquals("jdbc:mysql://localhost:3306/example", connectionUrl);
    connectionUrl += "?charset=utf8&useUnicode=true";
    String newConnectionUrl = driverAdapter.toConnectionUrl(connectionUrl, "localhost2", "3307", "", "example2");
    Assert.assertEquals("jdbc:mysql://localhost2:3307/example2?charset=utf8&useUnicode=true", newConnectionUrl);
  }
}