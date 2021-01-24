package com.ifengxue.plugin.entity;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;

@Data
@EqualsAndHashCode(callSuper = false)
public class MybatisGeneratorTableSchema extends TableSchema {

  private static final long serialVersionUID = -5758625651211348937L;

  private final IntrospectedTable introspectedTable;

  public MybatisGeneratorTableSchema(IntrospectedTable introspectedTable) {
    this.introspectedTable = introspectedTable;
    setTableSchema(introspectedTable.getFullyQualifiedTable().getIntrospectedCatalog());
    setTableName(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName());
    setTableComment(introspectedTable.getRemarks());
  }

  public List<ColumnSchema> toColumnSchemas() {
    List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
    AtomicInteger seq = new AtomicInteger(0);
    return columns.stream()
        .map(column -> new MybatisGeneratorColumnSchema(column, seq.getAndIncrement()))
        .collect(Collectors.toList());
  }
}
