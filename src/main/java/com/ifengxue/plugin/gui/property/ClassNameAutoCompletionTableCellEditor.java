package com.ifengxue.plugin.gui.property;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.util.containers.ContainerUtil;
import java.awt.Component;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ClassNameAutoCompletionTableCellEditor extends AbstractCellEditor implements TableCellEditor {

  private final Project project;
  private final Collection<String> classNames;
  private Document myDocument;
  private final List<DocumentListener> myListeners = ContainerUtil.createLockFreeCopyOnWriteList();

  public ClassNameAutoCompletionTableCellEditor(Project project, Collection<String> classNames) {
    this.project = project;
    this.classNames = classNames;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    TextFieldWithAutoCompletion<String> field = TextFieldWithAutoCompletion
        .create(project, classNames, true, (String) value);
    myDocument = field.getDocument();
    if (myDocument != null) {
      for (DocumentListener listener : myListeners) {
        field.addDocumentListener(listener);
      }
    }
    return field;
  }

  @Override
  public Object getCellEditorValue() {
    return myDocument.getText();
  }

  public void addDocumentListener(DocumentListener listener) {
    myListeners.add(listener);
  }

  public void clearListeners() {
    myListeners.clear();
  }
}
