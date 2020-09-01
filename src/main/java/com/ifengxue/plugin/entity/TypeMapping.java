package com.ifengxue.plugin.entity;

import com.ifengxue.plugin.gui.annotation.TableEditable;
import com.ifengxue.plugin.gui.annotation.TableProperty;
import com.ifengxue.plugin.gui.annotation.TableWidth;
import com.ifengxue.plugin.gui.property.ClassNamePropertyEditor;
import com.ifengxue.plugin.gui.property.JavaDataTypeEditorProvider;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TypeMapping {

    @TableProperty(name = "Java Type", columnClass = String.class, index = 1)
    @TableEditable(editorProvider = JavaDataTypeEditorProvider.class, propertyEditorProvider = ClassNamePropertyEditor.class)
    @TableWidth(minWidth = 60)
    private Class<?> javaType;

    @TableProperty(name = "DB Column Types", index = 100)
    @TableEditable
    private String dbColumnTypes;
}
