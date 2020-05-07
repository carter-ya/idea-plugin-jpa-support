package com.ifengxue.plugin.adapter;

import java.sql.Driver;
import lombok.Getter;
import lombok.experimental.Delegate;

public class DriverDelegate implements Driver {

  @Delegate
  @Getter
  private final Driver driver;
  @Getter
  private final DatabaseDrivers databaseDrivers;

  public DriverDelegate(Driver driver, DatabaseDrivers databaseDrivers) {
    this.driver = driver;
    this.databaseDrivers = databaseDrivers;
  }
}
