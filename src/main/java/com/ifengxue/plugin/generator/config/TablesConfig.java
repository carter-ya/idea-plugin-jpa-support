package com.ifengxue.plugin.generator.config;

import java.util.Arrays;

public class TablesConfig {

  private ORM orm;
  private boolean isUseLombok;
  private boolean useWrapper;
  private boolean isUseClassComment;
  private boolean isUseFieldComment;
  private boolean isUseMethodComment;
  private boolean useDefaultValue;
  private boolean serializable;
  private boolean useJava8DataType;
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

  public ORM getOrm() {
    return orm;
  }

  public TablesConfig setOrm(ORM orm) {
    this.orm = orm;
    return this;
  }

  public boolean isUseWrapper() {
    return useWrapper;
  }

  public TablesConfig setUseWrapper(boolean useWrapper) {
    this.useWrapper = useWrapper;
    return this;
  }

  public boolean isUseClassComment() {
    return isUseClassComment;
  }

  public TablesConfig setUseClassComment(boolean useClassComment) {
    isUseClassComment = useClassComment;
    return this;
  }

  public boolean isUseFieldComment() {
    return isUseFieldComment;
  }

  public TablesConfig setUseFieldComment(boolean useFieldComment) {
    isUseFieldComment = useFieldComment;
    return this;
  }

  public boolean isUseMethodComment() {
    return isUseMethodComment;
  }

  public TablesConfig setUseMethodComment(boolean useMethodComment) {
    isUseMethodComment = useMethodComment;
    return this;
  }

  public boolean isUseDefaultValue() {
    return useDefaultValue;
  }

  public TablesConfig setUseDefaultValue(boolean useDefaultValue) {
    this.useDefaultValue = useDefaultValue;
    return this;
  }

  public boolean isSerializable() {
    return serializable;
  }

  public TablesConfig setSerializable(boolean serializable) {
    this.serializable = serializable;
    return this;
  }

  public String getIndent() {
    return indent;
  }

  public TablesConfig setIndent(String indent) {
    this.indent = indent;
    return this;
  }

  public String getLineSeparator() {
    return lineSeparator;
  }

  public TablesConfig setLineSeparator(String lineSeparator) {
    this.lineSeparator = lineSeparator;
    return this;
  }

  public String getRemoveTablePrefix() {
    return removeTablePrefix;
  }

  public TablesConfig setRemoveTablePrefix(String removeTablePrefix) {
    this.removeTablePrefix = removeTablePrefix;
    return this;
  }

  public String getRemoveFieldPrefix() {
    return removeFieldPrefix;
  }

  public TablesConfig setRemoveFieldPrefix(String removeFieldPrefix) {
    this.removeFieldPrefix = removeFieldPrefix;
    return this;
  }

  public String getBasePackageName() {
    return basePackageName;
  }

  public TablesConfig setBasePackageName(String basePackageName) {
    this.basePackageName = basePackageName;
    return this;
  }

  public String getEntityPackageName() {
    return entityPackageName;
  }

  public TablesConfig setEntityPackageName(String entityPackageName) {
    this.entityPackageName = entityPackageName;
    return this;
  }

  public String getEnumSubPackageName() {
    return enumSubPackageName;
  }

  public TablesConfig setEnumSubPackageName(String enumSubPackageName) {
    this.enumSubPackageName = enumSubPackageName;
    return this;
  }

  public String getServiceSubPackageName() {
    return serviceSubPackageName;
  }

  public TablesConfig setServiceSubPackageName(String serviceSubPackageName) {
    this.serviceSubPackageName = serviceSubPackageName;
    return this;
  }

  public String getRepositoryPackageName() {
    return repositoryPackageName;
  }

  public TablesConfig setRepositoryPackageName(String repositoryPackageName) {
    this.repositoryPackageName = repositoryPackageName;
    return this;
  }

  public String getExtendsEntityName() {
    return extendsEntityName;
  }

  public TablesConfig setExtendsEntityName(String extendsEntityName) {
    this.extendsEntityName = extendsEntityName;
    return this;
  }

  public boolean isUseLombok() {
    return isUseLombok;
  }

  public TablesConfig setUseLombok(boolean useLombok) {
    isUseLombok = useLombok;
    return this;
  }

  public boolean isUseJava8DataType() {
    return useJava8DataType;
  }

  public TablesConfig setUseJava8DataType(boolean useJava8DataType) {
    this.useJava8DataType = useJava8DataType;
    return this;
  }

  @Override
  public String toString() {
    return "TablesConfig{" +
        "orm=" + orm +
        ", isUseLombok=" + isUseLombok +
        ", useWrapper=" + useWrapper +
        ", isUseClassComment=" + isUseClassComment +
        ", isUseFieldComment=" + isUseFieldComment +
        ", isUseMethodComment=" + isUseMethodComment +
        ", useDefaultValue=" + useDefaultValue +
        ", serializable=" + serializable +
        ", useJava8DataType=" + useJava8DataType +
        ", indent='" + indent + '\'' +
        ", lineSeparator='" + lineSeparator + '\'' +
        ", removeTablePrefix='" + removeTablePrefix + '\'' +
        ", removeFieldPrefix='" + removeFieldPrefix + '\'' +
        ", entityPackageName='" + entityPackageName + '\'' +
        ", enumSubPackageName='" + enumSubPackageName + '\'' +
        ", serviceSubPackageName='" + serviceSubPackageName + '\'' +
        ", repositoryPackageName='" + repositoryPackageName + '\'' +
        ", basePackageName='" + basePackageName + '\'' +
        ", extendsEntityName='" + extendsEntityName + '\'' +
        '}';
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
}
