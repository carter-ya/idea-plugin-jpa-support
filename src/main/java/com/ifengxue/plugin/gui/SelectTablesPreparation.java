package com.ifengxue.plugin.gui;

import com.ifengxue.plugin.entity.Selectable;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.util.StringHelper;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.Nullable;

final class SelectTablesPreparation {

  private SelectTablesPreparation() {
  }

  static Set<String> collectExistingEntityFiles(@Nullable VirtualFile entityDirectoryVF) {
    if (entityDirectoryVF == null) {
      return Set.of();
    }
    Set<String> existingEntityFiles = new HashSet<>();
    for (VirtualFile child : entityDirectoryVF.getChildren()) {
      if (!child.isDirectory()) {
        existingEntityFiles.add(child.getName());
      }
    }
    return existingEntityFiles;
  }

  static TableListBuildResult buildTableList(
      List<TableSchema> tableSchemaList,
      Set<String> existingEntityFiles,
      UnaryOperator<String> removeTablePrefix,
      UnaryOperator<String> concatPrefixAndSuffix,
      String repositorySuffix
  ) {
    long startNanos = System.nanoTime();
    List<Table> tableList = new ArrayList<>(tableSchemaList.size());
    int selectedCount = 0;
    for (TableSchema tableSchema : tableSchemaList) {
      String tableName = removeTablePrefix.apply(tableSchema.getTableName());
      String entityName = StringHelper.parseEntityName(tableName);
      entityName = concatPrefixAndSuffix.apply(entityName);
      boolean selected = !existingEntityFiles.contains(entityName + ".java");
      if (tableSchema instanceof Selectable) {
        selected = ((Selectable) tableSchema).isSelected();
      }
      if (selected && tableName.equals("flyway_schema_history")) {
        selected = false;
      }
      if (selected) {
        selectedCount++;
      }
      String repositoryName = entityName + repositorySuffix;
      tableList.add(Table.from(tableSchema, entityName, repositoryName, selected));
    }
    return new TableListBuildResult(
        tableList,
        selectedCount,
        existingEntityFiles.size(),
        elapsedMillis(startNanos)
    );
  }

  static TableViewPreparationResult prepareTableView(List<Table> tables) {
    long startNanos = System.nanoTime();
    AtomicInteger seq = new AtomicInteger(1);
    tables.sort(Comparator.comparing(Table::getTableName));
    int selectedCount = 0;
    for (Table table : tables) {
      table.setSequence(seq.getAndIncrement());
      if (table.isSelected()) {
        selectedCount++;
      }
    }
    return new TableViewPreparationResult(selectedCount, elapsedMillis(startNanos));
  }

  static long elapsedMillis(long startNanos) {
    return (System.nanoTime() - startNanos) / 1_000_000L;
  }

  static final class TableListBuildResult {

    private final List<Table> tables;
    private final int selectedCount;
    private final int existingEntityFileCount;
    private final long elapsedMillis;

    TableListBuildResult(List<Table> tables, int selectedCount, int existingEntityFileCount,
        long elapsedMillis) {
      this.tables = tables;
      this.selectedCount = selectedCount;
      this.existingEntityFileCount = existingEntityFileCount;
      this.elapsedMillis = elapsedMillis;
    }

    List<Table> getTables() {
      return tables;
    }

    int getSelectedCount() {
      return selectedCount;
    }

    int getExistingEntityFileCount() {
      return existingEntityFileCount;
    }

    long getElapsedMillis() {
      return elapsedMillis;
    }
  }

  static final class TableViewPreparationResult {

    private final int selectedCount;
    private final long elapsedMillis;

    TableViewPreparationResult(int selectedCount, long elapsedMillis) {
      this.selectedCount = selectedCount;
      this.elapsedMillis = elapsedMillis;
    }

    int getSelectedCount() {
      return selectedCount;
    }

    long getElapsedMillis() {
      return elapsedMillis;
    }
  }
}
