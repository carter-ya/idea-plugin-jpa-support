package com.ifengxue.plugin.util;

import com.ifengxue.plugin.entity.Column;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class ColumnUtil {

  public static void parseColumn(Column column, String removePrefix, boolean isUseWrapper) {
    column.setFieldName(StringHelper.parseFieldName(column.getColumnName(), removePrefix));
    Class<?> javaDataType = StringHelper
        .parseJavaDataType(column.getDbDataType(), column.getColumnName(), isUseWrapper);
    column.setJavaDataType(javaDataType);
    if (column.getDefaultValue() != null) {
      if (javaDataType == String.class) {
        column.setDefaultValue("\"" + column.getDefaultValue() + "\"");
      }
      Class<?> primitiveClass = StringHelper.getPrimitiveClass(javaDataType);
      if (primitiveClass == long.class) {
        column.setDefaultValue(column.getDefaultValue() + "L");
      }
      if (primitiveClass == float.class) {
        column.setDefaultValue(column.getDefaultValue() + "F");
      }
      if (primitiveClass == double.class) {
        column.setDefaultValue(column.getDefaultValue() + "D");
      }
      column.setHasDefaultValue(true);

      // 跳过设置 Date/Timestamp/BigDecimal等数据类型的默认值
      if (javaDataType == BigDecimal.class || javaDataType == Date.class || javaDataType == Timestamp.class) {
        column.setDefaultValue(null);
        column.setHasDefaultValue(false);
      }
    }
  }
}
