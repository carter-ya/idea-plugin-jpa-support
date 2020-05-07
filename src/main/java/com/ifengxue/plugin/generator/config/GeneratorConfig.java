package com.ifengxue.plugin.generator.config;

import java.util.List;

public class GeneratorConfig {

  private DriverConfig driverConfig;
  private TablesConfig tablesConfig;
  private List<PluginConfig> pluginConfigs;

  public DriverConfig getDriverConfig() {
    return driverConfig;
  }

  public void setDriverConfig(DriverConfig driverConfig) {
    this.driverConfig = driverConfig;
  }

  public TablesConfig getTablesConfig() {
    return tablesConfig;
  }

  public void setTablesConfig(TablesConfig tablesConfig) {
    this.tablesConfig = tablesConfig;
  }

  public List<PluginConfig> getPluginConfigs() {
    return pluginConfigs;
  }

  public void setPluginConfigs(List<PluginConfig> pluginConfigs) {
    this.pluginConfigs = pluginConfigs;
  }

  @Override
  public String toString() {
    return "GeneratorConfig{" +
        "driverConfig=" + driverConfig +
        ", tablesConfig=" + tablesConfig +
        ", pluginConfigs=" + pluginConfigs +
        '}';
  }
}