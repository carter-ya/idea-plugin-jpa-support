package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.config.Vendor;
import com.ifengxue.plugin.generator.tree.Element;
import com.ifengxue.plugin.state.SettingsState;
import com.ifengxue.plugin.util.StringHelper;
import com.intellij.openapi.components.ServiceManager;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.velocity.VelocityContext;

public class EntitySourceParserV2 extends AbstractSourceParser {

  @Override
  public String parse(GeneratorConfig config, Table table) {
    return parse(config, table,
        () -> ServiceManager.getService(SettingsState.class).loadTemplate(Constants.JPA_ENTITY_TEMPLATE_ID));
  }

  @Override
  public String parse(GeneratorConfig config, Table table, String template) {
    return parse(config, table, () -> template);
  }

  protected String parse(GeneratorConfig config, Table table, Supplier<String> templateProvider) {
    VelocityContext context = new VelocityContext();
    TablesConfig tablesConfig = config.getTablesConfig();
    context.put("config", config);
    context.put("tablesConfig", config.getTablesConfig());
    context.put("table", table);
    context.put("empty", "");
    context.put("stringHelper", new StringHelper());
    // 设置缩进
    context.put("indent", Element.Indent.findByDTDDeclare(tablesConfig.getIndent()));
    context.put("package", tablesConfig.getEntityPackageName());
    Set<String> importClassList = new HashSet<>();
    context.put("importClassList", importClassList);
    Set<String> annotationList = new HashSet<>();
    context.put("annotationList", annotationList);
    context.put("simpleName", table.getEntityName());
    context.put("parentClass", tablesConfig.getExtendsEntityName());
    Set<String> implementClassList = new HashSet<>();
    context.put("implementClassList", implementClassList);

    // 增加序列化注解
    if (tablesConfig.isSerializable()) {
      importClassList.add(Serializable.class.getName());
      implementClassList.add(Serializable.class.getSimpleName());
      context.put("serialVersionUID", "1");
    }

    // 设置是否使用Lombok
    context.put("useLombok", tablesConfig.isUseLombok());
    if (tablesConfig.isUseLombok()) {
      importClassList.add("lombok.Data");
      annotationList.add("Data");

      // 使用Fluid Programming Style
      if (tablesConfig.isUseFluidProgrammingStyle()) {
        importClassList.add("lombok.experimental.Accessors");
        annotationList.add("Accessors(chain = true)");
      }

      if (!tablesConfig.getExtendsEntityName().isEmpty()) {
        importClassList.add("lombok.EqualsAndHashCode");
        annotationList.add("EqualsAndHashCode(callSuper = true)");
      }
    }

    // 设置JPA相关信息
    importClassList.add("javax.persistence.Entity");
    annotationList.add("Entity");
    importClassList.add("javax.persistence.Table");
    annotationList.add("Table(name = \"" + table.getTableName() + "\")");

    // 处理表字段
    context.put("columns", table.getColumns());
    if (!table.getColumns().isEmpty()) {
      importClassList.add("javax.persistence.Column");
    }
    table.getColumns().forEach(column -> {
      if (column.isPrimary()) {
        importClassList.add("javax.persistence.Id");
      }
      if (column.isAutoIncrement()) {
        importClassList.add("javax.persistence.GeneratedValue");
        importClassList.add("javax.persistence.GenerationType");
        if (config.getDriverConfig().getVendor() == Vendor.ORACLE) {
          context.put("primaryKeyGeneratorStrategy", "GenerationType.SEQUENCE");
          context.put("primaryKeyGenerator", "//FIXME Please input your generator name");
          importClassList.add("javax.persistence.SequenceGenerator");
        } else {
          context.put("primaryKeyGeneratorStrategy", "GenerationType.IDENTITY");
        }
      }
      Class<?> type = StringHelper.expandArray(column.getJavaDataType());
      if (!type.isPrimitive() && !type.getName().startsWith("java.lang")) {
        importClassList.add(column.getJavaDataType().getName());
      }
    });
    return evaluate(context, templateProvider);
  }
}
