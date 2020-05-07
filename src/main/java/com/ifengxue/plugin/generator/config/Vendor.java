package com.ifengxue.plugin.generator.config;

public enum Vendor {
  MYSQL("mysql"),
  ORACLE("oracle"),
  SQL_SERVER("sqlserver"),
  POSTGRE_SQL("postgre sql");
  private final String alias;

  Vendor(String alias) {
    this.alias = alias;
  }

  public String getAlias() {
    return alias;
  }
}