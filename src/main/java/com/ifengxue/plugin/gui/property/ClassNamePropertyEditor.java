package com.ifengxue.plugin.gui.property;

import com.ifengxue.plugin.gui.annotation.PropertyEditorProvider;
import com.ifengxue.plugin.util.TypeUtil;
import fastjdbc.BeanUtil;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

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
    return TypeUtil.javaDbTypeToString((Class<?>) value);
  }

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    Class<?> type = TypeUtil.javaDbTypeToClassAndShowDialogOnError(text);
    if (type != null) {
      setValue(type);
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
