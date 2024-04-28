package com.ifengxue.plugin.action;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.adapter.DatabaseDrivers;
import com.ifengxue.plugin.entity.ColumnSchema;
import com.ifengxue.plugin.entity.DatabasePluginColumnSchema;
import com.ifengxue.plugin.entity.DatabasePluginTableSchema;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.gui.AutoGeneratorSettingsDialog;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.util.DatabasePluginUtil;
import com.intellij.database.Dbms;
import com.intellij.database.model.DasNamespace;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasTable;
import com.intellij.database.model.ObjectKind;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.JBIterable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Jpa Support with database plugin
 */
public class JpaSupportWithDatabasePlugin extends AbstractPluginSupport {

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    super.actionPerformed(e);

    List<DbTable> tables = DatabasePluginUtil.resolveDbTables(e.getData(LangDataKeys.PSI_ELEMENT_ARRAY));
    if (tables.isEmpty()) {
      Messages.showWarningDialog(LocaleContextHolder.format("not_select_valid_tables"), "JpaSupport");
      return;
    }
    PsiElement parent = tables.get(0).getParent();
    List<DasObject> allTables = new ArrayList<>();
    if (parent instanceof DasNamespace) {
      List<ObjectKind> kinds = Arrays.asList(
          ObjectKind.TABLE,
          ObjectKind.VIEW,
          ObjectKind.MAT_VIEW
      );
      for (ObjectKind kind : kinds) {
        JBIterable<DasTable> iterable = ((DasNamespace) parent).getDasChildren(kind).filter(DasTable.class);
        for (DasTable dasTable : iterable) {
          allTables.add(dasTable);
        }
      }
    }
    if (allTables.isEmpty()) {
      allTables.addAll(tables);
    }

    resolveDatabaseVendor(tables.get(0).getDataSource());

    Set<String> selectedTableNames = tables.stream().map(DasObject::getName).collect(toSet());
    List<TableSchema> tableSchemas = allTables
        .stream()
        .map(t -> toTableSchema(t, selectedTableNames.contains(t.getName())))
        .collect(toList());
    CompletableFuture<List<TableSchema>> tableSchemasFuture = CompletableFuture
        .completedFuture(tableSchemas);
    AutoGeneratorSettingsDialog.show(tableSchemasFuture, tableSchema -> {
      DasObject dasObject = ((DatabasePluginTableSchema) tableSchema).getDasObject();
      List<ColumnSchema> columnSchemas = new ArrayList<>();
      DasUtil.getColumns(dasObject).consumeEach(dasColumn -> {
        DatabasePluginColumnSchema columnSchema = new DatabasePluginColumnSchema(dasColumn);
        columnSchemas.add(columnSchema);
      });
      return columnSchemas;
    });
  }

  private TableSchema toTableSchema(DasObject dasObject, boolean isSelected) {
    DatabasePluginTableSchema tableSchema = new DatabasePluginTableSchema(dasObject);
    tableSchema.setTableName(dasObject.getName());
    tableSchema.setTableComment(StringUtils.trimToEmpty(dasObject.getComment()));
    tableSchema.setTableCatalog(DasUtil.getSchema(dasObject));
    tableSchema.setTableSchema(DasUtil.getSchema(dasObject));
    tableSchema.setSelected(isSelected);
    return tableSchema;
  }

  private void resolveDatabaseVendor(DbDataSource dataSource) {
    Dbms dbms = dataSource.getDbms();
    if (dbms.isMysql()) {
      Holder.registerDatabaseDrivers(DatabaseDrivers.MYSQL);
    } else if (dbms.isPostgres()) {
      Holder.registerDatabaseDrivers(DatabaseDrivers.POSTGRE_SQL);
    } else {
      Holder.registerDatabaseDrivers(DatabaseDrivers.UNKNOWN);
    }
  }
}
