package com.ifengxue.plugin.entity;

import com.intellij.database.psi.DbTable;
import lombok.Getter;

public class DatabasePluginTableSchema extends TableSchema {

  @Getter
  private final DbTable dbTable;

  public DatabasePluginTableSchema(DbTable dbTable) {
    this.dbTable = dbTable;
  }
}
