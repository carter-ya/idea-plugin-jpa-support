package com.ifengxue.plugin.gui.property;

import com.google.common.collect.Lists;
import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.gui.annotation.EditorProvider;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import java.beans.PropertyDescriptor;
import javax.swing.table.TableCellEditor;

public class JavaDataTypeEditorProvider implements EditorProvider {

  @Override
  public TableCellEditor createEditor(PropertyDescriptor pd) {
    ClassNameAutoCompletionTableCellEditor editor = new ClassNameAutoCompletionTableCellEditor(Holder.getProject(),
        Lists.newArrayList(
            byte.class.getName(),
            Byte.class.getName(),
            "byte[]",
            boolean.class.getName(),
            Boolean.class.getName(),
            char.class.getName(),
            Character.class.getName(),
            short.class.getName(),
            Short.class.getName(),
            int.class.getName(),
            Integer.class.getName(),
            long.class.getName(),
            Long.class.getName(),
            float.class.getName(),
            Float.class.getName(),
            double.class.getName(),
            Double.class.getName(),
            String.class.getName(),
            java.util.Date.class.getName(),
            java.sql.Blob.class.getName(),
            java.sql.Clob.class.getName(),
            java.sql.Date.class.getName(),
            java.sql.Time.class.getName(),
            java.sql.Timestamp.class.getName(),
            java.sql.Array.class.getName(),
            java.time.LocalTime.class.getName(),
            java.time.LocalDate.class.getName(),
            java.time.LocalDateTime.class.getName(),
            java.time.ZonedDateTime.class.getName()
        ));
    editor.addDocumentListener(new DocumentAdapter() {
      @Override
      public void documentChanged(DocumentEvent e) {

      }
    });
    return editor;
  }
}
