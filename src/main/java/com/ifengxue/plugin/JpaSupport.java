package com.ifengxue.plugin;

import static com.ifengxue.plugin.util.Key.createKey;
import static org.apache.commons.lang3.StringUtils.trim;

import com.ifengxue.plugin.adapter.DatabaseDrivers;
import com.ifengxue.plugin.adapter.DriverDelegate;
import com.ifengxue.plugin.component.DatabaseSettings;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.gui.AutoGeneratorSettingsFrame;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.i18n.LocaleItem;
import com.ifengxue.plugin.util.WindowUtil;
import com.intellij.ide.util.DirectoryUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.download.DownloadableFileDescription;
import com.intellij.util.download.DownloadableFileService;
import com.intellij.util.download.FileDownloader;
import com.intellij.util.lang.UrlClassLoader;
import fastjdbc.FastJdbc;
import fastjdbc.NoPoolDataSource;
import fastjdbc.SimpleFastJdbc;
import java.awt.event.ItemEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * jpa support 入口
 */
public class JpaSupport extends AnAction {

  private static final String DRIVER_VENDOR_PATH = "driver_vendor";
  private Logger log = Logger.getInstance(JpaSupport.class);
  private DatabaseSettings databaseSettings;
  public static AtomicReference<ClassLoader> classLoaderRef = new AtomicReference<>(JpaSupport.class.getClassLoader());

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (e.getProject() == null) {
      Messages.showWarningDialog("Project not activated!", "Jps Support");
      return;
    }
    Holder.registerProject(e.getProject());
    Holder.registerEvent(e);// 注册事件
    Holder.registerApplicationProperties(PropertiesComponent.getInstance());
    Holder.registerProjectProperties(PropertiesComponent.getInstance(e.getProject()));
    Holder.registerDatabaseDrivers(DatabaseDrivers.MYSQL);
    initI18n();

    JFrame databaseSettingsFrame = new JFrame(LocaleContextHolder.format("set_up_database_connection"));
    databaseSettings = new DatabaseSettings();
    databaseSettingsFrame.setContentPane(databaseSettings.getRootComponent());
    databaseSettingsFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    databaseSettingsFrame.setLocationRelativeTo(WindowUtil.getParentWindow(e.getProject()));
    databaseSettingsFrame.pack();
    // init text field
    initTextField(databaseSettings);
    databaseSettings.getTextHost().getDocument().addDocumentListener(new ConnectionUrlUpdateListener());
    databaseSettings.getTextPort().getDocument().addDocumentListener(new ConnectionUrlUpdateListener());
    databaseSettings.getTextUsername().getDocument().addDocumentListener(new ConnectionUrlUpdateListener());
    databaseSettings.getTextDatabase().getDocument().addDocumentListener(new ConnectionUrlUpdateListener());
    databaseSettingsFrame.setVisible(true);

