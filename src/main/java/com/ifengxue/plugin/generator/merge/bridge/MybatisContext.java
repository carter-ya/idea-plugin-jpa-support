package com.ifengxue.plugin.generator.merge.bridge;

import static org.mybatis.generator.internal.util.StringUtility.composeFullyQualifiedTableName;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import com.ifengxue.plugin.entity.Table;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.JavaTypeResolver;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.db.DatabaseIntrospector;

public class MybatisContext extends Context {

    private final Table table;

    public MybatisContext(ModelType defaultModelType, Table table) {
        super(defaultModelType);
        this.table = table;
    }

    private Field getField(String name) throws ReflectiveOperationException {
        Field field = Context.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    @Override
    public void introspectTables(ProgressCallback callback, List<String> warnings,
        Set<String> fullyQualifiedTableNames) throws SQLException, InterruptedException {

        JavaTypeResolver javaTypeResolver = ObjectFactory
            .createJavaTypeResolver(this, warnings);

        try {
            List<IntrospectedTable> introspectedTables = new ArrayList<>();
            Field introspectedTablesField = getField("introspectedTables");
            introspectedTablesField.set(this, introspectedTables);

            callback.startTask(getString("Progress.0")); //$NON-NLS-1$

            DatabaseIntrospector databaseIntrospector = new MybatisDatabaseIntrospector(
                this, javaTypeResolver, warnings, table);

            Field tableConfigurationsField = getField("tableConfigurations");
            @SuppressWarnings("unchecked")
            List<TableConfiguration> tableConfigurations = (List<TableConfiguration>) tableConfigurationsField
                .get(this);
            for (TableConfiguration tc : tableConfigurations) {
                String tableName = composeFullyQualifiedTableName(tc.getCatalog(), tc
                    .getSchema(), tc.getTableName(), '.');

                if (fullyQualifiedTableNames != null
                    && !fullyQualifiedTableNames.isEmpty()
                    && !fullyQualifiedTableNames.contains(tableName)) {
                    continue;
                }

                if (!tc.areAnyStatementsEnabled()) {
                    warnings.add(getString("Warning.0", tableName)); //$NON-NLS-1$
                    continue;
                }

                callback.startTask(getString("Progress.1", tableName)); //$NON-NLS-1$
                List<IntrospectedTable> tables = databaseIntrospector
                    .introspectTables(tc);

                if (tables != null) {
                    introspectedTables.addAll(tables);
                }

                callback.checkCancel();
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                "Can't introspect table for " + table.getTableName(), e);
        }
    }
}
