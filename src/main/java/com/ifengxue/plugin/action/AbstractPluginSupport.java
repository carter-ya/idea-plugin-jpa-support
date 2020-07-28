package com.ifengxue.plugin.action;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.adapter.DatabaseDrivers;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.i18n.LocaleItem;
import com.ifengxue.plugin.state.DatabaseSettingsState;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import java.util.Locale;
import java.util.Objects;

public abstract class AbstractPluginSupport extends AnAction {

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setEnabledAndVisible(e.getProject() != null);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    Objects.requireNonNull(e.getProject(), "Project inactivated.");

    // 初始化Holder
    Holder.registerProject(e.getProject());
    Holder.registerEvent(e);// 注册事件
    Holder.registerApplicationProperties(PropertiesComponent.getInstance());
    Holder.registerProjectProperties(PropertiesComponent.getInstance(e.getProject()));
    Holder.registerDatabaseDrivers(DatabaseDrivers.MYSQL);

    initI18n();
  }

  /**
   * 初始化I18n
   */
  private void initI18n() {
    DatabaseSettingsState databaseSettingsState = ServiceManager
        .getService(Holder.getProject(), DatabaseSettingsState.class);
    // 选择语言
    Locale locale = Locale.forLanguageTag(databaseSettingsState.getLanguage());
    int localeSelectIndex = -1;
    for (int i = 0; i < LocaleContextHolder.LOCALE_ITEMS.length; i++) {
      LocaleItem localeItem = LocaleContextHolder.LOCALE_ITEMS[i];
      if (localeItem.getLocale().equals(locale)) {
        localeSelectIndex = i;
        break;
      }
    }
    // only compare by language
    if (localeSelectIndex == -1) {
      for (int i = 0; i < LocaleContextHolder.LOCALE_ITEMS.length; i++) {
        LocaleItem localeItem = LocaleContextHolder.LOCALE_ITEMS[i];
        if (localeItem.getLocale().getLanguage().equalsIgnoreCase(locale.getLanguage())) {
          localeSelectIndex = i;
          break;
        }
      }
    }
    // not best match language for this locale, reset locale to english
    if (localeSelectIndex == -1) {
      localeSelectIndex = 0;
    }
    LocaleContextHolder.setCurrentLocale(LocaleContextHolder.LOCALE_ITEMS[localeSelectIndex].getLocale());
  }
}
