package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.tree.Annotation;
import com.ifengxue.plugin.generator.tree.Element;
import com.ifengxue.plugin.generator.tree.Element.KeyValuePair;
import com.ifengxue.plugin.util.StringHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;

public class EntitySourceParserV2 extends AbstractIDEASourceParser {

  @Override
  protected String parse(GeneratorConfig config, Table table, Supplier<String> templateProvider) {
    VelocityContext context = new VelocityContext();
    TablesConfig tablesConfig = config.getTablesConfig();
    context.put("config", config);
    context.put("tablesConfig", config.getTablesConfig());
    context.put("table", table);
    context.put("empty", "");
    // set indent
    context.put("indent", Element.Indent.findByDTDDeclare(tablesConfig.getIndent()));
    context.put("package", tablesConfig.getEntityPackageName());
    Set<String> importClassList = new HashSet<>();
    context.put("importClassList", importClassList);
    context.put("simpleName", table.getEntityName());
    context.put("parentClass", tablesConfig.getExtendsEntityName());
    Set<String> implementClassList = new HashSet<>();
    context.put("implementClassList", implementClassList);

    // implement Serializable
    if (tablesConfig.isSerializable()) {
      importClassList.add(Serializable.class.getName());
      implementClassList.add(Serializable.class.getSimpleName());
      context.put("serialVersionUID", "1");
    }

    // is use JPA annotations
    boolean isUseJpaAnnotation = tablesConfig.isUseJpaAnnotation();
    context.put("useJpaAnnotation", isUseJpaAnnotation);

    // is use Jakarta EE
    boolean isUseJakartaEE = tablesConfig.isUseJakartaEE();
    context.put("useJakartaEE", isUseJakartaEE);

    Set<String> classAnnotations = new HashSet<>();
    // is use lombok
    context.put("useLombok", tablesConfig.isUseLombok());
    if (tablesConfig.isUseLombok()) {
      importClassList.add("lombok.Data");
      classAnnotations.add("Data");

      // use Fluid Programming Style
      if (tablesConfig.isUseFluidProgrammingStyle()) {
        importClassList.add("lombok.experimental.Accessors");
        classAnnotations.add("Accessors(chain = true)");
      }

      if (!tablesConfig.getExtendsEntityName().isEmpty()) {
        importClassList.add("lombok.EqualsAndHashCode");
        classAnnotations.add("EqualsAndHashCode(callSuper = true)");
      }
    }

    // use Swagger UI 
    context.put("useSwaggerUIComment", tablesConfig.isUseSwaggerUIComment());
    if (tablesConfig.isUseSwaggerUIComment()) {
      if (StringUtils.isNotBlank(table.getTableComment())) {
        importClassList.add("io.swagger.annotations.ApiModel");
        classAnnotations.add("ApiModel(\"" + table.getTableComment() + "\")");
      }
    }

    // configure JPA settings
    if (isUseJpaAnnotation) {
      importClassList.add(StringHelper.getJakartaEEClassNameOrNot(isUseJakartaEE, "Entity"));
      classAnnotations.add("Entity");
      importClassList.add(StringHelper.getJakartaEEClassNameOrNot(isUseJakartaEE, "Table"));
    }
    String tableName = table.getTableName();
    if (tablesConfig.isAddSchemeNameToTableName()) {
      if (StringUtils.isNotBlank(table.getTableSchema())) {
        tableName = table.getTableSchema() + "." + tableName;
      } else if (StringUtils.isNotBlank(table.getTableCatalog())) {
        tableName = table.getTableCatalog() + "." + tableName;
      }
    }
    if (isUseJpaAnnotation) {
      classAnnotations.add("Table(name = \"" + tableName + "\")");
    }

    // process table columns
    context.put("columns", table.getColumns());
    if (isUseJpaAnnotation && !table.getColumns().isEmpty()) {
      importClassList.add(StringHelper.getJakartaEEClassNameOrNot(isUseJakartaEE, "Column"));
    }
    table.getColumns().forEach(column -> {
      column.setAnnotations(new ArrayList<>());

      if (isUseJpaAnnotation && column.isPrimary()) {
        importClassList.add(StringHelper.getJakartaEEClassNameOrNot(isUseJakartaEE, "Id"));
        Annotation columnAnnotation = new Annotation(StringHelper.getJakartaEEClassNameOrNot(isUseJakartaEE, "Id"),
            false);
        column.getAnnotations().add(columnAnnotation.toString());
      }
      if (isUseJpaAnnotation && (column.isAutoIncrement() || column.isSequenceColumn())) {
        Annotation columnAnnotation = new Annotation(
            StringHelper.getJakartaEEClassNameOrNot(isUseJakartaEE, "GeneratedValue"), false);
        importClassList.add(StringHelper.getJakartaEEClassNameOrNot(isUseJakartaEE, "GeneratedValue"));
        importClassList.add(StringHelper.getJakartaEEClassNameOrNot(isUseJakartaEE, "GenerationType"));
        if (column.isSequenceColumn()) {
          columnAnnotation
              .addKeyValuePair(KeyValuePair.fromPlain("strategy", "GenerationType.SEQUENCE"));
          columnAnnotation.addKeyValuePair(
              KeyValuePair.from("generator", "//FIXME Please input your generator name"));
          importClassList.add(StringHelper.getJakartaEEClassNameOrNot(isUseJakartaEE, "SequenceGenerator"));

          Annotation generateAnnotation = new Annotation(
              StringHelper.getJakartaEEClassNameOrNot(isUseJakartaEE, "SequenceGenerator"),
              false);
          generateAnnotation.addKeyValuePair(
              KeyValuePair.from("name", "//FIXME Please input your generator name"));
          generateAnnotation
              .addKeyValuePair(
                  KeyValuePair.from("sequenceName", "//FIXME Please input your generator name"));
          column.getAnnotations().add(generateAnnotation.toString());
        } else {
          columnAnnotation.addKeyValuePair(KeyValuePair.fromPlain("strategy", "GenerationType.IDENTITY"));
        }
        column.getAnnotations().add(columnAnnotation.toString());
      }
      Class<?> type = StringHelper.expandArray(column.getJavaDataType());
      if (!type.isPrimitive() && !type.getName().startsWith("java.lang")) {
        importClassList.add(column.getJavaDataType().getName());
      }

      // add column annotation
      Annotation columnAnnotation = new Annotation(StringHelper.getJakartaEEClassNameOrNot(isUseJakartaEE, "Column"),
          false);
      columnAnnotation.addKeyValuePair(KeyValuePair.from("name", column.getColumnName()));
      if (!column.isNullable()) {
        columnAnnotation.addKeyValuePair(KeyValuePair.from("nullable", false));
      }
      if (isUseJpaAnnotation) {
        column.getAnnotations().add(columnAnnotation.toString());
      }

      // add swagger annotation
      if (tablesConfig.isUseSwaggerUIComment() && StringUtils
          .isNotBlank(column.getColumnComment())) {
        importClassList.add("io.swagger.annotations.ApiModelProperty");
        columnAnnotation = new Annotation("io.swagger.annotations.ApiModelProperty", false);
        columnAnnotation.addKeyValuePair(KeyValuePair.from("value", column.getColumnComment()));
        column.getAnnotations().add(columnAnnotation.toString());
      }

      column.getAnnotations().sort(Comparator.comparingInt(String::length));
    });

    List<String> annotationList = new ArrayList<>(classAnnotations);
    annotationList.sort(Comparator.comparingInt(String::length));
    context.put("annotationList", annotationList);
    return evaluate(context, templateProvider);
  }

  @Override
  protected String getTemplateId() {
    return Constants.JPA_ENTITY_TEMPLATE_ID;
  }

}
