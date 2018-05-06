package com.ifengxue.plugin.generator.config;

import java.util.HashMap;
import java.util.Map;

public class EnumInstance {

  // 实例名
  private String name;
  private Map<String, String> propertyMap = new HashMap<>();

  public String getName() {
    return name;
  }

  public EnumInstance setName(String name) {
    this.name = name;
    return this;
  }

  public EnumInstance addProperty(String name, String value) {
    propertyMap.put(name, value);
    return this;
  }

  public Map<String, String> getPropertyMap() {
    return propertyMap;
  }

  @Override
  public String toString() {
    return "EnumInstance{" +
        "name='" + name + '\'' +
        ", propertyMap=" + propertyMap +
        '}';
  }
}
