package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.state.SettingsState;
import com.ifengxue.plugin.util.StringHelper;
import com.intellij.openapi.components.ServiceManager;
import org.apache.velocity.VelocityContext;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

public class JpaRepositorySourceParser extends AbstractSourceParser {

  @Override
  public String parse(GeneratorConfig config, Table table) {
    return parse(config, table,
        () -> ServiceManager.getService(SettingsState.class).loadTemplate(Constants.JPA_REPOSITORY_TEMPLATE_ID));
  }

  @Override
  public String parse(GeneratorConfig config, Table table, String template) {
    return parse(config, table, () -> template);
  }

  protected String parse(GeneratorConfig config, Table table, Supplier<String> templateProvider) {
    VelocityContext context = new VelocityContext();
    TablesConfig tablesConfig = config.getTablesConfig();
    if (tablesConfig.getBasePackageName().isEmpty()) {
      context.put("package", "");
      context.put("importClassList", Collections.emptyList());
    } else {
      context.put("package", tablesConfig.getRepositoryPackageName());
      context.put("importClassList",
          Collections.singletonList(tablesConfig.getEntityPackageName() + "." + table.getEntityName()));
    }
    context.put("simpleName", table.getRepositoryName());
    context.put("entitySimpleName", table.getEntityName());
    context.put("primaryKeyDataType",
        Optional.ofNullable(table.getPrimaryKeyClassType())
            .map(StringHelper::getWrapperClass)
            .map(Class::getSimpleName)
            .orElse("Void"));
    return evaluate(context, templateProvider);
  }
}
