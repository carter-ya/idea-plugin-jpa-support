package com.ifengxue.plugin.generator.source;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.DriverConfig;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.config.TablesConfig.ORM;
import com.ifengxue.plugin.generator.config.Vendor;
import com.ifengxue.plugin.util.MyLogChute;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.junit.Test;

public class SourceParserTest {

  private final VelocityEngine velocityEngine = new VelocityEngine();
  private AbstractIDEASourceParser sourceParser;

  public void initParser() {
    velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new MyLogChute());
    String encoding = StandardCharsets.UTF_8.name();
    velocityEngine.addProperty("input.encoding", encoding);
    velocityEngine.addProperty("output.encoding", encoding);
    sourceParser.setVelocityEngine(velocityEngine, encoding);
  }

  @Test
  public void entitySourceTest() throws IOException {
    sourceParser = new EntitySourceParserV2();
    initParser();

    String sourceCode = parse(Constants.JPA_ENTITY_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void controllerSourceTest() throws IOException {
    sourceParser = new ControllerSourceParser();
    initParser();

    String sourceCode = parse(Constants.CONTROLLER_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void serviceSourceTest() throws IOException {
    sourceParser = new ServiceSourceParser();
    initParser();

    String sourceCode = parse(Constants.JPA_SERVICE_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void serviceMybatisPlusSourceTest() throws IOException {
    sourceParser = new ServiceSourceParser();
    initParser();

    String sourceCode = parse(Constants.MYBATIS_PLUS_SERVICE_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void serviceTkMybatisSourceTest() throws IOException {
    sourceParser = new ServiceSourceParser();
    initParser();

    String sourceCode = parse(Constants.TK_MYBATIS_SERVICE_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void saveVOSourceTest() throws IOException {
    sourceParser = new SimpleBeanSourceParser();
    ((TemplateIdSetter) sourceParser).setTemplateId(Constants.SAVE_VO_TEMPLATE_ID);
    initParser();

    String sourceCode = parse(Constants.SAVE_VO_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void updateVOSourceTest() throws IOException {
    sourceParser = new SimpleBeanSourceParser();
    ((TemplateIdSetter) sourceParser).setTemplateId(Constants.UPDATE_VO_TEMPLATE_ID);
    initParser();

    String sourceCode = parse(Constants.UPDATE_VO_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void queryVOSourceTest() throws IOException {
    sourceParser = new SimpleBeanSourceParser();
    ((TemplateIdSetter) sourceParser).setTemplateId(Constants.QUERY_VO_TEMPLATE_ID);
    initParser();

    String sourceCode = parse(Constants.QUERY_VO_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void dtoSourceTest() throws IOException {
    sourceParser = new SimpleBeanSourceParser();
    ((TemplateIdSetter) sourceParser).setTemplateId(Constants.DTO_TEMPLATE_ID);
    initParser();

    String sourceCode = parse(Constants.DTO_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  private String parse(String templateId) throws IOException {
    GeneratorConfig config = new GeneratorConfig();
    config.setDriverConfig(
        new DriverConfig()
            .setVendor(Vendor.MYSQL)
    )
        .setPluginConfigs(Collections.emptyList())
        .setTablesConfig(
            new TablesConfig()
                .setBasePackageName("org.example")
                .setEntityPackageName("org.example.domain")
                .setControllerPackageName("org.example.controller")
                .setServicePackageName("org.example.service")
                .setVoSuffixName("Request")
                .setVoPackageName("org.example.vo")
                .setDtoSuffixName("Response")
                .setDtoPackageName("org.example.dto")
                .setExtendsEntityName("org.example.domain.AbstractEntity")
                .setIndent("  ")
                .setLineSeparator("\n")
                .setOrm(ORM.JPA)
                .setRemoveFieldPrefix("f_")
                .setRemoveTablePrefix("t_")
                .setRepositoryPackageName("org.example.repo")
                .setSerializable(true)
                .setUseClassComment(true)
                .setUseFieldComment(true)
                .setUseMethodComment(true)
                .setUseDefaultValue(true)
                .setUseDefaultDatetimeValue(false)
                .setUseJava8DateType(true)
                .setUseLombok(true)
                .setUseWrapper(true)
                .setUseSwaggerUIComment(true)
                .setUseJpa(true)
        );
    return sourceParser.parse(config, new Table()
        .setTableComment("表注释")
        .setTableName("t_table_name")
        .setTableSchema("test_db")
        .setEntityName("TableName")
        .setPackageName("org.example")
        .setControllerName("TableNameController")
        .setServiceName("TableNameService")
        .setRepositoryName("TableNameRepository")
        .setPrimaryKeyClassType(Long.class)
        .setPrimaryKeyCount(1)
        .setColumns(Arrays.asList(
            new Column().setAutoIncrement(true)
                .setColumnComment("自增主键")
                .setColumnName("f_id")
                .setDbDataType("BIGINT")
                .setFieldName("id")
                .setHasDefaultValue(false)
                .setJavaDataType(Long.class)
                .setNullable(false)
                .setPrimary(true),
            new Column().setAutoIncrement(true)
                .setColumnComment("乐观锁")
                .setColumnName("f_version")
                .setDbDataType("BIGINT")
                .setFieldName("version")
                .setHasDefaultValue(true)
                .setJavaDataType(Long.class)
                .setDefaultValue("0L")
                .setNullable(false)
                .setPrimary(false),
            new Column().setAutoIncrement(false)
                .setColumnComment("名称")
                .setColumnName("f_name")
                .setDbDataType("VARCHAR")
                .setFieldName("name")
                .setHasDefaultValue(false)
                .setJavaDataType(String.class)
                .setNullable(false)
                .setNotBlank(true)
            ,
            new Column().setAutoIncrement(false)
                .setColumnComment("创建时间")
                .setColumnName("f_created_at")
                .setDbDataType("DATETIME")
                .setFieldName("createdAt")
                .setHasDefaultValue(true)
                .setJavaDataType(Timestamp.class)
                .setDefaultValue("new Timestamp(System.currentTimeMillis())")
                .setNullable(false)
                .setPrimary(false),
            new Column().setAutoIncrement(false)
                .setColumnComment("更新时间")
                .setColumnName("f_updated_at")
                .setDbDataType("DATETIME")
                .setFieldName("updatedAt")
                .setHasDefaultValue(true)
                .setJavaDataType(Timestamp.class)
                .setDefaultValue("new Timestamp(System.currentTimeMillis())")
                .setNullable(false)
                .setPrimary(false)
        ))
        .setSelected(true), loadTemplate(templateId));
  }

  private String loadTemplate(String templateId) throws IOException {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(
            Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(templateId)),
            StandardCharsets.UTF_8))) {
      return IOUtils.toString(reader);
    }
  }
}
