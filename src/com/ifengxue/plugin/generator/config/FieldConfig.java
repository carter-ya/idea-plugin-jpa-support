package com.ifengxue.plugin.generator.config;

public class FieldConfig {

  private String type;
  private String name;

  public String getType() {
    return type;
  }

  public FieldConfig setType(String type) {
    this.type = type;
    return this;
  }

  public String getName() {
    return name;
  }

  public FieldConfig setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public String toString() {
    return "FieldConfig{" +
        "type='" + type + '\'' +
        ", name='" + name + '\'' +
        '}';
  }
}
