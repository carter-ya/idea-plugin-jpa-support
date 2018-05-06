package com.ifengxue.plugin.generator.config;

import java.util.HashMap;
import java.util.Map;

public class PluginConfig {

  /**
   * 插件名称
   */
  private String name;
  private Class<?> clazz;
  private Map<String, String> propertyMap = new HashMap<>();

  public String getName() {
    return name;
  }

  public PluginConfig setName(String name) {
    this.name = name;
    return this;
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public PluginConfig setClazz(Class<?> clazz) {
    this.clazz = clazz;
    return this;
  }

  public PluginConfig addProperty(String name, String value) {
    propertyMap.put(name, value);
    return this;
  }

  public Map<String, String> getPropertyMap() {
    return propertyMap;
  }

  @Override
  public String toString() {
    return "PluginConfig{" +
        "name='" + name + '\'' +
        ", clazz=" + clazz +
        ", propertyMap=" + propertyMap +
        '}';
  }
}
