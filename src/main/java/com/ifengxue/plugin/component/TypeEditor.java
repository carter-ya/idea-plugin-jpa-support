package com.ifengxue.plugin.component;

import com.ifengxue.plugin.util.TypeUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.TextFieldWithAutoCompletion;
import java.util.Optional;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lombok.Data;

@Data
public class TypeEditor {

    private JPanel rootComponent;
    private JTextField textDbType;
    private TextFieldWithAutoCompletion<String> textJavaType;

    private void createUIComponents() {
        ProjectManager.getInstance().getDefaultProject();
        Project project = Optional.ofNullable(ProjectManager.getInstance())
            .map(ProjectManager::getDefaultProject)
            .orElse(null);
        textJavaType = TextFieldWithAutoCompletion
            .create(project, TypeUtil.getAllJavaDbType(), true, String.class.getName());
    }
}
