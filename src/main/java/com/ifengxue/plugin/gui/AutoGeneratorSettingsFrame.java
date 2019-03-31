package com.ifengxue.plugin.gui;

import static com.ifengxue.plugin.util.Key.createKey;
import static java.util.stream.Collectors.joining;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.component.AutoGeneratorConfig;
import com.ifengxue.plugin.component.AutoGeneratorSettings;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.util.Args;
import com.ifengxue.plugin.util.StringHelper;
import com.ifengxue.plugin.util.WindowUtil;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.TreeJavaClassChooserDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.PackageChooser;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class AutoGeneratorSettingsFrame {

  private final JFrame frameHolder;
  private final AutoGeneratorSettings autoGeneratorSettingsHolder;

  private AutoGeneratorSettingsFrame(List<TableSchema> tableSchemaList) {
    this.frameHolder = new JFrame(LocaleContextHolder.format("auto_generation_settings"));
    this.autoGeneratorSettingsHolder = new AutoGeneratorSettings();
    frameHolder.setContentPane(autoGeneratorSettingsHolder.getRootComponent());
    frameHolder.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frameHolder.setLocationRelativeTo(WindowUtil.getParentWindow(Holder.getEvent().getProject()));
    frameHolder.pack();
    AutoGeneratorConfig config = new AutoGeneratorConfig();
    initTextField(autoGeneratorSettingsHolder, config);
    frameHolder.setVisible(true);
    autoGeneratorSettingsHolder.getChkBoxGenerateMethodComment().setEnabled(false);

    AnActionEvent actionEvent = Holder.getEvent();
    Project project = actionEvent.getProject();
    assert project != null;
    config.setProjectBasePath(project.getBasePath());
    // 选择父类
    autoGeneratorSettingsHolder.getBtnChooseSuperClass().addActionListener(event -> {
      TreeJavaClassChooserDialog classChooser = new TreeJavaClassChooserDialog(
          LocaleContextHolder.format("select_parent_class"), project);
      classChooser.show();
      PsiClass selectedClass = classChooser.getSelected();
      if (selectedClass != null) {
        String qualifiedName = selectedClass.getQualifiedName();
        autoGeneratorSettingsHolder.getTextExtendBaseClass().setText(qualifiedName);
        Set<String> excludeFieldSet = new HashSet<>();
        for (String excludeField : autoGeneratorSettingsHolder.getTextExcludeFields().getText().trim().split(",")) {
          excludeFieldSet.add(excludeField.trim());
        }
        Arrays.stream(selectedClass.getAllFields())
            .filter(psiField -> !psiField.getModifierList().hasModifierProperty("static"))
            .filter(psiField -> !psiField.getModifierList().hasModifierProperty("final"))
            .map(PsiField::getName)
            .forEach(excludeFieldSet::add);
        autoGeneratorSettingsHolder.getTextExcludeFields().setText(excludeFieldSet.stream().collect(joining(",")));
      }
      frameHolder.requestFocus();
    });
    // 选择entity包
    autoGeneratorSettingsHolder.getBtnChooseEntityPackage().addActionListener(event -> {
      PackageChooser packageChooser = new PackageChooserDialog(LocaleContextHolder.format("select_entity_package"),
          project);
      packageChooser.show();
      PsiPackage selectedPackage = packageChooser.getSelectedPackage();
      if (selectedPackage != null) {
        String qualifiedName = selectedPackage.getQualifiedName();
        autoGeneratorSettingsHolder.getTextEntityPackage().setText(qualifiedName);
        PsiDirectory[] directories = selectedPackage.getDirectories(GlobalSearchScope.projectScope(project));
        PsiDirectory directory = directories[0];
        config.setEntityDirectory(directory.getVirtualFile().getPath());
      }
      frameHolder.requestFocus();
    });
    // 选择repository包
    autoGeneratorSettingsHolder.getBtnChooseRepositoryPackage().addActionListener(event -> {
      PackageChooser packageChooser = new PackageChooserDialog(
          LocaleContextHolder.format("select_repository_package"), project);
      packageChooser.show();
      PsiPackage selectedPackage = packageChooser.getSelectedPackage();
      if (selectedPackage != null) {
        String qualifiedName = selectedPackage.getQualifiedName();
        autoGeneratorSettingsHolder.getTxtRepositoryPackage().setText(qualifiedName);
        PsiDirectory[] directories = selectedPackage.getDirectories(GlobalSearchScope.projectScope(project));
        PsiDirectory directory = directories[0];
        config.setRepositoryDirectory(directory.getVirtualFile().getPath());
      }
      frameHolder.requestFocus();
    });
    autoGeneratorSettingsHolder.getBtnCancel().addActionListener(event -> frameHolder.dispose());
    autoGeneratorSettingsHolder.getBtnNext().addActionListener(
        event -> {
          // 读取属性
          config.setRemoveTablePrefix(autoGeneratorSettingsHolder.getTextRemoveTablePrefix().getText().trim());
          config.setRemoveFieldPrefix(autoGeneratorSettingsHolder.getTextRemoveFieldPrefix().getText().trim());
          config.setExtendBaseClass(autoGeneratorSettingsHolder.getTextExtendBaseClass().getText().trim());
          config.setEntityPackage(
              Args.notEmpty(autoGeneratorSettingsHolder.getTextEntityPackage().getText().trim(), "entity package",
                  frameHolder));
          config.setRepositoryPackage(autoGeneratorSettingsHolder.getTxtRepositoryPackage().getText().trim());
          Set<String> excludeFieldSet = new HashSet<>();
          for (String excludeField : autoGeneratorSettingsHolder.getTextExcludeFields().getText().trim().split(",")) {
            excludeFieldSet.add(excludeField.trim());
          }
          config.setExcludeFields(excludeFieldSet);
          config.setUseLombok(autoGeneratorSettingsHolder.getChkBoxUseLombok().isSelected());
          config.setGenerateRepository(autoGeneratorSettingsHolder.getChkBoxGenerateRepository().isSelected());
          config.setImplementSerializable(autoGeneratorSettingsHolder.getChkBoxSerializable().isSelected());
          config.setGenerateClassComment(autoGeneratorSettingsHolder.getChkBoxGenerateClassComment().isSelected());
          config.setGenerateFieldComment(autoGeneratorSettingsHolder.getChkBoxGenerateFieldComment().isSelected());
          config.setGenerateMethodComment(autoGeneratorSettingsHolder.getChkBoxGenerateMethodComment().isSelected());
          if (config.getEntityPackage().isEmpty()) {
            autoGeneratorSettingsHolder.getTextEntityPackage().requestFocus();
            return;
          }
          if (config.isGenerateRepository() && config.getRepositoryPackage().isEmpty()) {
            autoGeneratorSettingsHolder.getTxtRepositoryPackage().requestFocus();
            return;
          }
          //TODO 保留主键类型
          List<Table> tableList = new ArrayList<>(tableSchemaList.size());
          VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(config.getEntityDirectory());
          assert vFile != null;
          for (TableSchema tableSchema : tableSchemaList) {
            String tableName = tableSchema.getTableName();
            if (!config.getRemoveTablePrefix().isEmpty() && tableName.startsWith(config.getRemoveTablePrefix())) {
              tableName = tableName.substring(config.getRemoveTablePrefix().length());
            }
            String entityName = StringHelper.parseEntityName(tableName);
            boolean selected = vFile.findChild(entityName + ".java") == null;
            if (selected) {
              // support flyway
              if (tableName.equals("flyway_schema_history")) {
                selected = false;
              }
            }
            tableList.add(Table.from(tableSchema, entityName, selected));
          }
          // 保存属性
          saveTextField(config);
          SelectTablesFrame.show(tableList, config);
          frameHolder.dispose();
        });
  }

  private void initTextField(AutoGeneratorSettings settings, AutoGeneratorConfig config) {
    PropertiesComponent applicationProperties = Holder.getApplicationProperties();
    PropertiesComponent projectProperties = Holder.getProjectProperties();
    settings.getTextRemoveTablePrefix().setText(applicationProperties.getValue(createKey("remove_table_prefix"), "t_"));
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
    settings.getTextEntityPackage().setText(projectProperties.getValue(createKey("entity_package"), ""));
    config.setEntityDirectory(projectProperties.getValue(createKey("entity_directory"), ""));
    settings.getTxtRepositoryPackage().setText(projectProperties.getValue(createKey("repository_package"), ""));
    config.setRepositoryDirectory(projectProperties.getValue(createKey("repository_directory"), ""));
  }

  private void saveTextField(AutoGeneratorConfig config) {
    PropertiesComponent applicationProperties = Holder.getApplicationProperties();
    PropertiesComponent projectProperties = Holder.getProjectProperties();
    applicationProperties.setValue(createKey("remove_table_prefix"), config.getRemoveTablePrefix());
    applicationProperties.setValue(createKey("remove_field_prefix"), config.getRemoveFieldPrefix());
    applicationProperties.setValue(createKey("use_lombok"), config.isUseLombok());
    applicationProperties.setValue(createKey("generate_repository"), config.isGenerateRepository());
    applicationProperties.setValue(createKey("implement_serializable"), config.isImplementSerializable());
    applicationProperties.setValue(createKey("generate_class_comment"), config.isGenerateClassComment());
    applicationProperties.setValue(createKey("generate_field_comment"), config.isGenerateFieldComment());
    applicationProperties.setValue(createKey("generate_method_comment"), config.isGenerateMethodComment());
    projectProperties.setValue(createKey("extend_base_class"), config.getExtendBaseClass());
    projectProperties.setValue(createKey("exclude_fields"), config.getExcludeFields().stream().collect(joining(",")));
    projectProperties.setValue(createKey("entity_package"), config.getEntityPackage());
    projectProperties.setValue(createKey("entity_directory"), config.getEntityDirectory());
    projectProperties.setValue(createKey("repository_package"), config.getRepositoryPackage());
    projectProperties.setValue(createKey("repository_directory"), config.getRepositoryDirectory());
  }

  public static void show(List<TableSchema> tableSchemaList) {
    new AutoGeneratorSettingsFrame(tableSchemaList);
  }
}
