package com.ifengxue.plugin.generator.tree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Class extends AbstractElement {

  private final String simpleName;
  private Extends anExtends;
  private Implements anImplements;
  private Set<Annotation> annotations = new HashSet<>();

  public Class(String simpleName) {
    this.simpleName = simpleName;
  }

  public List<Field> fields() {
    return children.stream()
        .filter(child -> child instanceof Field)
        .map(child -> (Field) child)
        .collect(Collectors.toList());
  }

  public ToStringMethod toStringMethod() {
    return children.stream()
        .filter(child -> child instanceof ToStringMethod)
        .map(child -> (ToStringMethod) child)
        .findAny()
        .orElse(null);
  }

  public String getSimpleName() {
    return simpleName;
  }

  public Implements getAnImplements() {
    return anImplements;
  }

  public Class setAnImplements(Implements anImplements) {
    this.anImplements = anImplements;
    return this;
  }

  public Extends getAnExtends() {
    return anExtends;
  }

  public Class setAnExtends(Extends anExtends) {
    this.anExtends = anExtends;
    return this;
  }

  public Set<Annotation> getAnnotations() {
    return annotations;
  }

  public Class addAnnotation(Annotation annotation) {
    this.annotations.add(annotation);
    return this;
  }

  @Override
  public String toString() {
    String implementsCode = anImplements == null ? "" : anImplements.toJavaCode();
    String extendsCode = anExtends == null ? "" : anExtends.toJavaCode();
    return "public class " + simpleName +
        (extendsCode.isEmpty() ? "" : " " + extendsCode) +
        (implementsCode.isEmpty() ? "" : " " + implementsCode);
  }
}
