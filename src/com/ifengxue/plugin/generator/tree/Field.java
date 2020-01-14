package com.ifengxue.plugin.generator.tree;

import com.ifengxue.plugin.util.StringHelper;
import java.lang.Class;

public class Field extends AbstractElement {

  private final String fieldName;
  private final Class<?> dataType;
  private boolean primaryKey;
  private boolean autoIncrement;
  private boolean nullable;
  private boolean hasDefaultValue;
  private String defaultValue;
  private String modifiers = "private";
  private GetMethod getMethod;
  private SetMethod setMethod;
  private boolean isUseLombok;

  public Field(String fieldName, Class<?> dataType, boolean isUseLombok) {
    this.fieldName = fieldName;
    this.dataType = dataType;
    this.isUseLombok = isUseLombok;
  }

  public boolean isPrimaryKey() {
    return primaryKey;
  }

  public Field setPrimaryKey(boolean primaryKey) {
    this.primaryKey = primaryKey;
    return this;
  }

  public boolean isAutoIncrement() {
    return autoIncrement;
  }

  public Field setAutoIncrement(boolean autoIncrement) {
    this.autoIncrement = autoIncrement;
    return this;
  }

  public boolean isNullable() {
    return nullable;
  }

  public Field setNullable(boolean nullable) {
    this.nullable = nullable;
    return this;
  }

  public boolean isHasDefaultValue() {
    return hasDefaultValue;
  }

  public Field setHasDefaultValue(boolean hasDefaultValue) {
    this.hasDefaultValue = hasDefaultValue;
    return this;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public Field setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  public Field setStringDefaultValue(String defaultValue) {
    this.defaultValue = "\"" + defaultValue + "\"";
    return this;
  }

  public String getModifiers() {
    return modifiers;
  }

  public Field setModifiers(String modifiers) {
    this.modifiers = modifiers;
    return this;
  }

  public Class<?> getDataType() {
    return dataType;
  }

  public String getFieldName() {
    return fieldName;
  }

  public GetMethod getMethod() {
    if (modifiers.contains("static final") || isUseLombok) {
      return null;
    }
    if (getMethod == null) {
      getMethod = new GetMethod(fieldName, dataType);
      this.findComment().ifPresent(getMethod::addChild);
    }
    return getMethod;
  }

  public SetMethod setMethod() {
    if (modifiers.contains("static final") || isUseLombok) {
      return null;
    }
    if (setMethod == null) {
      setMethod = new SetMethod(fieldName, dataType);
      this.findComment().ifPresent(setMethod::addChild);
    }
    return setMethod;
  }

  @Override
  public String toString() {
    String toString = modifiers + " " + dataType.getSimpleName() + " " + fieldName;
    if (hasDefaultValue) {
      toString += " = " + defaultValue;
    }
    return toString + ";";
  }

  public class GetMethod extends Field {

    public GetMethod(String fieldName, Class<?> dataType) {
      super(fieldName, dataType, false);
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder("public ").append(dataType.getSimpleName()).append(" ");
      if (dataType.equals(boolean.class) || dataType.equals(Boolean.class)) {
        builder.append("is");
      } else {
        builder.append("get");
      }
      builder.append(StringHelper.firstLetterUpper(fieldName))
          .append("() {").append(lineSeparator);
      builder.append(Field.this.indent.getIndent())
          .append("return ").append(fieldName).append(";").append(lineSeparator);
      builder.append("}");
      return builder.toString();
    }
  }

  public class SetMethod extends Field {

    public SetMethod(String fieldName, Class<?> dataType) {
      super(fieldName, dataType, false);
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder("public void ");
      builder.append("set").append(StringHelper.firstLetterUpper(fieldName))
          .append("(").append(dataType.getSimpleName()).append(" ").append(fieldName).append(") {")
          .append(lineSeparator);
      builder.append(Field.this.indent.getIndent()).append("this.").append(fieldName).append(" = ").append(fieldName)
          .append(";").append(lineSeparator);
      builder.append("}");
      return builder.toString();
    }
  }
}
