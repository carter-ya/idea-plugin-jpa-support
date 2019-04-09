package com.ifengxue.plugin.adapter;

import lombok.Getter;

public enum DatabaseDrivers {
  MYSQL("Mysql",
      "6.0.6",
      "com.mysql.jdbc.Driver",
      new MysqlDriverAdapter(),
      "http://central.maven.org/maven2/mysql/mysql-connector-java/6.0.6/mysql-connector-java-6.0.6.jar"),
  POSTGRE_SQL("PostgreSQL",
      "42.2.5",
      "org.postgresql.Driver",
      new PostgreSQLDriverAdapter(),
      "http://central.maven.org/maven2/org/postgresql/postgresql/42.2.5/postgresql-42.2.5.jar"),
  ;
  @Getter
  private final String vendor;
  @Getter
  private final String version;
  @Getter
  private final String driverClass;
  @Getter
  private final DriverAdapter driverAdapter;
  @Getter
  private final String url;

  DatabaseDrivers(String vendor, String version, String driverClass,
      DriverAdapter driverAdapter, String url) {
    this.vendor = vendor;
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
