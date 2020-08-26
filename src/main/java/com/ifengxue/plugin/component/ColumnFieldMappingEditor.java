package com.ifengxue.plugin.component;

import com.ifengxue.plugin.entity.Table;
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

  public void setData(Table table) {
    textClassname.setText(table.getEntityName());
    textRepositoryName.setText(table.getRepositoryName());
  }

  public void getData(Table table) {
    table.setEntityName(textClassname.getText());
    table.setRepositoryName(textRepositoryName.getText());
  }
}
