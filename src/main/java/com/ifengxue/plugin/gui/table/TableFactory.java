package com.ifengxue.plugin.gui.table;

import static java.util.stream.Collectors.toMap;

import com.ifengxue.plugin.gui.annotation.TableEditable;
import com.ifengxue.plugin.gui.annotation.TableHeight;
import com.ifengxue.plugin.gui.annotation.TableProperty;
import com.ifengxue.plugin.gui.annotation.TableProperty.NullClass;
import com.ifengxue.plugin.gui.annotation.TableWidth;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import fastjdbc.BeanUtil;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

public class TableFactory {

    public <T> void decorateTable(JTable table, Class<T> headClass, List<T> rows) {
        PropertyDescriptor[] pds = BeanUtil.findPropertyDescriptors(headClass);
        Map<String, Field> nameToField = BeanUtil.findFields(headClass)
            .stream()
            .collect(toMap(Field::getName, Function.identity()));
        List<PropertyHolder> propertyHolders = new ArrayList<>();
        boolean shouldSort = false;
        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod() == null) {
                continue;
            }
            Field field = nameToField.get(pd.getName());
            if (field == null) {
                continue;
            }
            TableProperty tableProperty = field.getAnnotation(TableProperty.class);
            if (tableProperty == null) {
                continue;
            }
            if (tableProperty.index() != TableProperty.DEFAULT_INDEX) {
                shouldSort = true;
            }
            propertyHolders.add(new PropertyHolder()
                .setTableProperty(tableProperty)
                .setTableWidth(field.getAnnotation(TableWidth.class))
                .setTableEditable(field.getAnnotation(TableEditable.class))
                .setColumnClass(field.getType())
                .setPropertyDescriptor(pd)
                .afterPropertiesSet()
            );
        }
        if (shouldSort) {
            propertyHolders.sort(Comparator.comparingInt(holder -> holder.getTableProperty().index()));
        }
        MyTableModel<T> myTableModel = new MyTableModel<>(propertyHolders, rows);
        table.setModel(myTableModel);
        TableHeight tableHeight = headClass.getAnnotation(TableHeight.class);
        if (tableHeight != null) {
            table.setRowHeight(tableHeight.height());
        }
        for (int i = 0; i < propertyHolders.size(); i++) {
            TableWidth tableWidth = propertyHolders.get(i).getTableWidth();
            if (tableWidth != null) {
                if (tableWidth.minWidth() != TableWidth.UNSET_WIDTH) {
                    table.getColumnModel().getColumn(i).setMinWidth(tableWidth.minWidth());
                }
                if (tableWidth.maxWidth() != TableWidth.UNSET_WIDTH) {
                    table.getColumnModel().getColumn(i).setMaxWidth(tableWidth.maxWidth());
                }
            }
            TableCellEditor tableCellEditor = propertyHolders.get(i).getTableCellEditor();
            if (tableCellEditor != null) {
                table.getColumnModel().getColumn(i).setCellEditor(tableCellEditor);
            }
            TableCellRenderer tableCellRenderer = propertyHolders.get(i).getTableCellRenderer();
            if (tableCellRenderer != null) {
                table.getColumnModel().getColumn(i).setCellRenderer(tableCellRenderer);
            }
        }
    }

    @Data
    @Accessors(chain = true)
    private static class PropertyHolder {

        private TableProperty tableProperty;
        private TableWidth tableWidth;
        private TableEditable tableEditable;
        private TableCellEditor tableCellEditor;
        private TableCellRenderer tableCellRenderer;
        private String columnName;
        private Class<?> columnClass;
        private PropertyDescriptor propertyDescriptor;

        private boolean isEditable() {
            return tableEditable != null && tableEditable.editable();
        }

        public Object getValue(Object obj) {
            PropertyEditor propertyEditor;
            if (tableEditable != null && (propertyEditor = BeanUtil.instantiate(tableEditable.propertyEditorProvider())
                .createPropertyEditor(obj, propertyDescriptor)) != null) {
                return propertyEditor.getAsText();
            }
            return BeanUtil.getValue(propertyDescriptor, obj);
        }

        public void setValue(Object obj, Object value) {
            PropertyEditor propertyEditor;
            if (tableEditable != null && (propertyEditor = BeanUtil.instantiate(tableEditable.propertyEditorProvider())
                .createPropertyEditor(obj, propertyDescriptor)) != null) {
                propertyEditor.setAsText((String) value);
                return;
            }
            BeanUtil.setValue(propertyDescriptor, obj, value);
        }

        private PropertyHolder afterPropertiesSet() {
            if (tableEditable != null) {
                tableCellEditor = BeanUtil.instantiate(tableEditable.editorProvider())
                    .createEditor(propertyDescriptor);
                tableCellRenderer = BeanUtil.instantiate(tableEditable.rendererProvider())
                    .createRenderer(propertyDescriptor);
            }
            if (StringUtils.isNotBlank(tableProperty.bundleName())) {
                columnName = LocaleContextHolder.format(tableProperty.bundleName());
            } else {
                columnName = tableProperty.name();
            }
            if (tableProperty.columnClass() != NullClass.class) {
                columnClass = tableProperty.columnClass();
            } else {
                columnClass = propertyDescriptor.getPropertyType();
            }
            return this;
        }
    }

    private static class MyTableModel<T> extends AbstractTableModel {

        private final List<PropertyHolder> propertyHolders;
        private final List<T> rows;

        private MyTableModel(List<PropertyHolder> propertyHolders, List<T> rows) {
            this.propertyHolders = propertyHolders;
            this.rows = rows;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return propertyHolders.size();
        }

        @Override
        public String getColumnName(int column) {
            return propertyHolders.get(column).getColumnName();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return propertyHolders.get(columnIndex).isEditable();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return propertyHolders.get(columnIndex).getColumnClass();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return propertyHolders.get(columnIndex).getValue(rows.get(rowIndex));
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            propertyHolders.get(columnIndex).setValue(rows.get(rowIndex), aValue);
        }
    }
}
