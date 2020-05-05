package com.ifengxue.plugin.generator.tree;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Import extends AbstractElement {

  private static final Set<String> PRIMITIVE_CLASS = new HashSet<>();

  static {
    PRIMITIVE_CLASS.add("byte");
    PRIMITIVE_CLASS.add("char");
    PRIMITIVE_CLASS.add("short");
    PRIMITIVE_CLASS.add("int");
    PRIMITIVE_CLASS.add("long");
    PRIMITIVE_CLASS.add("float");
    PRIMITIVE_CLASS.add("double");
    PRIMITIVE_CLASS.add("boolean");
  }

  private Set<String> imports = new HashSet<>();

  public Import addImportClass(String importClass) {
    if (importClass.startsWith("java.lang.")) {
      return this;
    }
    if (PRIMITIVE_CLASS.contains(importClass)) {
      return this;
    }
    imports.add(importClass);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    List<String> imports = new LinkedList<>(this.imports);
    imports.stream()
        .sorted()
        .forEach(importClass -> builder.append("import " + importClass + ";").append(lineSeparator));
    return builder.toString();
  }
}
