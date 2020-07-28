package com.ifengxue.plugin.adapter;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.ColumnSchema;
import com.ifengxue.plugin.entity.TableSchema;
import com.ifengxue.plugin.util.ColumnUtil;
import fastjdbc.FastJdbc;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class PostgreSQLDriverAdapter extends AbstractDriverAdapter {

  @Override
  protected String toConnectionUrl(String oldConnectionUrl, String host, String port, String username, String database,
      String params) {
    String connectionUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
    if (StringUtils.isNotBlank(params)) {
      connectionUrl += "?" + params;
    }
    return connectionUrl;
  }

  @Override
  public List<TableSchema> findDatabaseSchemas(String database) throws SQLException {
    FastJdbc fastJdbc = Holder.getFastJdbc();
    return fastJdbc.find("select a.relname as name , b.description as comment from pg_class a\n"
        + "left join (select * from pg_description where objsubid =0 ) b on a.oid = b.objoid\n"
        + "where a.relname in (select tablename from pg_tables where schemaname = 'public')", (row, rowNum) -> {
      TableSchema tableSchema = new TableSchema();
      tableSchema.setTableName(row.getString("name"));
      tableSchema.setTableComment(Optional.ofNullable(row.getString("comment")).orElse(""));
      tableSchema.setTableSchema("public");
      return tableSchema;
    });
  }

  @Override
  public List<ColumnSchema> findTableSchemas(String database, String table) throws SQLException {
    FastJdbc fastJdbc = Holder.getFastJdbc();
    String sql = "select ordinal_position                                               as ordinal_position,\n"
        + "       column_name                                                           as column_name,\n"
        + "       data_type                                                             as data_type,\n"
        + "       coalesce(character_maximum_length, numeric_precision, -1)             as length,\n"
        + "       numeric_scale                                                         as numeric_scale,\n"
        + "       case is_nullable when 'NO' then 0 else 1 end                          as is_nullable,\n"
        + "       column_default                                                        as column_default,\n"
        + "       case when position('nextval' in column_default) > 0 then 1 else 0 end as is_identity,\n"
        + "       case when b.pk_name is null then 0 else 1 end                         as is_primary_key,\n"
        + "       c.DeText                                                              as comment\n"
        + "from information_schema.columns\n"
        + "       left join (\n"
        + "  select pg_attr.attname as colname, pg_constraint.conname as pk_name\n"
        + "  from pg_constraint\n"
        + "         inner join pg_class on pg_constraint.conrelid = pg_class.oid\n"
        + "         inner join pg_attribute pg_attr on pg_attr.attrelid = pg_class.oid and pg_attr.attnum = pg_constraint.conkey[1]\n"
        + "         inner join pg_type on pg_type.oid = pg_attr.atttypid\n"
        + "  where pg_class.relname = ?\n"
        + "    and pg_constraint.contype = 'p'\n"
        + ") b on b.colname = information_schema.columns.column_name\n"
        + "       left join (\n"
        + "  select attname, description as DeText\n"
        + "  from pg_class\n"
        + "         left join pg_attribute pg_attr on pg_attr.attrelid = pg_class.oid\n"
        + "         left join pg_description pg_desc on pg_desc.objoid = pg_attr.attrelid and pg_desc.objsubid = pg_attr.attnum\n"
        + "  where pg_attr.attnum > 0\n"
        + "    and pg_attr.attrelid = pg_class.oid\n"
        + "    and pg_class.relname = ?\n"
        + ") c on c.attname = information_schema.columns.column_name\n"
        + "where table_schema = 'public'\n"
        + "  and table_name = ?\n"
        + "order by ordinal_position asc";
    return fastJdbc.find(sql, (row, rowNum) -> {
      ColumnSchema columnSchema = new ColumnSchema();
      columnSchema.setColumnName(row.getString("column_name"));
      columnSchema.setTableSchema(database);
      columnSchema.setTableName(table);
      columnSchema.setOrdinalPosition(row.getInt("ordinal_position"));
      columnSchema.setDataType(row.getString("data_type"));
      columnSchema.setColumnType("");
      columnSchema.setExtra(row.getString("is_identity"));
      columnSchema.setColumnComment(row.getString("comment"));
      columnSchema.setIsNullable(row.getString("is_nullable"));
      columnSchema.setColumnDefault(row.getString("column_default"));
      columnSchema.setColumnKey(row.getString("is_primary_key"));
      return columnSchema;
    }, table, table, table);
  }

  @Override
  public Column parseToColumn(ColumnSchema columnSchema, String removeFieldPrefix, boolean useWrapper,
      boolean useJava8DateType) {
    Column column = new Column();
    column.setColumnName(columnSchema.getColumnName());
    column.setSort(columnSchema.getOrdinalPosition());
    column.setDbDataType(columnSchema.getDataType());
    column.setPrimary("1".equals(columnSchema.getColumnKey()));
    column.setNullable("1".equals(columnSchema.getIsNullable()));
    column.setAutoIncrement("1".equals(columnSchema.getExtra()));
    column.setColumnComment(Optional.ofNullable(columnSchema.getColumnComment()).orElse(""));
    if (columnSchema.getColumnDefault() != null) {
      if (!columnSchema.getColumnDefault().contains("nextval(")) {
        column.setDefaultValue(columnSchema.getColumnDefault());
      }
    }
    ColumnUtil.parseColumn(this, column, removeFieldPrefix, useWrapper, useJava8DateType);
    return column;
  }
}
