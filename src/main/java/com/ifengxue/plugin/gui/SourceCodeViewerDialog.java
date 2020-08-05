package com.ifengxue.plugin.gui;

import com.ifengxue.plugin.component.SourceCodeViewer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

public class SourceCodeViewerDialog extends DialogWrapper {

    private SourceCodeViewer sourceCodeViewer;

    public SourceCodeViewerDialog(@Nullable Project project, boolean canBeParent) {
        super(project, canBeParent);
        sourceCodeViewer = new SourceCodeViewer();
        init();
        setTitle("Source Code Viewer");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return sourceCodeViewer.getRootComponent();
    }

    public void setSourceCode(String sourceCode) {
        sourceCodeViewer.getTxtSourceCode().setText(sourceCode);
    }
}
