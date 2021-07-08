package com.ifengxue.plugin.gui;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.component.AutoGeneratorSettings;
import com.ifengxue.plugin.component.MyPackageNameReferenceEditorCombo;
import com.ifengxue.plugin.entity.ColumnSchema;
import com.ifengxue.plugin.entity.Selectable;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.state.AutoGeneratorSettingsState;
import com.ifengxue.plugin.state.ModuleSettings;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

public class AutoGeneratorSettingsDialog extends DialogWrapper {

  private final AutoGeneratorSettings generatorSettings;
  private final List<TableSchema> tableSchemaList;
  private final AutoGeneratorSettingsState autoGeneratorSettingsState;
  private final Function<TableSchema, List<ColumnSchema>> mapping;

  protected AutoGeneratorSettingsDialog(Project project, List<TableSchema> tableSchemaList,
                                        Function<TableSchema, List<ColumnSchema>> mapping) {
    super(project, true);
    generatorSettings = new AutoGeneratorSettings();

    this.tableSchemaList = tableSchemaList;
    this.mapping = mapping;
    this.autoGeneratorSettingsState = ServiceManager.getService(project, AutoGeneratorSettingsState.class);
    init();
    setTitle(LocaleContextHolder.format("auto_generation_settings"));

    // select module
    Module[] modules = ModuleManager.getInstance(project).getModules();
    Module selectedModule = modules[0];
    for (Module module : modules) {
      generatorSettings.getCbxModule().addItem(module.getName());
      if (module.getName().equals(autoGeneratorSettingsState.getModuleName())) {
        generatorSettings.getCbxModule().setSelectedItem(module.getName());
        selectedModule = module;
      }
    }
    if (generatorSettings.getCbxModule().getSelectedIndex() == -1) {
      generatorSettings.getCbxModule().setSelectedIndex(0);
    }

    initTextField();
    moduleChange(selectedModule);
    setPackagePath(selectedModule, true);

    generatorSettings.getCbxModule().addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() != ItemEvent.SELECTED) {
        return;
      }
      String moduleName = (String) itemEvent.getItem();
      findModule(moduleName)
          .ifPresent(module -> {
            moduleChange(module);
            setPackagePath(module, false);
          });
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

