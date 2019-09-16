package com.ifengxue.plugin.util;

import static org.junit.Assert.assertEquals;

import com.ifengxue.plugin.adapter.MysqlDriverAdapter;
import java.sql.Date;
import java.time.LocalDate;
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
    assertEquals(StringHelper.parseFieldName("FIELD_NAME", "f_"), "fieldName");
  }

  @Test
  public void parseJavaDataType() {
    assertEquals(StringHelper.parseJavaDataType(new MysqlDriverAdapter(), "DATE", "date", true, false), Date.class);
    assertEquals(StringHelper.parseJavaDataType(new MysqlDriverAdapter(), "DATE", "date", true, true), LocalDate.class);
    assertEquals(StringHelper.parseJavaDataType(new MysqlDriverAdapter(), "BIGINT", "value", false, true),
        long.class);
    assertEquals(StringHelper.parseJavaDataType(new MysqlDriverAdapter(), "BIGINT", "value", true, true),
        Long.class);
  }
}