package com.ifengxue.plugin.gui;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.component.SourceCodeViewer;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import javax.swing.Action;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SourceCodeViewerDialog extends DialogWrapper {

    private SourceCodeViewer sourceCodeViewer;

    public SourceCodeViewerDialog(@Nullable Project project, Language language,
        boolean canBeParent) {
        super(project, canBeParent);
        sourceCodeViewer = new SourceCodeViewer(language);
        init();
        setTitle("Source Code Viewer");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return sourceCodeViewer.getRootComponent();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction()};
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return Constants.NAME + ":" + getClass().getName();
    }

    public void setSourceCode(String sourceCode) {
        sourceCodeViewer.getTxtSourceCode().setText(sourceCode);
    }
}
