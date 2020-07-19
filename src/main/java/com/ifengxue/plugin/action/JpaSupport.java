package com.ifengxue.plugin.action;

import com.ifengxue.plugin.gui.DatabaseSettingsDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class JpaSupport extends AbstractPluginSupport {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    super.actionPerformed(e);
    DatabaseSettingsDialog.showDialog();
  }
}
