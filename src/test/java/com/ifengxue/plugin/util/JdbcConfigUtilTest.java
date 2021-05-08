package com.ifengxue.plugin.util;

import com.ifengxue.plugin.util.JdbcConfigUtil.JdbcConfig;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcConfigUtilTest {

  private JdbcConfigUtil jdbcConfigUtil;

  @BeforeEach
  void setUp() throws IOException {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("jdbc_config.json")) {
      jdbcConfigUtil = new JdbcConfigUtil(in);
    }
  }

  @Test
  void test_not_ref() {
    JdbcConfig jdbcConfig = jdbcConfigUtil.findJdbcConfig("com.mysql.jdbc.Driver");
    Assertions.assertNotNull(jdbcConfig);
  }

  @Test
  void test_with_ref() {
    JdbcConfig jdbcConfig = jdbcConfigUtil.findJdbcConfig("com.mysql.cj.jdbc.Driver");
    Assertions.assertNotNull(jdbcConfig);
    Assertions.assertNotNull(jdbcConfig.getUrl());
    Assertions.assertFalse(jdbcConfig.getUrl().isEmpty());
  }

  @Test
  void test_no_parameter_original_url() {
    String originalUrl = "jdbc:mysql://localhost:1234/bar?useSSL=false&useUnicode=true&characterEncoding=UTF-8";
    String targetUrl = jdbcConfigUtil.tryParseUrl("com.mysql.cj.jdbc.Driver", "localhost", "3306",
        "root", "root", "foo", originalUrl);
    Assertions.assertEquals(originalUrl, targetUrl);
  }

  @Test
  void test_with_parameter_original_url() {
    String targetUrl = jdbcConfigUtil.tryParseUrl("com.mysql.cj.jdbc.Driver", "localhost", "3306",
        "root", "root", "foo",
        "jdbc:mysql://{host}:{port}/{database}?useSSL=false&useUnicode=true&characterEncoding=UTF-8");
    Assertions.assertEquals(
        "jdbc:mysql://localhost:3306/foo?useSSL=false&useUnicode=true&characterEncoding=UTF-8",
        targetUrl);
  }
}
