package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.util.StringHelper;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.velocity.VelocityContext;

public class JpaRepositorySourceParser extends AbstractIDEASourceParser {

  @Override
  protected String parse(GeneratorConfig config, Table table, Supplier<String> templateProvider) {
    VelocityContext context = new VelocityContext();
    TablesConfig tablesConfig = config.getTablesConfig();
    context.put("tablesConfig", tablesConfig);
    context.put("table", table);
    if (tablesConfig.getBasePackageName().isEmpty()) {
      context.put("package", "");
      context.put("importClassList", Collections.emptyList());
    } else {
      context.put("package", tablesConfig.getRepositoryPackageName());
      context.put("importClassList",
          Collections
              .singletonList(tablesConfig.getEntityPackageName() + "." + table.getEntityName()));
    }
    context.put("simpleName", table.getRepositoryName());
    context.put("entitySimpleName", table.getEntityName());
    context.put("primaryKeyDataType",
        Optional.ofNullable(table.getPrimaryKeyClassType())
            .map(StringHelper::getWrapperClass)
            .map(Class::getSimpleName)
            .orElse("Void"));
    context.put("useJakartaEE", config.getTablesConfig().isUseJakartaEE());
    return evaluate(context, templateProvider);
  }

  @Override
  protected String getTemplateId() {
    return Constants.JPA_REPOSITORY_TEMPLATE_ID;
  }
}
