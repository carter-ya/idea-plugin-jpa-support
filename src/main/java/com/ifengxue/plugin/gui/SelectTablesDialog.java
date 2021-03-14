package com.ifengxue.plugin.gui;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
import com.ifengxue.plugin.util.ColumnUtil;
import com.ifengxue.plugin.util.FileUtil;
import com.ifengxue.plugin.util.SourceFormatter;
import com.ifengxue.plugin.util.StringHelper;
import com.ifengxue.plugin.util.VelocityUtil;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
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
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
          autoGeneratorSettingsState.getRemoveFieldPrefix(), true,
          autoGeneratorSettingsState.isUseJava8DateType());
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

      String encoding = StandardCharsets.UTF_8.name();
      JpaRepositorySourceParser repositorySourceParser = new JpaRepositorySourceParser();
      repositorySourceParser.setVelocityEngine(VelocityUtil.getInstance(), encoding);

      EntitySourceParserV2 sourceParser = new EntitySourceParserV2();
      sourceParser.setVelocityEngine(VelocityUtil.getInstance(), encoding);

      AutoGeneratorSettingsState autoGeneratorSettingsState = ServiceManager
          .getService(Holder.getOrDefaultProject(), AutoGeneratorSettingsState.class);
      ModuleSettings moduleSettings = autoGeneratorSettingsState.getModuleSettings();

      CountDownLatch isReadForWrite = new CountDownLatch(1);
      ApplicationManager.getApplication().runWriteAction(() -> {
        // create entity directory
        FileUtil.mkdirs(PsiManager.getInstance(project), Paths.get(moduleSettings.getEntityParentDirectory(),
            StringHelper.packageNameToFolder(moduleSettings.getEntityPackageName())));
        // create repository dir
        if (autoGeneratorSettingsState.isGenerateRepository()) {
          FileUtil.mkdirs(PsiManager.getInstance(project), Paths.get(moduleSettings.getRepositoryParentDirectory(),
              StringHelper.packageNameToFolder(moduleSettings.getRepositoryPackageName())));
        }

        isReadForWrite.countDown();
      });

      AtomicInteger leftGenerateCount = new AtomicInteger(tableList.size());
      for (Table table : tableList) {
        table.setPackageName(moduleSettings.getEntityPackageName());
        if (table.getColumns() == null) {
          table.setColumns(findColumns(table));
        }
        List<Column> columns = table.getColumns()
            .stream()
            .filter(Column::isSelected)
            .collect(toList());
        table.setColumns(columns);

        // configure source code generator config
        GeneratorConfig generatorConfig = new GeneratorConfig();
        generatorConfig.setDriverConfig(new DriverConfig());
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
            .setUseSwaggerUIComment(autoGeneratorSettingsState.isGenerateSwaggerUIComment())
            .setAddSchemeNameToTableName(autoGeneratorSettingsState.isAddSchemaNameToTableName())
        );
        generatorConfig.setPluginConfigs(Collections.emptyList());
        String sourceCode = sourceParser.parse(generatorConfig, table);
        WriteCommandAction.runWriteCommandAction(project, () -> {
          try {
            isReadForWrite.await();

            String fileExtension = ".java";
            String filename = table.getEntityName() + fileExtension;
            writeContent(project, filename, moduleSettings.getEntityParentDirectory(),
                moduleSettings.getEntityPackageName(), sourceCode);
            if (autoGeneratorSettingsState.isGenerateRepository()) {
              filename = table.getRepositoryName() + fileExtension;
              String repositorySourceCode = repositorySourceParser.parse(generatorConfig, table);
              writeContent(project, filename, moduleSettings.getRepositoryParentDirectory(),
                  moduleSettings.getRepositoryPackageName(),
                  repositorySourceCode);
            }
          } catch (Exception e) {
            Bus.notify(new Notification(Constants.GROUP_ID, "Error", "Generate source code error. " + e,
                NotificationType.ERROR), project);
          } finally {
            if (leftGenerateCount.decrementAndGet() == 0) {
              ApplicationManager.getApplication().invokeLater(
                  () -> Messages.showInfoMessage(LocaleContextHolder.format("generate_source_code_success"),
                      Constants.NAME));
            }
          }
        });
      }
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

    private void writeContent(Project project, String filename, String parentPath, String packageName,
        String sourceCode) {
      PsiDirectory psiDirectory = FileUtil.mkdirs(PsiManager.getInstance(project),
          Paths.get(parentPath, StringHelper.packageNameToFolder(packageName)));
      PsiFile originalFile = psiDirectory.findFile(filename);
      if (originalFile != null && duplicateActionType.isIgnore()) {
        log.info("Ignore exists file " + filename);
        return;
      }
      PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
      PsiFile psiFile = psiFileFactory.createFileFromText(filename, JavaFileType.INSTANCE, sourceCode);
      if (originalFile != null) {
        if (duplicateActionType.isMerge()) {
          merge(originalFile, psiFile);
          log.info("Try merge exists file " + filename);
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

      SourceFormatter.formatJavaCode(project, psiFile);

      if (document != null) {
        PsiDocumentManager.getInstance(project).commitDocument(document);
      } else {
        // The new file needs to be saved last, otherwise the formatting will not take effect
        psiDirectory.add(psiFile);
      }
    }

    private void merge(PsiFile originalFile, PsiFile psiFile) {
      PsiClass[] originalPsiClasses = ((PsiJavaFile) originalFile).getClasses();
      PsiClass[] psiClasses = ((PsiJavaFile) psiFile).getClasses();
      PsiClass originalTopClass = originalPsiClasses[0];
      PsiClass psiTopClass = psiClasses[0];

      // merge annotations
      Map<String, PsiAnnotation> nameToAnnotation = Arrays.stream(originalTopClass.getAnnotations())
          .collect(toMap(PsiAnnotation::getQualifiedName, Function.identity()));
      for (PsiAnnotation annotation : psiTopClass.getAnnotations()) {
        if (!nameToAnnotation.containsKey(annotation.getQualifiedName())) {
          PsiModifierList modifierList = originalTopClass.getModifierList();
          if (modifierList != null && annotation.getQualifiedName() != null) {
            PsiAnnotation psiAnnotation = modifierList.addAnnotation(annotation.getQualifiedName());
            for (JvmAnnotationAttribute attribute : annotation.getAttributes()) {
              psiAnnotation.setDeclaredAttributeValue(attribute.getAttributeName(),
                  annotation.findAttributeValue(attribute.getAttributeName()));
            }
          }
        }
      }

      // merge implements
      try {
        Map<String, PsiClassType> nameToClassType = Arrays.stream(originalTopClass.getImplementsListTypes())
            .collect(toMap(psiClassType -> Objects.requireNonNull(psiClassType.resolve()).getQualifiedName(),
                Function.identity()));
        for (PsiClassType implementsListType : psiTopClass.getImplementsListTypes()) {
          PsiClass resolvePsiClass = implementsListType.resolve();
          if (resolvePsiClass != null && !nameToClassType.containsKey(resolvePsiClass.getQualifiedName())) {
            PsiJavaCodeReferenceElement implementsReference = ServiceManager
                .getService(Holder.getOrDefaultProject(), PsiElementFactory.class)
                .createReferenceFromText(Objects.requireNonNull(resolvePsiClass.getQualifiedName()),
                    originalTopClass);
            PsiReferenceList implementsList = originalTopClass.getImplementsList();
            if (implementsList != null) {
              implementsList.add(implementsReference);
            }
          }
        }
      } catch (Exception e) {
        log.warn("Can't merge implements", e);
      }

      // merge fields
      Map<String, PsiField> nameToField = Arrays.stream(originalTopClass.getFields())
          .collect(toMap(PsiField::getName, Function.identity()));
      for (PsiField field : psiTopClass.getFields()) {
        if (!nameToField.containsKey(field.getName())) {
          mergeFieldAndTryKeepOrder(field, psiTopClass, originalTopClass);
        }
      }

      // merge methods
      Map<String, PsiMethod> nameToMethod = Arrays.stream(originalTopClass.getMethods())
          .collect(toMap(PsiMethod::getName, Function.identity()));
      for (PsiMethod method : psiTopClass.getMethods()) {
        if (!nameToMethod.containsKey(method.getName())) {
          originalTopClass.add(method);
        }
      }
    }

    /**
     * merge field to target class, and try keep order
     */
    private void mergeFieldAndTryKeepOrder(PsiField field, PsiClass originalClass, PsiClass targetClass) {
      PsiField precursorField = null;
      boolean precursorIsFound = false;
      for (PsiField originalClassField : originalClass.getFields()) {
        if (originalClassField.getName().equals(field.getName())) {
          precursorIsFound = true;
          break;
        } else {
          precursorField = originalClassField;
        }
      }
      PsiField[] targetClassFields = targetClass.getFields();
      if (precursorIsFound && precursorField != null) {
        for (PsiField targetClassField : targetClassFields) {
          if (targetClassField.getName().equals(precursorField.getName())) {
            precursorField = targetClassField;
            break;
          }
        }
      }

      if (!precursorIsFound) {
        if (targetClassFields.length == 0) {
          PsiMethod[] methods = targetClass.getMethods();
          if (methods.length == 0) {
            targetClass.add(field);
          } else {
            targetClass.addBefore(field, methods[0]);
          }
        } else {
          PsiField firstNotStaticField = null;
          for (PsiField targetClassField : targetClassFields) {
            if (targetClassField.getModifierList() == null
                || !targetClassField.getModifierList().hasModifierProperty(PsiModifier.STATIC)) {
              firstNotStaticField = targetClassField;
              break;
            }
          }
          if (firstNotStaticField == null) {
            firstNotStaticField = targetClassFields[targetClassFields.length - 1];
          }
          targetClass.addBefore(field, firstNotStaticField);
        }
      } else {
        targetClass.addAfter(field, precursorField);
      }
    }
  }
}
