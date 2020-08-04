package com.ifengxue.plugin.component;

import com.ifengxue.plugin.state.SettingsState;
import com.ifengxue.plugin.util.Editors;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.LanguageTextField;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import lombok.Data;

@Data
public class Settings {

    private JPanel rootComponent;
    private JTabbedPane tabbedPane;
    private JComboBox<TemplateItem> cbxSelectCodeTemplate;
    private LanguageTextField txtSourceCode;
    private JButton btnTestTemplate;
    private JButton btnResetTemplate;

    private void createUIComponents() {
        Language velocityLanguage = Objects.requireNonNull(Language.findLanguageByID("VTL"));
        Project defaultProject = ProjectManager.getInstance().getDefaultProject();
        txtSourceCode = new LanguageTextField(velocityLanguage,
            defaultProject, "",
            (value, language, project) -> Editors.createSourceEditor(project, velocityLanguage, value, false)
                .getDocument(), false);
    }

    public void setData(SettingsState data) {

    }

    public void getData(SettingsState data) {
        TemplateItem item = (TemplateItem) cbxSelectCodeTemplate.getSelectedItem();
        Objects.requireNonNull(item);
        data.putTemplate(item.getId(), item.getTemplate());
    }

    public boolean isModified(SettingsState data) {
        TemplateItem item = (TemplateItem) cbxSelectCodeTemplate.getSelectedItem();
        Objects.requireNonNull(item);
        return !item.getTemplate().equals(data.loadTemplate(item.getId()));
    }
}
