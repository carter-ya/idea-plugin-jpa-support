package com.ifengxue.plugin.gui.annotation;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;

public interface PropertyEditorProvider {

  PropertyEditor createPropertyEditor(Object obj, PropertyDescriptor pd);
}
