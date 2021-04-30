package com.ifengxue.plugin.component;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.i18n.LocaleItem;
import com.ifengxue.plugin.state.DatabaseSettingsState;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.fields.ExpandableTextField;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;

@Data
public class DatabaseSettings {

  private JPanel rootComponent;
  private JTextField textHost;
  private JTextField textUsername;
  private JTextField textDatabase;
  private JPasswordField textPassword;
  private JTextField textPort;
  private JLabel lblSelectLanguage;
  private JTextField textConnectionUrl;
  private JComboBox<LocaleItem> cbxSelectLanguage;
  private JCheckBox chkBoxRequireSavePassword;
  private TextFieldWithBrowseButton textDriverPath;
  private JTextField textDriverClass;
  private ExpandableTextField textPreviewConnectionUrl;

  public void setData(DatabaseSettingsState data) {
    textHost.setText(data.getHost());
    textPort.setText(data.getPort() + "");
    textUsername.setText(data.getUsername());
    textDatabase.setText(data.getDatabase());
    textConnectionUrl.setText(data.getUrl());
    chkBoxRequireSavePassword.setSelected(data.isRequireSavePassword());
    textDriverPath.setText(data.getDriverPath());
    textDriverClass.setText(data.getDriverClass());
    // preview url
    String previewUrl = Holder.getJdbcConfigUtil()
        .tryParseUrl(data.getDriverClass(), data.getHost(),
            data.getPort() + "", data.getUrl(), "",
            data.getDatabase(), data.getUrl());
    textPreviewConnectionUrl.setText(previewUrl);
    // select language
    Locale locale = LocaleContextHolder.getCurrentLocale();
    cbxSelectLanguage.removeAllItems();
    for (LocaleItem localeItem : LocaleContextHolder.LOCALE_ITEMS) {
      cbxSelectLanguage.addItem(localeItem);
      if (locale.equals(localeItem.getLocale())) {
        cbxSelectLanguage.setSelectedItem(localeItem);
      }
    }
  }

  public void getData(DatabaseSettingsState data) {
    data.setHost(textHost.getText());
    if (NumberUtils.isDigits(textPort.getText())) {
      data.setPort(Integer.parseInt(textPort.getText()));
    }
    data.setUsername(textUsername.getText());
    data.setDatabase(textDatabase.getText());
    data.setUrl(textConnectionUrl.getText());
    data.setLanguage(((LocaleItem) Objects.requireNonNull(cbxSelectLanguage.getSelectedItem())).getLanguageTag());
    data.setRequireSavePassword(chkBoxRequireSavePassword.isSelected());
    data.setDriverPath(textDriverPath.getText());
    data.setDriverClass(textDriverClass.getText());
  }
}
