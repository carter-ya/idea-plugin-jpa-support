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
    if (getValue() == null) {
      return "";
    }
    return ((Class<?>) getValue()).getName();
  }

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    if (StringUtils.isBlank(text)) {
      throw new IllegalArgumentException("class can't empty");
    } else {
      try {
        setValue(Class.forName(text));
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException("class not found", e);
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
