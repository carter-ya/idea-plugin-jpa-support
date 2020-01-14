package com.ifengxue.plugin.generator.tree.visitor;

import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.tree.Element;

public class BasicVisitor extends VisitorSupport {

  public BasicVisitor(Table table, TablesConfig tablesConfig) {
    super(table, tablesConfig);
  }

  @Override
  public void visit(com.ifengxue.plugin.generator.tree.Class aClass) {
    super.visit(aClass);
  }

  @Override
  public void visit(com.ifengxue.plugin.generator.tree.Field field) {
    super.visit(field);
    if (!field.getFieldName().equals(Element.SERIAL_VERSION_UID)) {
      field.setHasDefaultValue(false);
    }
  }
}
