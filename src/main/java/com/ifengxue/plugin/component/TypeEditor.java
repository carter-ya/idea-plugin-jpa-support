package com.ifengxue.plugin.component;

import com.ifengxue.plugin.util.TypeUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TextFieldWithAutoCompletion;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lombok.Data;

@Data
public class TypeEditor {

    private JPanel rootComponent;
    private JTextField textDbType;
    private TextFieldWithAutoCompletion<String> textJavaType;

    private void createUIComponents() {
        textJavaType = TextFieldWithAutoCompletion
            .create(null, TypeUtil.getAllJavaDbType(), true, String.class.getName());
    }
}
