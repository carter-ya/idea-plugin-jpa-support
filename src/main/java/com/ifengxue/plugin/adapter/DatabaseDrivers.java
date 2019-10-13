package com.ifengxue.plugin.adapter;

import com.ifengxue.plugin.generator.config.Vendor;
import lombok.Getter;

public enum DatabaseDrivers {
  MYSQL_V_5_1_47("Mysql",
      Vendor.MYSQL,
      "5.1.47",
      "com.mysql.jdbc.Driver",
      new MysqlDriverAdapter(),
      "drivers_mysql_5_1_47"),
  MYSQL("Mysql",
      Vendor.MYSQL,
      "6.0.6",
      "com.mysql.cj.jdbc.Driver",
      new MysqlDriverAdapter(),
      "classpath:lib/mysql-connector-java-6.0.6.jar"),
  MYSQL_V_8_0_16("Mysql",
      Vendor.MYSQL,
      "8.0.16",
      "com.mysql.cj.jdbc.Driver",
      new MysqlDriverAdapter(),
      "drivers_mysql_8_0_16"),
  POSTGRE_SQL("PostgreSQL",
      Vendor.POSTGRE_SQL,
      "42.2.5",
      "org.postgresql.Driver",
      new PostgreSQLDriverAdapter(),
      "drivers_postgresql"),
  ;
  public static final String CLASSPATH_PREFIX = "classpath:";
  @Getter
  private final String vendor;
  @Getter
  private final Vendor vendor2;
  @Getter
  private final String version;
  @Getter
  private final String driverClass;
  @Getter
  private final DriverAdapter driverAdapter;
  @Getter
  private final String url;

  DatabaseDrivers(String vendor, Vendor vendor2, String version, String driverClass,
      DriverAdapter driverAdapter, String url) {
    this.vendor = vendor;
    this.vendor2 = vendor2;
    this.version = version;
    this.driverClass = driverClass;
    this.driverAdapter = driverAdapter;
    this.url = url;
  }

  public String getJarFilename() {
    return toString() + ".jar";
  }

  @Override
  public String toString() {
    return vendor + "-" + version;
  }
}
