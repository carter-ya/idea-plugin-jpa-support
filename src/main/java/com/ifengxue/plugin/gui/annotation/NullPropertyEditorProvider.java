package com.ifengxue.plugin.gui.annotation;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;

public class NullPropertyEditorProvider implements PropertyEditorProvider {

  @Override
  public PropertyEditor createPropertyEditor(Object obj, PropertyDescriptor pd) {
    return null;
  }
}
