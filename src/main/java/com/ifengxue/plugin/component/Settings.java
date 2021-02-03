package com.ifengxue.plugin.component;

import com.ifengxue.plugin.state.SettingsState;
import com.ifengxue.plugin.util.Editors;
import com.ifengxue.plugin.util.TypeUtil;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.components.JBList;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
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
    private JScrollPane sourceCodePane;
    private TextFieldWithAutoCompletion<String> textFallbackType;
    private JRadioButton radioBtnThrow;
    private JRadioButton radioBtnFallbackType;
    private JPanel typeMappingTablePane;
    private JBList<TemplateItem> templateList;
    private JComboBox<TemplateItem> cbxTemplateBindingEntity;
    private JComboBox<TemplateItem> cbxTemplateBindingService;
    private JComboBox<TemplateItem> cbxTemplateBindingRepository;

    private void createUIComponents() {
        sourceCodePane = ScrollPaneFactory.createScrollPane(txtSourceCode);
        Language velocityLanguage = Optional.ofNullable(Language.findLanguageByID("VTL"))
            .orElse(Language.findLanguageByID("TEXT"));
        Project defaultProject = ProjectManager.getInstance().getDefaultProject();
        txtSourceCode = new LanguageTextField(velocityLanguage,
            defaultProject, "",
            (value, language, project) -> Editors.createSourceEditor(project, velocityLanguage, value, false)
                .getDocument(), false);
        textFallbackType = TextFieldWithAutoCompletion
            .create(defaultProject, TypeUtil.getAllJavaDbType(), true, String.class.getName());
    }

    public void setData(SettingsState data) {
        // type config
        radioBtnFallbackType.setSelected(data.isFallbackType());
        radioBtnThrow.setSelected(data.isThrowException());
        textFallbackType.setText(TypeUtil.javaDbTypeToString(data.getFallbackTypeClass()));
    }

    public void getData(SettingsState data) throws ClassNotFoundException {
        // template config

        TemplateItem item = (TemplateItem) cbxSelectCodeTemplate.getSelectedItem();
        Objects.requireNonNull(item);
        data.putTemplate(item.getId(), item.getTemplate());

        // type config
        data.setFallbackType(radioBtnFallbackType.isSelected());
        data.setThrowException(radioBtnThrow.isSelected());
        data.setFallbackTypeClass(TypeUtil.javaDbTypeToClass(textFallbackType.getText()));
    }

    public boolean isModified(SettingsState data) {
        // template config
        TemplateItem item = (TemplateItem) cbxSelectCodeTemplate.getSelectedItem();
        Objects.requireNonNull(item);
        if (!item.getTemplate().equals(data.loadTemplate(item.getId()))) {
            return true;
        }

        // type config
        if (radioBtnFallbackType.isSelected() != data.isFallbackType()) {
            return true;
        }
        if (radioBtnThrow.isSelected() != data.isThrowException()) {
            return true;
        }
        if (!textFallbackType.getText().equals(TypeUtil.javaDbTypeToString(data.getFallbackTypeClass()))) {
            return true;
        }
        return false;
    }
}
