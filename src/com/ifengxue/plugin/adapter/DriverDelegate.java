package com.ifengxue.plugin.adapter;

import java.sql.Driver;
import lombok.Getter;
import lombok.experimental.Delegate;

public class DriverDelegate implements Driver {

  @Delegate
  @Getter
  private final Driver driver;

  public DriverDelegate(Driver driver) {
    this.driver = driver;
  }
}
