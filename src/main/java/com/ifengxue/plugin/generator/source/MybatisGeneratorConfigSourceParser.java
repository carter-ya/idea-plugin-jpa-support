package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import java.util.function.Supplier;
import org.apache.velocity.VelocityContext;

public class MybatisGeneratorConfigSourceParser extends AbstractIDEASourceParser {

    @Override
    protected String parse(GeneratorConfig config, Table table, Supplier<String> templateProvider) {
        VelocityContext context = new VelocityContext();
        context.put("tablesConfig", config.getTablesConfig());
        context.put("table", table);
        return evaluate(context, templateProvider);
    }

    @Override
    protected String getTemplateId() {
        return Constants.MYBATIS_GENERATOR_CONFIG_TEMPLATE_ID;
    }
}
