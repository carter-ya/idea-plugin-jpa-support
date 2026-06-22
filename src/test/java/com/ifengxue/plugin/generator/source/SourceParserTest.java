package com.ifengxue.plugin.generator.source;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.TemplateManager;
import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.DriverConfig;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.config.TablesConfig.ORM;
import com.ifengxue.plugin.generator.config.Vendor;
import com.ifengxue.plugin.generator.merge.XmlSourceFileMerger;
import com.ifengxue.plugin.generator.source.JpaRepositorySourceParser;
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
import org.slf4j.helpers.NOPLogger;

public class SourceParserTest {

  private final VelocityEngine velocityEngine = new VelocityEngine();
  private AbstractIDEASourceParser sourceParser;

  static {
    TemplateManager.debugTemplateMapping = templateId -> {
      try {
        return loadTemplate(templateId);
      } catch (IOException e) {
        return null;
      }
    };
    System.setProperty(Constants.IN_TEST_ENVIRONMENT, "true");
  }

  public void initParser() {
    velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, NOPLogger.NOP_LOGGER);
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
    // Bug 2: generated code must not contain "import import" (duplicate import keyword)
    assertFalse("Controller generated code should not contain duplicate 'import import'",
        sourceCode.contains("import import"));
    System.out.println(sourceCode);
  }

  @Test
  public void serviceSourceTest() throws IOException {
    sourceParser = new ServiceSourceParser();
    initParser();

    String sourceCode = parse(Constants.JPA_SERVICE_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    // Bug 3: query() return type must use dtoSuffixName ("Response") not hardcoded "DTO"
    assertFalse("Service query() should not use hardcoded 'DTO' in return type",
        sourceCode.contains("Page<TableNameDTO>"));
    assertTrue("Service query() should use dtoSuffixName 'Response' in return type",
        sourceCode.contains("Page<TableNameResponse>"));
    System.out.println(sourceCode);
  }

  @Test
  public void serviceMybatisPlusSourceTest() throws IOException {
    sourceParser = new ServiceSourceParser();
    initParser();

    String sourceCode = parse(Constants.MYBATIS_PLUS_SERVICE_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    // Bug 5: generated code must not contain unused OptimisticLockException import
    assertFalse("MybatisPlus service should not contain unused OptimisticLockException import",
        sourceCode.contains("OptimisticLockException"));
    System.out.println(sourceCode);
  }

  @Test
  public void serviceTkMybatisSourceTest() throws IOException {
    sourceParser = new ServiceSourceParser();
    initParser();

    String sourceCode = parse(Constants.TK_MYBATIS_SERVICE_TEMPLATE_ID);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    // Bug 5: generated code must not contain unused OptimisticLockException import
    assertFalse("TkMybatis service should not contain unused OptimisticLockException import",
        sourceCode.contains("OptimisticLockException"));
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

  @Test
  public void entityKotlinSourceTest() throws IOException {
    sourceParser = new EntitySourceParserV2();
    initParser();

    String sourceCode = parseKotlin("template/JpaEntity.kt.vm");
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void repositoryKotlinSourceTest() throws IOException {
    sourceParser = new JpaRepositorySourceParser();
    initParser();

    String sourceCode = parseKotlin("template/JpaRepository.kt.vm");
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void controllerKotlinSourceTest() throws IOException {
    sourceParser = new ControllerSourceParser();
    initParser();

    String sourceCode = parseKotlin("template/Controller.kt.vm");
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void serviceKotlinSourceTest() throws IOException {
    sourceParser = new ServiceSourceParser();
    initParser();

    String sourceCode = parseKotlin("template/Service.kt.vm");
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void serviceMybatisPlusKotlinSourceTest() throws IOException {
    sourceParser = new ServiceSourceParser();
    initParser();

    String sourceCode = parseKotlin("template/Service-MybatisPlus.kt.vm");
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    // Bug 5: must not contain unused OptimisticLockException
    assertFalse("MybatisPlus.kt service should not contain unused OptimisticLockException import",
        sourceCode.contains("OptimisticLockException"));
    System.out.println(sourceCode);
  }

  @Test
  public void serviceTkMybatisKotlinSourceTest() throws IOException {
    sourceParser = new ServiceSourceParser();
    initParser();

    String sourceCode = parseKotlin("template/Service-TkMybatis.kt.vm");
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    // Bug 5: must not contain unused OptimisticLockException
    assertFalse("TkMybatis.kt service should not contain unused OptimisticLockException import",
        sourceCode.contains("OptimisticLockException"));
    System.out.println(sourceCode);
  }

  @Test
  public void saveVOKotlinSourceTest() throws IOException {
    sourceParser = new SimpleBeanSourceParser();
    ((TemplateIdSetter) sourceParser).setTemplateId(Constants.SAVE_VO_TEMPLATE_ID);
    initParser();

    String sourceCode = parseKotlin("template/SaveVO.kt.vm");
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void updateVOKotlinSourceTest() throws IOException {
    sourceParser = new SimpleBeanSourceParser();
    ((TemplateIdSetter) sourceParser).setTemplateId(Constants.UPDATE_VO_TEMPLATE_ID);
    initParser();

    String sourceCode = parseKotlin("template/UpdateVO.kt.vm");
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void queryVOKotlinSourceTest() throws IOException {
    sourceParser = new SimpleBeanSourceParser();
    ((TemplateIdSetter) sourceParser).setTemplateId(Constants.QUERY_VO_TEMPLATE_ID);
    initParser();

    String sourceCode = parseKotlin("template/QueryVO.kt.vm");
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  @Test
  public void dtoKotlinSourceTest() throws IOException {
    sourceParser = new SimpleBeanSourceParser();
    ((TemplateIdSetter) sourceParser).setTemplateId(Constants.DTO_TEMPLATE_ID);
    initParser();

    String sourceCode = parseKotlin("template/DTO.kt.vm");
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    System.out.println(sourceCode);
  }

  // Bug 1: JpaEntity.vm toString() format verification — fields separated by ", " with leading quote
  @Test
  public void entityToStringWhenLombokDisabled() throws IOException {
    sourceParser = new EntitySourceParserV2();
    initParser();

    GeneratorConfig config = createGeneratorConfigWithSettings()
        .setTablesConfig(createBaseTablesConfig()
            .setUseLombok(false)
        );
    String sourceCode = parseWithConfig(Constants.JPA_ENTITY_TEMPLATE_ID, config);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    // toString should generate field1='value1', field2='value2' format with comma separator
    // The generated Java code should contain the ", " comma-space separator pattern
    // between fields inside the toString method body
    assertTrue("toString should contain comma separator \", \" between fields",
        sourceCode.contains("\", \""));
    // The generated toString should use single-quote around each value:
    // each field should appear as fieldName='" (equals, single-quote, double-quote)
    assertTrue("toString field should use '=\" pattern (equals-quote before value)",
        sourceCode.contains("='\""));
    System.out.println(sourceCode);
  }

  // Bug 4: Controller.vm @ApiParam should be guarded by $useSwaggerUI, not $useLombok
  @Test
  public void controllerApiParamWithSwaggerUINoLombok() throws IOException {
    sourceParser = new ControllerSourceParser();
    initParser();

    GeneratorConfig config = createGeneratorConfigWithSettings()
        .setTablesConfig(createBaseTablesConfig()
            .setUseLombok(false)
            .setUseSwaggerUIComment(true)
        );
    String sourceCode = parseWithConfig(Constants.CONTROLLER_TEMPLATE_ID, config);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    // Bug 4: when useSwaggerUI=true and useLombok=false, @ApiParam should still be generated
    assertTrue("@ApiParam should be generated when useSwaggerUI=true even if useLombok=false",
        sourceCode.contains("@ApiParam(\"id\")"));
    System.out.println(sourceCode);
  }

  // Bug 4 (Kotlin variant): Controller.kt.vm @ApiParam guard
  @Test
  public void controllerKotlinApiParamWithSwaggerUINoLombok() throws IOException {
    sourceParser = new ControllerSourceParser();
    initParser();

    GeneratorConfig config = createGeneratorConfigWithSettings()
        .setTablesConfig(createBaseTablesConfig()
            .setUseLombok(false)
            .setUseSwaggerUIComment(true)
        );
    String sourceCode = parseKotlinWithConfig("template/Controller.kt.vm", config);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    // Bug 4 Kotlin: when useSwaggerUI=true and useLombok=false, @ApiParam should still be generated
    assertTrue("@ApiParam should be generated in Kotlin when useSwaggerUI=true even if useLombok=false",
        sourceCode.contains("@ApiParam(\"id\")"));
    System.out.println(sourceCode);
  }

  // Bug 3 (Kotlin variant): Service.kt.vm query() must use dtoSuffixName not hardcoded DTO
  @Test
  public void serviceKotlinQueryReturnsDtoSuffix() throws IOException {
    sourceParser = new ServiceSourceParser();
    initParser();

    GeneratorConfig config = createGeneratorConfigWithSettings()
        .setTablesConfig(createBaseTablesConfig()
            .setDtoSuffixName("Response")
        );
    String sourceCode = parseKotlinWithConfig("template/Service.kt.vm", config);
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    // Bug 3 Kotlin: query() return type must not use hardcoded "DTO"
    assertFalse("Service.kt query() should not use hardcoded 'DTO' in return type",
        sourceCode.contains("Page<TableNameDTO>"));
    assertTrue("Service.kt query() should use dtoSuffixName 'Response' in return type",
        sourceCode.contains("Page<TableNameResponse>"));
    System.out.println(sourceCode);
  }

  // Bug 6: MapperXml.vm must not have nested ${} syntax on the columnNameJoining line
  @Test
  public void mapperXmlSourceTest() throws IOException {
    sourceParser = new MapperXmlSourceParser();
    initParser();

    String sourceCode = parseWithConfig(Constants.MAPPER_XML_TEMPLATE_ID, createGeneratorConfig());
    assertNotNull(sourceCode);
    assertFalse(sourceCode.isEmpty());
    // Bug 6: the generated XML should not contain nested ${} which would cause parse errors;
    // the fact that we successfully generated code already proves the fix.
    // Additionally verify the <sql> block contains the column list.
    assertTrue("MapperXml should contain Base_Column_List sql block",
        sourceCode.contains("Base_Column_List"));
    // Verify the generated output does not contain raw Velocity nested ${} artifacts
    assertFalse("MapperXml generated code should not contain nested Velocity ${}",
        sourceCode.contains("${table.allColumns}"));
    System.out.println(sourceCode);
  }

  @Test
  public void xmlSourceFileMerger() {
    new XmlSourceFileMerger().tryMerge(createGeneratorConfig(), createTable(), null, null);
  }

  private GeneratorConfig createGeneratorConfig() {
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
                .setUseOpenAPI3(true)
                .setUseJpa(true)
        );
    return config;
  }

  public Table createTable() {
    Table table = new Table()
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
                .setJdbcTypeName("BIGINT")
                .setFieldName("id")
                .setHasDefaultValue(false)
                .setJavaDataType(Long.class)
                .setNullable(false)
                .setPrimary(true),
            new Column().setAutoIncrement(true)
                .setColumnComment("乐观锁")
                .setColumnName("f_version")
                .setDbDataType("BIGINT")
                .setJdbcTypeName("BIGINT")
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
                .setJdbcTypeName("VARCHAR")
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
                .setJdbcTypeName("TIMESTAMP")
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
                .setJdbcTypeName("TIMESTAMP")
                .setFieldName("updatedAt")
                .setHasDefaultValue(true)
                .setJavaDataType(Timestamp.class)
                .setDefaultValue("new Timestamp(System.currentTimeMillis())")
                .setNullable(false)
                .setPrimary(false)
        ))
        .setSelected(true);
    table.setAllColumns(table.getColumns());
    return table;
  }

  private String parse(String templateId) throws IOException {
    GeneratorConfig config = createGeneratorConfig();
    Table table = createTable();
    return sourceParser.parse(config, table, loadTemplate(templateId));
  }

  private String parseWithConfig(String templateId, GeneratorConfig config) throws IOException {
    Table table = createTable();
    return sourceParser.parse(config, table, loadTemplate(templateId));
  }

  private String parseKotlin(String templateResourcePath) throws IOException {
    GeneratorConfig config = createGeneratorConfig();
    Table table = createTable();
    return sourceParser.parse(config, table, loadTemplate(templateResourcePath));
  }

  private String parseKotlinWithConfig(String templateResourcePath, GeneratorConfig config)
      throws IOException {
    Table table = createTable();
    return sourceParser.parse(config, table, loadTemplate(templateResourcePath));
  }

  private GeneratorConfig createGeneratorConfigWithSettings() {
    return new GeneratorConfig()
        .setDriverConfig(new DriverConfig().setVendor(Vendor.MYSQL))
        .setPluginConfigs(Collections.emptyList());
  }

  private TablesConfig createBaseTablesConfig() {
    return new TablesConfig()
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
        .setUseOpenAPI3(true)
        .setUseJpa(true);
  }

  private static String loadTemplate(String templateId) throws IOException {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(
            Objects.requireNonNull(
                SourceParserTest.class.getClassLoader().getResourceAsStream(templateId)),
            StandardCharsets.UTF_8))) {
      return IOUtils.toString(reader);
    }
  }
}
