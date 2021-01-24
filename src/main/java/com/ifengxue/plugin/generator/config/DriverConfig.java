package com.ifengxue.plugin.generator.config;

import javax.annotation.Nullable;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DriverConfig {

  @Nullable
  private Vendor vendor;
  private Class<?> driverClass;
  private String username;
  private String password;
  private String url;
}
