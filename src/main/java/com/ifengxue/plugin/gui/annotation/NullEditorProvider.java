package com.ifengxue.plugin.gui.annotation;

import javax.swing.table.TableCellEditor;

public class NullEditorProvider implements EditorProvider {

    @Override
    public TableCellEditor createEditor() {
        return null;
    }
}
