package com.ifengxue.plugin.action;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.gui.DatabaseSettingsDialog;
import com.ifengxue.plugin.util.BusUtil;
import com.ifengxue.plugin.util.JdbcConfigUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import java.io.InputStream;
import org.jetbrains.annotations.NotNull;

public class JpaSupport extends AbstractPluginSupport {

  private final Logger log = Logger.getInstance(JpaSupport.class);

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }
  
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    super.actionPerformed(e);
    ApplicationManager.getApplication().runReadAction(() -> {
      try (InputStream in = getClass().getClassLoader().getResourceAsStream("jdbc_config.json")) {
        JdbcConfigUtil jdbcConfigUtil = new JdbcConfigUtil(in);
        Holder.registerJdbcConfigUtil(jdbcConfigUtil);
        ApplicationManager.getApplication().invokeLater(DatabaseSettingsDialog::showDialog);
      } catch (Exception ex) {
        log.error("Can't load jdbc config", ex);
        ApplicationManager.getApplication().invokeLater(() -> BusUtil.notify(e.getProject(),
            "Can't load jdbc config, error message is " + ex.getMessage(),
            NotificationType.ERROR));
      }
    });
  }
}
