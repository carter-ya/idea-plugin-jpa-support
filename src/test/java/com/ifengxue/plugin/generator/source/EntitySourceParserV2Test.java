package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.DriverConfig;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.config.TablesConfig.ORM;
import com.ifengxue.plugin.generator.config.Vendor;
import com.ifengxue.plugin.util.MyLogChute;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.junit.Before;
import org.junit.Test;

public class EntitySourceParserV2Test {

  private VelocityEngine velocityEngine = new VelocityEngine();
  private EntitySourceParserV2 sourceParser = new EntitySourceParserV2();

  @Before
  public void setUp() throws Exception {
    velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new MyLogChute());
    String encoding = StandardCharsets.UTF_8.name();
    velocityEngine.addProperty("input.encoding", encoding);
    velocityEngine.addProperty("output.encoding", encoding);
    sourceParser.setVelocityEngine(velocityEngine, encoding);
  }

  @Test
  public void parse() {
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
        );
    String sourceCode = sourceParser.parse(config, new Table()
        .setTableComment("表注释")
        .setTableName("t_table_name")
        .setTableSchema("test_db")
        .setEntityName("TableName")
        .setPackageName("org.example")
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
            new Column().setAutoIncrement(true)
                .setColumnComment("创建时间")
                .setColumnName("f_created_at")
                .setDbDataType("DATETIME")
                .setFieldName("createdAt")
                .setHasDefaultValue(true)
                .setJavaDataType(Timestamp.class)
                .setDefaultValue("new Timestamp(System.currentTimeMillis())")
                .setNullable(false)
                .setPrimary(false),
            new Column().setAutoIncrement(true)
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
        .setSelected(true));
    System.out.println(sourceCode);
  }
}