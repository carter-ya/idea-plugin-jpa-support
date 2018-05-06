package com.ifengxue.plugin.generator.tree;

public class Extends extends AbstractElement {

  private String entityName;
  private String packageName;

  public Extends(String extendsEntityName) {
    int lastIndex = extendsEntityName.lastIndexOf('.');
    if (lastIndex == -1) {
      entityName = extendsEntityName;
      packageName = "";
    } else {
      entityName = extendsEntityName.substring(lastIndex + 1);
      packageName = extendsEntityName.substring(0, lastIndex);
    }
  }

  public String getEntityName() {
    return entityName;
  }

  public Extends setEntityName(String entityName) {
    this.entityName = entityName;
    return this;
  }

  public String getPackageName() {
    return packageName;
  }

  public Extends setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  @Override
  public String toString() {
    if (entityName.isEmpty()) {
      return "";
    }
    return "extends " + entityName;
  }
}