    // 注册取消事件
    databaseSettings.getBtnCancel().addActionListener(event -> databaseSettingsFrame.dispose());
    // 注册语言切换事件
    databaseSettings.getCbxSelectLanguage().addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() != ItemEvent.SELECTED) {
        return;
      }
      LocaleContextHolder.setCurrentLocale(((LocaleItem) itemEvent.getItem()).getLocale());
      WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
        PropertiesComponent applicationProperties = Holder.getApplicationProperties();
        applicationProperties
            .setValue(createKey("locale"),
                ((LocaleItem) databaseSettings.getCbxSelectLanguage().getSelectedItem()).getLanguageTag());
      });
    });
    // 注册数据库类型切换事件
    databaseSettings.getCbxSelectDatabase().addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() != ItemEvent.SELECTED) {
        return;
      }
      new DownloadDriverRunnable(e.getProject(), null, (DatabaseDrivers) itemEvent.getItem())
          .run();
    });
    // 注册下一步事件
    databaseSettings.getBtnNext().addActionListener(event -> {
      String host = databaseSettings.getTextHost().getText().trim();
      if (host.isEmpty()) {
        databaseSettings.getTextHost().requestFocus();
        return;
      }
      String port = databaseSettings.getTextPort().getText().trim();
      if (port.isEmpty()) {
        databaseSettings.getTextPort().requestFocus();
        return;
      }
      String username = databaseSettings.getTextUsername().getText().trim();
      if (username.isEmpty()) {
        databaseSettings.getTextUsername().requestFocus();
        return;
      }
      databaseSettings.getTextPassword().getPassword();
      String password = new String(databaseSettings.getTextPassword().getPassword()).trim();
      if (password.isEmpty()) {
        databaseSettings.getTextPassword().requestFocus();
        return;
      }
      String database = databaseSettings.getTextDatabase().getText().trim();
      if (database.isEmpty()) {
        databaseSettings.getTextDatabase().requestFocus();
        return;
      }
      String connectionUrl = databaseSettings.getTextConnectionUrl().getText().trim();
      if (connectionUrl.isEmpty()) {
        databaseSettings.getTextConnectionUrl().requestFocus();
        return;
      }
      saveTextField(host, port, username, password, database, connectionUrl);
      new Thread(() -> {
        try {
          if (!driverHasBeenLoaded(Holder.getDatabaseDrivers())) {
            try {
              Class.forName(Holder.getDatabaseDrivers().getDriverClass(), true, classLoaderRef.get());
            } catch (ClassNotFoundException ex) {
              // driver not loaded
              ApplicationManager.getApplication().invokeAndWait(() -> {
                int selectButton = Messages.showOkCancelDialog(e.getProject(),
                    LocaleContextHolder.format("driver_not_found", Holder.getDatabaseDrivers().getDriverClass()),
                    LocaleContextHolder.format("prompt"),
                    Messages.getQuestionIcon());
                if (selectButton == Messages.OK) {
                  new DownloadDriverRunnable(e.getProject(), null, Holder.getDatabaseDrivers()).run();
                }
              });
            }
            Driver driver = (Driver) Class
                .forName(Holder.getDatabaseDrivers().getDriverClass(), true, classLoaderRef.get())
                .newInstance();
            DriverManager.registerDriver(new DriverDelegate(driver));
          }
        } catch (ReflectiveOperationException | SQLException e1) {
          ApplicationManager.getApplication().invokeLater(() -> Bus
              .notify(new Notification("JpaSupport", "Error",
                  LocaleContextHolder.format("database_not_exists", Holder.getDatabaseDrivers().getDriverClass()),
                  NotificationType.ERROR)));
          return;
        }
        // 尝试获取连接
        try (Connection connection = DriverManager.getConnection(connectionUrl, username, password)) {
          FastJdbc fastJdbc = new SimpleFastJdbc(new NoPoolDataSource(connectionUrl, username, password));
          Holder.registerFastJdbc(fastJdbc);
        } catch (SQLException se) {
          ApplicationManager.getApplication().invokeLater(() -> Bus
              .notify(new Notification("JpaSupport", "Error",
                  LocaleContextHolder.format("connect_to_database_failed",
                      se.getErrorCode(), se.getSQLState(), se.getLocalizedMessage()), NotificationType.ERROR)));
          log.error("connect to database failed", se);
          return;
        }

        List<TableSchema> tableSchemaList = null;
        try {
          tableSchemaList = Holder.getDatabaseDrivers().getDriverAdapter().findDatabaseSchemas(database);
        } catch (SQLException se) {
          StringBuilder sb = new StringBuilder();
          sb.append("SQL error code: ").append(se.getErrorCode())
              .append("\nSQL error state: ").append(se.getSQLState()).append("\n");
          sb.append("Error message: ").append(se.getLocalizedMessage());
          ApplicationManager.getApplication()
              .invokeLater(() -> Bus.notify(new Notification("JpaSupport", "Error",
                  sb.toString(), NotificationType.ERROR)));
        }

        // 显示自动生成器配置窗口
        AutoGeneratorSettingsFrame.show(tableSchemaList);

        databaseSettingsFrame.dispose();// 释放数据库设置窗口
      }).start();
    });
  }

  private void loadDriverClass(VirtualFile virtualFile, DatabaseDrivers databaseDrivers) {
    if (driverHasBeenLoaded(databaseDrivers)) {
      Holder.registerDatabaseDrivers(databaseDrivers);
      ApplicationManager.getApplication().invokeLater(() -> updateConnectionUrl(true));
      return;
    }
    log.info("driver path" + virtualFile.getPath());
    try {
      UrlClassLoader urlClassLoader = UrlClassLoader.build()
          .urls(new File(virtualFile.getPath()).toURI().toURL())
          .get();
      Driver driver = (Driver) urlClassLoader.loadClass(databaseDrivers.getDriverClass()).newInstance();
      DriverManager.registerDriver(new DriverDelegate(driver));
      log.info("driver " + databaseDrivers.getDriverClass() + " has been loaded");
      classLoaderRef.set(urlClassLoader);
      Holder.registerDatabaseDrivers(databaseDrivers);
      ApplicationManager.getApplication().invokeLater(() -> updateConnectionUrl(true));

      // save database driver path
      PropertiesComponent applicationProperties = Holder.getApplicationProperties();
      applicationProperties.setValue(
          createKey("database_driver_path", databaseDrivers.getVendor(), databaseDrivers.getVersion()),
          virtualFile.getPath());
    } catch (MalformedURLException ex) {
      log.error("url not valid " + databaseDrivers.getDriverClass(), ex);
      ApplicationManager.getApplication().invokeLater(() -> Bus.notify(
          new Notification("JpaSupport", "Error",
              "url not valid " + databaseDrivers.getDriverClass(),
              NotificationType.ERROR)
      ));
    } catch (ReflectiveOperationException | SQLException ex) {
      log.error("driver class not found", ex);
      ApplicationManager.getApplication().invokeLater(() -> Bus.notify(
          new Notification("JpaSupport", "Error",
              "driver class not found: " + databaseDrivers.getDriverClass(), NotificationType.ERROR)
      ));
    }
  }

  private boolean driverHasBeenLoaded(DatabaseDrivers databaseDrivers) {
    Enumeration<Driver> driverEnumeration = DriverManager.getDrivers();
    while (driverEnumeration.hasMoreElements()) {
      Driver driver = driverEnumeration.nextElement();
      String driverName = driver.getClass().getName();
      if (driver instanceof DriverDelegate) {
        driverName = ((DriverDelegate) driver).getDriver().getClass().getName();
      }
      if (driverName.equals(databaseDrivers.getDriverClass())) {
        return true;
      }
    }
    return false;
  }

  private void updateConnectionUrl(boolean switchDatabaseVendor) {
    String oldConnectionUrl = trim(databaseSettings.getTextConnectionUrl().getText());
    if (switchDatabaseVendor) {
      oldConnectionUrl = "";
    }
    String newConnectionUrl = Holder.getDatabaseDrivers().getDriverAdapter().toConnectionUrl(
        oldConnectionUrl,
        trim(databaseSettings.getTextHost().getText()),
        trim(databaseSettings.getTextPort().getText()),
        trim(databaseSettings.getTextUsername().getText()),
        trim(databaseSettings.getTextDatabase().getText()));
    databaseSettings.getTextConnectionUrl().setText(newConnectionUrl);
  }

  private void saveTextField(String host, String port, String username, String password, String database,
      String connectionUrl) {
    PropertiesComponent applicationProperties = Holder.getApplicationProperties();
    applicationProperties.setValue(createKey("host"), host);
    applicationProperties.setValue(createKey("port"), port);
    applicationProperties.setValue(createKey("username"), username);
    applicationProperties
        .setValue(createKey("password"), Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
    applicationProperties.setValue(createKey("database"), database);
    applicationProperties.setValue(createKey("url"), connectionUrl);
    applicationProperties
        .setValue(createKey("locale"),
            ((LocaleItem) databaseSettings.getCbxSelectLanguage().getSelectedItem()).getLanguageTag());
    applicationProperties.setValue(createKey("database_vendor"),
        ((DatabaseDrivers) databaseSettings.getCbxSelectDatabase().getSelectedItem()).toString());
  }

  private void initTextField(DatabaseSettings databaseSettings) {
    PropertiesComponent applicationProperties = Holder.getApplicationProperties();
    databaseSettings.getTextHost().setText(applicationProperties.getValue(createKey("host"), "localhost"));
    databaseSettings.getTextPort().setText(applicationProperties.getValue(createKey("port"), "3306"));
    databaseSettings.getTextUsername().setText(applicationProperties.getValue(createKey("username"), "root"));
    databaseSettings.getTextPassword()
        .setText(new String(Base64.getDecoder().decode(applicationProperties.getValue(createKey("password"), "")),
            StandardCharsets.UTF_8));
    databaseSettings.getTextDatabase().setText(applicationProperties.getValue(createKey("database"), ""));
    databaseSettings.getTextConnectionUrl().setText(applicationProperties.getValue(createKey("url"), ""));

    // select language
    Locale locale = LocaleContextHolder.getCurrentLocale();
    databaseSettings.getCbxSelectLanguage().removeAllItems();
    for (LocaleItem localeItem : LocaleContextHolder.LOCALE_ITEMS) {
      databaseSettings.getCbxSelectLanguage().addItem(localeItem);
      if (locale.equals(localeItem.getLocale())) {
        databaseSettings.getCbxSelectLanguage().setSelectedItem(localeItem);
      }
    }

    // select database
    databaseSettings.getCbxSelectDatabase().removeAllItems();
    String databaseVendor = applicationProperties
        .getValue(createKey("database_vendor"), DatabaseDrivers.MYSQL.toString());
    for (DatabaseDrivers databaseDrivers : DatabaseDrivers.values()) {
      databaseSettings.getCbxSelectDatabase().addItem(databaseDrivers);
      if (databaseDrivers.toString().equalsIgnoreCase(databaseVendor)) {
        databaseSettings.getCbxSelectDatabase().setSelectedItem(databaseDrivers);
        Holder.registerDatabaseDrivers(databaseDrivers);
      }
    }

    // update connection url
    updateConnectionUrl(false);

    DatabaseDrivers databaseDrivers = Holder.getDatabaseDrivers();
    // load driver path
    String databaseDriverPath = applicationProperties
        .getValue(createKey("database_driver_path", databaseDrivers.getVendor(), databaseDrivers.getVersion()));
    if (StringUtils.isNotBlank(databaseDriverPath)) {
      try {
        classLoaderRef.set(UrlClassLoader.build()
            .urls(new File(databaseDriverPath).toURI().toURL())
            .get());
      } catch (MalformedURLException e) {
        log.error("url not valid " + databaseDrivers.getDriverClass(), e);
        ApplicationManager.getApplication().invokeLater(() -> Bus.notify(
            new Notification("JpaSupport", "Error",
                "url not valid " + databaseDrivers.getDriverClass(),
                NotificationType.ERROR)));
      }
    }
  }

  private void initI18n() {
    PropertiesComponent applicationProperties = Holder.getApplicationProperties();
    // select language
    Locale locale = Locale.forLanguageTag(applicationProperties
        .getValue(createKey("locale"), LocaleContextHolder.getCurrentLocale().toLanguageTag()));
    int localeSelectIndex = -1;
    for (int i = 0; i < LocaleContextHolder.LOCALE_ITEMS.length; i++) {
      LocaleItem localeItem = LocaleContextHolder.LOCALE_ITEMS[i];
      if (localeItem.getLocale().equals(locale)) {
        localeSelectIndex = i;
        break;
      }
    }
    // only compare by language
    if (localeSelectIndex == -1) {
      for (int i = 0; i < LocaleContextHolder.LOCALE_ITEMS.length; i++) {
        LocaleItem localeItem = LocaleContextHolder.LOCALE_ITEMS[i];
        if (localeItem.getLocale().getLanguage().equalsIgnoreCase(locale.getLanguage())) {
          localeSelectIndex = i;
          break;
        }
      }
    }
    // not best match language for this locale, reset locale to english
    if (localeSelectIndex == -1) {
      localeSelectIndex = 0;
    }
    LocaleContextHolder.setCurrentLocale(LocaleContextHolder.LOCALE_ITEMS[localeSelectIndex].getLocale());
  }

  private class ConnectionUrlUpdateListener implements DocumentListener {

    private final boolean switchDatabaseVendor;

    public ConnectionUrlUpdateListener() {
      this(false);
    }

    public ConnectionUrlUpdateListener(boolean switchDatabaseVendor) {
      this.switchDatabaseVendor = switchDatabaseVendor;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      JpaSupport.this.updateConnectionUrl(switchDatabaseVendor);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      JpaSupport.this.updateConnectionUrl(switchDatabaseVendor);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      JpaSupport.this.updateConnectionUrl(switchDatabaseVendor);
    }
  }

  private class DownloadDriverRunnable implements Runnable {

    private final Project project;
    private final JComponent parentComponent;
    private final DatabaseDrivers databaseDrivers;

    private DownloadDriverRunnable(Project project, JComponent parentComponent, DatabaseDrivers databaseDrivers) {
      this.project = project;
      this.parentComponent = parentComponent;
      this.databaseDrivers = databaseDrivers;
    }

    @Override
    public void run() {
      WriteCommandAction.runWriteCommandAction(project, () -> {
        // fix issue #15, DirectoryUtil.mkdirs not support path separator '\'
        String dirPath = System.getProperty("user.dir") + File.separator + DRIVER_VENDOR_PATH;
        if (File.separatorChar != '/') {
          dirPath = dirPath.replace('\\', '/');
        }
        PsiDirectory driverVendorPath = DirectoryUtil.mkdirs(PsiManager.getInstance(project), dirPath);
        PsiFile jarFile = driverVendorPath.findFile(databaseDrivers.getJarFilename());
        // driver 不存在，需要下载
        if (jarFile == null) {
          if (databaseDrivers.getUrl().startsWith(DatabaseDrivers.CLASSPATH_PREFIX)) {
            String filePath = databaseDrivers.getUrl().substring(DatabaseDrivers.CLASSPATH_PREFIX.length());
            try (BufferedInputStream bis = new BufferedInputStream(
                Optional.ofNullable(getClass().getClassLoader().getResourceAsStream(filePath))
                    .orElseThrow(NoSuchElementException::new))) {
              int available = bis.available();
              byte[] bytes = new byte[available];
              bis.read(bytes);
              PsiFile file = driverVendorPath.createFile(databaseDrivers.getJarFilename());
              VirtualFile virtualFile = file.getVirtualFile();
              virtualFile.setWritable(true);
              virtualFile.setCharset(StandardCharsets.UTF_8);
              virtualFile.setBinaryContent(bytes);
              loadDriverClass(virtualFile, databaseDrivers);
              return;
            } catch (IOException e) {
              log.error("copy file error, file path " + databaseDrivers.getUrl(), e);
              ApplicationManager.getApplication().invokeLater(() -> Bus.notify(
                  new Notification("JpaSupport", "Error",
                      "copy file error, file path " + databaseDrivers.getJarFilename(),
                      NotificationType.ERROR)));
              return;
            }
          }
          String downloadUrl = LocaleContextHolder.format(databaseDrivers.getUrl());
          ApplicationManager.getApplication().invokeLater(
              () -> Bus.notify(new Notification(
                  "JpaSupport", "Download drivers", "Download drivers from " + downloadUrl,
                  NotificationType.INFORMATION)));
          DownloadableFileService downloadableFileService = DownloadableFileService.getInstance();
          DownloadableFileDescription downloadableFileDescription = downloadableFileService
              .createFileDescription(downloadUrl, databaseDrivers.getJarFilename() + ".tmp");
          FileDownloader fileDownloader = downloadableFileService
              .createDownloader(Collections.singletonList(downloadableFileDescription),
                  databaseDrivers.getJarFilename());
          List<VirtualFile> virtualFiles = fileDownloader
              .downloadFilesWithProgress(driverVendorPath.getVirtualFile().getPath(), project, parentComponent);
          if (virtualFiles == null) {
            return;
          }
          try {
            virtualFiles.get(0).rename(this, databaseDrivers.getJarFilename());
          } catch (IOException e1) {
            log.error("rename driver error", e1);
            ApplicationManager.getApplication().invokeLater(() -> Bus.notify(
                new Notification("JpaSupport", "Error", "rename driver error " + databaseDrivers.getJarFilename(),
                    NotificationType.ERROR))
            );
            return;
          }
          loadDriverClass(virtualFiles.get(0), databaseDrivers);
        } else {
          loadDriverClass(jarFile.getVirtualFile(), databaseDrivers);
        }
      });
    }
  }
}
