package com.ifengxue.plugin.entity;

import com.intellij.database.model.DasColumn;
import com.intellij.database.util.DasUtil;
import org.apache.commons.lang3.StringUtils;

public class DatabasePluginColumnSchema extends ColumnSchema implements ColumnSchemaExtension<DasColumn> {

  private static final long serialVersionUID = 8104233110923069156L;

  private final DasColumn dasColumn;

  public DatabasePluginColumnSchema(DasColumn dasColumn) {
    this.dasColumn = dasColumn;

    setColumnName(dasColumn.getName());
    setTableSchema(DasUtil.getSchema(dasColumn.getTable()));
    setTableName(dasColumn.getTableName());
    setOrdinalPosition(dasColumn.getPosition());
    setDataType(dasColumn.getDataType().typeName);
    setColumnType(dasColumn.getDataType().typeName);
    setColumnComment(StringUtils.trimToEmpty(dasColumn.getComment()));
    String defaultVal = dasColumn.getDefault();
    if (defaultVal != null && defaultVal.startsWith("'") && defaultVal.endsWith("'")) {
      defaultVal = defaultVal.substring(1, defaultVal.length() - 1);
    }
    setColumnDefault(defaultVal);
  }

  @Override
  public DasColumn origin() {
    return dasColumn;
  }

  @Override
  public boolean nullable() {
    return !dasColumn.isNotNull();
  }

  @Override
  public boolean primary() {
    return DasUtil.isPrimary(dasColumn);
  }

  @Override
  public boolean autoIncrement() {
    return dasColumn.getTable().getColumnAttrs(dasColumn).contains(DasColumn.Attribute.AUTO_GENERATED);
  }
}
