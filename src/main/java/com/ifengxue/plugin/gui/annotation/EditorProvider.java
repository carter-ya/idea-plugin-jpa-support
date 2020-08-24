package com.ifengxue.plugin.gui.annotation;

import java.beans.PropertyDescriptor;
import javax.swing.table.TableCellEditor;

public interface EditorProvider {

  TableCellEditor createEditor(PropertyDescriptor pd);
}
