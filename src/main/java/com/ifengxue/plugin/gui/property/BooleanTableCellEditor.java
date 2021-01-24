package com.ifengxue.plugin.gui.property;

import com.ifengxue.plugin.gui.annotation.EditorProvider;
import java.beans.PropertyDescriptor;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.table.TableCellEditor;

public class BooleanTableCellEditor extends DefaultCellEditor implements EditorProvider {

    public BooleanTableCellEditor() {
        super(new JCheckBox());
    }

    @Override
    public TableCellEditor createEditor(PropertyDescriptor pd) {
        return new BooleanTableCellEditor();
    }
}
