package com.ifengxue.plugin.entity;

import com.intellij.database.model.DasObject;
import lombok.Getter;

public class DatabasePluginTableSchema extends TableSchema implements Selectable {

  private static final long serialVersionUID = -2363626231931472587L;
  @Getter
  private final DasObject dasObject;
  private boolean selected;

  public DatabasePluginTableSchema(DasObject dasObject) {
    this.dasObject = dasObject;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  public boolean isSelected() {
    return selected;
  }
}
