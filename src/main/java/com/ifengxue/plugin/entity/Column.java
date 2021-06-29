package com.ifengxue.plugin.entity;

import com.ifengxue.plugin.gui.annotation.TableEditable;
import com.ifengxue.plugin.gui.annotation.TableHeight;
import com.ifengxue.plugin.gui.annotation.TableProperty;
import com.ifengxue.plugin.gui.annotation.TableWidth;
import com.ifengxue.plugin.gui.property.BooleanTableCellEditor;
import com.ifengxue.plugin.gui.property.ClassNamePropertyEditor;
import com.ifengxue.plugin.gui.property.ExpandableTextTableCellEditor;
import com.ifengxue.plugin.gui.property.JavaDataTypeEditorProvider;
import com.ifengxue.plugin.util.StringHelper;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableHeight(height = 20)
public class Column implements Selectable {

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
  @TableProperty(bundleName = "table_sequence", index = 25)
  @TableWidth(maxWidth = 40)
  private int sequence;
  /**
   * 数据库字段名
   */
  @TableProperty(bundleName = "table_column_name_title", index = 50)
  @TableEditable
  private String columnName;
  /**
   * 实体字段名
   */
  @TableProperty(bundleName = "table_field_name_title", index = 100)
  @TableEditable
  private String fieldName;
  /**
   * 字段顺序
   */
  private int sort;
  /**
   * 数据库数据类型
   */
  private String dbDataType;
  /**
   * @see ColumnSchemaExtension#jdbcType()
   */
  private int jdbcType;
  /**
   * @see ColumnSchemaExtension#jdbcTypeName()
   */
  private String jdbcTypeName;
  /**
   * Java数据类型
   */
  @TableProperty(bundleName = "table_field_java_type_title", columnClass = String.class, index = 200)
  @TableEditable(editorProvider = JavaDataTypeEditorProvider.class, propertyEditorProvider = ClassNamePropertyEditor.class)
  @TableWidth(minWidth = 60)
  private Class<?> javaDataType;
  /**
   * 是否是主键
   */
  private boolean primary;
  /**
   * 是否允许为null
   */
  @TableProperty(bundleName = "Nullable", columnClass = Boolean.class, index = 400)
  @TableWidth(maxWidth = 60)
  @TableEditable(editorProvider = BooleanTableCellEditor.class)
  private boolean nullable = true;
  /**
   * 是否允许为null或空串或仅包含空白字符的字符串
   */
  @TableProperty(bundleName = "NotBlank", columnClass = Boolean.class, index = 410)
  @TableWidth(maxWidth = 60)
  @TableEditable(editorProvider = BooleanTableCellEditor.class)
  private boolean notBlank;
  /**
   * 是否允许为null或空串
   */
  @TableProperty(bundleName = "NotBlank", columnClass = Boolean.class, index = 420)
  @TableWidth(maxWidth = 60)
  @TableEditable(editorProvider = BooleanTableCellEditor.class)
  private boolean notEmpty;
  /**
   * 是否是自增字段
   */
  private boolean autoIncrement;
  private boolean sequenceColumn;
  /**
   * 是否有默认值
   */
  private boolean hasDefaultValue;
  /**
   * 默认值，如果是字符串则默认值是"默认值"
   */
  private String defaultValue;
  /**
   * 字段注释
   */
  @TableProperty(bundleName = "table_column_comment_title", index = 300)
  @TableEditable(editorProvider = ExpandableTextTableCellEditor.class)
  private String columnComment;
  /**
   * 字段注解
   */
  private List<String> annotations;

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  public boolean isSelected() {
    return selected;
  }

  public String getGetterMethodName() {
    if (javaDataType == boolean.class) {
      return StringHelper.parseIsMethodName(fieldName);
    }
    return StringHelper.parseGetMethodName(fieldName);
  }

  public String getSetterMethodName() {
    return StringHelper.parseSetMethodName(fieldName);
  }
}
