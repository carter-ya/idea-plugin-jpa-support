package com.ifengxue.plugin.entity;

import com.intellij.database.model.DasColumn;
import com.intellij.database.util.DasUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.JavaTypeResolver;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

public class DatabasePluginColumnSchema extends ColumnSchema implements ColumnSchemaExtension<DasColumn> {

  private static final long serialVersionUID = 8104233110923069156L;

  private final DasColumn dasColumn;
  private final JavaTypeResolver javaTypeResolver = new JavaTypeResolverDefaultImpl();

  public DatabasePluginColumnSchema(DasColumn dasColumn) {
    this.dasColumn = dasColumn;

    setColumnName(dasColumn.getName());
    setTableSchema(DasUtil.getSchema(dasColumn.getTable()));
    setTableName(dasColumn.getTableName());
    setOrdinalPosition(dasColumn.getPosition());
    setDataType(dasColumn.getDataType().typeName);
    setColumnType(dasColumn.getDataType().typeName);
    setColumnComment(StringUtils.trimToEmpty(dasColumn.getComment()));
    String defaultVal = dasColumn.getDefault();
    if (defaultVal != null && defaultVal.startsWith("'") && defaultVal.endsWith("'")) {
      defaultVal = defaultVal.substring(1, defaultVal.length() - 1);
    }
    setColumnDefault(defaultVal);
  }

  @Override
  public DasColumn origin() {
    return dasColumn;
  }

  @Override
  public boolean nullable() {
    return !dasColumn.isNotNull();
  }

  @Override
  public boolean primary() {
    return DasUtil.isPrimary(dasColumn);
  }

  @Override
  public boolean autoIncrement() {
    return dasColumn.getTable().getColumnAttrs(dasColumn).contains(DasColumn.Attribute.AUTO_GENERATED);
  }

  @Override
  public boolean sequenceColumn() {
    return false;
  }

  @Override
  public boolean generateColumn() {
    return false;
  }

  @Override
  public int jdbcType() {
    for (Field field : Types.class.getFields()) {
      if (Modifier.isStatic(field.getModifiers())
          && Modifier.isFinal(field.getModifiers())
          && (field.getType() == int.class || field.getType() == Integer.class)) {
        if (field.getName().equalsIgnoreCase(dasColumn.getDataType().typeName)) {
          try {
            return field.getInt(Types.class);
          } catch (ReflectiveOperationException e) {
            return Types.VARCHAR;
          }
        }
      }
    }
    return Types.VARCHAR;
  }

  @Nullable
  @Override
  public String jdbcTypeName() {
    for (Field field : Types.class.getFields()) {
      if (Modifier.isStatic(field.getModifiers())
          && Modifier.isFinal(field.getModifiers())
          && (field.getType() == int.class || field.getType() == Integer.class)) {
        if (field.getName().equalsIgnoreCase(dasColumn.getDataType().typeName)) {
          return dasColumn.getDataType().typeName.toUpperCase();
        }
      }
    }
    return "VARCHAR";
  }

  @Nullable
  @Override
  public Class<?> javaTypeClass() {
    IntrospectedColumn column = new IntrospectedColumn();
    column.setJdbcType(jdbcType());
    column.setLength(dasColumn.getDataType().getLength());
    column.setScale(dasColumn.getDataType().getScale());
    FullyQualifiedJavaType type = javaTypeResolver.calculateJavaType(column);
    return Optional.ofNullable(type)
        .map(t -> {
          try {
            return Class.forName(t.getFullyQualifiedName());
          } catch (ClassNotFoundException ignored) {
            return null;
          }
        })
        .filter(clazz -> clazz != Object.class) // skip Object
        .orElse(null);
  }
}
