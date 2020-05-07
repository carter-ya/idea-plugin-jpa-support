package com.ifengxue.plugin.i18n;

import com.intellij.openapi.diagnostic.Logger;
import java.util.Locale;
import java.util.ResourceBundle;

public final class LocaleContextHolder {

  public static final LocaleItem[] LOCALE_ITEMS;
  private static final Logger LOGGER = Logger.getInstance(LocaleContextHolder.class);
  private static Locale currentLocale = Locale.getDefault();
  private static ResourceBundle resourceBundle;

  static {
    LOCALE_ITEMS = new LocaleItem[]{
        new LocaleItem(Locale.US, "English"),
        new LocaleItem(Locale.CHINESE, "简体中文")
    };
    resourceBundle = ResourceBundle.getBundle("/bundles/ui_i18n", currentLocale);
  }

  public static synchronized Locale getCurrentLocale() {
    return currentLocale;
  }

  public static synchronized void setCurrentLocale(Locale locale) {
    currentLocale = locale;
    Locale.setDefault(locale);
    resourceBundle = ResourceBundle.getBundle("/bundles/ui_i18n", locale);
    LOGGER.info("set current locale " + locale.toLanguageTag());
  }

  /**
   * 格式化i18n字符串
   *
   * @param key key
   * @param args 参数列表
   */
  public static String format(String key, Object... args) {
    return String.format(resourceBundle.getString(key), args);
  }
}
