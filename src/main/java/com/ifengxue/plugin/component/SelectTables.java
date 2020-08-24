package com.ifengxue.plugin.component;

import com.ifengxue.plugin.entity.Table;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import lombok.Data;

@Data
public class SelectTables {

  private final List<Table> tableList;
  private JTable tblTableSchema;
  private JPanel rootComponent;
  private JButton btnSelectOther;
  private JButton btnSelectAll;
  private JButton btnSelectNone;
  private JButton btnCancel;
  private JButton btnGenerate;
  private JButton btnSelectByRegex;
  private JButton btnTest;

  public SelectTables(List<Table> tableList) {
    this.tableList = tableList;
  }
  
}
