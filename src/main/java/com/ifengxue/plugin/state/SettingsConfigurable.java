package com.ifengxue.plugin.state;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.component.Settings;
import com.ifengxue.plugin.component.TemplateItem;
import com.ifengxue.plugin.entity.TypeMapping;
import com.ifengxue.plugin.generator.source.AbstractSourceParser;
import com.ifengxue.plugin.generator.source.EntitySourceParserV2;
import com.ifengxue.plugin.generator.source.JpaRepositorySourceParser;
import com.ifengxue.plugin.generator.source.JpaServiceSourceParser;
import com.ifengxue.plugin.gui.SourceCodeViewerDialog;
import com.ifengxue.plugin.gui.TypeEditorDialog;
import com.ifengxue.plugin.gui.table.TableFactory;
import com.ifengxue.plugin.gui.table.TableFactory.MyTableModel;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.state.wrapper.ClassWrapper;
import com.ifengxue.plugin.util.TestTemplateHelper;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.xmlb.annotations.Transient;
import java.awt.event.ItemEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import lombok.Data;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class SettingsConfigurable implements SearchableConfigurable {

  private static final Logger log = Logger.getInstance(SettingsConfigurable.class);
  @Transient
  private Settings settings;
  @Transient
  private JBTable typeMappingTable;
  private SettingsState settingsState;

  @NotNull
  @Override
  public String getId() {
    return Constants.NAME;
  }

  @Nls(capitalization = Capitalization.Title)
  @Override
  public String getDisplayName() {
    return getId();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void reset() {
    settingsState = ServiceManager.getService(SettingsState.class);

    JTabbedPane tabbedPane = settings.getTabbedPane();
    tabbedPane.setTitleAt(0, LocaleContextHolder.format("source_code_template_tip"));
    tabbedPane.setTitleAt(1, LocaleContextHolder.format("db_java_type_tip"));

    // template config

    Map<String, Class<? extends AbstractSourceParser>> templateIdToParserClass = new LinkedHashMap<>();
    templateIdToParserClass.put(Constants.JPA_ENTITY_TEMPLATE_ID, EntitySourceParserV2.class);
    templateIdToParserClass.put(Constants.JPA_REPOSITORY_TEMPLATE_ID, JpaRepositorySourceParser.class);
    templateIdToParserClass.put(Constants.JPA_Service_TEMPLATE_ID, JpaServiceSourceParser.class);
    settings.getCbxSelectCodeTemplate().removeAllItems();
    templateIdToParserClass.forEach((templateId, parserClass) -> {
      settings.getCbxSelectCodeTemplate().addItem(new TemplateItem()
          .setId(templateId)
          .setName(templateId.replace("template/", ""))
          .setTemplate(settingsState.loadTemplate(templateId))
          .setSourceParseClass(parserClass)
      );
    });
    settings.getCbxSelectCodeTemplate().addItemListener(event -> {
      if (event.getStateChange() == ItemEvent.SELECTED) {
        TemplateItem item = (TemplateItem) event.getItem();
        settings.getTxtSourceCode().setText(item.getTemplate());
      }
    });
    settings.getCbxSelectCodeTemplate().setSelectedIndex(0);
    settings.getTxtSourceCode().setText(settings.getCbxSelectCodeTemplate().getItemAt(0).getTemplate());
    settings.getBtnResetTemplate().addActionListener(event -> {
      TemplateItem item = settings.getCbxSelectCodeTemplate()
          .getItemAt(settings.getCbxSelectCodeTemplate().getSelectedIndex());
      item.setTemplate(settingsState.forceLoadTemplate(item.getId()));
      settings.getTxtSourceCode().setText(item.getTemplate());
    });
    settings.getBtnTestTemplate().addActionListener(event -> {
      SourceCodeViewerDialog dialog = new SourceCodeViewerDialog(ProjectManager.getInstance().getDefaultProject(),
          false);
      dialog.setSourceCode(TestTemplateHelper.evaluateToString(settings.getCbxSelectCodeTemplate()
              .getItemAt(settings.getCbxSelectCodeTemplate().getSelectedIndex()).getSourceParseClass(),
          settings.getTxtSourceCode().getText()));
      dialog.show();
    });
    settings.getTxtSourceCode().addDocumentListener(new DocumentListener() {
      @Override
      public void documentChanged(@Nonnull DocumentEvent e) {
        TemplateItem item = settings.getCbxSelectCodeTemplate()
            .getItemAt(settings.getCbxSelectCodeTemplate().getSelectedIndex());
        item.setTemplate(e.getDocument().getText());
      }
    });

    // type config
    settings.getTextFallbackType().setPreferredWidth(200);
    settings.getRadioBtnFallbackType().addItemListener(
        event -> settings.getTextFallbackType().setEnabled(event.getStateChange() == ItemEvent.SELECTED));

    if (settingsState.getDbTypeToJavaType() == null) {
      settingsState.resetTypeMapping();
    }
    if (typeMappingTable == null) {
      typeMappingTable = new JBTable();
      typeMappingTable.setAutoCreateRowSorter(true);

      new TableFactory().decorateTable(typeMappingTable, TypeMapping.class,
          TypeMapping.from(settingsState.getDbTypeToJavaType()));
      MyTableModel<TypeMapping> tableModel = (MyTableModel<TypeMapping>) typeMappingTable.getModel();
      JPanel tablePanel = ToolbarDecorator.createDecorator(typeMappingTable)
          .setAddAction(anActionButton -> {
            TypeEditorDialog dialog = new TypeEditorDialog(null, null, tableModel.getRows());
            if (dialog.showAndGet()) {
              tableModel.addRow(new TypeMapping()
                  .setDbColumnType(dialog.getDbType())
                  .setJavaType(dialog.getJavaType()));
            }
          })
          .setEditAction(anActionButton -> {
            int selectedRow = typeMappingTable.getSelectedRow();
            int realRowIndex = typeMappingTable.convertRowIndexToModel(selectedRow);
            TypeMapping typeMapping = tableModel.getRow(realRowIndex);
            TypeEditorDialog dialog = new TypeEditorDialog(null, typeMapping, tableModel.getRows());
            if (dialog.showAndGet()) {
              tableModel.updateRow(new TypeMapping()
                  .setDbColumnType(dialog.getDbType())
                  .setJavaType(dialog.getJavaType()), realRowIndex);
            }
          })
          .setRemoveAction(anActionButton -> {
            int[] selectedRows = typeMappingTable.getSelectedRows();
            if (selectedRows.length == 0) {
              return;
            }
            for (int index = selectedRows.length - 1; index >= 0; index--) {
              log.info("db type is " + typeMappingTable.getValueAt(selectedRows[index], 0));
              (tableModel)
                  .removeRow(typeMappingTable.convertRowIndexToModel(selectedRows[index]));
            }
            typeMappingTable.updateUI();
          })
          .createPanel();
      settings.getTypeMappingTablePane().add(tablePanel);
    } else {
      ((MyTableModel<TypeMapping>) typeMappingTable.getModel())
          .resetRows(TypeMapping.from(settingsState.getDbTypeToJavaType()));
    }

    // bind data
    settings.setData(settingsState);
  }

  @Override
  public void disposeUIResources() {
    settings = null;
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    settings = new Settings();
    return settings.getRootComponent();
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean isModified() {
    if (settings.isModified(settingsState)) {
      return true;
    }
    List<TypeMapping> rows = ((MyTableModel<TypeMapping>) typeMappingTable.getModel()).getRows();
    if (rows.size() != settingsState.getDbTypeToJavaType().size()) {
      return true;
    }
    for (TypeMapping row : rows) {
      ClassWrapper classWrapper = settingsState.getDbTypeToJavaType().get(row.getDbColumnType());
      if (classWrapper == null || classWrapper.getClazz() != row.getJavaType()) {
        return true;
      }
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void apply() {
    try {
      settings.getData(settingsState);
    } catch (ClassNotFoundException e) {
      log.error("apply data error", e);
    }
    List<TypeMapping> rows = ((MyTableModel<TypeMapping>) typeMappingTable.getModel()).getRows();
    settingsState.setDbTypeToJavaType(rows.stream()
        .collect(Collectors.toMap(TypeMapping::getDbColumnType, tm -> new ClassWrapper(tm.getJavaType()))));
  }

}
