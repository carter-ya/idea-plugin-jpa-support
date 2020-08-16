package com.ifengxue.plugin.gui;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.component.AutoGeneratorSettings;
import com.ifengxue.plugin.entity.ColumnSchema;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.state.AutoGeneratorSettingsState;
import com.ifengxue.plugin.util.BusUtil;
import com.ifengxue.plugin.util.StringHelper;
import com.intellij.ide.util.TreeJavaClassChooserDialog;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

public class AutoGeneratorSettingsDialog extends DialogWrapper {

  private final AutoGeneratorSettings generatorSettings;
  private final List<TableSchema> tableSchemaList;
  private AutoGeneratorSettingsState autoGeneratorSettingsState;
  private final Function<TableSchema, List<ColumnSchema>> mapping;

  protected AutoGeneratorSettingsDialog(Project project, List<TableSchema> tableSchemaList,
      Function<TableSchema, List<ColumnSchema>> mapping) {
    super(project, true);
    generatorSettings = new AutoGeneratorSettings();

    this.tableSchemaList = tableSchemaList;
    this.mapping = mapping;
    this.autoGeneratorSettingsState = ServiceManager.getService(AutoGeneratorSettingsState.class);
    init();
    setTitle(LocaleContextHolder.format("auto_generation_settings"));

    initTextField(generatorSettings);

    // 选择模块
    Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      generatorSettings.getCbxModule().addItem(module.getName());
      if (module.getName().equals(autoGeneratorSettingsState.getModuleName())) {
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
            .filter(psiField -> psiField.getModifierList() != null)
            .filter(psiField -> !psiField.getModifierList().hasModifierProperty(PsiModifier.STATIC))
            .filter(psiField -> !psiField.getModifierList().hasModifierProperty(PsiModifier.FINAL))
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
    autoGeneratorSettingsState.setModuleName(moduleName);
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
    generatorSettings.getData(autoGeneratorSettingsState);
    //TODO 保留主键类型
    List<Table> tableList = new ArrayList<>(tableSchemaList.size());
    String entityDirectory = Paths.get(autoGeneratorSettingsState.getEntityParentDirectory(),
        StringHelper.packageNameToFolder(autoGeneratorSettingsState.getEntityPackageName()))
        .toAbsolutePath().toString();
    VirtualFile entityDirectoryVF = LocalFileSystem.getInstance().findFileByPath(entityDirectory);
    for (TableSchema tableSchema : tableSchemaList) {
      String tableName = autoGeneratorSettingsState.removeTablePrefix(tableSchema.getTableName());
      String entityName = StringHelper.parseEntityName(tableName);
      entityName = autoGeneratorSettingsState.concatPrefixAndSuffix(entityName);
      // 是否默认选中文件
      boolean selected = entityDirectoryVF == null || entityDirectoryVF.findChild(entityName + ".java") == null;
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
      String repositoryName = entityName + autoGeneratorSettingsState.getRepositorySuffix();
      tableList.add(Table.from(tableSchema, entityName, repositoryName, selected));
    }
    ApplicationManager.getApplication().invokeLater(() -> {
      dispose();
      SelectTablesDialog.show(tableList, mapping);
    });
  }

  @Override
  public void doCancelAction() {
    super.doCancelAction();
  }

  @Nullable
  @Override
  protected String getDimensionServiceKey() {
    return Constants.NAME + ":" + getClass().getName();
  }

  private void initTextField(AutoGeneratorSettings settings) {
    // 初始化取消，下一步按钮标题
    setOKButtonText(LocaleContextHolder.format("button_next_step"));
    setCancelButtonText(LocaleContextHolder.format("button_cancel"));

    settings.setData(autoGeneratorSettingsState);
  }

  public static void show(List<TableSchema> tableSchemaList, Function<TableSchema, List<ColumnSchema>> mapping) {
    new AutoGeneratorSettingsDialog(Holder.getProject(), tableSchemaList, mapping).show();
  }
}
