package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.state.SettingsState;
import com.intellij.openapi.components.ServiceManager;
import java.util.function.Supplier;

public abstract class AbstractIDEASourceParser extends AbstractSourceParser {

    @Override
    public String parse(GeneratorConfig config, Table table) {
        return parse(config, table,
            () -> ServiceManager.getService(SettingsState.class).loadTemplate(getTemplateId()));
    }

    @Override
    public String parse(GeneratorConfig config, Table table, String template) {
        return parse(config, table, () -> template);
    }

    protected abstract String parse(GeneratorConfig config, Table table,
        Supplier<String> templateProvider);

    /**
     * 获取模板id
     */
    protected abstract String getTemplateId();
}
