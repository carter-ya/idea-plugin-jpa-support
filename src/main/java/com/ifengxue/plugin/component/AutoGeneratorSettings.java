package com.ifengxue.plugin.component;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AutoGeneratorSettings {

  private JPanel rootComponent;
  private JTextField textRemoveTablePrefix;
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

  public JPanel getRootComponent() {
    return rootComponent;
  }

  public JTextField getTextRemoveTablePrefix() {
    return textRemoveTablePrefix;
  }

  public JTextField getTextRemoveFieldPrefix() {
    return textRemoveFieldPrefix;
  }

  public JTextField getTextExtendBaseClass() {
    return textExtendBaseClass;
  }

  public JCheckBox getChkBoxUseLombok() {
    return chkBoxUseLombok;
  }

  public JCheckBox getChkBoxGenerateRepository() {
    return chkBoxGenerateRepository;
  }

  public JTextField getTextEntityPackage() {
    return textEntityPackage;
  }

  public JTextField getTxtRepositoryPackage() {
    return txtRepositoryPackage;
  }

  public JButton getBtnNext() {
    return btnNext;
  }

  public JCheckBox getChkBoxSerializable() {
    return chkBoxSerializable;
  }

  public JCheckBox getChkBoxGenerateClassComment() {
    return chkBoxGenerateClassComment;
  }

  public JCheckBox getChkBoxGenerateFieldComment() {
    return chkBoxGenerateFieldComment;
  }

  public JCheckBox getChkBoxGenerateMethodComment() {
    return chkBoxGenerateMethodComment;
  }

  public JButton getBtnCancel() {
    return btnCancel;
  }

  public JTextField getTextExcludeFields() {
    return textExcludeFields;
  }

  public JButton getBtnChooseEntityPackage() {
    return btnChooseEntityPackage;
  }

  public JButton getBtnChooseRepositoryPackage() {
    return btnChooseRepositoryPackage;
  }

  public JButton getBtnChooseSuperClass() {
    return btnChooseSuperClass;
  }
}
