package com.ifengxue.plugin.component;

import com.ifengxue.plugin.generator.source.AbstractSourceParser;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TemplateItem {

    /**
     * 模板id
     */
    private String id;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板内容
     */
    private String template;
    /**
     * source parser class
     */
    private Class<? extends AbstractSourceParser> sourceParseClass;

    @Override
    public String toString() {
        return name;
    }
}
