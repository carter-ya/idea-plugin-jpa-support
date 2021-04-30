package com.ifengxue.plugin.util;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

public class JdbcConfigUtil {

  private final Map<String, JdbcConfig> driverToJdbcConfig;

  public JdbcConfigUtil(InputStream in) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(in, StandardCharsets.UTF_8))) {
      List<JdbcConfig> jdbcConfigs = objectMapper
          .readValue(br, new TypeReference<List<JdbcConfig>>() {
          });
      driverToJdbcConfig = jdbcConfigs.stream()
          .collect(toMap(JdbcConfig::getDriver, Function.identity()));
    }
  }

  public JdbcConfig findJdbcConfig(String driver) {
    JdbcConfig jdbcConfig = driverToJdbcConfig.get(driver);
    if (jdbcConfig == null) {
      return null;
    }
    if (StringUtils.isBlank(jdbcConfig.getRef())) {
      return jdbcConfig;
    }
    JdbcConfig refDriver = findJdbcConfig(jdbcConfig.getRef());
    jdbcConfig.setVendor(refDriver.getVendor());
    jdbcConfig.setPort(refDriver.getPort());
    jdbcConfig.setUsername(refDriver.getUsername());
    jdbcConfig.setDatabase(refDriver.getDatabase());
    jdbcConfig.setUrl(refDriver.getUrl());
    return jdbcConfig;
  }

  public String tryParseUrl(String driver, String host, String port, String username,
      String password,
      String database, String originalUrl) {
    JdbcConfig jdbcConfig = findJdbcConfig(driver);
    if (jdbcConfig == null) {
      return originalUrl;
    }
    return originalUrl.replace("{host}", host)
        .replace("{port}", port)
        .replace("{username}", username)
        .replace("{password}", password)
        .replace("{database}", database);
  }

  @Data
  public static class JdbcConfig {

    private String vendor;
    private String driver;
    private int port;
    private String username;
    private String database;
    private String url;
    private String ref;
  }

}
