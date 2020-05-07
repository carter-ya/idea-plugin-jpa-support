package com.ifengxue.plugin.generator.tree.visitor;

import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.tree.Element;
import com.ifengxue.plugin.generator.tree.EntitySourceFile;
import com.ifengxue.plugin.generator.tree.Extends;
import com.ifengxue.plugin.generator.tree.Field;
import com.ifengxue.plugin.generator.tree.Import;
import java.io.Serializable;

public class VisitorSupport implements Visitor {

  protected final Table table;
  protected final TablesConfig tablesConfig;
  protected com.ifengxue.plugin.generator.tree.Import anImport;

  public VisitorSupport(Table table, TablesConfig tablesConfig) {
    this.table = table;
    this.tablesConfig = tablesConfig;
  }

  @Override
  public void visit(com.ifengxue.plugin.generator.tree.Package aPackage) {

  }

  @Override
  public void visit(com.ifengxue.plugin.generator.tree.Import anImport) {
    this.anImport = anImport;
  }

  @Override
  public void visit(com.ifengxue.plugin.generator.tree.Class aClass) {
    if (anImport == null) {
      anImport = ((EntitySourceFile) aClass.parent()).getAnImport();
    }
    if (aClass.getAnImplements().contains(Serializable.class)) {
      com.ifengxue.plugin.generator.tree.Field uidField = new Field(Element.SERIAL_VERSION_UID, long.class, false);
      uidField.setModifiers("private static final");
      uidField.setDefaultValue("1L");
      uidField.setHasDefaultValue(true);
      aClass.addChild(uidField);
    }
    aClass.getAnImplements().getImplementSet().forEach(anImport::addImportClass);
    Extends anExtends = aClass.getAnExtends();
    if (anExtends != null && !anExtends.getPackageName().isEmpty() &&
        !anExtends.getPackageName().equals(table.getPackageName())) {
      anImport.addImportClass(anExtends.getPackageName() + "." + anExtends.getEntityName());
    }
  }

  @Override
  public void visit(Field field) {
    if (anImport == null) {
      anImport = (Import) field.parent().siblings("import").get(0);
    }
    anImport.addImportClass(field.getDataType().getName());
  }
}
