package com.ifengxue.plugin.component;

import com.ifengxue.plugin.generator.source.AbstractSourceParser;
import com.ifengxue.plugin.state.converter.ClassConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xmlb.annotations.OptionTag;
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
     * 系统内置模板
     */
    private boolean builtin;
    /**
     * 模板内容
     */
    private String template;
    /**
     * source parser class
     */
    @OptionTag(converter = ClassConverter.class)
    private Class<? extends AbstractSourceParser> sourceParseClass;

    @Override
    public String toString() {
        return name;
    }
}
