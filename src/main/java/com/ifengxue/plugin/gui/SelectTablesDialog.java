package com.ifengxue.plugin.gui;

import static java.util.stream.Collectors.toList;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.component.SelectTables;
import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.ColumnSchema;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.exception.TemplateNotFoundException;
import com.ifengxue.plugin.generator.config.DriverConfig;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.config.TablesConfig.ORM;
import com.ifengxue.plugin.generator.merge.SourceFileMerger;
import com.ifengxue.plugin.generator.merge.SourceFileMergerFactory;
import com.ifengxue.plugin.generator.source.ControllerSourceParser;
import com.ifengxue.plugin.generator.source.EntitySourceParserV2;
import com.ifengxue.plugin.generator.source.JpaRepositorySourceParser;
import com.ifengxue.plugin.generator.source.MapperXmlSourceParser;
import com.ifengxue.plugin.generator.source.ServiceSourceParser;
import com.ifengxue.plugin.generator.source.SimpleBeanSourceParser;
import com.ifengxue.plugin.generator.source.SourceParser;
import com.ifengxue.plugin.generator.source.VelocityEngineAware;
import com.ifengxue.plugin.gui.table.TableFactory;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.state.AutoGeneratorSettingsState;
import com.ifengxue.plugin.state.ModuleSettings;
import com.ifengxue.plugin.util.ColumnUtil;
import com.ifengxue.plugin.util.FileUtil;
import com.ifengxue.plugin.util.SourceFormatter;
import com.ifengxue.plugin.util.StringHelper;
import com.ifengxue.plugin.util.VelocityUtil;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import java.awt.event.MouseEvent;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectTablesDialog extends DialogWrapper {

  private final Logger log = Logger.getInstance(getClass());
  private final SelectTables selectTables;
  private final Function<TableSchema, List<ColumnSchema>> mapping;

  protected SelectTablesDialog(@Nullable Project project, List<Table> tables,
      Function<TableSchema, List<ColumnSchema>> mapping) {
    super(project, true);
    this.mapping = mapping;
    selectTables = new SelectTables();
    init();
    setTitle(LocaleContextHolder.format("select_database_tables"));

    // sequence
    AtomicInteger seq = new AtomicInteger(1);
    tables.sort(Comparator.comparing(Table::getTableName));
    tables.forEach(table -> table.setSequence(seq.getAndIncrement()));

    JTable table = new JBTable();
    new TableFactory().decorateTable(table, Table.class, tables);
    new TableSpeedSearch(table);
    JPanel tablePanel = ToolbarDecorator.createDecorator(table)
        .setEditAction(anActionButton -> new ColumnFieldMappingEditorDialog(project, true,
            tables.get(table.getSelectedRow()), SelectTablesDialog.this::findColumns).showAndGet())
        .createPanel();
    selectTables.getTablePanel().add(tablePanel);

    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(@Nonnull MouseEvent event) {
        if (table.getSelectedRow() == -1) {
          return false;
        }
        new ColumnFieldMappingEditorDialog(project, true,
            tables.get(table.getSelectedRow()), SelectTablesDialog.this::findColumns).showAndGet();
        return true;
      }
    }.installOn(table);

    // 已选择行数
    Runnable updateSelected = () -> {
      long selected = tables.stream()
          .filter(Table::isSelected)
          .count();
      selectTables.getLblSelectCount().setText(selected + " of " + tables.size());
    };
    updateSelected.run();

    table.getModel().addTableModelListener((e) -> updateSelected.run());

    selectTables.getBtnCancel().addActionListener(event -> dispose());
    // 选中所有行
    selectTables.getBtnSelectAll().addActionListener(event -> {
      for (Table t : tables) {
        t.setSelected(true);
      }
      table.updateUI();
      updateSelected.run();
    });
    // 全不选
    selectTables.getBtnSelectNone().addActionListener(event -> {
      for (Table t : tables) {
        t.setSelected(false);
      }
      table.updateUI();
      updateSelected.run();
    });
    // 反选
    selectTables.getBtnSelectOther().addActionListener(event -> {
      for (Table t : tables) {
        t.setSelected(!t.isSelected());
      }
      table.updateUI();
      updateSelected.run();
    });
    // 正则选择
    AtomicReference<String> initialValueRef = new AtomicReference<>(null);
    selectTables.getBtnSelectByRegex().addActionListener(event -> {
      String regex = Messages
          .showInputDialog(selectTables.getRootComponent(), LocaleContextHolder.format("select_by_regex_tip"),
              Constants.NAME, Messages.getQuestionIcon(), initialValueRef.get(),
              new InputValidator() {
                @Override
                public boolean checkInput(String inputString) {
                  if (StringUtils.isBlank(inputString)) {
                    return true;
                  }
                  try {
                    Pattern.compile(inputString);
                    return true;
                  } catch (Exception e) {
                    return false;
                  }
                }

                @Override
                public boolean canClose(String inputString) {
                  return !StringUtils.isNotBlank(inputString) || checkInput(inputString);
                }
              });
      if (StringUtils.isBlank(regex)) {
        return;
      }
      initialValueRef.set(regex);
      Pattern pattern = Pattern.compile(regex);
      Table firstSelectTable = null;
      for (Table t : tables) {
        boolean matches = pattern.matcher(t.getTableName()).matches();
        if (matches && firstSelectTable == null) {
          firstSelectTable = t;
        }
        t.setSelected(matches);
      }
      if (firstSelectTable != null) {
        table.scrollRectToVisible(table.getCellRect(firstSelectTable.getSequence() - 1, 1, true));
      }
      table.updateUI();
      updateSelected.run();
    });
    // 开始生成
    selectTables.getBtnGenerate().addActionListener(event -> {
      if (tables.stream().noneMatch(Table::isSelected)) {
        Messages.showWarningDialog(Holder.getEvent().getProject(),
            LocaleContextHolder.format("at_least_select_one_table"),
            LocaleContextHolder.format("prompt"));
        return;
      }
      new GeneratorRunner(tables, new DuplicateActionType()).run();
    });
  }

  /**
   * find columns
   */
  private List<Column> findColumns(Table table) {
    AutoGeneratorSettingsState autoGeneratorSettingsState = ServiceManager
        .getService(Holder.getOrDefaultProject(), AutoGeneratorSettingsState.class);
    List<ColumnSchema> columnSchemas = mapping.apply(table.getRawTableSchema());
    if (columnSchemas == null) {
      return Collections.emptyList();
    }
    // column schema to column
    List<Column> columns = new ArrayList<>(columnSchemas.size());
    int sequence = 1;
    for (ColumnSchema columnSchema : columnSchemas) {
      Column column = ColumnUtil.columnSchemaToColumn(columnSchema,
          autoGeneratorSettingsState.getRemoveFieldPrefix(),
          autoGeneratorSettingsState.getIfJavaKeywordAddSuffix(),
          true, autoGeneratorSettingsState.isUseJava8DateType());
      column.setSequence(sequence++);
      column.setSelected(!autoGeneratorSettingsState.getIgnoredFields().contains(column.getFieldName()));
      if (column.isPrimary()) {
        table.setPrimaryKeyClassType(column.getJavaDataType());
        table.incPrimaryKeyCount();
      }
      columns.add(column);
    }
    return columns;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return selectTables.getRootComponent();
  }

  @Override
  protected Action @NotNull [] createActions() {
    return new Action[0];
  }

  @Nullable
  @Override
  protected String getDimensionServiceKey() {
    return Constants.NAME + ":" + getClass().getName();
  }

  public static void show(List<Table> tableList, Function<TableSchema, List<ColumnSchema>> mapping) {
    new SelectTablesDialog(Holder.getProject(), tableList, mapping).show();
  }

  private class DuplicateActionType {

    boolean isIgnore() {
      return selectTables.getDuplicateActionIgnoreButton().isSelected();
    }

    boolean isRewrite() {
      return selectTables.getDuplicateActionRewriteButton().isSelected();
    }

    boolean isMerge() {
      return selectTables.getDuplicateActionMergeButton().isSelected();
    }
  }

  /**
   * 生成器
   */
  private class GeneratorRunner implements Runnable {

    private final List<Table> tableList;
    private final DuplicateActionType duplicateActionType;

    public GeneratorRunner(List<Table> tableList, DuplicateActionType duplicateActionType) {
      this.tableList = Collections
          .unmodifiableList(tableList.stream()
              .filter(Table::isSelected)
              .collect(toList()));
      this.duplicateActionType = duplicateActionType;
    }

    @Override
    public void run() {
      AnActionEvent event = Holder.getEvent();
      Project project = event.getProject();
      assert project != null;

      AutoGeneratorSettingsState autoGeneratorSettingsState = ServiceManager
          .getService(Holder.getOrDefaultProject(), AutoGeneratorSettingsState.class);
      ModuleSettings moduleSettings = autoGeneratorSettingsState.getModuleSettings();

      List<GeneratorTask> tasks = buildTask(moduleSettings);

      CountDownLatch isReadForWrite = new CountDownLatch(1);
      ApplicationManager.getApplication().runWriteAction(() -> {
        Object[][] directoryAndPackageNames = {
            {
                moduleSettings.isGenerateEntity(),
                moduleSettings.getEntityParentDirectory(),
                moduleSettings.getEntityPackageName()
            },
            {
                moduleSettings.isGenerateRepository(),
                moduleSettings.getRepositoryParentDirectory(),
                moduleSettings.getRepositoryPackageName()
            },
            {
                moduleSettings.isGenerateController(),
                moduleSettings.getControllerParentDirectory(),
                moduleSettings.getControllerPackageName()
            },
            {
                moduleSettings.isGenerateMapperXml(),
                moduleSettings.getMapperXmlParentDirectory(),
                moduleSettings.getMapperXmlPackageName()
            },
            {
                moduleSettings.isGenerateService(),
                moduleSettings.getServiceParentDirectory(),
                moduleSettings.getServicePackageName()
            },
            {
                moduleSettings.isGenerateVO(),
                moduleSettings.getVoParentDirectory(),
                moduleSettings.getVoPackageName()
            },
            {
                moduleSettings.isGenerateDTO(),
                moduleSettings.getDtoParentDirectory(),
                moduleSettings.getDtoPackageName()
            },
        };
        for (Object[] directoryAndPackageName : directoryAndPackageNames) {
          if (!(boolean) directoryAndPackageName[0]) {
            continue;
          }
          FileUtil.mkdirs(PsiManager.getInstance(project),
              Paths.get((String) directoryAndPackageName[1],
                  StringHelper.packageNameToFolder((String) directoryAndPackageName[2])));
        }
        isReadForWrite.countDown();
      });

      AtomicInteger leftGenerateCount = new AtomicInteger(tableList.size());
      for (Table table : tableList) {
        table.setPackageName(moduleSettings.getEntityPackageName());
        if (table.getColumns() == null) {
          table.setColumns(findColumns(table));
        }
        List<Column> allColumns = table.getColumns();
        table.setAllColumns(allColumns);
        
        List<Column> columns = allColumns
            .stream()
            .filter(Column::isSelected)
            .collect(toList());
        table.setColumns(columns);

        // configure source code generator config
        GeneratorConfig generatorConfig = new GeneratorConfig();
        generatorConfig.setDriverConfig(new DriverConfig());
        generatorConfig.setFileExtension(moduleSettings.getFileExtension());
        int lastIndex;
        String basePackageName = moduleSettings.getEntityPackageName();
        if ((lastIndex = basePackageName.lastIndexOf('.')) != -1) {
          basePackageName = basePackageName.substring(0, lastIndex);
        }
        generatorConfig.setTablesConfig(new TablesConfig()
            .setBasePackageName(basePackageName)
            .setEntityPackageName(moduleSettings.getEntityPackageName())
            .setEnumSubPackageName(basePackageName + ".enums")
            .setControllerPackageName(moduleSettings.getControllerPackageName())
            .setServicePackageName(moduleSettings.getServicePackageName())
            .setVoSuffixName(moduleSettings.getVoSuffixName())
            .setVoPackageName(moduleSettings.getVoPackageName())
            .setDtoSuffixName(moduleSettings.getDtoSuffixName())
            .setDtoPackageName(moduleSettings.getDtoPackageName())
            .setIndent(getIndent())
            .setLineSeparator(getLineSeparator())
            .setOrm(ORM.JPA)
            .setExtendsEntityName(autoGeneratorSettingsState.getInheritedParentClassName())
            .setRemoveTablePrefix(autoGeneratorSettingsState.getRemoveEntityPrefix())
            .setRemoveFieldPrefix(autoGeneratorSettingsState.getRemoveFieldPrefix())
            .setRepositoryPackageName(moduleSettings.getRepositoryPackageName())
            .setSerializable(autoGeneratorSettingsState.isSerializable())
            .setUseClassComment(autoGeneratorSettingsState.isGenerateClassComment())
            .setUseFieldComment(autoGeneratorSettingsState.isGenerateFieldComment())
            .setUseMethodComment(autoGeneratorSettingsState.isGenerateMethodComment())
            .setUseDefaultValue(autoGeneratorSettingsState.isGenerateDefaultValue())
            .setUseDefaultDatetimeValue(autoGeneratorSettingsState.isGenerateDatetimeDefaultValue())
            .setUseWrapper(true)
            .setUseLombok(autoGeneratorSettingsState.isUseLombok())
            .setUseJava8DateType(autoGeneratorSettingsState.isUseJava8DateType())
            .setUseFluidProgrammingStyle(autoGeneratorSettingsState.isUseFluidProgrammingStyle())
            .setUseSwaggerUIComment(autoGeneratorSettingsState.isGenerateSwaggerUIComment())
            .setUseJpaAnnotation(autoGeneratorSettingsState.isGenerateJpaAnnotation())
            .setAddSchemeNameToTableName(autoGeneratorSettingsState.isAddSchemaNameToTableName())
            .setUseJakartaEE(autoGeneratorSettingsState.isUseJakartaEE())
            .setUseJpa(moduleSettings.isRepositoryTypeJPA())
            .setUseMybatisPlus(moduleSettings.isRepositoryTypeMybatisPlus())
            .setUseTkMybatis(moduleSettings.isRepositoryTypeTkMybatis())
        );
        generatorConfig.setPluginConfigs(Collections.emptyList());
        WriteCommandAction.runWriteCommandAction(project, () -> {
          try {
            isReadForWrite.await();

            if (StringUtils.isBlank(table.getServiceName())) {
              table.setServiceName(table.getEntityName() + "Service");
            }
            if (StringUtils.isBlank(table.getControllerName())) {
              table.setControllerName(table.getEntityName() + "Controller");
            }
            for (GeneratorTask task : tasks) {
              if (!task.shouldRun) {
                continue;
              }
              String sourceCode = task.sourceParser.parse(generatorConfig, table);
              writeContent(generatorConfig, table,
                  project, task.filenameMapping.apply(table),
                  task.directory, task.packageName, sourceCode);
            }
          } catch (TemplateNotFoundException e) {
            Bus.notify(
                new Notification(
                    Constants.GROUP_ID, "Template Not found",
                    "Generate source code error. " + e,
                    NotificationType.WARNING), project
            );
            Logger.getInstance(getClass()).warn(e.getMessage(), e);
          } catch (Exception e) {
            Bus.notify(
                new Notification(Constants.GROUP_ID, "Error", "Generate source code error. " + e,
                    NotificationType.ERROR), project);
            Logger.getInstance(getClass()).warn("Generate source code error", e);
          } finally {
            if (leftGenerateCount.decrementAndGet() == 0) {
              ApplicationManager.getApplication().invokeLater(
                  () -> Messages
                      .showInfoMessage(LocaleContextHolder.format("generate_source_code_success"),
                          Constants.NAME));
            }
          }
        });
      }
    }

    private List<GeneratorTask> buildTask(ModuleSettings moduleSettings) {
      Function<String, SourceParser> templateIdToSourceParserMapping = templateId -> {
        SimpleBeanSourceParser parser = new SimpleBeanSourceParser();
        parser.setTemplateId(templateId);
        return parser;
      };
      String fileExtension = "." + moduleSettings.getFileExtension();
      List<GeneratorTask> tasks = Arrays.asList(
          GeneratorTask.builder()
              .shouldRun(moduleSettings.isGenerateEntity())
              .sourceParser(new EntitySourceParserV2())
              .directory(moduleSettings.getEntityParentDirectory())
              .packageName(moduleSettings.getEntityPackageName())
              .filenameMapping(t -> t.getEntityName() + fileExtension)
              .build(),
          GeneratorTask.builder()
              .shouldRun(moduleSettings.isGenerateRepository())
              .sourceParser(new JpaRepositorySourceParser())
              .directory(moduleSettings.getRepositoryParentDirectory())
              .packageName(moduleSettings.getRepositoryPackageName())
              .filenameMapping(t -> t.getRepositoryName() + fileExtension)
              .build(),
          GeneratorTask.builder()
              .shouldRun(moduleSettings.isGenerateController())
              .sourceParser(new ControllerSourceParser())
              .directory(moduleSettings.getControllerParentDirectory())
              .packageName(moduleSettings.getControllerPackageName())
              .filenameMapping(t -> StringUtils.firstNonBlank(t.getControllerName() + fileExtension,
                  t.getEntityName() + "Controller" + fileExtension))
              .build(),
          GeneratorTask.builder()
              .shouldRun(moduleSettings.isGenerateService())
              .sourceParser(new ServiceSourceParser())
              .directory(moduleSettings.getServiceParentDirectory())
              .packageName(moduleSettings.getServicePackageName())
              .filenameMapping(t -> StringUtils.firstNonBlank(t.getServiceName() + fileExtension,
                  t.getEntityName() + "Service" + fileExtension))
              .build(),
          GeneratorTask.builder()
              .shouldRun(moduleSettings.isGenerateMapperXml())
              .sourceParser(new MapperXmlSourceParser())
              .directory(moduleSettings.getMapperXmlParentDirectory())
              .packageName(moduleSettings.getMapperXmlPackageName())
              .filenameMapping(t -> t.getRepositoryName() + ".xml")
              .build(),
          GeneratorTask.builder()
              .shouldRun(moduleSettings.isGenerateVO())
              .sourceParser(templateIdToSourceParserMapping.apply(Constants.SAVE_VO_TEMPLATE_ID))
              .directory(moduleSettings.getVoParentDirectory())
              .packageName(moduleSettings.getVoPackageName())
              .filenameMapping(
                  t -> t.getEntityName() + moduleSettings.getVoSuffixName() + fileExtension)
              .build(),
          GeneratorTask.builder()
              .shouldRun(moduleSettings.isGenerateVO())
              .sourceParser(templateIdToSourceParserMapping.apply(Constants.UPDATE_VO_TEMPLATE_ID))
              .directory(moduleSettings.getVoParentDirectory())
              .packageName(moduleSettings.getVoPackageName())
              .filenameMapping(t -> t.getEntityName() + "Update" + moduleSettings.getVoSuffixName()
                  + fileExtension)
              .build(),
          GeneratorTask.builder()
              .shouldRun(moduleSettings.isGenerateVO())
              .sourceParser(templateIdToSourceParserMapping.apply(Constants.QUERY_VO_TEMPLATE_ID))
              .directory(moduleSettings.getVoParentDirectory())
              .packageName(moduleSettings.getVoPackageName())
              .filenameMapping(t -> t.getEntityName() + "Query" + moduleSettings.getVoSuffixName()
                  + fileExtension)
              .build(),
          GeneratorTask.builder()
              .shouldRun(moduleSettings.isGenerateDTO())
              .sourceParser(templateIdToSourceParserMapping.apply(Constants.DTO_TEMPLATE_ID))
              .directory(moduleSettings.getDtoParentDirectory())
              .packageName(moduleSettings.getDtoPackageName())
              .filenameMapping(
                  t -> t.getEntityName() + moduleSettings.getDtoSuffixName() + fileExtension)
              .build()
      );
      for (GeneratorTask task : tasks) {
        if (task.sourceParser instanceof VelocityEngineAware) {
          ((VelocityEngineAware) task.sourceParser)
              .setVelocityEngine(VelocityUtil.getInstance(), "UTF-8");
        }
      }
      return tasks;
    }

    private String getIndent() {
      CodeStyleSettingsManager codeStyleSettingsManager = CodeStyleSettingsManager
          .getInstance(Holder.getProject());
      IndentOptions indentOptions = Optional.of(codeStyleSettingsManager.getCurrentSettings())
          .map(css -> css.getIndentOptions(JavaFileType.INSTANCE))
          .orElseGet(() -> CodeStyleSettings.getDefaults().getIndentOptions(JavaFileType.INSTANCE));
      String indent;
      if (indentOptions.USE_TAB_CHARACTER) {
        indent = indentOptions.TAB_SIZE <= 1 ? "tab" : indentOptions.TAB_SIZE + "tab";
      } else {
        indent = indentOptions.TAB_SIZE + "space";
      }
      return indent;
    }

    private String getLineSeparator() {
      CodeStyleSettingsManager codeStyleSettingsManager = CodeStyleSettingsManager
          .getInstance(Holder.getProject());
      return Optional.of(codeStyleSettingsManager.getCurrentSettings())
          .map(CodeStyleSettings::getLineSeparator)
          .orElseGet(() -> CodeStyleSettings.getDefaults().getLineSeparator());
    }

    private FileType guessFileType(String filename) {
      if (filename.endsWith(".xml")) {
        return XmlFileType.INSTANCE;
      } else {
        return JavaFileType.INSTANCE;
      }
    }

    private void writeContent(GeneratorConfig generatorConfig, Table table, Project project,
        String filename, String parentPath,
        String packageName, String sourceCode) {
      PsiDirectory psiDirectory = FileUtil.mkdirs(PsiManager.getInstance(project),
          Paths.get(parentPath, StringHelper.packageNameToFolder(packageName)));
      PsiFile originalFile = psiDirectory.findFile(filename);
      if (originalFile != null && duplicateActionType.isIgnore()) {
        log.info("Ignore exists file " + filename);
        return;
      }

      FileType fileType = guessFileType(filename);
      PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
      PsiFile psiFile = psiFileFactory.createFileFromText(filename, fileType, sourceCode);
      if (originalFile != null) {
        if (duplicateActionType.isMerge()) {
          SourceFileMerger merger = SourceFileMergerFactory.createMerger(fileType);
          if (merger != null) {
            merger.tryMerge(generatorConfig, table, originalFile, psiFile);
            log.info("Try merge exists file " + filename);
          } else {
            log.warn("File type " + fileType.getName() + " not support to merged");
          }
        } else {
          Document document = PsiDocumentManager.getInstance(project).getDocument(originalFile);
          assert document != null;
          document.replaceString(0, document.getTextLength(), psiFile.getText());
          log.info("Try rewrite exits file " + filename);
        }
        psiFile = originalFile;
      }

      Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
      if (document != null) {
        PsiDocumentManager.getInstance(project).commitDocument(document);
      }

      SourceFormatter.format(project, psiFile, fileType);

      if (document != null) {
        PsiDocumentManager.getInstance(project).commitDocument(document);
      } else {
        // The new file needs to be saved last, otherwise the formatting will not take effect
        psiDirectory.add(psiFile);
      }
    }
  }

  @Builder
  private static class GeneratorTask {

    private final boolean shouldRun;
    private final SourceParser sourceParser;
    private final String directory;
    private final String packageName;
    private final Function<Table, String> filenameMapping;

  }
}
