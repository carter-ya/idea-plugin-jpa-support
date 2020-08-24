package com.ifengxue.plugin.gui.annotation;

import java.beans.PropertyDescriptor;
import javax.swing.table.TableCellEditor;

public class NullEditorProvider implements EditorProvider {

  @Override
  public TableCellEditor createEditor(PropertyDescriptor pd) {
    return null;
  }
}
