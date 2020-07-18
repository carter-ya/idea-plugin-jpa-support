package com.ifengxue.plugin.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.project.Project;

public enum BusUtil {
  ;

  public static void notify(Project project, String message, NotificationType notificationType) {
    Bus.notify(new Notification("JpaSupport", "Error", message, notificationType), project);
  }
}
