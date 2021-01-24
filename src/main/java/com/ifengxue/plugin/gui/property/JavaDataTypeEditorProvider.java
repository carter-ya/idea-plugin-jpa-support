package com.ifengxue.plugin.gui.property;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.gui.annotation.EditorProvider;
import com.ifengxue.plugin.util.TypeUtil;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import java.beans.PropertyDescriptor;
import javax.swing.table.TableCellEditor;

public class JavaDataTypeEditorProvider implements EditorProvider {

  @Override
  public TableCellEditor createEditor(PropertyDescriptor pd) {
    Project project = Holder.getProject();
    if (project == null) {
      project = ProjectManager.getInstance().getDefaultProject();
    }
    ClassNameAutoCompletionTableCellEditor editor = new ClassNameAutoCompletionTableCellEditor(project,
        TypeUtil.getAllJavaDbType());
    editor.addDocumentListener(new DocumentAdapter() {
      @Override
      public void documentChanged(DocumentEvent e) {

      }
    });
    return editor;
  }
}
