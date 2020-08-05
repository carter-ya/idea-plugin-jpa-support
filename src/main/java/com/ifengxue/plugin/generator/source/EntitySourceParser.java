package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.tree.Comment;
import com.ifengxue.plugin.generator.tree.Element;
import com.ifengxue.plugin.generator.tree.EntitySourceFile;
import com.ifengxue.plugin.generator.tree.Extends;
import com.ifengxue.plugin.generator.tree.Field;
import com.ifengxue.plugin.generator.tree.Implements;
import com.ifengxue.plugin.generator.tree.Import;
import com.ifengxue.plugin.generator.tree.ToStringMethod;
import com.ifengxue.plugin.generator.tree.visitor.BasicVisitor;
import com.ifengxue.plugin.generator.tree.visitor.JpaVisitor;
import com.ifengxue.plugin.generator.tree.visitor.MybatisVisitor;
import java.io.Serializable;

/**
 * 实体源码解析器
 */
@Deprecated
public class EntitySourceParser implements SourceParser {

  @Override
  public String parse(GeneratorConfig config, Table table) {
    EntitySourceFile sourceFile = new EntitySourceFile();
    sourceFile.addChild(new com.ifengxue.plugin.generator.tree.Package(table.getPackageName()));
    Import myImport = new Import();
    sourceFile.addChild(myImport);
    com.ifengxue.plugin.generator.tree.Class myClass = new com.ifengxue.plugin.generator.tree.Class(
        table.getEntityName());
    sourceFile.addChild(myClass);

    TablesConfig tablesConfig = config.getTablesConfig();
    Element.Indent indent = Element.Indent.findByDTDDeclare(tablesConfig.getIndent());
    sourceFile.setIndent(indent);
    myClass.setIndent(indent);
    if (tablesConfig.isUseClassComment() && !table.getTableComment().isEmpty()) {
      Comment classComment = Comment.newClassComment(table.getTableComment());
      classComment.setIndent(indent);
      myClass.addChild(classComment);
    }
    // 重写toString
    if (tablesConfig.isUseLombok()) {
      myClass.addChild(new ToStringMethod() {
        @Override
        public String toString() {
          return "";
        }
      });
    }
    Extends myExtends = new Extends(tablesConfig.getExtendsEntityName());
    if (!myExtends.getEntityName().isEmpty()) {
      myClass.setAnExtends(myExtends);
    }
    Implements myImplements = new Implements();
    myImplements.setIndent(indent);
    if (tablesConfig.isSerializable()) {
      myImplements.addImplementClass(Serializable.class.getName());
    }
    myClass.setAnImplements(myImplements);

    table.getColumns().forEach(column -> {
      Field field = new Field(column.getFieldName(), column.getJavaDataType(), tablesConfig.isUseLombok());
      field.setPrimaryKey(column.isPrimary());
      field.setAutoIncrement(column.isAutoIncrement());
      field.setNullable(column.isNullable());
      field.setHasDefaultValue(column.isHasDefaultValue());
      field.setDefaultValue(column.getDefaultValue());
      if (tablesConfig.isUseFieldComment() && !column.getColumnComment().isEmpty()) {
        field.addChild(Comment.newFieldComment(column.getColumnComment(), indent));
      }
      field.setIndent(indent);
      myClass.addChild(field);
    });
    switch (config.getTablesConfig().getOrm()) {
      case BASIC:
        sourceFile.accept(new BasicVisitor(table, config.getTablesConfig()));
        break;
      case MYBATIS:
        sourceFile.accept(new MybatisVisitor(table, config.getTablesConfig()));
        break;
      case JPA:
        sourceFile.accept(new JpaVisitor(table, config.getTablesConfig(), config.getDriverConfig().getVendor()));
        break;
      default:
        throw new IllegalStateException();
    }
    return sourceFile.toJavaCode();
  }
}
