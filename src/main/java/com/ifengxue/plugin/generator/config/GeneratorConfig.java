package com.ifengxue.plugin.generator.config;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GeneratorConfig {
  private String fileExtension;
  private DriverConfig driverConfig;
  private TablesConfig tablesConfig;
  private List<PluginConfig> pluginConfigs;
}
