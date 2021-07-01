package com.ifengxue.plugin.gui;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.TemplateManager;
import com.ifengxue.plugin.component.ColumnFieldMappingEditor;
import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.source.ControllerSourceParser;
import com.ifengxue.plugin.generator.source.EntitySourceParserV2;
import com.ifengxue.plugin.generator.source.JpaRepositorySourceParser;
import com.ifengxue.plugin.generator.source.ServiceSourceParser;
import com.ifengxue.plugin.gui.table.TableFactory;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.state.AutoGeneratorSettingsState;
import com.ifengxue.plugin.state.ModuleSettings;
import com.ifengxue.plugin.util.TestTemplateHelper;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import java.awt.event.ActionEvent;
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
  private final Project project;

  protected ColumnFieldMappingEditorDialog(@Nullable Project project, boolean canBeParent,
      Table table, Function<Table, List<Column>> columnsMapping) {
    super(project, canBeParent);
    this.project = project;
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

    // 已选择行数
    Runnable updateSelected = () -> {
      long selected = table.getColumns()
          .stream()
          .filter(Column::isSelected)
          .count();
      columnFieldMappingEditor.getLblSelectCount().setText(selected + " of " + table.getColumns().size());
    };
    updateSelected.run();
    jbTable.getModel().addTableModelListener(e -> updateSelected.run());
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

  @Override
  protected Action @NotNull [] createActions() {
    return new Action[]{getOKAction()};
  }

  @Override
  protected Action @NotNull [] createLeftSideActions() {

    return new Action[]{
        new PreviewAction(LocaleContextHolder.format("preview_bean"), () -> {
          String template = TemplateManager.getInstance()
              .loadTemplate(Constants.JPA_ENTITY_TEMPLATE_ID);
          String sourceCode = TestTemplateHelper
              .evaluateToString(EntitySourceParserV2.class, table, template);
          SourceCodeViewerDialog dialog = new SourceCodeViewerDialog(project, true);
          dialog.setSourceCode(sourceCode);
          dialog.show();
        }),
        new PreviewAction(LocaleContextHolder.format("preview_repository"), () -> {
          String template = TemplateManager.getInstance()
              .loadTemplate(Constants.JPA_REPOSITORY_TEMPLATE_ID);
          String sourceCode = TestTemplateHelper
              .evaluateToString(JpaRepositorySourceParser.class, table, template);
          SourceCodeViewerDialog dialog = new SourceCodeViewerDialog(project, true);
          dialog.setSourceCode(sourceCode);
          dialog.show();
        }),
        new PreviewAction(LocaleContextHolder.format("preview_controller"), () -> {
          String template = TemplateManager.getInstance()
              .loadTemplate(Constants.CONTROLLER_TEMPLATE_ID);
          String sourceCode = TestTemplateHelper
              .evaluateToString(ControllerSourceParser.class, table, template);
          SourceCodeViewerDialog dialog = new SourceCodeViewerDialog(project, true);
          dialog.setSourceCode(sourceCode);
          dialog.show();
        }),
        new PreviewAction(LocaleContextHolder.format("preview_service"), () -> {
          AutoGeneratorSettingsState state = ServiceManager
              .getService(project, AutoGeneratorSettingsState.class);
          ModuleSettings moduleSettings = state.getModuleSettings(state.getModuleName());
          String templateId = Constants.JPA_SERVICE_TEMPLATE_ID;
          if (moduleSettings.isRepositoryTypeMybatisPlus()) {
            templateId = Constants.MYBATIS_PLUS_SERVICE_TEMPLATE_ID;
          } else if (moduleSettings.isRepositoryTypeTkMybatis()) {
            templateId = Constants.TK_MYBATIS_SERVICE_TEMPLATE_ID;
          }
          String template = TemplateManager.getInstance().loadTemplate(templateId);
          String sourceCode = TestTemplateHelper
              .evaluateToString(ServiceSourceParser.class, table, template);
          SourceCodeViewerDialog dialog = new SourceCodeViewerDialog(project, true);
          dialog.setSourceCode(sourceCode);
          dialog.show();
        }),
    };
  }

  @Nullable
  @Override
  protected String getDimensionServiceKey() {
    return Constants.NAME + ":" + getClass().getName();
  }

  protected class PreviewAction extends DialogWrapper.DialogWrapperAction {

    private static final long serialVersionUID = 4464807636902829651L;
    private final Runnable runnable;

    protected PreviewAction(String name, Runnable runnable) {
      super(name);
      this.runnable = runnable;
    }

    @Override
    protected void doAction(ActionEvent actionEvent) {
      runnable.run();
    }
  }
}
