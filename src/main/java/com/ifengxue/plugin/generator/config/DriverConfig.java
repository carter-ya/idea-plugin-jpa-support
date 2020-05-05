package com.ifengxue.plugin.generator.config;

public class DriverConfig {

  private Vendor vendor;
  private Class<?> driverClass;
  private String username;
  private String password;
  private String url;

  public Vendor getVendor() {
    return vendor;
  }

  public DriverConfig setVendor(Vendor vendor) {
    this.vendor = vendor;
    return this;
  }

  public Class<?> getDriverClass() {
    return driverClass;
  }

  public DriverConfig setDriverClass(Class<?> driverClass) {
    this.driverClass = driverClass;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public DriverConfig setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public DriverConfig setPassword(String password) {
    this.password = password;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public DriverConfig setUrl(String url) {
    this.url = url;
    return this;
  }

  @Override
  public String toString() {
    return "DriverConfig{" +
        "vendor=" + vendor +
        ", driverClass=" + driverClass +
        ", username='" + username + '\'' +
        ", password='" + password + '\'' +
        ", url='" + url + '\'' +
        '}';
  }

}
