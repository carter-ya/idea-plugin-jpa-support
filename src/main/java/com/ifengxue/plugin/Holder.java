package com.ifengxue.plugin;

import com.ifengxue.plugin.adapter.DatabaseDrivers;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fastjdbc.FastJdbc;
import java.io.IOException;
import java.util.Objects;

public class Holder {

  private static volatile Project project;
  private static volatile AnActionEvent eventHolder;
  private static volatile PropertiesComponent applicationProperties;
  private static volatile PropertiesComponent projectProperties;
  private static volatile DatabaseDrivers databaseDrivers;
  private static volatile FastJdbc fastJdbc;
  private static volatile boolean selectAllTables = false;

  public static synchronized void registerProject(Project project) {
    Holder.project = project;
  }

  public static synchronized Project getProject() {
    return project;
  }

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

  public static synchronized void registerDatabaseDrivers(DatabaseDrivers databaseDrivers) {
    Holder.databaseDrivers = databaseDrivers;
  }

  public static DatabaseDrivers getDatabaseDrivers() {
    return databaseDrivers;
  }

  public static synchronized void registerFastJdbc(FastJdbc fastJdbc) {
    if (Holder.fastJdbc != null) {
      try {
        Holder.fastJdbc.close();
      } catch (IOException e) {
        // ignore
      }
    }
    Holder.fastJdbc = fastJdbc;
  }

  public static synchronized FastJdbc getFastJdbc() {
    return fastJdbc;
  }

  public static synchronized void setSelectAllTables(boolean selectAllTables) {
    Holder.selectAllTables = selectAllTables;
  }

  public static synchronized boolean isSelectAllTables() {
    return selectAllTables;
  }
}
