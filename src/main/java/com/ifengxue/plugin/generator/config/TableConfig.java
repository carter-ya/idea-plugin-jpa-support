package com.ifengxue.plugin.generator.config;

import java.util.List;

public class TableConfig {

  private String tableName;
  private String simpleName;
  private List<EnumConfig> enumConfigs;

  public String getTableName() {
    return tableName;
  }

  public TableConfig setTableName(String tableName) {
    this.tableName = tableName;
    return this;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public TableConfig setSimpleName(String simpleName) {
    this.simpleName = simpleName;
    return this;
  }

  public List<EnumConfig> getEnumConfigs() {
    return enumConfigs;
  }

  public TableConfig setEnumConfigs(List<EnumConfig> enumConfigs) {
    this.enumConfigs = enumConfigs;
    return this;
  }

  @Override
  public String toString() {
    return "TableConfig{" +
        "tableName='" + tableName + '\'' +
        ", simpleName='" + simpleName + '\'' +
        ", enumConfigs=" + enumConfigs +
        '}';
  }
}
