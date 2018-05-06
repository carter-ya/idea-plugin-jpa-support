package com.ifengxue.plugin.generator.tree.visitor;

public interface Visitor {

  void visit(com.ifengxue.plugin.generator.tree.Package aPackage);

  void visit(com.ifengxue.plugin.generator.tree.Import anImport);

  void visit(com.ifengxue.plugin.generator.tree.Class aClass);

  void visit(com.ifengxue.plugin.generator.tree.Field field);
}
