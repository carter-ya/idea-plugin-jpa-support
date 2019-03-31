package com.ifengxue.plugin.component;

import com.ifengxue.plugin.i18n.LocaleItem;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import lombok.Data;

@Data
public class DatabaseSettings {

  private JPanel rootComponent;
  private JTextField textHost;
  private JTextField textUsername;
  private JTextField textDatabase;
  private JButton btnCancel;
  private JButton btnNext;
  private JPasswordField textPassword;
  private JTextField textPort;
  private JLabel lblSelectLanguage;
  private JComboBox<LocaleItem> cbxSelectLanguage;
}
