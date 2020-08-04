package com.ifengxue.plugin.state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Data
@State(name = "JpaSupportSettings", storages = {
    @Storage(value = StateConstants.APPLICATION_STATE_NAME, roamingType = RoamingType.PER_OS)
})
public class SettingsState implements PersistentStateComponent<SettingsState> {

    private static final Logger log = Logger.getInstance(SettingsState.class);
    /**
     * template id to template
     */
    private Map<String, String> templateIdToTemplate = new HashMap<>();

    @Nullable
    @Override
    public SettingsState getState() {
        return this;
    }

    @Override
    public void loadState(SettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void putTemplate(String templateId, String template) {
        templateIdToTemplate.put(templateId, template);
    }

    @Transient
    public String loadTemplate(String templateId) {
        String template = templateIdToTemplate.get(templateId);
        if (!StringUtils.isBlank(template)) {
            return template;
        }
        return forceLoadTemplate(templateId);
    }

    public String forceLoadTemplate(String templateId) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(templateId)) {
            if (input == null) {
                log.warn("template " + templateId + " not exists.");
                return "";
            }
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("read template " + templateId + " error", e);
            return "";
        }
    }
}
