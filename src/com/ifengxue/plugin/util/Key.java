package com.ifengxue.plugin.util;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public enum Key {
  ;

  public static String createKey(String name) {
    return "com.ifengxue.com.ifengxue.plugin.jps-support." + name;
  }

  public static String createKey(String name, Object... args) {
    String prefix = createKey(name);
    String suffix = Arrays.stream(args)
        .map(Object::toString)
        .collect(joining("_"));
    if (StringUtils.isEmpty(suffix)) {
      return prefix;
    } else {
      return prefix + "_" + suffix;
    }
  }
}
