package com.ifengxue.plugin.util;

import com.intellij.database.psi.DbTable;
import com.intellij.psi.PsiElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum DatabasePluginUtil {
  ;
  public static List<DbTable> resolveDbTables(PsiElement[] elements) {
    if (elements == null || elements.length == 0) {
      return Collections.emptyList();
    }
    List<DbTable> tables = new ArrayList<>(elements.length);
    for (PsiElement element : elements) {
      if (element instanceof DbTable) {
        tables.add((DbTable) element);
      }
    }
    return tables;
  }
}
