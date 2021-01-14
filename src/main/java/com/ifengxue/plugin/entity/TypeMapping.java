package com.ifengxue.plugin.entity;

import com.ifengxue.plugin.gui.annotation.TableEditable;
import com.ifengxue.plugin.gui.annotation.TableProperty;
import com.ifengxue.plugin.gui.annotation.TableWidth;
import com.ifengxue.plugin.gui.property.ClassNamePropertyEditor;
import com.ifengxue.plugin.gui.property.JavaDataTypeEditorProvider;
import com.ifengxue.plugin.state.wrapper.ClassWrapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TypeMapping {

    @TableProperty(name = "DB Column Type", index = 1)
    @TableEditable
    private String dbColumnType;

    @TableProperty(name = "Java Type", columnClass = String.class, index = 100)
    @TableEditable(editorProvider = JavaDataTypeEditorProvider.class, propertyEditorProvider = ClassNamePropertyEditor.class)
    @TableWidth(minWidth = 60)
    private Class<?> javaType;

    public static TypeMapping from(Entry<String, ClassWrapper> entry) {
        return new TypeMapping()
            .setDbColumnType(entry.getKey())
            .setJavaType(entry.getValue().getClazz());
    }

    public static List<TypeMapping> from(Map<String, ClassWrapper> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        return map.entrySet().stream()
            .map(TypeMapping::from)
            .collect(Collectors.toList());
    }
}
