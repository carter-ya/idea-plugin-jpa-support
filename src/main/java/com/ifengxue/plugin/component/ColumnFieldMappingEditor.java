package com.ifengxue.plugin.component;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import lombok.Data;

@Data
public class ColumnFieldMappingEditor {

  private JTextField textClassname;
  private JTextField textRepositoryName;
  private JPanel rootComponent;
  private JTable tableMapping;
}
