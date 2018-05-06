package com.ifengxue.plugin;

import static com.ifengxue.plugin.util.Key.createKey;

import com.ifengxue.fastjdbc.FastJdbc;
import com.ifengxue.fastjdbc.FastJdbcConfig;
import com.ifengxue.fastjdbc.SimpleFastJdbc;
import com.ifengxue.fastjdbc.Sql;
import com.ifengxue.fastjdbc.SqlBuilder;
import com.ifengxue.plugin.component.DatabaseSettings;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.gui.AutoGeneratorSettingsFrame;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.WindowManager;
import com.mysql.jdbc.Driver;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * jpa support 入口
 */
public class JpaSupport extends AnAction {

  static {
    try {
      Class.forName(Driver.class.getName());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    Holder.registerEvent(e);// 注册事件
    Holder.registerApplicationProperties(PropertiesComponent.getInstance());
    Holder.registerProjectProperties(PropertiesComponent.getInstance(e.getProject()));

    JFrame databaseSettingsFrame = new JFrame("设置数据库属性");
    DatabaseSettings databaseSettings = new DatabaseSettings();
    databaseSettingsFrame.setContentPane(databaseSettings.getRootComponent());
    databaseSettingsFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    databaseSettingsFrame.setLocationRelativeTo(WindowManager.getInstance().getIdeFrame(e.getProject())
        .getComponent());
    databaseSettingsFrame.pack();
    // init text field
    initTextField(databaseSettings);
    databaseSettingsFrame.setVisible(true);

    // 注册取消事件
    databaseSettings.getBtnCancel().addActionListener(event -> databaseSettingsFrame.dispose());
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
      saveTextField(host, port, username, password, database);
      String url =
          "jdbc:mysql://" + host + ":" + port + "/" + database + "?useUnicode=true&charset=utf8";
      new Thread(() -> {
        // 尝试获取连接
        try {
          DriverManager.getConnection(url, username, password);
        } catch (SQLException se) {
          ApplicationManager.getApplication().invokeLater(() -> Bus
              .notify(new Notification("JpaSupport", "Error",
                  "连接数据库失败(" + se.getErrorCode() + "," + se.getSQLState() + "," + se
                      .getLocalizedMessage() + ")", NotificationType.ERROR)));
          se.printStackTrace();
          return;
        }
        Properties properties = new Properties();
        properties.setProperty("driverClass", Driver.class.getName());
        properties.setProperty("writableUrl", url);
        properties.setProperty("writableUsername", username);
        properties.setProperty("writablePassword", password);
        FastJdbcConfig.load(properties);

        FastJdbc fastJdbc = new SimpleFastJdbc();
        Sql sql = SqlBuilder.newSelectBuilder(TableSchema.class)
            .select()
            .from()
            .where()
            .equal("tableSchema", database)
            .build();
        List<TableSchema> tableSchemaList;
        try {
          tableSchemaList = fastJdbc
              .find(sql.getSql(), TableSchema.class, sql.getArgs().toArray());
        } catch (SQLException se) {
          ApplicationManager.getApplication()
              .invokeLater(() -> Bus.notify(new Notification("JpaSupport", "Error",
                  se.getErrorCode() + "," + se.getSQLState() + "," + se.getLocalizedMessage(),
                  NotificationType.ERROR)));
          se.printStackTrace();
          return;
        }

        // 显示自动生成器配置窗口
        AutoGeneratorSettingsFrame.show(tableSchemaList);

        databaseSettingsFrame.dispose();// 释放数据库设置窗口
      }).start();
    });
  }

  private void saveTextField(String host, String port, String username, String password, String database) {
    PropertiesComponent applicationProperties = Holder.getApplicationProperties();
    applicationProperties.setValue(createKey("host"), host);
    applicationProperties.setValue(createKey("port"), port);
    applicationProperties.setValue(createKey("username"), username);
    applicationProperties
        .setValue(createKey("password"), Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
    applicationProperties.setValue(createKey("database"), database);
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
  }
}
