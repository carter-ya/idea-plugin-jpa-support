package com.ifengxue.plugin.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.gui.table.TableFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.TableSpeedSearch;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.JTable;
import org.junit.Test;

public class SelectTablesPreparationTest {

  @Test
  public void benchmarkLargeTablePreparation() {
    for (int size : List.of(100, 500, 2000, 5000)) {
      List<TableSchema> tableSchemas = IntStream.range(0, size)
          .mapToObj(this::createTableSchema)
          .collect(Collectors.toList());
      Set<String> existingEntityFiles = IntStream.range(0, size / 3)
          .mapToObj(index -> "Order" + index + ".java")
          .collect(Collectors.toCollection(HashSet::new));

      SelectTablesPreparation.TableListBuildResult tableListBuildResult =
          SelectTablesPreparation.buildTableList(
              tableSchemas,
              existingEntityFiles,
              tableName -> tableName,
              entityName -> entityName,
              "Repository"
          );
      List<Table> tables = tableListBuildResult.getTables();
      SelectTablesPreparation.TableViewPreparationResult tableViewPreparationResult =
          SelectTablesPreparation.prepareTableView(tables);

      assertEquals(size, tables.size());
      assertEquals(size / 3, tableListBuildResult.getExistingEntityFileCount());
      assertEquals(1, tables.get(0).getSequence());
      assertEquals(size, tables.get(tables.size() - 1).getSequence());

      System.out.println("SelectTables benchmark size=" + size
          + " buildTableListMs=" + tableListBuildResult.getElapsedMillis()
          + " prepareRowsMs=" + tableViewPreparationResult.getElapsedMillis()
          + " selected=" + tableViewPreparationResult.getSelectedCount()
          + " existingEntityFiles=" + tableListBuildResult.getExistingEntityFileCount());
    }
  }

  @Test
  public void benchmarkLargeTableUiInitialization() {
    for (int size : List.of(100, 500, 2000, 5000)) {
      List<Table> tables = IntStream.range(0, size)
          .mapToObj(this::createTable)
          .collect(Collectors.toList());
      SelectTablesPreparation.prepareTableView(tables);

      JTable table = new JTable();
      long decorateStartNanos = System.nanoTime();
      new TableFactory().decorateTable(table, Table.class, tables);
      long decorateMs = SelectTablesPreparation.elapsedMillis(decorateStartNanos);

      long speedSearchMs = -1L;
      boolean speedSearchAvailable = ApplicationManager.getApplication() != null;
      if (speedSearchAvailable) {
        long speedSearchStartNanos = System.nanoTime();
        new TableSpeedSearch(table);
        speedSearchMs = SelectTablesPreparation.elapsedMillis(speedSearchStartNanos);
      }

      long modelReadStartNanos = System.nanoTime();
      int columnCount = table.getModel().getColumnCount();
      int rowCount = table.getModel().getRowCount();
      for (int row = 0; row < rowCount; row++) {
        for (int column = 0; column < columnCount; column++) {
          assertNotNull(table.getModel().getValueAt(row, column));
        }
      }
      long modelReadMs = SelectTablesPreparation.elapsedMillis(modelReadStartNanos);

      System.out.println("SelectTables UI benchmark size=" + size
          + " decorateTableMs=" + decorateMs
          + " speedSearchMs=" + speedSearchMs
          + " modelReadMs=" + modelReadMs
          + " rows=" + rowCount
          + " columns=" + columnCount
          + " speedSearchAvailable=" + speedSearchAvailable);
    }
  }

  private TableSchema createTableSchema(int index) {
    TableSchema tableSchema = new TableSchema();
    tableSchema.setTableName("order_" + index);
    tableSchema.setTableComment("comment_" + index);
    tableSchema.setTableCatalog("catalog");
    tableSchema.setTableSchema("schema");
    return tableSchema;
  }

  private Table createTable(int index) {
    TableSchema tableSchema = createTableSchema(index);
    return Table.from(tableSchema, "Order" + index, "Order" + index + "Repository", true);
  }
}
