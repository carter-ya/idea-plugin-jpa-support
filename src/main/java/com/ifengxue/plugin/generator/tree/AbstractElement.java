package com.ifengxue.plugin.generator.tree;

import com.ifengxue.plugin.generator.tree.visitor.Visitor;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractElement implements Element {

  protected Element parent;
  protected List<Element> children = new LinkedList<>();
  protected Indent indent = Indent.TWO_SPACE;
  protected String lineSeparator = LINE_SEPARATOR;
  protected Visitor visitor;

  @Override
  public void setLineSeparator(String lineSeparator) {
    this.lineSeparator = lineSeparator;
  }

  @Override
  public void setIndent(Indent indent) {
    Objects.requireNonNull(indent);
    this.indent = indent;
  }

  @Override
  public void setParent(Element parent) {
    this.parent = parent;
  }

  @Override
  public Element parent() {
    return parent;
  }

  @Override
  public Element parents(String name) {
    Element parent = this.parent;
    while (parent != null) {
      if (parent.name().equals(name)) {
        return parent;
      }
      parent = parent.parent();
    }
    return null;
  }

  @Override
  public synchronized void addChild(Element child) {
    Objects.requireNonNull(child);
    child.setParent(this);
    children.add(child);
  }

  @Override
  public List<Element> children() {
    return children;
  }

  @Override
  public Element firstSibling(String name) {
    List<Element> siblings = siblings(name);
    return siblings.isEmpty() ? null : siblings.get(0);
  }

  @Override
  public List<Element> siblings(String name) {
    return parent.children()
        .stream()
        .filter(child -> child.name().equals(name))
        .collect(Collectors.toList());
  }

  @Override
  public List<Element> siblings() {
    return parent.children();
  }

  @Override
  public String name() {
    return getClass().getSimpleName().toLowerCase();
  }

  @Override
  public String toJavaCode() {
    return toString();
  }

  @Override
  public void accept(Visitor visitor) {
    this.visitor = visitor;
  }

  protected Optional<Comment> findComment() {
    return children().stream()
        .filter(child -> child instanceof Comment)
        .findAny()
        .map(child -> (Comment) child);
  }

  protected List<Annotation> findAnnotations() {
    return children().stream()
        .filter(child -> child instanceof Annotation)
        .map(child -> (Annotation) child)
        .collect(Collectors.toList());
  }
}
