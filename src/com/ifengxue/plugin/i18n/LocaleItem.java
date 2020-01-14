package com.ifengxue.plugin.i18n;

import java.io.Serializable;
import java.util.Locale;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class LocaleItem implements Serializable, Comparable<LocaleItem> {

  private static final long serialVersionUID = 4358892090079199203L;
  private final Locale locale;
  private final String value;

  public String getLanguageTag() {
    return locale.toLanguageTag();
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public int compareTo(@NotNull LocaleItem o) {
    return locale.toLanguageTag().compareTo(o.getLocale().toLanguageTag());
  }
}
