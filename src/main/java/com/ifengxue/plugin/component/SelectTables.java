package com.ifengxue.plugin.component;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import lombok.Data;

@Data
public class SelectTables {

  private JPanel rootComponent;
  private JButton btnSelectOther;
  private JButton btnSelectAll;
  private JButton btnSelectNone;
  private JButton btnCancel;
  private JButton btnGenerate;
  private JButton btnSelectByRegex;
  private JPanel tablePanel;
  private JRadioButton duplicateActionIgnoreButton;
  private JRadioButton duplicateActionRewriteButton;
  private JRadioButton duplicateActionMergeButton;
  private JLabel lblSelectCount;
}
