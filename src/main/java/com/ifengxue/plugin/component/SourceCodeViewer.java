package com.ifengxue.plugin.component;

import com.ifengxue.plugin.util.Editors;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.LanguageTextField;
import javax.swing.JPanel;
import lombok.Data;

@Data
public class SourceCodeViewer {

    private JPanel rootComponent;
    private LanguageTextField txtSourceCode;

    private void createUIComponents() {
        Project defaultProject = ProjectManager.getInstance().getDefaultProject();
        txtSourceCode = new LanguageTextField(JavaLanguage.INSTANCE, defaultProject, "",
            (value, language, project) -> Editors
                .createSourceEditor(project, JavaLanguage.INSTANCE, value, false)
                .getDocument(), false);
    }
}
