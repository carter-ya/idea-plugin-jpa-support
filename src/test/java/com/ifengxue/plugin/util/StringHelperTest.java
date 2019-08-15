package com.ifengxue.plugin.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringHelperTest {

  @Test
  public void parseFieldName() {
    assertEquals(StringHelper.parseFieldName("f_field_name", "f_"), "fieldName");
    assertEquals(StringHelper.parseFieldName("field_name", "f_"), "fieldName");
    assertEquals(StringHelper.parseFieldName("fieldname", "f_"), "fieldname");
    assertEquals(StringHelper.parseFieldName("fieldName", "f_"), "fieldName");
    assertEquals(StringHelper.parseFieldName("realizedPNL", "f_"), "realizedPNL");
    assertEquals(StringHelper.parseFieldName("f_realizedPNL", "f_"), "realizedPNL");
  }
}