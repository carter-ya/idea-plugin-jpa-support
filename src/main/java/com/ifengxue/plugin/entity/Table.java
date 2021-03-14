package com.ifengxue.plugin.entity;

import com.ifengxue.plugin.gui.annotation.TableEditable;
import com.ifengxue.plugin.gui.annotation.TableHeight;
import com.ifengxue.plugin.gui.annotation.TableProperty;
import com.ifengxue.plugin.gui.annotation.TableWidth;
import com.ifengxue.plugin.gui.property.BooleanTableCellEditor;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableHeight(height = 20)
public class Table implements Selectable {

  /**
   * table schema
   */
  private TableSchema rawTableSchema;

  /**
   * 是否被选择，选择后才可生成类
   */
  @TableProperty(bundleName = "table_selected", columnClass = Boolean.class, index = 0)
  @TableWidth(maxWidth = 60)
  @TableEditable(editorProvider = BooleanTableCellEditor.class)
  private boolean selected;

  /**
   * 序号
   */
  @TableProperty(bundleName = "table_sequence", index = 1000)
  @TableWidth(maxWidth = 40)
  private int sequence;

  /**
   * 表名
   */
  @TableProperty(bundleName = "table_table_name", index = 2000)
  private String tableName;

  /**
   * 表注释
   */
  @TableProperty(bundleName = "table_class_comment", index = 5000)
  private String tableComment;

  /**
   * catalog
   */
  private String tableCatalog;

  /**
   * 数据库名称
   */
  private String tableSchema;

  /**
   * 实体名称
   */
  @TableProperty(bundleName = "table_class_name", index = 3000)
  private String entityName;

  /**
   * Repository 名称
   */
  @TableProperty(bundleName = "table_repository_name", index = 4000)
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
    table.setTableCatalog(tableSchema.getTableCatalog());
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
