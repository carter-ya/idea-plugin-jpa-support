package com.ifengxue.plugin.component;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.state.AutoGeneratorSettingsState;
import com.intellij.openapi.components.ServiceManager;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lombok.Getter;

@Getter
public class AutoGeneratorSettings {

  private JPanel rootComponent;
  private JTextField textRemoveFieldPrefix;
  private JTextField textExtendBaseClass;
  private JCheckBox chkBoxUseLombok;
  private JCheckBox chkBoxGenerateRepository;
  private JCheckBox chkBoxSerializable;
  private JCheckBox chkBoxGenerateClassComment;
  private JCheckBox chkBoxGenerateFieldComment;
  private JCheckBox chkBoxGenerateMethodComment;
  private JTextField textExcludeFields;
  private JButton btnChooseSuperClass;
  private JCheckBox chkBoxGenerateService;
  private JCheckBox chkBoxUseJava8DateType;
  private JTextField textRemoveTablePrefix;
  private JLabel addTableNamePrefix;
  private JTextField textAddTableNamePrefix;
  private JTextField textAddTableNameSuffix;
  private MyPackageNameReferenceEditorCombo entityPackageReferenceEditorCombo;
  private MyPackageNameReferenceEditorCombo repositoryPackageReferenceEditorCombo;
  private JComboBox<String> cbxModule;
  private JTextField textEntityPackageParentPath;
  private JTextField textRepositoryPackageParentPath;
  private JCheckBox chkBoxGenerateDefaultValue;
  private JCheckBox chkBoxGenerateDatetimeDefaultValue;
  private JCheckBox chkBoxUseFluidProgrammingStyle;

  private void createUIComponents() {
    AutoGeneratorSettingsState service = ServiceManager
        .getService(AutoGeneratorSettingsState.class);
    entityPackageReferenceEditorCombo = new MyPackageNameReferenceEditorCombo(service.getEntityPackageName(),
        Holder.getProject(), "", "Entity");
    repositoryPackageReferenceEditorCombo = new MyPackageNameReferenceEditorCombo(service.getRepositoryPackageName(),
        Holder.getProject(), "",
        "Repository");
  }

  public void setData(AutoGeneratorSettingsState data) {
    textRemoveFieldPrefix.setText(data.getRemoveFieldPrefix());
    chkBoxUseLombok.setSelected(data.isUseLombok());
    chkBoxGenerateRepository.setSelected(data.isGenerateRepository());
    chkBoxSerializable.setSelected(data.isSerializable());
    chkBoxGenerateClassComment.setSelected(data.isGenerateClassComment());
    chkBoxGenerateFieldComment.setSelected(data.isGenerateFieldComment());
    chkBoxGenerateMethodComment.setSelected(data.isGenerateMethodComment());
    textExcludeFields.setText(String.join(",", data.getIgnoredFields()));
    textExtendBaseClass.setText(data.getInheritedParentClassName());
    chkBoxUseJava8DateType.setSelected(data.isUseJava8DateType());
    textRemoveTablePrefix.setText(data.getRemoveEntityPrefix());
    textAddTableNamePrefix.setText(data.getAddEntityPrefix());
    textAddTableNameSuffix.setText(data.getAddEntitySuffix());
    entityPackageReferenceEditorCombo.setText(data.getEntityPackageName());
    repositoryPackageReferenceEditorCombo.setText(data.getRepositoryPackageName());
    cbxModule.setSelectedItem(data.getModuleName());
    textEntityPackageParentPath.setText(data.getEntityParentDirectory());
    textRepositoryPackageParentPath.setText(data.getRepositoryParentDirectory());
    chkBoxGenerateDefaultValue.setSelected(data.isGenerateDefaultValue());
    chkBoxGenerateDatetimeDefaultValue.setSelected(data.isGenerateDatetimeDefaultValue());
    chkBoxUseFluidProgrammingStyle.setSelected(data.isUseFluidProgrammingStyle());
  }

  public void getData(AutoGeneratorSettingsState data) {
    data.setRemoveFieldPrefix(textRemoveFieldPrefix.getText());
    data.setUseLombok(chkBoxUseLombok.isSelected());
    data.setGenerateRepository(chkBoxGenerateRepository.isSelected());
    data.setSerializable(chkBoxSerializable.isSelected());
    data.setGenerateClassComment(chkBoxGenerateClassComment.isSelected());
    data.setGenerateFieldComment(chkBoxGenerateFieldComment.isSelected());
    data.setGenerateMethodComment(chkBoxGenerateMethodComment.isSelected());
    data.setIgnoredFields(Arrays.stream(textExcludeFields.getText().split(",")).collect(Collectors.toSet()));
    data.setInheritedParentClassName(textExtendBaseClass.getText());
    data.setUseJava8DateType(chkBoxUseJava8DateType.isSelected());
    data.setRemoveEntityPrefix(textRemoveTablePrefix.getText());
    data.setAddEntityPrefix(textAddTableNamePrefix.getText());
    data.setAddEntitySuffix(textAddTableNameSuffix.getText());
    data.setEntityPackageName(entityPackageReferenceEditorCombo.getText());
    data.setRepositoryPackageName(repositoryPackageReferenceEditorCombo.getText());
    data.setModuleName(Optional.ofNullable(cbxModule.getSelectedItem()).map(Object::toString).orElse(""));
    data.setEntityParentDirectory(textEntityPackageParentPath.getText());
    data.setRepositoryParentDirectory(textRepositoryPackageParentPath.getText());
    data.setGenerateDefaultValue(chkBoxGenerateDefaultValue.isSelected());
    data.setGenerateDatetimeDefaultValue(chkBoxGenerateDatetimeDefaultValue.isSelected());
    data.setUseFluidProgrammingStyle(chkBoxUseFluidProgrammingStyle.isSelected());
  }
}
