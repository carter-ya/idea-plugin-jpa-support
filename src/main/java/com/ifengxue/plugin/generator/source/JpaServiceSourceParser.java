package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.state.SettingsState;
import com.ifengxue.plugin.util.StringHelper;
import com.intellij.openapi.components.ServiceManager;
import java.util.Collections;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;

public class JpaServiceSourceParser extends AbstractSourceParser {

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
    context.put("stringHelper", new StringHelper());

    if (tablesConfig.getBasePackageName().isEmpty()) {
      context.put("package", "");
      context.put("importClassList", Collections.emptyList());
    } else {
      context.put("package", tablesConfig.getServiceSubPackageName());
      context.put("importClassList",
          Collections.singletonList(tablesConfig.getRepositoryPackageName() + "." + table.getRepositoryName()));
    }

    context.put("simpleName", table.getServiceName());
    if (StringUtils.isNotBlank(table.getRepositoryName())) {
      context.put("repositorySimpleName", table.getRepositoryName());
      context.put("repositoryVariableName", StringHelper.firstLetterLower(table.getRepositoryName()));
    }
    return evaluate(context, templateProvider);
  }
}
