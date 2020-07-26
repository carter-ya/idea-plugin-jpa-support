package com.ifengxue.plugin.gui;

import static com.ifengxue.plugin.util.Key.createKey;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.component.AutoGeneratorConfig;
import com.ifengxue.plugin.component.AutoGeneratorSettings;
import com.ifengxue.plugin.entity.ColumnSchema;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.util.BusUtil;
import com.ifengxue.plugin.util.StringHelper;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.TreeJavaClassChooserDialog;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.swing.Action;
import javax.swing.JComponent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

public class AutoGeneratorSettingsDialog extends DialogWrapper {

  private final AutoGeneratorSettings generatorSettings;
  private final AutoGeneratorConfig config;
  private final List<TableSchema> tableSchemaList;
  private final Function<TableSchema, List<ColumnSchema>> mapping;

  protected AutoGeneratorSettingsDialog(Project project, List<TableSchema> tableSchemaList,
      Function<TableSchema, List<ColumnSchema>> mapping) {
    super(project, true);
    generatorSettings = new AutoGeneratorSettings();
    config = new AutoGeneratorConfig();
    this.tableSchemaList = tableSchemaList;
    this.mapping = mapping;
    init();
    setTitle(LocaleContextHolder.format("auto_generation_settings"));

    initTextField(generatorSettings, config);
    generatorSettings.getChkBoxGenerateMethodComment().setEnabled(false);

    assert project != null;
    config.setProjectBasePath(project.getBasePath());
    // 选择模块
    Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      generatorSettings.getCbxModule().addItem(module.getName());
      if (module.getName().equals(config.getModule())) {
        generatorSettings.getCbxModule().setSelectedItem(module.getName());
      }
    }
    if (generatorSettings.getCbxModule().getSelectedIndex() == -1) {
      generatorSettings.getCbxModule().setSelectedIndex(0);
    }
    setPackagePath((String) generatorSettings.getCbxModule().getSelectedItem(), true);
    generatorSettings.getCbxModule().addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() != ItemEvent.SELECTED) {
        return;
      }
      String moduleName = (String) itemEvent.getItem();
      setPackagePath(moduleName, false);
    });
    // 选择父类
    generatorSettings.getBtnChooseSuperClass().addActionListener(event -> {
      TreeJavaClassChooserDialog classChooser = new TreeJavaClassChooserDialog(
          LocaleContextHolder.format("select_parent_class"), project);
      classChooser.show();
      PsiClass selectedClass = classChooser.getSelected();
      if (selectedClass != null) {
        String qualifiedName = selectedClass.getQualifiedName();
        generatorSettings.getTextExtendBaseClass().setText(qualifiedName);
        Set<String> excludeFieldSet = new LinkedHashSet<>();
        for (String excludeField : generatorSettings.getTextExcludeFields().getText().trim().split(",")) {
          if (StringUtils.isNotBlank(excludeField)) {
            excludeFieldSet.add(excludeField.trim());
          }
        }
        Arrays.stream(selectedClass.getAllFields())
            .filter(psiField -> !psiField.getModifierList().hasModifierProperty("static"))
            .filter(psiField -> !psiField.getModifierList().hasModifierProperty("final"))
            .map(PsiField::getName)
            .forEach(excludeFieldSet::add);
        generatorSettings.getTextExcludeFields().setText(String.join(",", excludeFieldSet));
      }
    });
  }

  private void setPackagePath(String moduleName, boolean checkEmpty) {
    Module module = ModuleManager.getInstance(Holder.getProject()).findModuleByName(moduleName);
    if (module == null) {
      BusUtil.notify(Holder.getProject(), "Module " + moduleName + " not exists.", NotificationType.WARNING);
      return;
    }
    config.setModule(moduleName);
    ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
    List<VirtualFile> sourceRoots = moduleRootManager.getSourceRoots(JavaSourceRootType.SOURCE);
    if (sourceRoots.isEmpty()) {
      BusUtil.notify(Holder.getProject(), "Module " + moduleName + " does not contain Source Root.",
          NotificationType.WARNING);
      return;
    }
    VirtualFile firstSourceRoot = sourceRoots.get(0);
    if (!checkEmpty || generatorSettings.getTextEntityPackageParentPath().getText().isEmpty()) {
      generatorSettings.getTextEntityPackageParentPath().setText(firstSourceRoot.getPath());
    }
    if (!checkEmpty || generatorSettings.getTextRepositoryPackageParentPath().getText().isEmpty()) {
      generatorSettings.getTextRepositoryPackageParentPath().setText(firstSourceRoot.getPath());
    }
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return generatorSettings.getRootComponent();
  }

  @NotNull
  @Override
  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    String entityPackage = generatorSettings.getEntityPackageReferenceEditorCombo().getText().trim();
    if (entityPackage.isEmpty()) {
      generatorSettings.getEntityPackageReferenceEditorCombo().requestFocus();
      return new ValidationInfo("Must set entity package",
          generatorSettings.getEntityPackageReferenceEditorCombo());
    }
    if (generatorSettings.getChkBoxSerializable().isSelected() && generatorSettings
        .getRepositoryPackageReferenceEditorCombo().getText().trim().isEmpty()) {
      return new ValidationInfo("Must set repository package",
          generatorSettings.getRepositoryPackageReferenceEditorCombo());
    }
    return null;
  }

  @Override
  protected void doOKAction() {
    // 读取属性
    config.setRemoveTablePrefix(generatorSettings.getTextRemoveTablePrefix().getText().trim());
    config.setAddTablePrefix(generatorSettings.getTextAddTableNamePrefix().getText().trim());
    config.setAddTableSuffix(generatorSettings.getTextAddTableNameSuffix().getText().trim());
    config.setRemoveFieldPrefix(generatorSettings.getTextRemoveFieldPrefix().getText().trim());
    config.setExtendBaseClass(generatorSettings.getTextExtendBaseClass().getText().trim());
    config.setEntityPackage(generatorSettings.getEntityPackageReferenceEditorCombo().getText().trim());
    config.setEntityDirectory(generatorSettings.getTextEntityPackageParentPath().getText().trim());
    config
        .setRepositoryPackage(generatorSettings.getRepositoryPackageReferenceEditorCombo().getText().trim());
    config.setRepositoryDirectory(generatorSettings.getTextRepositoryPackageParentPath().getText().trim());
    Set<String> excludeFieldSet = new HashSet<>();
    for (String excludeField : generatorSettings.getTextExcludeFields().getText().trim().split(",")) {
      excludeFieldSet.add(excludeField.trim());
    }
    config.setExcludeFields(excludeFieldSet);
    config.setUseLombok(generatorSettings.getChkBoxUseLombok().isSelected());
    config.setGenerateRepository(generatorSettings.getChkBoxGenerateRepository().isSelected());
    config.setImplementSerializable(generatorSettings.getChkBoxSerializable().isSelected());
    config.setGenerateClassComment(generatorSettings.getChkBoxGenerateClassComment().isSelected());
    config.setGenerateFieldComment(generatorSettings.getChkBoxGenerateFieldComment().isSelected());
    config.setGenerateMethodComment(generatorSettings.getChkBoxGenerateMethodComment().isSelected());
    config.setUseJava8DataType(generatorSettings.getChkBoxUseJava8DateType().isSelected());
    //TODO 保留主键类型
    List<Table> tableList = new ArrayList<>(tableSchemaList.size());
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(config.getEntityDirectory());
    if (vFile == null) {
      Messages.showMessageDialog(
          LocaleContextHolder.format("path_not_exists", config.getEntityDirectory()),
          LocaleContextHolder.format("prompt"), Messages.getErrorIcon());
      return;
    }
    for (TableSchema tableSchema : tableSchemaList) {
      String tableName = tableSchema.getTableName();
      if (!config.getRemoveTablePrefix().isEmpty() && tableName.startsWith(config.getRemoveTablePrefix())) {
        tableName = tableName.substring(config.getRemoveTablePrefix().length());
      }
      String entityName = StringHelper.parseEntityName(tableName);
      entityName = config.getAddTablePrefix() + entityName + config.getAddTableSuffix();
      boolean selected = vFile.findChild(entityName + ".java") == null;
      if (selected) {
        // support flyway
        if (tableName.equals("flyway_schema_history")) {
          selected = false;
        }
      }
      if (!selected) {
        // 强制选择所有表
        selected = Holder.isSelectAllTables();
      }
      tableList.add(Table.from(tableSchema, entityName, selected));
    }
    // 保存属性
    saveTextField(config);
    ApplicationManager.getApplication().invokeLater(() -> {
      dispose();
      SelectTablesDialog.show(tableList, mapping, config);
    });
  }

  @Override
  public void doCancelAction() {
    super.doCancelAction();
  }

  private void initTextField(AutoGeneratorSettings settings, AutoGeneratorConfig config) {
    // 初始化取消，下一步按钮标题
    setOKButtonText(LocaleContextHolder.format("button_next_step"));
    setCancelButtonText(LocaleContextHolder.format("button_cancel"));

    PropertiesComponent applicationProperties = Holder.getApplicationProperties();
    PropertiesComponent projectProperties = Holder.getProjectProperties();
    settings.getTextRemoveTablePrefix().setText(applicationProperties.getValue(createKey("remove_table_prefix"), "t_"));
    settings.getTextAddTableNamePrefix().setText(applicationProperties.getValue(createKey("add_table_prefix"), ""));
    settings.getTextAddTableNameSuffix().setText(applicationProperties.getValue(createKey("add_table_suffix"), ""));
    config.setModule(projectProperties.getValue(createKey("module"), ""));
    settings.getTextRemoveFieldPrefix().setText(applicationProperties.getValue(createKey("remove_field_prefix"), "f_"));
    settings.getChkBoxUseLombok()
        .setSelected((applicationProperties.getBoolean(createKey("use_lombok"), true)));
    settings.getChkBoxGenerateRepository()
        .setSelected((applicationProperties.getBoolean(createKey("generate_repository"), true)));
    settings.getChkBoxSerializable()
        .setSelected((applicationProperties.getBoolean(createKey("implement_serializable"), true)));
    settings.getChkBoxGenerateClassComment()
        .setSelected((applicationProperties.getBoolean(createKey("generate_class_comment"), true)));
    settings.getChkBoxGenerateFieldComment()
        .setSelected((applicationProperties.getBoolean(createKey("generate_field_comment"), true)));
    settings.getChkBoxGenerateMethodComment()
        .setSelected((applicationProperties.getBoolean(createKey("generate_method_comment"), true)));
    settings.getTextExtendBaseClass().setText(projectProperties.getValue(createKey("extend_base_class"), ""));
    settings.getTextExcludeFields().setText(projectProperties.getValue(createKey("exclude_fields"), ""));
    settings.getEntityPackageReferenceEditorCombo()
        .setText(projectProperties.getValue(createKey("entity_package"), ""));

    config.setEntityDirectory(projectProperties.getValue(createKey("entity_directory"), ""));
    settings.getTextEntityPackageParentPath().setText(config.getEntityDirectory());

    settings.getRepositoryPackageReferenceEditorCombo()
        .setText(projectProperties.getValue(createKey("repository_package"), ""));

    config.setRepositoryDirectory(projectProperties.getValue(createKey("repository_directory"), ""));
    settings.getTextRepositoryPackageParentPath().setText(config.getRepositoryDirectory());

    settings.getChkBoxUseJava8DateType()
        .setSelected(applicationProperties.getBoolean(createKey("use_java8_data_type"), false));
  }

  private void saveTextField(AutoGeneratorConfig config) {
    PropertiesComponent applicationProperties = Holder.getApplicationProperties();
    PropertiesComponent projectProperties = Holder.getProjectProperties();
    applicationProperties.setValue(createKey("remove_table_prefix"), config.getRemoveTablePrefix());
    applicationProperties.setValue(createKey("add_table_prefix"), config.getAddTablePrefix());
    applicationProperties.setValue(createKey("add_table_suffix"), config.getAddTableSuffix());
    applicationProperties.setValue(createKey("remove_field_prefix"), config.getRemoveFieldPrefix());
    applicationProperties.setValue(createKey("use_lombok"), config.isUseLombok());
    applicationProperties.setValue(createKey("generate_repository"), config.isGenerateRepository());
    applicationProperties.setValue(createKey("implement_serializable"), config.isImplementSerializable());
    applicationProperties.setValue(createKey("generate_class_comment"), config.isGenerateClassComment());
    applicationProperties.setValue(createKey("generate_field_comment"), config.isGenerateFieldComment());
    applicationProperties.setValue(createKey("generate_method_comment"), config.isGenerateMethodComment());
    applicationProperties.setValue(createKey("use_java8_data_type"), config.isUseJava8DataType());
    projectProperties.setValue(createKey("extend_base_class"), config.getExtendBaseClass());
    projectProperties.setValue(createKey("exclude_fields"), String.join(",", config.getExcludeFields()));
    projectProperties.setValue(createKey("entity_package"), config.getEntityPackage());
    projectProperties.setValue(createKey("entity_directory"), config.getEntityDirectory());
    projectProperties.setValue(createKey("repository_package"), config.getRepositoryPackage());
    projectProperties.setValue(createKey("repository_directory"), config.getRepositoryDirectory());
  }

  public static void show(List<TableSchema> tableSchemaList, Function<TableSchema, List<ColumnSchema>> mapping) {
    new AutoGeneratorSettingsDialog(Holder.getProject(), tableSchemaList, mapping).show();
  }
}
