package com.ifengxue.plugin;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import java.util.Objects;

public class Holder {

  private static volatile AnActionEvent eventHolder;
  private static volatile PropertiesComponent applicationProperties;
  private static volatile PropertiesComponent projectProperties;

  public static synchronized void registerEvent(AnActionEvent event) {
    eventHolder = event;
  }

  public static synchronized AnActionEvent getEvent() {
    Objects.requireNonNull(eventHolder);
    return eventHolder;
  }

  public static synchronized void registerApplicationProperties(PropertiesComponent propertiesComponent) {
    applicationProperties = propertiesComponent;
  }

  public static synchronized PropertiesComponent getApplicationProperties() {
    return applicationProperties;
  }

  public static synchronized void registerProjectProperties(PropertiesComponent propertiesComponent) {
    projectProperties = propertiesComponent;
  }

  public static synchronized PropertiesComponent getProjectProperties() {
    return projectProperties;
  }
}
