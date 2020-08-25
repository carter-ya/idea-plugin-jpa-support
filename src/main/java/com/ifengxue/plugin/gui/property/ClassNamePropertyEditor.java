package com.ifengxue.plugin.gui.property;

import com.ifengxue.plugin.gui.annotation.PropertyEditorProvider;
import fastjdbc.BeanUtil;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.apache.commons.lang3.StringUtils;

public class ClassNamePropertyEditor extends PropertyEditorSupport implements PropertyEditorProvider {

  private PropertyDescriptor propertyDescriptor;

  public ClassNamePropertyEditor() {
    super();
  }

  public ClassNamePropertyEditor(Object source, PropertyDescriptor propertyDescriptor) {
    super(source);
    this.propertyDescriptor = propertyDescriptor;
    setValue(BeanUtil.getValue(propertyDescriptor, source));
  }

  @Override
  public String getAsText() {
    Object value = getValue();
    if (value == null) {
      return "";
    }
    String name = ((Class<?>) value).getName();
    if (name.equals("[B")) {
      return "byte[]";
    } else {
      return name;
    }
  }

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    if (StringUtils.isBlank(text)) {
      throw new IllegalArgumentException("class can't empty");
    } else {
      switch (text) {
        case "byte":
          setValue(byte.class);
          break;
        case "char":
          setValue(char.class);
          break;
        case "short":
          setValue(short.class);
          break;
        case "int":
          setValue(int.class);
          break;
        case "long":
          setValue(long.class);
          break;
        case "float":
          setValue(float.class);
          break;
        case "double":
          setValue(double.class);
          break;
        case "boolean":
          setValue(boolean.class);
          break;
        case "byte[]":
          setValue(byte[].class);
          break;
        default:
          try {
            setValue(Class.forName(text));
          } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("class not found", e);
          }
          break;
      }
    }
  }

  @Override
  public void setValue(Object value) {
    BeanUtil.setValue(propertyDescriptor, getSource(), value);
    super.setValue(value);
  }

  @Override
  public Object getValue() {
    return super.getValue();
  }

  @Override
  public PropertyEditor createPropertyEditor(Object obj, PropertyDescriptor pd) {
    return new ClassNamePropertyEditor(obj, pd);
  }
}
