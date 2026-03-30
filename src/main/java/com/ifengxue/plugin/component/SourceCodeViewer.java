package com.ifengxue.plugin.component;

import com.ifengxue.plugin.util.Editors;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.ScrollPaneFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import lombok.Data;

@Data
public class SourceCodeViewer implements Disposable {

    private JPanel rootComponent;
    private JPanel sourceCodePanel;
    private LanguageTextField txtSourceCode;
    private Editor editor;

    public SourceCodeViewer(Language language) {
        Project defaultProject = ProjectManager.getInstance().getDefaultProject();
        txtSourceCode = new LanguageTextField(language, defaultProject, "",
            (value, lang, project) -> {
                releaseEditor();
                editor = Editors.createSourceEditor(project, language, value, false);
                return editor.getDocument();
            }, false);
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(txtSourceCode);
        sourceCodePanel.add(scrollPane);
    }

    @Override
    public void dispose() {
        releaseEditor();
    }

    private void releaseEditor() {
        if (editor != null) {
            Editors.release(editor);
            editor = null;
        }
    }
}
