package com.ifengxue.plugin.component;

import com.ifengxue.plugin.Holder;
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
  private JCheckBox chkBoxUseJava8DataType;
  private JTextField textRemoveTablePrefix;
  private JLabel addTableNamePrefix;
  private JTextField textAddTableNamePrefix;
  private JTextField textAddTableNameSuffix;
  private MyPackageNameReferenceEditorCombo entityPackageReferenceEditorCombo;
  private MyPackageNameReferenceEditorCombo repositoryPackageReferenceEditorCombo;
  private JComboBox<String> cbxModule;
  private JTextField textEntityPackageParentPath;
  private JTextField textRepositoryPackageParentPath;

  private void createUIComponents() {
    entityPackageReferenceEditorCombo = new MyPackageNameReferenceEditorCombo(null, Holder.getProject(), "", "Entity");
    repositoryPackageReferenceEditorCombo = new MyPackageNameReferenceEditorCombo(null, Holder.getProject(), "",
        "Repository");
  }
}
