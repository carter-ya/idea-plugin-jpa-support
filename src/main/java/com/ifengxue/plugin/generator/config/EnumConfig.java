package com.ifengxue.plugin.generator.config;

import java.util.List;

public class EnumConfig {

  private String simpleName;
  private List<FieldConfig> fieldConfigs;
  private List<EnumInstance> instances;

  public String getSimpleName() {
    return simpleName;
  }

  public EnumConfig setSimpleName(String simpleName) {
    this.simpleName = simpleName;
    return this;
  }

  public List<EnumInstance> getInstances() {
    return instances;
  }

  public EnumConfig setInstances(List<EnumInstance> instances) {
    this.instances = instances;
    return this;
  }

  public List<FieldConfig> getFieldConfigs() {
    return fieldConfigs;
  }

  public EnumConfig setFieldConfigs(List<FieldConfig> fieldConfigs) {
    this.fieldConfigs = fieldConfigs;
    return this;
  }

  @Override
  public String toString() {
    return "EnumConfig{" +
        "simpleName='" + simpleName + '\'' +
        ", fieldConfigs=" + fieldConfigs +
        ", instances=" + instances +
        '}';
  }
}
