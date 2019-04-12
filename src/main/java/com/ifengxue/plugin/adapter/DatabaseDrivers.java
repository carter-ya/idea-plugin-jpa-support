package com.ifengxue.plugin.adapter;

import com.ifengxue.plugin.generator.config.Vendor;
import lombok.Getter;

public enum DatabaseDrivers {
  MYSQL("Mysql",
      Vendor.MYSQL,
      "6.0.6",
      "com.mysql.cj.jdbc.Driver",
      new MysqlDriverAdapter(),
      "http://central.maven.org/maven2/mysql/mysql-connector-java/6.0.6/mysql-connector-java-6.0.6.jar"),
  POSTGRE_SQL("PostgreSQL",
      Vendor.POSTGRE_SQL,
      "42.2.5",
      "org.postgresql.Driver",
      new PostgreSQLDriverAdapter(),
      "http://central.maven.org/maven2/org/postgresql/postgresql/42.2.5/postgresql-42.2.5.jar"),
  ;
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
