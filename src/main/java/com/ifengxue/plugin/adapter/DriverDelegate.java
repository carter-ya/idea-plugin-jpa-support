package com.ifengxue.plugin.adapter;

import com.ifengxue.plugin.Holder;
import java.sql.Driver;
import java.sql.SQLException;
import lombok.Getter;
import lombok.experimental.Delegate;

public class DriverDelegate implements Driver {

  private interface ExcludeMethods {

    boolean acceptsURL(String url) throws SQLException;
  }

  @Getter
  @Delegate(excludes = ExcludeMethods.class)
  private final Driver driver;
  @Getter
  private final DatabaseDrivers databaseDrivers;

  public DriverDelegate(Driver driver, DatabaseDrivers databaseDrivers) {
    this.driver = driver;
    this.databaseDrivers = databaseDrivers;
  }

  @Override
  public boolean acceptsURL(String url) throws SQLException {
    return Holder.getDatabaseDrivers() == databaseDrivers && driver.acceptsURL(url);
  }

}
