package com.ifengxue.plugin.gui;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.component.SelectTables;
import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.ColumnSchema;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.generator.config.DriverConfig;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.generator.config.TablesConfig.ORM;
import com.ifengxue.plugin.generator.source.EntitySourceParserV2;
import com.ifengxue.plugin.generator.source.JpaRepositorySourceParser;
import com.ifengxue.plugin.gui.table.TableFactory;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.state.AutoGeneratorSettingsState;
import com.ifengxue.plugin.state.ModuleSettings;
import com.ifengxue.plugin.util.FileUtil;
import com.ifengxue.plugin.util.StringHelper;
import com.ifengxue.plugin.util.VelocityUtil;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

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
    setSize(600, 600);
    setTitle(LocaleContextHolder.format("select_database_tables"));

    // sequence
    AtomicInteger seq = new AtomicInteger(1);
    tables.forEach(table -> table.setSequence(seq.getAndIncrement()));

    JTable table = new JBTable();
    new TableFactory().decorateTable(table, Table.class, tables);
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

    selectTables.getBtnCancel().addActionListener(event -> dispose());
    // 选中所有行
    selectTables.getBtnSelectAll().addActionListener(event -> {
      for (Table t : tables) {
        t.setSelected(true);
      }
      table.updateUI();
    });
    // 全不选
    selectTables.getBtnSelectNone().addActionListener(event -> {
      for (Table t : tables) {
        t.setSelected(false);
      }
      table.updateUI();
    });
    // 反选
    selectTables.getBtnSelectOther().addActionListener(event -> {
      for (Table t : tables) {
        t.setSelected(!t.isSelected());
      }
      table.updateUI();
    });
    // 正则选择
    AtomicReference<String> initialValueRef = new AtomicReference<>(null);
    selectTables.getBtnSelectByRegex().addActionListener(event -> {
      String regex = Messages
          .showInputDialog(selectTables.getRootComponent(), LocaleContextHolder.format("select_by_regex_tip"),
              "JpaSupport", Messages.getQuestionIcon(), initialValueRef.get(),
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
      for (Table t : tables) {
        t.setSelected(pattern.matcher(t.getTableName()).matches());
      }
      table.updateUI();
    });
    // 开始生成
    selectTables.getBtnGenerate().addActionListener(event -> {
      if (tables.stream().noneMatch(Table::isSelected)) {
        Messages.showWarningDialog(Holder.getEvent().getProject(),
            LocaleContextHolder.format("at_least_select_one_table"),
            LocaleContextHolder.format("prompt"));
        return;
      }
      // 开始生成
      new GeneratorRunner(tables).run();
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
    // 解析字段列表
    List<Column> columns = new ArrayList<>(columnSchemas.size());
    for (ColumnSchema columnSchema : columnSchemas) {
      Column column = Holder.getDatabaseDrivers().getDriverAdapter().parseToColumn(columnSchema,
          autoGeneratorSettingsState.getRemoveFieldPrefix(), true,
          autoGeneratorSettingsState.isUseJava8DateType());
      if (column.isPrimary()) {
        table.setPrimaryKeyClassType(column.getJavaDataType());
        table.incPrimaryKeyCount();
      }
      if (!autoGeneratorSettingsState.getIgnoredFields().contains(column.getFieldName())) {
        columns.add(column);
      }
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

  /**
   * 生成器
   */
  private class GeneratorRunner implements Runnable {

    private final List<Table> tableList;

    public GeneratorRunner(List<Table> tableList) {
      this.tableList = Collections
          .unmodifiableList(tableList.stream()
              .filter(Table::isSelected)
              .collect(toList()));
    }

    @Override
    public void run() {
      AnActionEvent event = Holder.getEvent();
      Project project = event.getProject();
      assert project != null;

      String encoding = StandardCharsets.UTF_8.name();
      JpaRepositorySourceParser repositorySourceParser = new JpaRepositorySourceParser();
      repositorySourceParser.setVelocityEngine(VelocityUtil.getInstance(), encoding);

      EntitySourceParserV2 sourceParser = new EntitySourceParserV2();
      sourceParser.setVelocityEngine(VelocityUtil.getInstance(), encoding);

      AutoGeneratorSettingsState autoGeneratorSettingsState = ServiceManager
          .getService(Holder.getOrDefaultProject(), AutoGeneratorSettingsState.class);
      ModuleSettings moduleSettings = autoGeneratorSettingsState.getModuleSettings();

      // create entity directory
      PsiDirectory entityDirectory = FileUtil.mkdirs(PsiManager.getInstance(project),
          Paths.get(moduleSettings.getEntityParentDirectory(),
              StringHelper.packageNameToFolder(moduleSettings.getEntityPackageName())));
      PsiDirectory repositoryDirectory = null;
      // create repository dir
      if (autoGeneratorSettingsState.isGenerateRepository()) {
        repositoryDirectory = FileUtil.mkdirs(PsiManager.getInstance(project),
            Paths.get(moduleSettings.getRepositoryParentDirectory(),
                StringHelper.packageNameToFolder(moduleSettings.getRepositoryPackageName())));
      }
      Map<String, Boolean> filenameToOverride = awaitFilenameToOverride(autoGeneratorSettingsState,
          entityDirectory, repositoryDirectory);
      for (Table table : tableList) {
        table.setPackageName(moduleSettings.getEntityPackageName());
        if (table.getColumns() == null) {
          table.setColumns(findColumns(table));
        }

        // configure source code generator config
        GeneratorConfig generatorConfig = new GeneratorConfig();
        generatorConfig.setDriverConfig(new DriverConfig()
            .setVendor(Holder.getDatabaseDrivers().getVendor2()));
        int lastIndex;
        String basePackageName = moduleSettings.getEntityPackageName();
        if ((lastIndex = basePackageName.lastIndexOf('.')) != -1) {
          basePackageName = basePackageName.substring(0, lastIndex);
        }
        generatorConfig.setTablesConfig(new TablesConfig()
            .setBasePackageName(basePackageName)
            .setEntityPackageName(moduleSettings.getEntityPackageName())
            .setEnumSubPackageName(basePackageName + ".enums")
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
        );
        generatorConfig.setPluginConfigs(Collections.emptyList());
        String sourceCode = sourceParser.parse(generatorConfig, table);
        WriteCommandAction.runWriteCommandAction(project, () -> {
          String fileExtension = ".java";
          String filename = table.getEntityName() + fileExtension;
          try {
            writeContent(project, filename, moduleSettings.getEntityParentDirectory(),
                moduleSettings.getEntityPackageName(), sourceCode,
                filenameToOverride.get(filename));
            if (autoGeneratorSettingsState.isGenerateRepository()) {
              filename = table.getRepositoryName() + fileExtension;
              String repositorySourceCode = repositorySourceParser.parse(generatorConfig, table);
              writeContent(project, filename, moduleSettings.getRepositoryParentDirectory(),
                  moduleSettings.getRepositoryPackageName(),
                  repositorySourceCode, filenameToOverride.get(filename));
            }
          } catch (Exception e) {
            Bus.notify(
                new Notification(Constants.GROUP_ID, "Error", "Generate source code error. " + e, NotificationType.ERROR),
                project);
          }
        });
      }
    }

    private Map<String, Boolean> awaitFilenameToOverride(AutoGeneratorSettingsState settingsState,
                                                         PsiDirectory entityDirectory, PsiDirectory repositoryDirectory) {
      Map<String, Boolean> filenameToOverwrite = new HashMap<>();
      String[] filenames = new String[2];
      PsiDirectory[] directories = new PsiDirectory[2];
      for (Table table : tableList) {
        String fileExtension = ".java";
        filenames[0] = table.getEntityName() + fileExtension;
        directories[0] = entityDirectory;
        if (settingsState.isGenerateRepository()) {
          filenames[1] = table.getRepositoryName() + fileExtension;
          directories[1] = repositoryDirectory;
        }
        for (int i = 0; i < filenames.length; i++) {
          String filename = filenames[i];
          PsiDirectory directory = directories[i];
          if (filename != null) {
            PsiFile psiFile = directory.findFile(filename);
            if (psiFile != null) {
              int selectButton = Messages
                  .showOkCancelDialog(
                      LocaleContextHolder.format("file_already_exists_overwritten", filename),
                      LocaleContextHolder.format("prompt"),
                      Messages.OK_BUTTON,
                      Messages.CANCEL_BUTTON,
                      Messages.getQuestionIcon());
              filenameToOverwrite.put(filename, selectButton == Messages.OK);
            } else {
              filenameToOverwrite.put(filename, Boolean.TRUE);
            }
          }
        }
      }
      return filenameToOverwrite;
    }

    private String getIndent() {
      CodeStyleSettingsManager codeStyleSettingsManager = CodeStyleSettingsManager.getInstance(Holder.getProject());
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
      CodeStyleSettingsManager codeStyleSettingsManager = CodeStyleSettingsManager.getInstance(Holder.getProject());
      return Optional.of(codeStyleSettingsManager.getCurrentSettings())
          .map(CodeStyleSettings::getLineSeparator)
          .orElseGet(() -> CodeStyleSettings.getDefaults().getLineSeparator());
    }

    private void writeContent(Project project, String filename, String parentPath, String packageName,
                              String sourceCode, boolean override) {
      PsiDirectory psiDirectory = FileUtil.mkdirs(PsiManager.getInstance(project),
          Paths.get(parentPath, StringHelper.packageNameToFolder(packageName)));
      PsiFile originalFile = psiDirectory.findFile(filename);
      if (originalFile == null || override) {
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
        PsiFile psiFile;
        if (originalFile != null) {
          Document document = PsiDocumentManager.getInstance(project).getDocument(originalFile);
          assert document != null;
          document.replaceString(0, document.getTextLength(), sourceCode);
          psiFile = originalFile;
        } else {
          psiFile = psiFileFactory.createFileFromText(filename, JavaFileType.INSTANCE, sourceCode);
          psiDirectory.add(psiFile);
        }
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document != null) {
          PsiDocumentManager.getInstance(project).commitDocument(document);
        }
        JavaCodeStyleManager javaCodeStyleManager = JavaCodeStyleManager.getInstance(project);
        try {
          javaCodeStyleManager.optimizeImports(psiFile);
        } catch (Exception e) {
          log.error("optimize imports error", e);
        }
        try {
          CodeStyleManager.getInstance(project).reformat(psiFile);
        } catch (Exception e) {
          log.error("reformat source code error", e);
        }
      }
    }
  }
}
