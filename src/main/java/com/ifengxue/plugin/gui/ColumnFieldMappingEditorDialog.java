package com.ifengxue.plugin.gui;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.component.ColumnFieldMappingEditor;
import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.gui.table.TableFactory;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import java.util.List;
import java.util.function.Function;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ColumnFieldMappingEditorDialog extends DialogWrapper {

  private final ColumnFieldMappingEditor columnFieldMappingEditor;
  private final Table table;

  protected ColumnFieldMappingEditorDialog(@Nullable Project project, boolean canBeParent,
      Table table, Function<Table, List<Column>> columnsMapping) {
    super(project, canBeParent);
    this.table = table;
    columnFieldMappingEditor = new ColumnFieldMappingEditor();
    init();
    setTitle(LocaleContextHolder.format("column_field_mapping_title"));

    if (table.getColumns() == null) {
      table.setColumns(columnsMapping.apply(table));
    }

    JBTable jbTable = new JBTable();
    new TableFactory().decorateTable(jbTable, Column.class, this.table.getColumns());
    JPanel tablePanel = ToolbarDecorator.createDecorator(jbTable).createPanel();
    columnFieldMappingEditor.getTablePanel().add(tablePanel);
    columnFieldMappingEditor.setData(table);
  }

  @Override
  protected void doOKAction() {
    columnFieldMappingEditor.getData(table);
    close(CLOSE_EXIT_CODE);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return columnFieldMappingEditor.getRootComponent();
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
}
