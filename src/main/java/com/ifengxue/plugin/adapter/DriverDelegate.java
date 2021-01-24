package com.ifengxue.plugin.adapter;

import java.sql.Driver;
import java.sql.SQLException;
import lombok.Getter;
import lombok.experimental.Delegate;

public class DriverDelegate implements Driver {

  private interface ExcludeMethods {

    boolean acceptsURL(String url) throws SQLException;
  }

  @Getter
  @Delegate
  private final Driver driver;
  @Getter
  private final String driverPath;

  public DriverDelegate(Driver driver, String driverPath) {
    this.driver = driver;
    this.driverPath = driverPath;
  }

}
