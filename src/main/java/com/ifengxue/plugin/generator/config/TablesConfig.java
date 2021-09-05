package com.ifengxue.plugin.generator.config;

import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.util.StringHelper;
import java.util.Arrays;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

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
  private boolean useDefaultDatetimeValue;
  private boolean serializable;
  private boolean useJava8DateType;
  private boolean useFluidProgrammingStyle;
  private boolean useSwaggerUIComment;
  private boolean useJpaAnnotation;
  private boolean addSchemeNameToTableName;
  private String indent;
  private String lineSeparator;
  private String removeTablePrefix;
  private String removeFieldPrefix;
  private String entityPackageName;
  private String enumSubPackageName;
  private String repositoryPackageName;
  private String basePackageName;
  private String extendsEntityName;

  // controller

  private String controllerPackageName;

  // service

  private boolean useJpa;
  private boolean useMybatisPlus;
  private boolean useTkMybatis;
  private String servicePackageName;

  // vo
  private String voSuffixName;
  private String voPackageName;

  // dto
  private String dtoSuffixName;
  private String dtoPackageName;

  /**
   * 是否需要输出默认值
   */
  public boolean requireWriteDefaultValue(Column column) {
    if (!column.isHasDefaultValue()) {
      return false;
    }
    if (StringHelper.isDatetimeType(column.getJavaDataType())) {
      return useDefaultValue && useDefaultDatetimeValue;
    }
    return useDefaultValue;
  }

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

  public String getVoSuffixVariableName() {
    if (StringUtils.isBlank(voSuffixName)) {
      return "";
    }
    return StringHelper.firstLetterLower(voSuffixName);
  }

  public String getDtoSuffixVariableName() {
    if (StringUtils.isBlank(dtoSuffixName)) {
      return "";
    }
    return StringHelper.firstLetterLower(dtoSuffixName);
  }
}
