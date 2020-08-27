package com.ifengxue.plugin.component;

import com.ifengxue.plugin.entity.Table;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lombok.Data;

@Data
public class ColumnFieldMappingEditor {

  private JTextField textClassname;
  private JTextField textRepositoryName;
  private JPanel rootComponent;
  private JTextField textComment;
  private JPanel tablePanel;

  public void setData(Table table) {
    textClassname.setText(table.getEntityName());
    textRepositoryName.setText(table.getRepositoryName());
    textComment.setText(table.getTableComment());
  }

  public void getData(Table table) {
    table.setEntityName(textClassname.getText());
    table.setRepositoryName(textRepositoryName.getText());
    table.setTableComment(textComment.getText());
  }
}
