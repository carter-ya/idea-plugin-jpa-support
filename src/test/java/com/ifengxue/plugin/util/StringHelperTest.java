package com.ifengxue.plugin.util;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

public class StringHelperTest {

  @Test
  public void packagePathTrimToParentFolder() {
    String path = "/path/to/package/parent/path/com/foo";
    String packageName = "com.foo";
    if (SystemUtils.IS_OS_WINDOWS) {
      path = "C:\\path\\to\\package\\parent\\path\\com\\foo";
      assertEquals("C:\\path\\to\\package\\parent\\path",
          StringHelper.packagePathTrimToParentFolder(path, packageName));
    } else {
      assertEquals("/path/to/package/parent/path", StringHelper.packagePathTrimToParentFolder(path, packageName));
      assertEquals("/path/to/package/parent/path/com/foo",
          StringHelper.packagePathTrimToParentFolder(path, packageName + ".bar"));
    }
  }

  @Test
  public void parseFieldName() {
    assertEquals("columnName", StringHelper.parseFieldName("column_name"));
    assertEquals("columnName", StringHelper.parseFieldName("_column_name_"));
    assertEquals("columnName", StringHelper.parseFieldName("COLUMN_NAME"));
    assertEquals("columnName", StringHelper.parseFieldName("ColumnName"));
    assertEquals("columnName", StringHelper.parseFieldName("Column Name"));
    assertEquals("columnName", StringHelper.parseFieldName("Column    Name"));
    assertEquals("columnName", StringHelper.parseFieldName("column  name"));
    assertEquals("column", StringHelper.parseFieldName("COLUMN"));
    assertEquals("columnname", StringHelper.parseFieldName("COLUMNNAME"));
  }

  @Test
  public void parseFieldNameWithRemovePrefix() {
    assertEquals("columnName", StringHelper.parseFieldName("column_name", "", ""));
    assertEquals("columnName", StringHelper.parseFieldName("column_name", "f_", ""));
    assertEquals("columnName", StringHelper.parseFieldName("f_column_name", "f_", ""));
    assertEquals("columnName", StringHelper.parseFieldName("f_column_name", "f_,field_", ""));
  }

  @Test
  public void parseFieldNameWithJavaKeyword() {
    assertEquals("implementsF", StringHelper.parseFieldName("implements", "", "F"));
    assertEquals("publicF", StringHelper.parseFieldName("public", "", "F"));
    assertEquals("voidF", StringHelper.parseFieldName("void", "", "F"));
  }
}
