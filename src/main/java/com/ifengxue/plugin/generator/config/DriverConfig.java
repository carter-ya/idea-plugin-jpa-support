package com.ifengxue.plugin.generator.config;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DriverConfig {

  private Vendor vendor;
  private Class<?> driverClass;
  private String username;
  private String password;
  private String url;
}
