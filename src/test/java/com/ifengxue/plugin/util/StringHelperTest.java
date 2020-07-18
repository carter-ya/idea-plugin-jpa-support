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
}