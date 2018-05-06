package com.ifengxue.plugin.generator.tree.visitor;

import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.config.Vendor;
import com.ifengxue.plugin.generator.tree.Annotation;
import com.ifengxue.plugin.generator.tree.Element;
import com.ifengxue.plugin.generator.tree.Element.KeyValuePair;
import com.ifengxue.plugin.generator.tree.Field;
import java.util.NoSuchElementException;

public class JpaVisitor extends VisitorSupport {

  private final Table table;
  private final TablesConfig tablesConfig;
  private final Vendor vendor;

  public JpaVisitor(Table table, TablesConfig tablesConfig, Vendor vendor) {
    this.table = table;
    this.tablesConfig = tablesConfig;
    this.vendor = vendor;
  }

  @Override
  public void visit(com.ifengxue.plugin.generator.tree.Class aClass) {
    super.visit(aClass);
    Annotation entityAnnotation = new Annotation("javax.persistence.Entity");
    anImport.addImportClass(entityAnnotation.getClassName());
    aClass.addAnnotation(entityAnnotation);
    Annotation tableAnnotation = new Annotation("javax.persistence.Table");
    tableAnnotation.addKeyValuePair(Element.KeyValuePair.newKeyAndStringValuePair("name", table.getTableName()));
    anImport.addImportClass(tableAnnotation.getClassName());
    aClass.addAnnotation(tableAnnotation);

    // lombok support
    if (tablesConfig.isUseLombok()) {
      Annotation annotation = new Annotation("lombok.Data");
      anImport.addImportClass(annotation.getClassName());
      aClass.addAnnotation(annotation);
      if (!tablesConfig.getExtendsEntityName().isEmpty()) {
        annotation = new Annotation("lombok.EqualsAndHashCode");
        annotation.addKeyValuePair(KeyValuePair.newKeyValuePair("callSuper", "true"));
        anImport.addImportClass(annotation.getClassName());
        aClass.addAnnotation(annotation);
      }
    }
  }

  @Override
  public void visit(Field field) {
    super.visit(field);
    if (field.getModifiers().contains("static final")) {
      return;
    }
    if (field.isPrimaryKey()) {
      Annotation idAnnotation = new Annotation("javax.persistence.Id");
      anImport.addImportClass(idAnnotation.getClassName());
      field.addChild(idAnnotation);
    }
    if (field.isAutoIncrement()) {
      Annotation generatedAnnotation = new Annotation("javax.persistence.GeneratedValue");
      switch (vendor) {
        case MYSQL:
        case SQL_SERVER:
          generatedAnnotation
              .addKeyValuePair(Element.KeyValuePair.newKeyValuePair("strategy", "GenerationType.SEQUENCE"));
          break;
        case ORACLE:
          generatedAnnotation
              .addKeyValuePair(Element.KeyValuePair.newKeyValuePair("strategy", "GenerationType.IDENTITY"));
          break;
        default:
          throw new IllegalStateException();
      }
      anImport.addImportClass(generatedAnnotation.getClassName());
      anImport.addImportClass("javax.persistence.GenerationType");
      field.addChild(generatedAnnotation);
    }
    Column matchColumn = table.getColumns().stream()
        .filter(column -> column.getFieldName().equals(field.getFieldName()))
        .findAny()
        .orElseThrow(() -> new NoSuchElementException("field " + field.getFieldName() + " not found!"));
    Annotation columnAnnotation = new Annotation("javax.persistence.Column");
    anImport.addImportClass(columnAnnotation.getClassName());
    columnAnnotation
        .addKeyValuePair(Element.KeyValuePair.newKeyAndStringValuePair("name", matchColumn.getColumnName()));
    if (field.isPrimaryKey()) {
      columnAnnotation.addKeyValuePair(Element.KeyValuePair.newKeyValuePair("insertable", "false"));
    }
    if (field.isNullable()) {
      columnAnnotation.addKeyValuePair(Element.KeyValuePair.newKeyValuePair("nullable", "false"));
    }
    field.addChild(columnAnnotation);
  }
}
