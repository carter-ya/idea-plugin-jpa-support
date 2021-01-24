package com.ifengxue.plugin.gui;

import com.ifengxue.plugin.component.TypeEditor;
import com.ifengxue.plugin.entity.TypeMapping;
import com.ifengxue.plugin.util.TypeUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TypeEditorDialog extends DialogWrapper {

    private TypeEditor typeEditor;
    private final TypeMapping typeMapping;
    private final List<TypeMapping> existsTypeMappings;

    public TypeEditorDialog(@Nullable Project project, @Nullable TypeMapping typeMapping,
        List<TypeMapping> existsTypeMappings) {
        super(project, true);
        this.typeMapping = typeMapping;
        this.existsTypeMappings = existsTypeMappings;
        this.typeEditor = new TypeEditor();
        init();
        setTitle("DB Type and Java Class Mapping");
        if (typeMapping != null) {
            typeEditor.getTextDbType().setText(typeMapping.getDbColumnType());
            typeEditor.getTextJavaType().setText(TypeUtil.javaDbTypeToString(typeMapping.getJavaType()));
        }
        typeEditor.getTextJavaType().requestFocus();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return typeEditor.getRootComponent();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction(), getCancelAction()};
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        String textDbType = typeEditor.getTextDbType().getText().trim();
        if (StringUtils.isEmpty(textDbType)) {
            return new ValidationInfo("DB type can't be empty", typeEditor.getTextDbType());
        }
        String textJavaType = typeEditor.getTextJavaType().getText().trim();
        if (StringUtils.isEmpty(textJavaType)) {
            return new ValidationInfo("Java class can't be empty", typeEditor.getTextJavaType());
        }
        try {
            TypeUtil.javaDbTypeToClass(textJavaType);
        } catch (ClassNotFoundException e) {
            return new ValidationInfo("Invalid java class name", typeEditor.getTextJavaType());
        }
        long count = existsTypeMappings.stream()
            .filter(tp -> tp.getDbColumnType().equals(textDbType))
            .count();
        // edit mode or contains no duplicate db type
        if (count == 0) {
            return null;
        }
        if (typeMapping != null && count == 1) {
            return null;
        }
        return new ValidationInfo("Detect duplicate db type mapping");
    }

    @Override
    protected @Nullable
    @NonNls
    String getDimensionServiceKey() {
        return getClass().getName();
    }

    public String getDbType() {
        return typeEditor.getTextDbType().getText().trim();
    }

    public Class<?> getJavaType() {
        try {
            return TypeUtil.javaDbTypeToClass(typeEditor.getTextJavaType().getText().trim());
        } catch (ClassNotFoundException ignore) {
        }
        return null;
    }
}
