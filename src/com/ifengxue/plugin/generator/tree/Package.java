package com.ifengxue.plugin.generator.tree;

public class Package extends AbstractElement {

  private final String packageName;

  public Package(final String packageName) {
    this.packageName = packageName;
  }

  @Override
  public String toString() {
    return "package " + packageName + ";";
  }
}