    // bind enable button listener
    AtomicInteger paneIndex = new AtomicInteger(0);
    ItemListener listener = itemEvent -> generatorSettings
        .setForegroundColor(paneIndex.get(), itemEvent.getStateChange() == ItemEvent.SELECTED);
    generatorSettings.getChkBoxGenerateController().addItemListener(e -> {
      paneIndex.set(0);
      listener.itemStateChanged(e);
    });
    generatorSettings.getChkBoxGenerateService().addItemListener(e -> {
      paneIndex.set(1);
      listener.itemStateChanged(e);
    });
    generatorSettings.getChkBoxGenerateMapperXml().addItemListener(e -> {
      paneIndex.set(2);
      listener.itemStateChanged(e);
    });
    generatorSettings.getChkBoxGenerateVO().addItemListener(e -> {
      paneIndex.set(3);
      listener.itemStateChanged(e);
    });
    generatorSettings.getChkBoxGenerateDTO().addItemListener(e -> {
      paneIndex.set(4);
      listener.itemStateChanged(e);
    });
  }

  private void setPackagePath(Module module, boolean checkEmpty) {
    String moduleName = module.getName();
    autoGeneratorSettingsState.setModuleName(moduleName);
    ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
    List<VirtualFile> sourceRoots = moduleRootManager.getSourceRoots(JavaSourceRootType.SOURCE);
    String sourceRoot;
    String resourceRoot;
    if (sourceRoots.isEmpty()) {
      VirtualFile[] contentRoots = moduleRootManager.getContentRoots();
      if (contentRoots.length == 0) {
        BusUtil.notify(Holder.getProject(),
            "Module " + moduleName + " does not contain Source Root.", NotificationType.WARNING);
        return;
      }
      sourceRoot = Paths
          .get(Objects.requireNonNull(contentRoots[0].getCanonicalPath()), "src", "main", "java")
          .toString();
    } else {
      sourceRoot = sourceRoots.get(0).getCanonicalPath();
    }
    assert sourceRoot != null;
    resourceRoot = Paths.get(sourceRoot).resolveSibling("resources").toString();

    if (!checkEmpty || generatorSettings.getTextEntityPackageParentPath().getText().isEmpty()) {
      generatorSettings.getTextEntityPackageParentPath().setText(sourceRoot);
    }
    if (!checkEmpty || generatorSettings.getTextRepositoryPackageParentPath().getText().isEmpty()) {
      generatorSettings.getTextRepositoryPackageParentPath().setText(sourceRoot);
    }
    if (!checkEmpty || generatorSettings.getTextControllerPackageParentPath().getText().isEmpty()) {
      generatorSettings.getTextControllerPackageParentPath().setText(sourceRoot);
    }
    if (!checkEmpty || generatorSettings.getTextServicePackageParentPath().getText().isEmpty()) {
      generatorSettings.getTextServicePackageParentPath().setText(sourceRoot);
    }
    if (!checkEmpty || generatorSettings.getTextMapperXmlParentPath().getText().isEmpty()) {
      generatorSettings.getTextMapperXmlParentPath().setText(resourceRoot);
    }
    if (!checkEmpty || generatorSettings.getTextVOPackageParentPath().getText().isEmpty()) {
      generatorSettings.getTextVOPackageParentPath().setText(sourceRoot);
    }
    if (!checkEmpty || generatorSettings.getTextDTOPackageParentPath().getText().isEmpty()) {
      generatorSettings.getTextDTOPackageParentPath().setText(sourceRoot);
    }
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return generatorSettings.getRootComponent();
  }

  @Override
  protected Action @NotNull [] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    Module module = ModuleManager.getInstance(Holder.getProject())
        .findModuleByName(
            (String) Objects.requireNonNull(generatorSettings.getCbxModule().getSelectedItem()));
    if (module == null) {
      return new ValidationInfo("Must select valid module", generatorSettings.getCbxModule());
    }
    if (generatorSettings.getChkBoxGenerateEntity().isSelected()) {
      if (generatorSettings.getEntityPackageReferenceEditorCombo().getText().trim().isEmpty()) {
        return new ValidationInfo("Must set entity package",
            generatorSettings.getEntityPackageReferenceEditorCombo());
      }
      if (generatorSettings.getTextEntityPackageParentPath().getText().trim().isEmpty()) {
        return new ValidationInfo("Must set entity path",
            generatorSettings.getTextEntityPackageParentPath());
      }
    }
    if (generatorSettings.getChkBoxGenerateRepository().isSelected()) {
      if (generatorSettings.getRepositoryPackageReferenceEditorCombo().getText().trim().isEmpty()) {
        return new ValidationInfo("Must set repository package",
            generatorSettings.getRepositoryPackageReferenceEditorCombo());
      }
      if (generatorSettings.getTextRepositoryPackageParentPath().getText().trim().isEmpty()) {
        return new ValidationInfo("Must set repository path",
            generatorSettings.getTextRepositoryPackageParentPath());
      }
    }
    if (generatorSettings.getChkBoxGenerateController().isSelected()) {
      if (generatorSettings.getControllerPackageReferenceEditorCombo().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(0);
        return new ValidationInfo("Must set controller package",
            generatorSettings.getControllerPackageReferenceEditorCombo());
      }
      if (generatorSettings.getTextControllerPackageParentPath().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(0);
        return new ValidationInfo("Must set controller path",
            generatorSettings.getTextControllerPackageParentPath());
      }
    }
    if (generatorSettings.getChkBoxGenerateService().isSelected()) {
      if (generatorSettings.getServicePackageReferenceEditorCombo().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(1);
        return new ValidationInfo("Must set service package",
            generatorSettings.getServicePackageReferenceEditorCombo());
      }
      if (generatorSettings.getTextServicePackageParentPath().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(1);
        return new ValidationInfo("Must set service path",
            generatorSettings.getTextServicePackageParentPath());
      }
    }
    if (generatorSettings.getChkBoxGenerateMapperXml().isSelected()) {
      if (generatorSettings.getMapperXmlReferenceEditorCombo().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(2);
        return new ValidationInfo("Must set Mapper XML directory",
            generatorSettings.getMapperXmlReferenceEditorCombo());
      }
      if (generatorSettings.getTextMapperXmlParentPath().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(2);
        return new ValidationInfo("Must set Mapper XML path",
            generatorSettings.getTextMapperXmlParentPath());
      }
    }
    if (generatorSettings.getChkBoxGenerateVO().isSelected()) {
      if (generatorSettings.getVoPackageReferenceEditorCombo().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(3);
        return new ValidationInfo("Must set VO package",
            generatorSettings.getVoPackageReferenceEditorCombo());
      }
      if (generatorSettings.getTextVOPackageParentPath().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(3);
        return new ValidationInfo("Must set VO path",
            generatorSettings.getTextVOPackageParentPath());
      }
      if (generatorSettings.getTextVOSuffixName().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(3);
        return new ValidationInfo("Must set suffix name",
            generatorSettings.getTextVOSuffixName());
      }
    }
    if (generatorSettings.getChkBoxGenerateDTO().isSelected()) {
      if (generatorSettings.getDtoPackageReferenceEditorCombo().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(4);
        return new ValidationInfo("Must set DTO package",
            generatorSettings.getDtoPackageReferenceEditorCombo());
      }
      if (generatorSettings.getTextDTOPackageParentPath().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(4);
        return new ValidationInfo("Must set DTO path",
            generatorSettings.getTextDTOPackageParentPath());
      }
      if (generatorSettings.getTextDTOSuffixName().getText().trim().isEmpty()) {
        generatorSettings.getExtensionPane().setSelectedIndex(4);
        return new ValidationInfo("Must set suffix name",
            generatorSettings.getTextDTOSuffixName());
      }
    }
    return null;
  }

  @Override
  protected void doOKAction() {
    ModuleSettings moduleSettings = autoGeneratorSettingsState.getModuleSettings(
        (String) generatorSettings.getCbxModule().getSelectedItem());
    // read attributes
    generatorSettings.getData(autoGeneratorSettingsState, moduleSettings);
    List<Table> tableList = new ArrayList<>(tableSchemaList.size());
    String entityDirectory = Paths.get(moduleSettings.getEntityParentDirectory(),
        StringHelper.packageNameToFolder(moduleSettings.getEntityPackageName()))
        .toAbsolutePath().toString();
    VirtualFile entityDirectoryVF = LocalFileSystem.getInstance().findFileByPath(entityDirectory);
    for (TableSchema tableSchema : tableSchemaList) {
      String tableName = autoGeneratorSettingsState.removeTablePrefix(tableSchema.getTableName());
      String entityName = StringHelper.parseEntityName(tableName);
      entityName = autoGeneratorSettingsState.concatPrefixAndSuffix(entityName);
      // If the path contains a file with the same name, it is not selected by default
      boolean selected = entityDirectoryVF == null || entityDirectoryVF.findChild(entityName + ".java") == null;
      if (tableSchema instanceof Selectable) {
        selected = ((Selectable) tableSchema).isSelected();
      }
      if (selected) {
        // support flyway
        if (tableName.equals("flyway_schema_history")) {
          selected = false;
        }
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

  private void initTextField() {
    // init button title
    setOKButtonText(LocaleContextHolder.format("button_next_step"));
    setCancelButtonText(LocaleContextHolder.format("button_cancel"));
  }

  private void moduleChange(Module newModule) {
    ModuleSettings moduleSettings = autoGeneratorSettingsState.getModuleSettings(newModule.getName());
    generatorSettings.setData(autoGeneratorSettingsState, moduleSettings);

    if (moduleSettings != null) {
      Object[][] combos = {
          {
              generatorSettings.getEntityPackageReferenceEditorCombo(),
              moduleSettings.getEntityPackageName(),
              generatorSettings.getTextEntityPackageParentPath(),
              moduleSettings.getEntityParentDirectory()
          },
          {
              generatorSettings.getRepositoryPackageReferenceEditorCombo(),
              moduleSettings.getRepositoryPackageName(),
              generatorSettings.getTextRepositoryPackageParentPath(),
              moduleSettings.getRepositoryParentDirectory()
          },
          {
              generatorSettings.getControllerPackageReferenceEditorCombo(),
              moduleSettings.getControllerPackageName(),
              generatorSettings.getTextControllerPackageParentPath(),
              moduleSettings.getControllerParentDirectory()
          },
          {
              generatorSettings.getServicePackageReferenceEditorCombo(),
              moduleSettings.getServicePackageName(),
              generatorSettings.getTextServicePackageParentPath(),
              moduleSettings.getServiceParentDirectory()
          },
          {
              generatorSettings.getMapperXmlReferenceEditorCombo(),
              moduleSettings.getMapperXmlPackageName(),
              generatorSettings.getTextMapperXmlParentPath(),
              moduleSettings.getMapperXmlParentDirectory()
          },
          {
              generatorSettings.getVoPackageReferenceEditorCombo(),
              moduleSettings.getVoPackageName(),
              generatorSettings.getTextVOPackageParentPath(),
              moduleSettings.getVoParentDirectory()
          },
          {
              generatorSettings.getDtoPackageReferenceEditorCombo(),
              moduleSettings.getDtoPackageName(),
              generatorSettings.getTextDTOPackageParentPath(),
              moduleSettings.getDtoParentDirectory()
          }
      };
      for (Object[] combo : combos) {
        String packageName = (String) combo[1];
        ((MyPackageNameReferenceEditorCombo) combo[0]).setText(packageName);
        if (StringUtils.isNotBlank(packageName)) {
          ((MyPackageNameReferenceEditorCombo) combo[0]).prependItem(packageName);
          ((MyPackageNameReferenceEditorCombo) combo[0]).appendItem(packageName);
        }
        ((JTextField) combo[2]).setText((String) combo[3]);
      }
    }
  }

  private Optional<Module> findModule(String moduleName) {
    return Optional.ofNullable(ModuleManager.getInstance(Holder.getProject())
        .findModuleByName(moduleName));
  }

  public static void show(List<TableSchema> tableSchemaList, Function<TableSchema, List<ColumnSchema>> mapping) {
    new AutoGeneratorSettingsDialog(Holder.getProject(), tableSchemaList, mapping).show();
  }
}
