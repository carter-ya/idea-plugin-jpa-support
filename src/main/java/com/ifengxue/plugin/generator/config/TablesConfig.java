package com.ifengxue.plugin.generator.config;

import java.util.Arrays;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TablesConfig {

  private ORM orm;
  private boolean isUseLombok;
  private boolean useWrapper;
  private boolean isUseClassComment;
  private boolean isUseFieldComment;
  private boolean isUseMethodComment;
  private boolean useDefaultValue;
  private boolean serializable;
  private boolean useJava8DateType;
  private String indent;
  private String lineSeparator;
  private String removeTablePrefix;
  private String removeFieldPrefix;
  private String entityPackageName;
  private String enumSubPackageName;
  private String serviceSubPackageName;
  private String repositoryPackageName;
  private String basePackageName;
  private String extendsEntityName;

  public enum ORM {
    BASIC, MYBATIS, JPA
  }

  public enum LineSeparator {
    WINDOWS("\r\n"), UNIX("\n");
    private final String lineSeparator;

    LineSeparator(String lineSeparator) {
      this.lineSeparator = lineSeparator;
    }

    public static LineSeparator find(String name) {
      return Arrays.stream(values())
          .filter(lineSeparator -> lineSeparator.name().equalsIgnoreCase(name))
          .findAny()
          .orElseThrow(() -> new IllegalStateException("未定义的换行平台:" + name));
    }

    public String getLineSeparator() {
      return lineSeparator;
    }
  }
}
