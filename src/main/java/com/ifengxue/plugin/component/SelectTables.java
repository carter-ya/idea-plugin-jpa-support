package com.ifengxue.plugin.component;

import com.ifengxue.plugin.entity.Table;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

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

  public SelectTables(List<Table> tableList) {
    this.tableList = tableList;
  }

  public JTable getTblTableSchema() {
    return tblTableSchema;
  }

  public JPanel getRootComponent() {
    return rootComponent;
  }

  public JButton getBtnSelectOther() {
    return btnSelectOther;
  }

  public JButton getBtnSelectAll() {
    return btnSelectAll;
  }

  public JButton getBtnSelectNone() {
    return btnSelectNone;
  }

  public JButton getBtnCancel() {
    return btnCancel;
  }

  public JButton getBtnGenerate() {
    return btnGenerate;
  }

  public JButton getBtnSelectByRegex() {
    return btnSelectByRegex;
  }

  public List<Table> getTableList() {
    return tableList;
  }
}
