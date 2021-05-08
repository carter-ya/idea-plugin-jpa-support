package com.ifengxue.plugin.gui.property;

import com.ifengxue.plugin.gui.annotation.EditorProvider;
import com.intellij.ui.components.fields.ExpandableTextField;
import java.beans.PropertyDescriptor;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;

public class ExpandableTextTableCellEditor extends DefaultCellEditor implements EditorProvider {

  private static final long serialVersionUID = -3968793762462384695L;

  public ExpandableTextTableCellEditor() {
    super(new ExpandableTextField());
  }

  @Override
  public TableCellEditor createEditor(PropertyDescriptor pd) {
    return new ExpandableTextTableCellEditor();
  }
}
