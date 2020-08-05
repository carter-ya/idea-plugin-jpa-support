package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.util.StringHelper;
import java.util.Collections;
import java.util.Optional;
import org.apache.velocity.VelocityContext;

public class JpaRepositorySourceParser extends AbstractSourceParser {

  @Override
  public String parse(GeneratorConfig config, Table table) {
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
    context.put("simpleName", table.getEntityName() + "Repository");
    context.put("entitySimpleName", table.getEntityName());
    context.put("primaryKeyDataType",
        Optional.ofNullable(table.getPrimaryKeyClassType())
            .map(StringHelper::getWrapperClass)
            .map(Class::getSimpleName)
            .orElse("Void"));
    return evaluate(context, Constants.JPA_REPOSITORY_TEMPLATE_ID);
  }
}
