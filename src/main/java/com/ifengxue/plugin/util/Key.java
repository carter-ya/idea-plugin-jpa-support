package com.ifengxue.plugin.util;

public enum Key {
  ;

  public static String createKey(String name) {
    return "com.ifengxue.plugin.jps-support." + name;
  }
}
