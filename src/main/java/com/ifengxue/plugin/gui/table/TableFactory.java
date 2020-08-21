package com.ifengxue.plugin.gui.table;

import static java.util.stream.Collectors.toMap;

import com.ifengxue.plugin.gui.annotation.TableEditable;
import com.ifengxue.plugin.gui.annotation.TableProperty;
import com.ifengxue.plugin.gui.annotation.TableProperty.NullClass;
import com.ifengxue.plugin.gui.annotation.TableWidth;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import fastjdbc.BeanUtil;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
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
        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod() == null || pd.getWriteMethod() == null) {
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
            propertyHolders.add(new PropertyHolder()
                .setTableProperty(tableProperty)
                .setTableWidth(field.getAnnotation(TableWidth.class))
                .setTableEditable(field.getAnnotation(TableEditable.class))
                .setColumnClass(field.getType())
                .setPropertyDescriptor(pd)
                .afterPropertiesSet()
            );
        }
        propertyHolders.sort(Comparator.comparingInt(holder -> holder.getTableProperty().index()));
        MyTableModel<T> myTableModel = new MyTableModel<>(propertyHolders, rows);
        table.setModel(myTableModel);
        for (int i = 0; i < propertyHolders.size(); i++) {
            TableWidth tableWidth = propertyHolders.get(i).getTableWidth();
            if (tableWidth != null) {
                table.getColumnModel().getColumn(i).setMaxWidth(tableWidth.width());
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
        private String columnName;
        private Class<?> columnClass;
        private PropertyDescriptor propertyDescriptor;

        private boolean isEditable() {
            return tableEditable != null && tableEditable.editable();
        }

        public Object getValue(Object obj) {
            return BeanUtil.getValue(propertyDescriptor, obj);
        }

        public void setValue(Object obj, Object value) {
            BeanUtil.setValue(propertyDescriptor, obj, value);
        }

        private PropertyHolder afterPropertiesSet() {
            if (tableEditable != null) {
                tableCellEditor = BeanUtil.instantiate(tableEditable.editorProvider()).createEditor();
            }
            if (StringUtils.isNotBlank(tableProperty.bundleName())) {
                columnName = LocaleContextHolder.format(tableProperty.bundleName());
            } else {
                columnName = tableProperty.name();
            }
            if (tableProperty.columnClass() != NullClass.class) {
                columnClass = tableProperty.columnClass();
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
