package com.ifengxue.plugin.generator.tree;

import com.ifengxue.plugin.util.StringHelper;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Annotation extends AbstractElement {

  private final String className;
  private final String annotationName;
  private Set<KeyValuePair> keyValuePairs = new HashSet<>();

  public Annotation(String className) {
    this.className = className;
    this.annotationName = StringHelper.parseSimpleName(className);
  }

  public Annotation addKeyValuePair(KeyValuePair keyValuePair) {
    keyValuePairs.add(keyValuePair);
    return this;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("@").append(annotationName);
    if (keyValuePairs.isEmpty()) {
      return builder.toString();
    }
    builder.append("(");
    builder.append(keyValuePairs.stream()
        .map(keyValuePair -> keyValuePair.getKey() + " = " + keyValuePair.getValue())
        .collect(Collectors.joining(", ")));
    builder.append(")");
    return builder.toString();
  }
}
