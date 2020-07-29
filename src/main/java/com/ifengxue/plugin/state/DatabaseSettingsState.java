package com.ifengxue.plugin.state;

import com.ifengxue.plugin.adapter.DatabaseDrivers;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@State(name = "DatabaseSettingsState", storages = {
    @Storage(value = StateConstants.PROJECT_STATE_NAME, roamingType = RoamingType.DISABLED)
})
public class DatabaseSettingsState implements PersistentStateComponent<DatabaseSettingsState> {

  private String host = "localhost";
  private int port = 3306;
  private String username = "root";
  private String database = "";
  private String url = "";
  private String language = LocaleContextHolder.getCurrentLocale().toLanguageTag();
  private String databaseDriver = DatabaseDrivers.MYSQL.toString();
  private Map<String, String> databaseDriverToJarPath = new HashMap<>();
  /**
   * 是否需要保存数据库密码
   */
  private boolean requireSavePassword = true;

  @Nullable
  @Override
  public DatabaseSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(DatabaseSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public String getJarPath(DatabaseDrivers drivers) {
    return databaseDriverToJarPath.get(drivers.toString());
  }

  public void registerJarPath(DatabaseDrivers drivers, String jarPath) {
    databaseDriverToJarPath.put(drivers.toString(), jarPath);
  }
}
