package com.ifengxue.plugin.component;

import javax.swing.JButton;
import javax.swing.JPanel;
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
}
