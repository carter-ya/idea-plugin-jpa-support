package com.ifengxue.plugin.component;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import lombok.Getter;

@Getter
public class AutoGeneratorSettings {

  private JPanel rootComponent;
  private JTextField textRemoveFieldPrefix;
  private JTextField textExtendBaseClass;
  private JCheckBox chkBoxUseLombok;
  private JCheckBox chkBoxGenerateRepository;
  private JTextField textEntityPackage;
  private JTextField txtRepositoryPackage;
  private JButton btnNext;
  private JCheckBox chkBoxSerializable;
  private JCheckBox chkBoxGenerateClassComment;
  private JCheckBox chkBoxGenerateFieldComment;
  private JCheckBox chkBoxGenerateMethodComment;
  private JButton btnCancel;
  private JTextField textExcludeFields;
  private JButton btnChooseEntityPackage;
  private JButton btnChooseRepositoryPackage;
  private JButton btnChooseSuperClass;
  private JCheckBox chkBoxGenerateService;
  private JCheckBox chkBoxUseJava8DataType;
  private JTextField textRemoveTablePrefix;
  private JLabel addTableNamePrefix;
  private JTextField textAddTableNamePrefix;
  private JTextField textAddTableNameSuffix;
}
