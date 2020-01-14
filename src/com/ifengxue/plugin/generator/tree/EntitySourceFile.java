package com.ifengxue.plugin.generator.tree;

import com.ifengxue.plugin.util.StringHelper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EntitySourceFile extends AbstractElement {

  private Package aPackage;
  private Import anImport;
  private Class aClass;
  private boolean isVisit;

  @Override
  public void addChild(Element child) {
    super.addChild(child);
    if (child instanceof Package) {
      setPackage((Package) child);
    }
    if (child instanceof Import) {
      setImport((Import) child);
    }
    if (child instanceof Class) {
      setClass((Class) child);
    }
  }

  private EntitySourceFile setClass(Class aClass) {
    this.aClass = aClass;
    return this;
  }

  private EntitySourceFile setPackage(Package aPackage) {
    this.aPackage = aPackage;
    return this;
  }

  private EntitySourceFile setImport(Import anImport) {
    this.anImport = anImport;
    return this;
  }

  public Package getaPackage() {
    return aPackage;
  }

  public Import getAnImport() {
    return anImport;
  }

  public Class getaClass() {
    return aClass;
  }

  protected void doVisit() {
    if (visitor == null) {
      return;
    }
    if (isVisit) {
      return;
    }
    visitor.visit(aPackage);
    visitor.visit(aClass);
    aClass.fields().forEach(visitor::visit);
    visitor.visit(anImport);
    isVisit = true;
  }

  @Override
  public String toString() {
    doVisit();
    StringBuilder builder = new StringBuilder(1000);
    // 包
    builder.append(aPackage.toJavaCode()).append(StringHelper.repeat(lineSeparator, 2));
    // 导入的类
    String importString = anImport.toJavaCode();
    if (!importString.isEmpty()) {
      builder.append(anImport.toJavaCode()).append(lineSeparator);
    }
    // 类的注释
    aClass.findComment().ifPresent(comment -> builder.append(comment.toJavaCode()).append(lineSeparator));
    // 类注解
    String annotationString = aClass.getAnnotations()
        .stream()
        .map(Annotation::toJavaCode)
        .collect(Collectors.joining(lineSeparator));
    if (!annotationString.isEmpty()) {
      builder.append(annotationString)
          .append(lineSeparator);
    }
    // 类声明
    builder.append(aClass.toJavaCode()).append(" {").append(lineSeparator);
    aClass.fields().stream()
        .filter(field -> field.getFieldName()
            .equals(SERIAL_VERSION_UID))
        .findAny().ifPresent(field -> builder.append(indent.getIndent()).append(field.toJavaCode())
        .append(StringHelper.repeat(lineSeparator, 2)));

    aClass.fields().forEach(field -> {
      if (field.getFieldName().equals(SERIAL_VERSION_UID)) {
        return;
      }
      field.findComment()
          .ifPresent(comment -> builder.append(indent.getIndent()).append(comment.toJavaCode()).append(lineSeparator));
      List<Annotation> annotations = field.findAnnotations();
      if (!annotations.isEmpty()) {
        builder.append(annotations.stream()
            .map(annotation -> indent.getIndent() + annotation.toJavaCode())
            .collect(Collectors.joining(lineSeparator))).append(lineSeparator);
      }
      builder.append(indent.getIndent()).append(field.toJavaCode()).append(StringHelper.repeat(lineSeparator, 2));
    });
    aClass.fields().forEach(field -> {
      Optional.ofNullable(field.getMethod())
          .ifPresent(getMethod -> {
            field.findComment().ifPresent(
                comment -> builder.append(indent.getIndent()).append(comment.toJavaCode()).append(lineSeparator));
            builder.append(StringHelper.insertIndentBefore(getMethod.toJavaCode(), lineSeparator, indent.getIndent()))
                .append(StringHelper.repeat(lineSeparator, 2));
          });
      Optional.ofNullable(field.setMethod())
          .ifPresent(setMethod -> {
            field.findComment().ifPresent(
                comment -> builder.append(indent.getIndent()).append(comment.toJavaCode()).append(lineSeparator));
            builder.append(StringHelper.insertIndentBefore(setMethod.toJavaCode(), lineSeparator, indent.getIndent()))
                .append(StringHelper.repeat(lineSeparator, 2));
          });
    });
    // toString
    ToStringMethod toStringMethod = Optional.ofNullable(aClass.toStringMethod())
        .orElseGet(() -> {
          ToStringMethod method = new ToStringMethod();
          aClass.addChild(method);
          return method;
        });
    builder.append(StringHelper.insertIndentBefore(toStringMethod.toJavaCode(), lineSeparator, indent.getIndent()))
        .append(lineSeparator);
    builder.append("}");
    return builder.toString();
  }
}
