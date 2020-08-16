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
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.state.AutoGeneratorSettingsState;
import com.ifengxue.plugin.util.StringHelper;
import com.ifengxue.plugin.util.VelocityUtil;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.DirectoryUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

public class SelectTablesDialog extends DialogWrapper {

  private final Logger log = Logger.getInstance(getClass());
  private final SelectTables selectTables;
  private final Function<TableSchema, List<ColumnSchema>> mapping;

  protected SelectTablesDialog(@Nullable Project project, List<Table> tableList,
      Function<TableSchema, List<ColumnSchema>> mapping) {
    super(project, true);
    this.mapping = mapping;
    selectTables = new SelectTables(tableList);
    init();
    setTitle(LocaleContextHolder.format("select_database_tables"));

    int rowCount = tableList.size();
    JTable table = selectTables.getTblTableSchema();
    table.setModel(new AbstractTableModel() {
      private static final long serialVersionUID = 8974669315458199207L;
      final String[] columns = {
          LocaleContextHolder.format("table_selected"),
          LocaleContextHolder.format("table_sequence"),
          LocaleContextHolder.format("table_table_name"),
          LocaleContextHolder.format("table_class_name"),
          LocaleContextHolder.format("table_repository_name"),
          LocaleContextHolder.format("table_class_comment")
      };

      @Override
      public int getRowCount() {
        return rowCount;
      }

      @Override
      public int getColumnCount() {
        return columns.length;
      }

      @Override
      public String getColumnName(int column) {
        return columns[column];
      }

      @Override
      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0 || columnIndex == 3 || columnIndex == 4 || columnIndex == 5;
      }

      @Override
      public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
          case 0:
            return Boolean.class;
          case 1:
            return Integer.class;
          default:
            return String.class;
        }
      }

