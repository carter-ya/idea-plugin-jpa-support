package com.ifengxue.plugin.gui;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.action.JpaSupport;
import com.ifengxue.plugin.adapter.DatabaseDrivers;
import com.ifengxue.plugin.adapter.DriverDelegate;
import com.ifengxue.plugin.component.DatabaseSettings;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.i18n.LocaleItem;
import com.ifengxue.plugin.state.DatabaseSettingsState;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.util.DirectoryUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.StringUtils.trim;

public class DatabaseSettingsDialog extends DialogWrapper {

  private static final String DRIVER_VENDOR_PATH = ".Jpa Support" + File.separator + "driver_vendor";
  public static AtomicReference<ClassLoader> classLoaderRef = new AtomicReference<>(JpaSupport.class.getClassLoader());
  private final Logger log = Logger.getInstance(DatabaseSettingsDialog.class);
  private final DatabaseSettings databaseSettings;
  private final Project project;

  protected DatabaseSettingsDialog(Project project) {
    super(project, true);
    this.project = project;
    databaseSettings = new DatabaseSettings();
    init();
    setTitle(LocaleContextHolder.format("set_up_database_connection"));

    // init text field
    initTextField(databaseSettings);
    databaseSettings.getTextHost().getDocument().addDocumentListener(new ConnectionUrlUpdateListener());
    databaseSettings.getTextPort().getDocument().addDocumentListener(new ConnectionUrlUpdateListener());
    databaseSettings.getTextUsername().getDocument().addDocumentListener(new ConnectionUrlUpdateListener());
    databaseSettings.getTextDatabase().getDocument().addDocumentListener(new ConnectionUrlUpdateListener());

    // 注册语言切换事件
    databaseSettings.getCbxSelectLanguage().addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() != ItemEvent.SELECTED) {
        return;
      }
      LocaleContextHolder.setCurrentLocale(((LocaleItem) itemEvent.getItem()).getLocale());
      databaseSettings.getData(ServiceManager.getService(project, DatabaseSettingsState.class));
    });
    // 注册数据库类型切换事件
    databaseSettings.getCbxSelectDatabase().addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() != ItemEvent.SELECTED) {
        return;
      }
      new DownloadDriverRunnable(project, null, (DatabaseDrivers) itemEvent.getItem())
          .run();
    });
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return databaseSettings.getRootComponent();
  }

  @NotNull
  @Override
  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    if (databaseSettings.getTextHost().getText().trim().isEmpty()) {
      return new ValidationInfo("Must set host", databaseSettings.getTextHost());
    }
    if (databaseSettings.getTextPort().getText().trim().isEmpty()) {
      return new ValidationInfo("Must set port", databaseSettings.getTextHost());
    }
    if (databaseSettings.getTextUsername().getText().trim().isEmpty()) {
      return new ValidationInfo("Must set username", databaseSettings.getTextUsername());
    }
    if (databaseSettings.getTextPassword().getPassword().length == 0) {
      return new ValidationInfo("Must set password", databaseSettings.getTextPassword());
    }
    if (databaseSettings.getTextDatabase().getText().trim().isEmpty()) {
      return new ValidationInfo("Must set database", databaseSettings.getTextDatabase());
    }
    if (databaseSettings.getTextConnectionUrl().getText().trim().isEmpty()) {
      return new ValidationInfo("Must set connection url", databaseSettings.getTextConnectionUrl());
    }
    return super.doValidate();
  }

  @Override
  protected void doOKAction() {
    String host = databaseSettings.getTextHost().getText().trim();
    String port = databaseSettings.getTextPort().getText().trim();
    String username = databaseSettings.getTextUsername().getText().trim();
    String password = new String(databaseSettings.getTextPassword().getPassword()).trim();
    String database = databaseSettings.getTextDatabase().getText().trim();
    String connectionUrl = databaseSettings.getTextConnectionUrl().getText().trim();
    saveTextField(host, port, username, password);
    WriteCommandAction.runWriteCommandAction(project, () -> {
      try {
        if (!driverHasBeenLoaded(Holder.getDatabaseDrivers())) {
          try {
            Class.forName(Holder.getDatabaseDrivers().getDriverClass(), true, classLoaderRef.get());
          } catch (ClassNotFoundException ex) {
            // driver not loaded
            ApplicationManager.getApplication().invokeAndWait(() -> {
              int selectButton = Messages.showOkCancelDialog(project,
                  LocaleContextHolder.format("driver_not_found", Holder.getDatabaseDrivers().getDriverClass()),
                  LocaleContextHolder.format("prompt"),
                  Messages.OK_BUTTON,
                  Messages.CANCEL_BUTTON,
                  Messages.getQuestionIcon());
              if (selectButton == Messages.OK) {
                new DownloadDriverRunnable(project, null, Holder.getDatabaseDrivers()).run();
              }
            });
          }
          DatabaseDrivers databaseDrivers = Holder.getDatabaseDrivers();
          Driver driver = (Driver) Class
              .forName(databaseDrivers.getDriverClass(), true, classLoaderRef.get())
              .getDeclaredConstructor().newInstance();
          DriverManager.registerDriver(new DriverDelegate(driver, databaseDrivers));
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

      try {
        List<TableSchema> tableSchemaList = Holder.getDatabaseDrivers().getDriverAdapter()
            .findDatabaseSchemas(database);
        ApplicationManager.getApplication().invokeLater(() -> {
          dispose();
          // 显示自动生成器配置窗口
          AutoGeneratorSettingsDialog.show(tableSchemaList, tableSchema -> {
            try {
              return Holder.getDatabaseDrivers().getDriverAdapter()
                  .findTableSchemas(tableSchema.getTableSchema(), tableSchema.getTableName());
            } catch (SQLException se) {
              log.error("read table " + tableSchema.getTableName() + " schema failed", se);
              ApplicationManager.getApplication()
                  .invokeLater(() -> Bus.notify(new Notification("JpaSupport", "Error",
                      se.getErrorCode() + "," + se.getSQLState() + "," + se.getLocalizedMessage(),
                      NotificationType.ERROR)));
              return null;
            }
          });
        });
      } catch (SQLException se) {
        String sb = "SQL error code: " + se.getErrorCode()
            + "\nSQL error state: " + se.getSQLState() + "\n"
            + "Error message: " + se.getLocalizedMessage();
        ApplicationManager.getApplication()
            .invokeLater(() -> Bus.notify(new Notification("JpaSupport", "Error",
                sb, NotificationType.ERROR)));
      }
    });
  }

  @Nullable
  @Override
  protected String getDimensionServiceKey() {
    return Constants.NAME + ":" + getClass().getName();
  }

  public static void showDialog() {
    new DatabaseSettingsDialog(Holder.getProject()).show();
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
          .parent(getClass().getClassLoader())
          .get();
      Driver driver = (Driver) urlClassLoader.loadClass(databaseDrivers.getDriverClass()).getDeclaredConstructor()
          .newInstance();
      DriverManager.registerDriver(new DriverDelegate(driver, databaseDrivers));
      log.info("driver " + databaseDrivers.getDriverClass() + " has been loaded");
      classLoaderRef.set(urlClassLoader);
      Holder.registerDatabaseDrivers(databaseDrivers);
      ApplicationManager.getApplication().invokeLater(() -> updateConnectionUrl(true));

      // save database driver path
      DatabaseSettingsState databaseSettingsState = ServiceManager.getService(project, DatabaseSettingsState.class);
      databaseSettingsState.registerJarPath(databaseDrivers, virtualFile.getPath());
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
      if (driver instanceof DriverDelegate) {
        if (((DriverDelegate) driver).getDatabaseDrivers() == databaseDrivers) {
          return true;
        }
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

  private void saveTextField(String host, String port, String username, String password) {
    DatabaseSettingsState databaseSettingsState = ServiceManager.getService(project, DatabaseSettingsState.class);
    databaseSettings.getData(databaseSettingsState);
    if (databaseSettingsState.isRequireSavePassword()) {
      // 存储密码
      CredentialAttributes credentialAttributes = createCredentialAttributes(host, port, username);
      Credentials credentials = new Credentials(username, password);
      PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }
  }

  @NotNull
  private CredentialAttributes createCredentialAttributes(String host, String port, String username) {
    return new CredentialAttributes("JpaSupport://" + host + ":" + port,
        username, getClass(), false);
  }

  private void initTextField(DatabaseSettings databaseSettings) {
    setOKButtonText(LocaleContextHolder.format("button_next_step"));
    setCancelButtonText(LocaleContextHolder.format("button_cancel"));

    DatabaseSettingsState databaseSettingsState = ServiceManager.getService(project, DatabaseSettingsState.class);
    databaseSettings.setData(databaseSettingsState);
    // update connection url
    updateConnectionUrl(false);

    if (databaseSettingsState.isRequireSavePassword()) {
      // 加载密码
      CredentialAttributes credentialAttributes = createCredentialAttributes(databaseSettings.getTextHost().getText(),
          databaseSettings.getTextPort().getText(), databaseSettings.getTextUsername().getText());
      Credentials credentials = PasswordSafe.getInstance().get(credentialAttributes);
      String password = "";
      if (credentials != null) {
        password = credentials.getPasswordAsString();
      }
      databaseSettings.getTextPassword().setText(password);
    }

    DatabaseDrivers databaseDrivers = Holder.getDatabaseDrivers();
    // load driver path
    String databaseDriverPath = databaseSettingsState.getJarPath(databaseDrivers);
    if (StringUtils.isNotBlank(databaseDriverPath)) {
      try {
        classLoaderRef.set(UrlClassLoader.build()
            .urls(new File(databaseDriverPath).toURI().toURL())
            .parent(getClass().getClassLoader())
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
      DatabaseSettingsDialog.this.updateConnectionUrl(switchDatabaseVendor);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      DatabaseSettingsDialog.this.updateConnectionUrl(switchDatabaseVendor);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      DatabaseSettingsDialog.this.updateConnectionUrl(switchDatabaseVendor);
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
        String dirPath = System.getProperty("user.home") + File.separator + DRIVER_VENDOR_PATH;
        if (File.separatorChar != '/') {
          dirPath = dirPath.replace('\\', '/');
        }
        // 创建目录，确保目录存在
        PsiDirectory driverVendorPath = DirectoryUtil.mkdirs(PsiManager.getInstance(project), dirPath);
        VirtualFile jarFile = LocalFileSystem.getInstance()
            .refreshAndFindFileByPath(dirPath + "/" + databaseDrivers.getJarFilename());
        // driver 不存在，需要下载
        if (jarFile == null || !jarFile.exists() ||
            !new File(dirPath + "/" + databaseDrivers.getJarFilename()).exists()) {
          // 兼容处理：VirtualFile认为文件存在File认为文件不存在，因此需要删除这个虚拟文件
          if (jarFile != null && jarFile.exists()) {
            try {
              jarFile.delete(this);
            } catch (IOException e) {
              ApplicationManager.getApplication().invokeLater(() -> Bus.notify(
                  new Notification("JpaSupport", "Error",
                      "delete invalid file error: " + e.getLocalizedMessage(),
                      NotificationType.ERROR)));
            }
          }
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
              LocalFileSystem.getInstance().refresh(true);
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
              .downloadFilesWithProgress(dirPath, project, parentComponent);
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
          loadDriverClass(jarFile, databaseDrivers);
        }
      });
    }
  }
}
