package com.ifengxue.plugin.state;

import static java.util.stream.Collectors.toMap;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.component.TemplateItem;
import com.ifengxue.plugin.generator.source.EntitySourceParserV2;
import com.ifengxue.plugin.generator.source.JpaRepositorySourceParser;
import com.ifengxue.plugin.generator.source.JpaServiceSourceParser;
import com.ifengxue.plugin.state.converter.ClassConverter;
import com.ifengxue.plugin.state.wrapper.ClassWrapper;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Transient;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import lombok.Data;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.Nullable;

@Data
@State(name = "JpaSupportSettings", storages = {
    @Storage(value = StateConstants.APPLICATION_STATE_NAME, roamingType = RoamingType.PER_OS)
})
public class SettingsState implements PersistentStateComponent<SettingsState> {

    private static final Logger log = Logger.getInstance(SettingsState.class);
    // template config

    /**
     * template id to template
     */
    @Deprecated
    private Map<String, String> templateIdToTemplate = new LinkedHashMap<>();
    /**
     * templates
     */
    private List<TemplateItem> templateItems = new ArrayList<>();
    @Transient
    private Map<String, TemplateItem> templateIdToTemplateItem;

    // type config
    private boolean fallbackType = true;
    private boolean throwException;
    @OptionTag(converter = ClassConverter.class)
    private Class<?> fallbackTypeClass = java.lang.String.class;
    /**
     * db type to java type
     */
    private Map<String, ClassWrapper> dbTypeToJavaType;

    @Nullable
    @Override
    public SettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@Nonnull SettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
        if (state.dbTypeToJavaType == null) {
            resetTypeMapping();
        }
        dbTypeToJavaType = ListOrderedMap.listOrderedMap(new CaseInsensitiveMap<>(dbTypeToJavaType));
    }

    private String formatTemplateName(String templateId) {
        return templateId.replace(Constants.TEMPLATE_ID_PREFIX, "");
    }

    public List<TemplateItem> getOrResetTemplateItems() {
        if (!templateItems.isEmpty()) {
            return templateItems;
        }
        if (templateIdToTemplate != null) {
            templateIdToTemplate.forEach((templateId, template) -> {
                TemplateItem templateItem = new TemplateItem()
                    .setId(templateId)
                    .setBuiltin(true)
                    .setName(formatTemplateName(templateId))
                    .setTemplate(template);
                if (templateId.equals(Constants.JPA_ENTITY_TEMPLATE_ID)) {
                    templateItem.setSourceParseClass(EntitySourceParserV2.class);
                } else if (templateId.equals(Constants.JPA_REPOSITORY_TEMPLATE_ID)) {
                    templateItem.setSourceParseClass(JpaRepositorySourceParser.class);
                } else {
                    templateItem.setSourceParseClass(JpaServiceSourceParser.class);
                }
                templateItems.add(templateItem);
            });
            templateIdToTemplate = null;
        }
        templateIdToTemplateItem = templateItems.stream()
            .collect(toMap(TemplateItem::getId, Function.identity()));

        Constants.BUILTIN_TEMPLATE_IDS.forEach((id, clazz) -> {
            if (!templateIdToTemplateItem.containsKey(id)) {
                TemplateItem templateItem = new TemplateItem()
                    .setId(id)
                    .setBuiltin(true)
                    .setName(formatTemplateName(id))
                    .setTemplate(forceLoadTemplate(id))
                    .setSourceParseClass(clazz);
                templateItems.add(templateItem);
                templateIdToTemplateItem.put(id, templateItem);
            }
        });
        return templateItems;
    }

    public Map<String, ClassWrapper> getOrResetDbTypeToJavaType() {
        if (dbTypeToJavaType == null) {
            resetTypeMapping();
        }
        return dbTypeToJavaType;
    }

    public void resetTypeMapping() {
        dbTypeToJavaType = ListOrderedMap.listOrderedMap(new CaseInsensitiveMap<>());
        dbTypeToJavaType.put("CLOB", new ClassWrapper(String.class));
        dbTypeToJavaType.put("TEXT", new ClassWrapper(String.class));
        dbTypeToJavaType.put("VARCHAR", new ClassWrapper(String.class));
        dbTypeToJavaType.put("CHAR", new ClassWrapper(String.class));
        dbTypeToJavaType.put("JSON", new ClassWrapper(String.class));

        dbTypeToJavaType.put("TINYBLOB", new ClassWrapper(byte[].class));
        dbTypeToJavaType.put("BLOB", new ClassWrapper(byte[].class));
        dbTypeToJavaType.put("MEDIUMBLOB", new ClassWrapper(byte[].class));
        dbTypeToJavaType.put("LONGBLOB", new ClassWrapper(byte[].class));

        dbTypeToJavaType.put("ID", new ClassWrapper(Long.class));
        dbTypeToJavaType.put("BIGINT", new ClassWrapper(Long.class));
        dbTypeToJavaType.put("INTEGER", new ClassWrapper(Long.class));

        dbTypeToJavaType.put("TINYINT", new ClassWrapper(Integer.class));
        dbTypeToJavaType.put("SMALLINT", new ClassWrapper(Integer.class));
        dbTypeToJavaType.put("INT", new ClassWrapper(Integer.class));
        dbTypeToJavaType.put("MEDIUMINT", new ClassWrapper(Integer.class));

        dbTypeToJavaType.put("BIT", new ClassWrapper(Boolean.class));

        dbTypeToJavaType.put("FLOAT", new ClassWrapper(Float.class));

        dbTypeToJavaType.put("DOUBLE", new ClassWrapper(Double.class));

        dbTypeToJavaType.put("DECIMAL", new ClassWrapper(BigDecimal.class));

        dbTypeToJavaType.put("YEAR", new ClassWrapper(Short.class));

        dbTypeToJavaType.put("DATE", new ClassWrapper(java.sql.Date.class));

        dbTypeToJavaType.put("TIME", new ClassWrapper(Time.class));

        dbTypeToJavaType.put("DATETIME", new ClassWrapper(Date.class));
        dbTypeToJavaType.put("TIMESTAMP", new ClassWrapper(Date.class));

        // Postgre SQL
        dbTypeToJavaType.put("CHARACTER VARYING", new ClassWrapper(String.class));

        dbTypeToJavaType.put("BYTEA", new ClassWrapper(byte[].class));

        dbTypeToJavaType.put("BIGSERIAL", new ClassWrapper(Long.class));

        dbTypeToJavaType.put("REAL", new ClassWrapper(Float.class));

        dbTypeToJavaType.put("DOUBLE PRECISION", new ClassWrapper(Double.class));

        dbTypeToJavaType.put("NUMERIC", new ClassWrapper(BigDecimal.class));
    }

    public void updateTemplate(String templateId, String template) {
        TemplateItem templateItem = templateIdToTemplateItem.get(templateId);
        if (templateItem == null) {
            templateItem = new TemplateItem()
                .setId(templateId)
                .setName(formatTemplateName(templateId))
                .setBuiltin(false)
                .setTemplate(template)
                .setSourceParseClass(EntitySourceParserV2.class);
            templateItems.add(templateItem);
            templateIdToTemplateItem.put(templateId, templateItem);
        } else {
            templateItem.setTemplate(template);
        }
    }

    @Transient
    public String loadTemplate(String templateId) {
        TemplateItem templateItem = templateIdToTemplateItem.get(templateId);
        if (templateItem == null) {
            return null;
        }
        if (templateItem.getTemplate() == null) {
            templateItem.setTemplate(forceLoadTemplate(templateId));
        }
        return templateItem.getTemplate();
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
