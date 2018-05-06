package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.util.StringHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class JpaRepositorySourceParser implements SourceParser, VelocityEngineAware {

  private VelocityEngine velocityEngine;
  private String encoding;

  @Override
  public String parse(GeneratorConfig config, Table table) {
    VelocityContext context = new VelocityContext();
    TablesConfig tablesConfig = config.getTablesConfig();
    if (tablesConfig.getBasePackageName().isEmpty()) {
      context.put("package", "");
      context.put("importClassList", Collections.emptyList());
    } else {
      context.put("package", tablesConfig.getRepositoryPackageName());
      context.put("importClassList", Arrays.asList(tablesConfig.getEntityPackageName() + "." + table.getEntityName()));
    }
    context.put("simpleName", table.getEntityName() + "Repository");
    context.put("entitySimpleName", table.getEntityName());
    context.put("primaryKeyDataType",
        Optional.ofNullable(table.getPrimaryKeyClassType())
            .map(StringHelper::getWrapperClass)
            .map(Class::getSimpleName)
            .orElse("Void"));
    StringWriter writer = new StringWriter();
    try (InputStream input = getClass().getClassLoader()
        .getResourceAsStream(getClass().getPackage().getName().replace('.', '/') + "/JpaRepository.vm")) {
      byte[] buffer = new byte[input.available()];
      input.read(buffer);
      velocityEngine.evaluate(context, writer, "repository", new String(buffer, encoding));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return writer.toString();
  }

  @Override
  public void setVelocityEngine(VelocityEngine ve, String encoding) {
    this.velocityEngine = ve;
    this.encoding = encoding;
  }
}
