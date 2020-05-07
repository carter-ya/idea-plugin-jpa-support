package com.ifengxue.plugin.generator.tree;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ToStringMethod extends AbstractElement {

  @Override
  public String toString() {
    Objects.requireNonNull(parent);
    Class aClass = (Class) this.parent;
    List<Element> fields = siblings("field");
    if (fields.isEmpty()) {
      return aClass.getSimpleName() + "{}";
    }
    StringBuilder builder = new StringBuilder("public String toString() {").append(lineSeparator);
    builder.append(indent.getIndent()).append("return \"").append(aClass.getSimpleName()).append("{");
    builder.append(fields.stream()
        .map(element -> (Field) element)
        .filter(field -> !field.getModifiers().contains("static final"))
        .map(field -> field.getFieldName() + "=\" + " + field.getFieldName() + " + " + lineSeparator + indent
            .getDoubleIndent() + "\"")
        .collect(Collectors.joining(", ")));
    builder.append("}\";").append(lineSeparator).append("}");
    return builder.toString();
  }
}
