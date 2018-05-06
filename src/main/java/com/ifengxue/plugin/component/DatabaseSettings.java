package com.ifengxue.plugin.component;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class DatabaseSettings {

  private JPanel rootComponent;
  private JTextField textHost;
  private JTextField textUsername;
  private JTextField textDatabase;
  private JButton btnCancel;
  private JButton btnNext;
  private JPasswordField textPassword;
  private JTextField textPort;

  public JPanel getRootComponent() {
    return rootComponent;
  }

  public void setRootComponent(JPanel rootComponent) {
    this.rootComponent = rootComponent;
  }

  public JTextField getTextHost() {
    return textHost;
  }

  public void setTextHost(JTextField textHost) {
    this.textHost = textHost;
  }

  public JTextField getTextUsername() {
    return textUsername;
  }

  public void setTextUsername(JTextField textUsername) {
    this.textUsername = textUsername;
  }

  public JTextField getTextDatabase() {
    return textDatabase;
  }

  public void setTextDatabase(JTextField textDatabase) {
    this.textDatabase = textDatabase;
  }

  public JButton getBtnCancel() {
    return btnCancel;
  }

  public void setBtnCancel(JButton btnCancel) {
    this.btnCancel = btnCancel;
  }

  public JButton getBtnNext() {
    return btnNext;
  }

  public void setBtnNext(JButton btnNext) {
    this.btnNext = btnNext;
  }

  public JPasswordField getTextPassword() {
    return textPassword;
  }

  public void setTextPassword(JPasswordField textPassword) {
    this.textPassword = textPassword;
  }

  public JTextField getTextPort() {
    return textPort;
  }

  public void setTextPort(JTextField textPort) {
    this.textPort = textPort;
  }
}
