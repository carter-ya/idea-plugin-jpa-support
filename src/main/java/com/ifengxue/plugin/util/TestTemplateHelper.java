package com.ifengxue.plugin.util;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.DriverConfig;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.config.TablesConfig.ORM;
import com.ifengxue.plugin.generator.config.Vendor;
import com.ifengxue.plugin.generator.source.AbstractSourceParser;
import com.ifengxue.plugin.generator.source.EvaluateSourceCodeException;
import com.ifengxue.plugin.generator.tree.Element.Indent;
import com.ifengxue.plugin.state.AutoGeneratorSettingsState;
import com.ifengxue.plugin.state.ModuleSettings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.FileType;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

public enum TestTemplateHelper {
    ;

    public static Object evaluate(Class<? extends AbstractSourceParser> clazz, Table table,
        String template, FileType fileType) {
        AutoGeneratorSettingsState settingsState = ServiceManager.getService(
            Holder.getOrDefaultProject(), AutoGeneratorSettingsState.class);
        ModuleSettings moduleSettings = settingsState
            .getModuleSettings(settingsState.getModuleName());
        GeneratorConfig config = new GeneratorConfig();
        config.setDriverConfig(new DriverConfig()
            .setVendor(Vendor.MYSQL))
            .setPluginConfigs(Collections.emptyList())
            .setTablesConfig(
                new TablesConfig()
                    .setBasePackageName("org.example.jpa.support.test")
                    .setEntityPackageName("org.example.jpa.support.test.domain")
                    .setExtendsEntityName(settingsState.getInheritedParentClassName())
                    .setRepositoryPackageName("org.example.jpa.support.test.repository")
                    .setControllerPackageName("org.example.jpa.support.test.controller")
                    .setServicePackageName("org.example.jpa.support.test.service")
                    .setVoSuffixName(moduleSettings.getVoSuffixName())
                    .setVoPackageName("org.example.jpa.support.test.vo")
                    .setDtoSuffixName(moduleSettings.getDtoSuffixName())
                    .setDtoPackageName("org.example.jpa.support.test.dto")
                    .setIndent(Indent.FOUR_SPACE.getIndent())
                    .setLineSeparator("\n")
                    .setOrm(ORM.JPA)
                    .setRemoveFieldPrefix(settingsState.getRemoveFieldPrefix())
                    .setRemoveTablePrefix(settingsState.getRemoveEntityPrefix())
                    .setRepositoryPackageName("org.example.jpa.support.test.repo")
                    .setSerializable(settingsState.isSerializable())
                    .setUseClassComment(settingsState.isGenerateClassComment())
                    .setUseFieldComment(settingsState.isGenerateFieldComment())
                    .setUseMethodComment(settingsState.isGenerateMethodComment())
                    .setUseDefaultValue(settingsState.isGenerateDefaultValue())
                    .setUseDefaultDatetimeValue(settingsState.isGenerateDatetimeDefaultValue())
                    .setUseJava8DateType(settingsState.isUseJava8DateType())
                    .setUseLombok(settingsState.isUseLombok())
                    .setUseFluidProgrammingStyle(settingsState.isUseFluidProgrammingStyle())
                    .setUseSwaggerUIComment(settingsState.isGenerateSwaggerUIComment())
                    .setUseWrapper(true)
                    .setUseJpa(moduleSettings.isRepositoryTypeJPA())
                    .setUseMybatisPlus(moduleSettings.isRepositoryTypeMybatisPlus())
                    .setUseTkMybatis(moduleSettings.isRepositoryTypeTkMybatis())
            );
        AbstractSourceParser sourceParser = newInstance(clazz);
        sourceParser.setVelocityEngine(VelocityUtil.getInstance(), "UTF-8");
        try {
            return SourceFormatter.format(sourceParser.parse(config, table, template), fileType);
        } catch (Exception ex) {
            return ex;
        }
    }

    public static String evaluateToString(Class<? extends AbstractSourceParser> clazz,
        String template, FileType fileType) {
        AutoGeneratorSettingsState settingsState = ServiceManager.getService(
            Holder.getOrDefaultProject(), AutoGeneratorSettingsState.class);
        Table table = new Table();
        table
            .setTableComment("Example Table Comment")
            .setTableName("t_example_table")
            .setTableSchema("example_db")
            .setEntityName(
                StringHelper.parseEntityName(settingsState.removeTablePrefix(table.getTableName())))
            .setRepositoryName(table.getEntityName() + "Repository")
            .setPackageName("org.example")
            .setPrimaryKeyClassType(Long.class)
            .setPrimaryKeyCount(1)
            .setColumns(Arrays.asList(
                new Column()
                    .setAutoIncrement(true)
                    .setColumnName("f_id")
                    .setSort(0)
                    .setDbDataType("BIGINT")
                    .setPrimary(true)
                    .setAutoIncrement(true)
                    .setColumnComment("Id"),
                new Column()
                    .setColumnName("f_version")
                    .setSort(4)
                    .setDbDataType("BIGINT")
                    .setColumnComment("Version number")
                    .setDefaultValue("0"),
                new Column()
                    .setColumnName("film_title")
                    .setSort(8)
                    .setDbDataType("VARCHAR")
                    .setNullable(true)
                    .setColumnComment("Film title")
                    .setDefaultValue("Interstellar IMAX"),
                new Column()
                    .setColumnName("film_description")
                    .setSort(12)
                    .setDbDataType("TEXT")
                    .setNullable(true)
                    .setColumnComment("Film description"),
                new Column()
                    .setColumnName("film_language_id")
                    .setSort(16)
                    .setDbDataType("TINYINT")
                    .setColumnComment("Film language id"),
                new Column()
                    .setColumnName("film_rental_duration")
                    .setSort(20)
                    .setDbDataType("TINYINT")
                    .setNullable(true)
                    .setColumnComment("Film rental duration"),
                new Column()
                    .setColumnName("film_rental_rate")
                    .setSort(24)
                    .setDbDataType("DECIMAL")
                    .setColumnComment("Film rental rate")
                    .setDefaultValue("4.99"),
                new Column()
                    .setColumnName("f_created_at")
                    .setSort(98)
                    .setDbDataType("DATETIME")
                    .setColumnComment("Created at")
                    .setDefaultValue("CURRENT_TIMESTAMP"),
                new Column()
                    .setColumnName("f_updated_at")
                    .setSort(99)
                    .setDbDataType("DATETIME")
                    .setColumnComment("Updated at")
                    .setDefaultValue("CURRENT_TIMESTAMP")
            ))
            .setSelected(true);
        table.getColumns().forEach(column -> ColumnUtil
            .parseColumn(column, settingsState.getRemoveFieldPrefix(),
                settingsState.getIfJavaKeywordAddSuffix(), true,
                settingsState.isUseJava8DateType()));
        table.setAllColumns(table.getColumns());
        return evaluateToString(clazz, table, template, fileType);
    }

    public static String evaluateToString(Class<? extends AbstractSourceParser> clazz, Table table,
        String template, FileType fileType) {
        Object evaluate = evaluate(clazz, table, template, fileType);
        if (evaluate instanceof String) {
            return (String) evaluate;
        }
        Throwable throwable = (Throwable) evaluate;
        if (throwable instanceof EvaluateSourceCodeException) {
            throwable = throwable.getCause();
        }
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return writer.toString();
    }

    private static AbstractSourceParser newInstance(Class<? extends AbstractSourceParser> clazz) {
        try {
            return clazz.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("create instance failed", e);
        }
    }
}
