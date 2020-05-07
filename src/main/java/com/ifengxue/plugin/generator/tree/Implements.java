package com.ifengxue.plugin.generator.tree;

import com.ifengxue.plugin.util.StringHelper;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Implements extends AbstractElement {

  private Set<String> implementSet = new HashSet<>();

  public Implements addImplementClass(String implementClass) {
    implementSet.add(implementClass);
    return this;
  }

  public Implements addImplementClass(java.lang.Class<?> implementClass) {
    return addImplementClass(implementClass.getName());
  }

  public boolean contains(String implementClass) {
    return implementSet.contains(implementClass);
  }

  public boolean contains(java.lang.Class<?> implementClass) {
    return implementSet.contains(implementClass.getName());
  }

  public Set<String> getImplementSet() {
    return implementSet;
  }

  @Override
  public String toString() {
    if (implementSet.isEmpty()) {
      return "";
    }
    return "implements " + implementSet.stream()
        .map(StringHelper::parseSimpleName)
        .collect(Collectors.joining(", "));
  }
}
