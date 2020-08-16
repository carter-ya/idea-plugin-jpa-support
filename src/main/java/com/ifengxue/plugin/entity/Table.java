package com.ifengxue.plugin.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Table {

  /**
   * table schema
   */
  private TableSchema rawTableSchema;

  /**
   * 是否被选择，选择后才可生成类
   */
  private boolean selected;

  /**
   * 表名
   */
  private String tableName;

  /**
   * 表注释
   */
  private String tableComment;

  /**
   * 数据库名称
   */
  private String tableSchema;

  /**
   * 实体名称
   */
  private String entityName;

  /**
   * Repository 名称
   */
  private String repositoryName;

  /**
   * 包名称
   */
  private String packageName;

  /**
   * 主键类型
   */
  private Class<?> primaryKeyClassType;

  /**
   * 主键数量
   */
  private int primaryKeyCount;

  private List<Column> columns;

  public static Table from(
      TableSchema tableSchema,
      String entityName,
      String repositoryName,
      boolean selected) {
    Table table = new Table();
    table.setRawTableSchema(tableSchema);
    table.setTableName(tableSchema.getTableName());
    table.setTableComment(tableSchema.getTableComment());
    table.setTableSchema(tableSchema.getTableSchema());
    table.setEntityName(entityName);
    table.setRepositoryName(repositoryName);
    table.setSelected(selected);
    return table;
  }

  public void incPrimaryKeyCount() {
    primaryKeyCount++;
  }
}
