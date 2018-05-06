package com.ifengxue.plugin.util;

import javax.swing.JFrame;
import javax.swing.JTextField;

public enum Args {
  ;

  public static String notEmpty(String text, String name, JFrame parent) {
    if (text == null || (text = text.trim()).isEmpty()) {
    }
    return text;
  }

  public static String notEmpty(JTextField textField) {
    String text = textField.getText().trim();
    if (text.isEmpty()) {
      textField.requestFocus();
      return null;
    }
    return text;
  }
}