      @Override
      public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
          case 0:
            return tableList.get(rowIndex).isSelected();
          case 1:
            return rowIndex + 1;
          case 2:
            return tableList.get(rowIndex).getTableName();
          case 3:
            return tableList.get(rowIndex).getEntityName();
          case 4:
            return tableList.get(rowIndex).getRepositoryName();
          case 5:
            return tableList.get(rowIndex).getTableComment();
          default:
            throw new IllegalStateException("无法识别的列索引:" + columnIndex);
        }
      }

      @Override
      public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
          case 0:
            tableList.get(rowIndex).setSelected((Boolean) aValue);
            break;
          case 3:
            tableList.get(rowIndex).setEntityName((String) aValue);
            break;
          case 4:
            tableList.get(rowIndex).setRepositoryName((String) aValue);
            break;
          case 5:
            tableList.get(rowIndex).setTableComment((String) aValue);
            break;
          default:
            break;
        }
      }
    });
    table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));
    table.getColumnModel().getColumn(0).setMaxWidth(60);
    table.getColumnModel().getColumn(1).setMaxWidth(40);

    selectTables.getBtnCancel().addActionListener(event -> dispose());
    // 选中所有行
    selectTables.getBtnSelectAll().addActionListener(event -> {
      for (Table t : tableList) {
        t.setSelected(true);
      }
      table.updateUI();
    });
    // 全不选
    selectTables.getBtnSelectNone().addActionListener(event -> {
      for (Table t : tableList) {
        t.setSelected(false);
      }
      table.updateUI();
    });
    // 反选
    selectTables.getBtnSelectOther().addActionListener(event -> {
      for (Table t : tableList) {
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
      for (Table t : tableList) {
        t.setSelected(pattern.matcher(t.getTableName()).matches());
      }
      table.updateUI();
    });
    // 开始生成
    selectTables.getBtnGenerate().addActionListener(event -> {
      if (tableList.stream().noneMatch(Table::isSelected)) {
        Messages.showWarningDialog(Holder.getEvent().getProject(),
            LocaleContextHolder.format("at_least_select_one_table"),
            LocaleContextHolder.format("prompt"));
        return;
      }
      dispose();
      // 开始生成
      new GeneratorRunner(tableList).run();
    });
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return selectTables.getRootComponent();
  }

  @NotNull
  @Override
  protected Action[] createActions() {
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

      String encoding = StandardCharsets.UTF_8.name();
      JpaRepositorySourceParser repositorySourceParser = new JpaRepositorySourceParser();
      repositorySourceParser.setVelocityEngine(VelocityUtil.getInstance(), encoding);

      EntitySourceParserV2 sourceParser = new EntitySourceParserV2();
      sourceParser.setVelocityEngine(VelocityUtil.getInstance(), encoding);

      AutoGeneratorSettingsState autoGeneratorSettingsState = ServiceManager
          .getService(AutoGeneratorSettingsState.class);
      // 生成数量
      for (Table table : tableList) {
        List<ColumnSchema> columnSchemaList = mapping.apply(table.getRawTableSchema());
        if (columnSchemaList == null) {
          // skip empty column schemas
          continue;
        }
        // 解析字段列表
        List<Column> columnList = new ArrayList<>(columnSchemaList.size());
        for (ColumnSchema columnSchema : columnSchemaList) {
          Column column = Holder.getDatabaseDrivers().getDriverAdapter().parseToColumn(columnSchema,
              autoGeneratorSettingsState.getRemoveFieldPrefix(), true,
              autoGeneratorSettingsState.isUseJava8DateType());
          if (column.isPrimary()) {
            table.setPrimaryKeyClassType(column.getJavaDataType());
            table.incPrimaryKeyCount();
          }
          if (!autoGeneratorSettingsState.getIgnoredFields().contains(column.getFieldName())) {
            columnList.add(column);
          }
        }
        table.setPackageName(autoGeneratorSettingsState.getEntityPackageName());
        table.setColumns(columnList);

        // 配置源码生成信息
        GeneratorConfig generatorConfig = new GeneratorConfig();
        generatorConfig.setDriverConfig(new DriverConfig()
            .setVendor(Holder.getDatabaseDrivers().getVendor2()));
        int lastIndex;
        String basePackageName = autoGeneratorSettingsState.getEntityPackageName();
        if ((lastIndex = basePackageName.lastIndexOf('.')) != -1) {
          basePackageName = basePackageName.substring(0, lastIndex);
        }
        generatorConfig.setTablesConfig(new TablesConfig()
            .setBasePackageName(basePackageName)
            .setEntityPackageName(autoGeneratorSettingsState.getEntityPackageName())
            .setEnumSubPackageName(basePackageName + ".enums")
            .setIndent(getIndent())
            .setLineSeparator(getLineSeparator())
            .setOrm(ORM.JPA)
            .setExtendsEntityName(autoGeneratorSettingsState.getInheritedParentClassName())
            .setRemoveTablePrefix(autoGeneratorSettingsState.getRemoveEntityPrefix())
            .setRemoveFieldPrefix(autoGeneratorSettingsState.getRemoveFieldPrefix())
            .setRepositoryPackageName(autoGeneratorSettingsState.getRepositoryPackageName())
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

        // 生成源码
        String sourceCode = sourceParser.parse(generatorConfig, table);
        WriteCommandAction.runWriteCommandAction(project, () -> {
          String fileExtension = ".java";
          String filename = table.getEntityName() + fileExtension;
          try {
            writeContent(project, filename, autoGeneratorSettingsState.getEntityParentDirectory(),
                autoGeneratorSettingsState.getEntityPackageName(), sourceCode);
            if (autoGeneratorSettingsState.isGenerateRepository()) {
              filename = table.getRepositoryName() + fileExtension;
              String repositorySourceCode = repositorySourceParser.parse(generatorConfig, table);
              writeContent(project, filename, autoGeneratorSettingsState.getRepositoryParentDirectory(),
                  autoGeneratorSettingsState.getRepositoryPackageName(),
                  repositorySourceCode);
            }
          } catch (Exception e) {
            Bus.notify(
                new Notification("JpaSupport", "Error", "Generate source code error. " + e, NotificationType.ERROR),
                project);
          }
        });
      }
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
        String sourceCode) {
      String directory = Paths.get(parentPath, StringHelper.packageNameToFolder(packageName)).toAbsolutePath()
          .toString();
      PsiDirectory psiDirectory = DirectoryUtil.mkdirs(PsiManager.getInstance(project), directory);
      PsiFile psiFile = psiDirectory.findFile(filename);
      if (psiFile != null) {
        // 切换UI线程
        ApplicationManager.getApplication().invokeLater(() -> {
          int selectButton = Messages
              .showOkCancelDialog(
                  LocaleContextHolder.format("file_already_exists_overwritten", filename),
                  LocaleContextHolder.format("prompt"),
                  Messages.OK_BUTTON,
                  Messages.CANCEL_BUTTON,
                  Messages.getQuestionIcon());
          // 不覆盖
          if (selectButton != Messages.OK) {
            return;
          }
          // 切换IO线程
          WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiFile pf = psiDirectory.findFile(filename);
            assert pf != null;
            VirtualFile vf = pf.getVirtualFile();
            writeContent(sourceCode, vf, project, pf);
            JavaCodeStyleManager javaCodeStyleManager = JavaCodeStyleManager.getInstance(Holder.getProject());
            try {
              javaCodeStyleManager.optimizeImports(pf);
            } catch (Exception e) {
              log.error("optimize imports error", e);
            }
            try {
              CodeStyleManager.getInstance(Holder.getProject()).reformat(pf);
            } catch (Exception e) {
              log.error("reformat source code error", e);
            }
          });
        });
      } else {
        psiFile = psiDirectory.createFile(filename);
        VirtualFile vFile = psiFile.getVirtualFile();
        writeContent(sourceCode, vFile, project, psiFile);
        JavaCodeStyleManager javaCodeStyleManager = JavaCodeStyleManager.getInstance(Holder.getProject());
        try {
          javaCodeStyleManager.optimizeImports(psiFile);
        } catch (Exception e) {
          log.error("optimize imports error", e);
        }
        try {
          CodeStyleManager.getInstance(Holder.getProject()).reformat(psiFile);
        } catch (Exception e) {
          log.error("reformat source code error", e);
        }
      }
    }

    private void writeContent(String sourceCode, VirtualFile vFile, Project project, PsiFile psiFile) {
      try {
        vFile.setWritable(true);
        vFile.setCharset(StandardCharsets.UTF_8);
        vFile.setBinaryContent(sourceCode.getBytes(StandardCharsets.UTF_8));

        // commit document
        Document cachedDocument = PsiDocumentManager.getInstance(project).getCachedDocument(psiFile);
        if (cachedDocument != null) {
          PsiDocumentManager.getInstance(project).commitDocument(cachedDocument);
        }
      } catch (IOException e) {
        log.error("generate source code error", e);
      }
    }
  }
}
