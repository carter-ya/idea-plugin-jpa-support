package com.ifengxue.plugin.exception;

/**
 * @author Carter
 */
public class TemplateNotFoundException extends PluginException {

    private final String templateId;

    public TemplateNotFoundException(String templateId) {
        super("Template not found: " + templateId);
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }
}
