package com.ifengxue.plugin.generator.tree.visitor;

import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.tree.Class;
import com.ifengxue.plugin.generator.tree.Element;

public class BasicVisitor extends VisitorSupport {

  private TablesConfig tablesConfig;

  public BasicVisitor(TablesConfig tablesConfig) {
    this.tablesConfig = tablesConfig;
  }

  @Override
  public void visit(Class aClass) {
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
