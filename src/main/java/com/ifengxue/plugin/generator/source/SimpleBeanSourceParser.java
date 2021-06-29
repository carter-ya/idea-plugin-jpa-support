package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import java.util.function.Supplier;
import org.apache.velocity.VelocityContext;

public class SimpleBeanSourceParser extends AbstractIDEASourceParser {

    private final String templateId;

    public SimpleBeanSourceParser(String templateId) {
        this.templateId = templateId;
    }

    @Override
    protected String parse(GeneratorConfig config, Table table, Supplier<String> templateProvider) {
        VelocityContext context = new VelocityContext();
        TablesConfig tablesConfig = config.getTablesConfig();
        context.put("table", table);
        context.put("tablesConfig", tablesConfig);
        return evaluate(context, templateProvider);
    }

    @Override
    protected String getTemplateId() {
        return templateId;
    }
}
