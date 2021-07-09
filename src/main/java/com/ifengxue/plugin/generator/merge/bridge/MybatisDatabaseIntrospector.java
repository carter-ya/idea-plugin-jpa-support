package com.ifengxue.plugin.generator.merge.bridge;

import static org.mybatis.generator.internal.util.StringUtility.composeFullyQualifiedTableName;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.Table;
import com.intellij.openapi.diagnostic.Logger;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.JavaTypeResolver;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.db.ActualTableName;
import org.mybatis.generator.internal.db.DatabaseIntrospector;

public class MybatisDatabaseIntrospector extends DatabaseIntrospector {

    private final Logger logger = Logger.getInstance(getClass());
    private final Context context;
    private final List<String> warnings;
    private final Table table;

    public MybatisDatabaseIntrospector(Context context, JavaTypeResolver javaTypeResolver,
        List<String> warnings, Table table) {
        super(context, new DatabaseMetaDataProxy(), javaTypeResolver, warnings);
        this.context = context;
        this.warnings = warnings;
        this.table = table;
    }

    @SuppressWarnings("unchecked")
    private <T> T invokeMethod(String methodName, Object... args)
        throws ReflectiveOperationException {
        for (Method method : DatabaseIntrospector.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                method.setAccessible(true);
                return (T) method.invoke(this, args);
            }
        }
        throw new NoSuchMethodException(
            DatabaseIntrospector.class.getName() + "." + methodName + "()");
    }

    @Override
    public List<IntrospectedTable> introspectTables(TableConfiguration tc) {
        // get the raw columns from the DB
        Map<ActualTableName, List<IntrospectedColumn>> columns = getColumns(tc);

        if (columns.isEmpty()) {
            warnings.add(getString("Warning.19", tc.getCatalog(), //$NON-NLS-1$
                tc.getSchema(), tc.getTableName()));
            return Collections.emptyList();
        }

        try {
            invokeMethod("removeIgnoredColumns", tc, columns);
            invokeMethod("calculateExtraColumnInformation", tc, columns);
            invokeMethod("applyColumnOverrides", tc, columns);
            invokeMethod("calculateIdentityColumns", tc, columns);

            List<IntrospectedTable> introspectedTables = invokeMethod(
                "calculateIntrospectedTables", tc, columns);

            // now introspectedTables has all the columns from all the
            // tables in the configuration. Do some validation...

            Iterator<IntrospectedTable> iter = introspectedTables.iterator();
            while (iter.hasNext()) {
                IntrospectedTable introspectedTable = iter.next();
                Column primaryColumn = table.getPrimaryColumn();
                if (primaryColumn != null) {
                    introspectedTable.addPrimaryKeyColumn(primaryColumn.getColumnName());
                }

                if (!introspectedTable.hasAnyColumns()) {
                    // add warning that the table has no columns, remove from the
                    // list
                    String warning = getString(
                        "Warning.1",
                        introspectedTable.getFullyQualifiedTable().toString()); //$NON-NLS-1$
                    warnings.add(warning);
                    iter.remove();
                } else if (!introspectedTable.hasPrimaryKeyColumns()
                    && !introspectedTable.hasBaseColumns()) {
                    // add warning that the table has only BLOB columns, remove from
                    // the list
                    String warning = getString(
                        "Warning.18",
                        introspectedTable.getFullyQualifiedTable().toString()); //$NON-NLS-1$
                    warnings.add(warning);
                    iter.remove();
                } else {
                    // now make sure that all columns called out in the
                    // configuration
                    // actually exist
                    invokeMethod("reportIntrospectionWarnings", introspectedTable, tc,
                        introspectedTable.getFullyQualifiedTable());
                }
            }
            return introspectedTables;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                "Can't introspect table for " + table.getTableName(), e);
        }
    }

    protected Map<ActualTableName, List<IntrospectedColumn>> getColumns(TableConfiguration tc) {
        String localCatalog = table.getTableCatalog();
        String localSchema = table.getTableSchema();
        String localTableName = table.getTableName();

        if (logger.isDebugEnabled()) {
            String fullTableName = composeFullyQualifiedTableName(localCatalog, localSchema,
                localTableName, '.');
            logger.debug(getString("Tracing.1", fullTableName)); //$NON-NLS-1$
        }

        ActualTableName atn = new ActualTableName(
            localCatalog, //$NON-NLS-1$
            localSchema, //$NON-NLS-1$
            localTableName); //$NON-NLS-1$

        List<IntrospectedColumn> introspectedColumns = new ArrayList<>();
        for (Column column : table.getColumns()) {
            IntrospectedColumn introspectedColumn = ObjectFactory.createIntrospectedColumn(context);

            introspectedColumn.setTableAlias(tc.getAlias());
            introspectedColumn.setJdbcType(column.getJdbcType()); //$NON-NLS-1$
            introspectedColumn.setActualTypeName(column.getJdbcTypeName()); //$NON-NLS-1$
            // NOT SET Length
            introspectedColumn.setLength(0); //$NON-NLS-1$
            introspectedColumn.setActualColumnName(column.getColumnName()); //$NON-NLS-1$
            introspectedColumn.setNullable(column.isNullable()); //$NON-NLS-1$
            // NOT SET SCALE
            introspectedColumn.setScale(0); //$NON-NLS-1$
            introspectedColumn.setRemarks(column.getColumnComment()); //$NON-NLS-1$
            introspectedColumn.setDefaultValue(column.getDefaultValue()); //$NON-NLS-1$

            introspectedColumn
                .setAutoIncrement(column.isAutoIncrement()); //$NON-NLS-1$ //$NON-NLS-2$

            introspectedColumns.add(introspectedColumn);

            if (logger.isDebugEnabled()) {
                logger.debug(getString(
                    "Tracing.2", //$NON-NLS-1$
                    introspectedColumn.getActualColumnName(), Integer
                        .toString(introspectedColumn.getJdbcType()),
                    atn.toString()));
            }
        }
        Map<ActualTableName, List<IntrospectedColumn>> columns = new HashMap<>();
        columns.put(atn, introspectedColumns);
        return columns;
    }

    private static class DatabaseMetaDataProxy implements DatabaseMetaData {

        @Override
        public boolean allProceduresAreCallable() throws SQLException {
            return false;
        }

        @Override
        public boolean allTablesAreSelectable() throws SQLException {
            return false;
        }

        @Override
        public String getURL() throws SQLException {
            return null;
        }

        @Override
        public String getUserName() throws SQLException {
            return null;
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return false;
        }

        @Override
        public boolean nullsAreSortedHigh() throws SQLException {
            return false;
        }

        @Override
        public boolean nullsAreSortedLow() throws SQLException {
            return false;
        }

        @Override
        public boolean nullsAreSortedAtStart() throws SQLException {
            return false;
        }

        @Override
        public boolean nullsAreSortedAtEnd() throws SQLException {
            return false;
        }

        @Override
        public String getDatabaseProductName() throws SQLException {
            return null;
        }

        @Override
        public String getDatabaseProductVersion() throws SQLException {
            return null;
        }

        @Override
        public String getDriverName() throws SQLException {
            return null;
        }

        @Override
        public String getDriverVersion() throws SQLException {
            return null;
        }

        @Override
        public int getDriverMajorVersion() {
            return 0;
        }

        @Override
        public int getDriverMinorVersion() {
            return 0;
        }

        @Override
        public boolean usesLocalFiles() throws SQLException {
            return false;
        }

        @Override
        public boolean usesLocalFilePerTable() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsMixedCaseIdentifiers() throws SQLException {
            return false;
        }

        @Override
        public boolean storesUpperCaseIdentifiers() throws SQLException {
            return false;
        }

        @Override
        public boolean storesLowerCaseIdentifiers() throws SQLException {
            return false;
        }

        @Override
        public boolean storesMixedCaseIdentifiers() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
            return false;
        }

        @Override
        public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
            return false;
        }

        @Override
        public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
            return false;
        }

        @Override
        public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
            return false;
        }

        @Override
        public String getIdentifierQuoteString() throws SQLException {
            return null;
        }

        @Override
        public String getSQLKeywords() throws SQLException {
            return null;
        }

        @Override
        public String getNumericFunctions() throws SQLException {
            return null;
        }

        @Override
        public String getStringFunctions() throws SQLException {
            return null;
        }

        @Override
        public String getSystemFunctions() throws SQLException {
            return null;
        }

        @Override
        public String getTimeDateFunctions() throws SQLException {
            return null;
        }

        @Override
        public String getSearchStringEscape() throws SQLException {
            return null;
        }

        @Override
        public String getExtraNameCharacters() throws SQLException {
            return null;
        }

        @Override
        public boolean supportsAlterTableWithAddColumn() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsAlterTableWithDropColumn() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsColumnAliasing() throws SQLException {
            return false;
        }

        @Override
        public boolean nullPlusNonNullIsNull() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsConvert() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsConvert(int fromType, int toType) throws SQLException {
            return false;
        }

        @Override
        public boolean supportsTableCorrelationNames() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsDifferentTableCorrelationNames() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsExpressionsInOrderBy() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsOrderByUnrelated() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsGroupBy() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsGroupByUnrelated() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsGroupByBeyondSelect() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsLikeEscapeClause() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsMultipleResultSets() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsMultipleTransactions() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsNonNullableColumns() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsMinimumSQLGrammar() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsCoreSQLGrammar() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsExtendedSQLGrammar() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsANSI92EntryLevelSQL() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsANSI92IntermediateSQL() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsANSI92FullSQL() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsIntegrityEnhancementFacility() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsOuterJoins() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsFullOuterJoins() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsLimitedOuterJoins() throws SQLException {
            return false;
        }

        @Override
        public String getSchemaTerm() throws SQLException {
            return null;
        }

        @Override
        public String getProcedureTerm() throws SQLException {
            return null;
        }

        @Override
        public String getCatalogTerm() throws SQLException {
            return null;
        }

        @Override
        public boolean isCatalogAtStart() throws SQLException {
            return false;
        }

        @Override
        public String getCatalogSeparator() throws SQLException {
            return null;
        }

        @Override
        public boolean supportsSchemasInDataManipulation() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsSchemasInProcedureCalls() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsSchemasInTableDefinitions() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsSchemasInIndexDefinitions() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsCatalogsInDataManipulation() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsCatalogsInProcedureCalls() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsCatalogsInTableDefinitions() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsPositionedDelete() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsPositionedUpdate() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsSelectForUpdate() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsStoredProcedures() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsSubqueriesInComparisons() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsSubqueriesInExists() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsSubqueriesInIns() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsSubqueriesInQuantifieds() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsCorrelatedSubqueries() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsUnion() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsUnionAll() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
            return false;
        }

        @Override
        public int getMaxBinaryLiteralLength() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxCharLiteralLength() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxColumnNameLength() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxColumnsInGroupBy() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxColumnsInIndex() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxColumnsInOrderBy() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxColumnsInSelect() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxColumnsInTable() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxConnections() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxCursorNameLength() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxIndexLength() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxSchemaNameLength() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxProcedureNameLength() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxCatalogNameLength() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxRowSize() throws SQLException {
            return 0;
        }

        @Override
        public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
            return false;
        }

        @Override
        public int getMaxStatementLength() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxStatements() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxTableNameLength() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxTablesInSelect() throws SQLException {
            return 0;
        }

        @Override
        public int getMaxUserNameLength() throws SQLException {
            return 0;
        }

        @Override
        public int getDefaultTransactionIsolation() throws SQLException {
            return 0;
        }

        @Override
        public boolean supportsTransactions() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
            return false;
        }

        @Override
        public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
            return false;
        }

        @Override
        public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
            return false;
        }

        @Override
        public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
            return false;
        }

        @Override
        public ResultSet getProcedures(String catalog, String schemaPattern,
            String procedureNamePattern) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getProcedureColumns(String catalog, String schemaPattern,
            String procedureNamePattern, String columnNamePattern) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern,
            String[] types) throws SQLException {
            return new ResultSetProxy();
        }

        @Override
        public ResultSet getSchemas() throws SQLException {
            return null;
        }

        @Override
        public ResultSet getCatalogs() throws SQLException {
            return null;
        }

        @Override
        public ResultSet getTableTypes() throws SQLException {
            return null;
        }

        @Override
        public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern,
            String columnNamePattern) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getColumnPrivileges(String catalog, String schema, String table,
            String columnNamePattern) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getTablePrivileges(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getBestRowIdentifier(String catalog, String schema, String table,
            int scope, boolean nullable) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getVersionColumns(String catalog, String schema, String table)
            throws SQLException {
            return null;
        }

        @Override
        public ResultSet getPrimaryKeys(String catalog, String schema, String table)
            throws SQLException {
            return new ResultSetProxy();
        }

        @Override
        public ResultSet getImportedKeys(String catalog, String schema, String table)
            throws SQLException {
            return null;
        }

        @Override
        public ResultSet getExportedKeys(String catalog, String schema, String table)
            throws SQLException {
            return null;
        }

        @Override
        public ResultSet getCrossReference(String parentCatalog, String parentSchema,
            String parentTable, String foreignCatalog, String foreignSchema, String foreignTable)
            throws SQLException {
            return null;
        }

        @Override
        public ResultSet getTypeInfo() throws SQLException {
            return null;
        }

        @Override
        public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique,
            boolean approximate) throws SQLException {
            return null;
        }

        @Override
        public boolean supportsResultSetType(int type) throws SQLException {
            return false;
        }

        @Override
        public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
            return false;
        }

        @Override
        public boolean ownUpdatesAreVisible(int type) throws SQLException {
            return false;
        }

        @Override
        public boolean ownDeletesAreVisible(int type) throws SQLException {
            return false;
        }

        @Override
        public boolean ownInsertsAreVisible(int type) throws SQLException {
            return false;
        }

        @Override
        public boolean othersUpdatesAreVisible(int type) throws SQLException {
            return false;
        }

        @Override
        public boolean othersDeletesAreVisible(int type) throws SQLException {
            return false;
        }

        @Override
        public boolean othersInsertsAreVisible(int type) throws SQLException {
            return false;
        }

        @Override
        public boolean updatesAreDetected(int type) throws SQLException {
            return false;
        }

        @Override
        public boolean deletesAreDetected(int type) throws SQLException {
            return false;
        }

        @Override
        public boolean insertsAreDetected(int type) throws SQLException {
            return false;
        }

        @Override
        public boolean supportsBatchUpdates() throws SQLException {
            return false;
        }

        @Override
        public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern,
            int[] types) throws SQLException {
            return null;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return null;
        }

        @Override
        public boolean supportsSavepoints() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsNamedParameters() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsMultipleOpenResults() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsGetGeneratedKeys() throws SQLException {
            return false;
        }

        @Override
        public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern)
            throws SQLException {
            return null;
        }

        @Override
        public ResultSet getSuperTables(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
            String attributeNamePattern) throws SQLException {
            return null;
        }

        @Override
        public boolean supportsResultSetHoldability(int holdability) throws SQLException {
            return false;
        }

        @Override
        public int getResultSetHoldability() throws SQLException {
            return 0;
        }

        @Override
        public int getDatabaseMajorVersion() throws SQLException {
            return 0;
        }

        @Override
        public int getDatabaseMinorVersion() throws SQLException {
            return 0;
        }

        @Override
        public int getJDBCMajorVersion() throws SQLException {
            return 0;
        }

        @Override
        public int getJDBCMinorVersion() throws SQLException {
            return 0;
        }

        @Override
        public int getSQLStateType() throws SQLException {
            return 0;
        }

        @Override
        public boolean locatorsUpdateCopy() throws SQLException {
            return false;
        }

        @Override
        public boolean supportsStatementPooling() throws SQLException {
            return false;
        }

        @Override
        public RowIdLifetime getRowIdLifetime() throws SQLException {
            return null;
        }

        @Override
        public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
            return null;
        }

        @Override
        public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
            return false;
        }

        @Override
        public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
            return false;
        }

        @Override
        public ResultSet getClientInfoProperties() throws SQLException {
            return null;
        }

        @Override
        public ResultSet getFunctions(String catalog, String schemaPattern,
            String functionNamePattern) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getFunctionColumns(String catalog, String schemaPattern,
            String functionNamePattern, String columnNamePattern) throws SQLException {
            return null;
        }

        @Override
        public ResultSet getPseudoColumns(String catalog, String schemaPattern,
            String tableNamePattern, String columnNamePattern) throws SQLException {
            return null;
        }

        @Override
        public boolean generatedKeyAlwaysReturned() throws SQLException {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }

    private static class ResultSetProxy implements ResultSet {

        @Override
        public boolean next() throws SQLException {
            return false;
        }

        @Override
        public void close() throws SQLException {

        }

        @Override
        public boolean wasNull() throws SQLException {
            return false;
        }

        @Override
        public String getString(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public boolean getBoolean(int columnIndex) throws SQLException {
            return false;
        }

        @Override
        public byte getByte(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public short getShort(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public int getInt(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public long getLong(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public float getFloat(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public double getDouble(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
            return null;
        }

        @Override
        public byte[] getBytes(int columnIndex) throws SQLException {
            return new byte[0];
        }

        @Override
        public Date getDate(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getAsciiStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getUnicodeStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getBinaryStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public boolean getBoolean(String columnLabel) throws SQLException {
            return false;
        }

        @Override
        public byte getByte(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public short getShort(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public int getInt(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public long getLong(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public float getFloat(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public double getDouble(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
            return null;
        }

        @Override
        public byte[] getBytes(String columnLabel) throws SQLException {
            return new byte[0];
        }

        @Override
        public Date getDate(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getAsciiStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getUnicodeStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getBinaryStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        @Override
        public void clearWarnings() throws SQLException {

        }

        @Override
        public String getCursorName() throws SQLException {
            return null;
        }

        @Override
        public ResultSetMetaData getMetaData() throws SQLException {
            return null;
        }

        @Override
        public Object getObject(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Object getObject(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public int findColumn(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public Reader getCharacterStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Reader getCharacterStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public boolean isBeforeFirst() throws SQLException {
            return false;
        }

        @Override
        public boolean isAfterLast() throws SQLException {
            return false;
        }

        @Override
        public boolean isFirst() throws SQLException {
            return false;
        }

        @Override
        public boolean isLast() throws SQLException {
            return false;
        }

        @Override
        public void beforeFirst() throws SQLException {

        }

        @Override
        public void afterLast() throws SQLException {

        }

        @Override
        public boolean first() throws SQLException {
            return false;
        }

        @Override
        public boolean last() throws SQLException {
            return false;
        }

        @Override
        public int getRow() throws SQLException {
            return 0;
        }

        @Override
        public boolean absolute(int row) throws SQLException {
            return false;
        }

        @Override
        public boolean relative(int rows) throws SQLException {
            return false;
        }

        @Override
        public boolean previous() throws SQLException {
            return false;
        }

        @Override
        public void setFetchDirection(int direction) throws SQLException {

        }

        @Override
        public int getFetchDirection() throws SQLException {
            return 0;
        }

        @Override
        public void setFetchSize(int rows) throws SQLException {

        }

        @Override
        public int getFetchSize() throws SQLException {
            return 0;
        }

        @Override
        public int getType() throws SQLException {
            return 0;
        }

        @Override
        public int getConcurrency() throws SQLException {
            return 0;
        }

        @Override
        public boolean rowUpdated() throws SQLException {
            return false;
        }

        @Override
        public boolean rowInserted() throws SQLException {
            return false;
        }

        @Override
        public boolean rowDeleted() throws SQLException {
            return false;
        }

        @Override
        public void updateNull(int columnIndex) throws SQLException {

        }

        @Override
        public void updateBoolean(int columnIndex, boolean x) throws SQLException {

        }

        @Override
        public void updateByte(int columnIndex, byte x) throws SQLException {

        }

        @Override
        public void updateShort(int columnIndex, short x) throws SQLException {

        }

        @Override
        public void updateInt(int columnIndex, int x) throws SQLException {

        }

        @Override
        public void updateLong(int columnIndex, long x) throws SQLException {

        }

        @Override
        public void updateFloat(int columnIndex, float x) throws SQLException {

        }

        @Override
        public void updateDouble(int columnIndex, double x) throws SQLException {

        }

        @Override
        public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

        }

        @Override
        public void updateString(int columnIndex, String x) throws SQLException {

        }

        @Override
        public void updateBytes(int columnIndex, byte[] x) throws SQLException {

        }

        @Override
        public void updateDate(int columnIndex, Date x) throws SQLException {

        }

        @Override
        public void updateTime(int columnIndex, Time x) throws SQLException {

        }

        @Override
        public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x, int length)
            throws SQLException {

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x, int length)
            throws SQLException {

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x, int length)
            throws SQLException {

        }

        @Override
        public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

        }

        @Override
        public void updateObject(int columnIndex, Object x) throws SQLException {

        }

        @Override
        public void updateNull(String columnLabel) throws SQLException {

        }

        @Override
        public void updateBoolean(String columnLabel, boolean x) throws SQLException {

        }

        @Override
        public void updateByte(String columnLabel, byte x) throws SQLException {

        }

        @Override
        public void updateShort(String columnLabel, short x) throws SQLException {

        }

        @Override
        public void updateInt(String columnLabel, int x) throws SQLException {

        }

        @Override
        public void updateLong(String columnLabel, long x) throws SQLException {

        }

        @Override
        public void updateFloat(String columnLabel, float x) throws SQLException {

        }

        @Override
        public void updateDouble(String columnLabel, double x) throws SQLException {

        }

        @Override
        public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

        }

        @Override
        public void updateString(String columnLabel, String x) throws SQLException {

        }

        @Override
        public void updateBytes(String columnLabel, byte[] x) throws SQLException {

        }

        @Override
        public void updateDate(String columnLabel, Date x) throws SQLException {

        }

        @Override
        public void updateTime(String columnLabel, Time x) throws SQLException {

        }

        @Override
        public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x, int length)
            throws SQLException {

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x, int length)
            throws SQLException {

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader, int length)
            throws SQLException {

        }

        @Override
        public void updateObject(String columnLabel, Object x, int scaleOrLength)
            throws SQLException {

        }

        @Override
        public void updateObject(String columnLabel, Object x) throws SQLException {

        }

        @Override
        public void insertRow() throws SQLException {

        }

        @Override
        public void updateRow() throws SQLException {

        }

        @Override
        public void deleteRow() throws SQLException {

        }

        @Override
        public void refreshRow() throws SQLException {

        }

        @Override
        public void cancelRowUpdates() throws SQLException {

        }

        @Override
        public void moveToInsertRow() throws SQLException {

        }

        @Override
        public void moveToCurrentRow() throws SQLException {

        }

        @Override
        public Statement getStatement() throws SQLException {
            return null;
        }

        @Override
        public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public Ref getRef(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Blob getBlob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Clob getClob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Array getArray(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public Ref getRef(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Blob getBlob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Clob getClob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Array getArray(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Date getDate(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Date getDate(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public URL getURL(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public URL getURL(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateRef(int columnIndex, Ref x) throws SQLException {

        }

        @Override
        public void updateRef(String columnLabel, Ref x) throws SQLException {

        }

        @Override
        public void updateBlob(int columnIndex, Blob x) throws SQLException {

        }

        @Override
        public void updateBlob(String columnLabel, Blob x) throws SQLException {

        }

        @Override
        public void updateClob(int columnIndex, Clob x) throws SQLException {

        }

        @Override
        public void updateClob(String columnLabel, Clob x) throws SQLException {

        }

        @Override
        public void updateArray(int columnIndex, Array x) throws SQLException {

        }

        @Override
        public void updateArray(String columnLabel, Array x) throws SQLException {

        }

        @Override
        public RowId getRowId(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public RowId getRowId(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateRowId(int columnIndex, RowId x) throws SQLException {

        }

        @Override
        public void updateRowId(String columnLabel, RowId x) throws SQLException {

        }

        @Override
        public int getHoldability() throws SQLException {
            return 0;
        }

        @Override
        public boolean isClosed() throws SQLException {
            return false;
        }

        @Override
        public void updateNString(int columnIndex, String nString) throws SQLException {

        }

        @Override
        public void updateNString(String columnLabel, String nString) throws SQLException {

        }

        @Override
        public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

        }

        @Override
        public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

        }

        @Override
        public NClob getNClob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public NClob getNClob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public SQLXML getSQLXML(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public SQLXML getSQLXML(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

        }

        @Override
        public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

        }

        @Override
        public String getNString(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public String getNString(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Reader getNCharacterStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Reader getNCharacterStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateNCharacterStream(int columnIndex, Reader x, long length)
            throws SQLException {

        }

        @Override
        public void updateNCharacterStream(String columnLabel, Reader reader, long length)
            throws SQLException {

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x, long length)
            throws SQLException {

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x, long length)
            throws SQLException {

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x, long length)
            throws SQLException {

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x, long length)
            throws SQLException {

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x, long length)
            throws SQLException {

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader, long length)
            throws SQLException {

        }

        @Override
        public void updateBlob(int columnIndex, InputStream inputStream, long length)
            throws SQLException {

        }

        @Override
        public void updateBlob(String columnLabel, InputStream inputStream, long length)
            throws SQLException {

        }

        @Override
        public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateNClob(String columnLabel, Reader reader, long length)
            throws SQLException {

        }

        @Override
        public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

        }

        @Override
        public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

        }

        @Override
        public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

        }

        @Override
        public void updateClob(int columnIndex, Reader reader) throws SQLException {

        }

        @Override
        public void updateClob(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public void updateNClob(int columnIndex, Reader reader) throws SQLException {

        }

        @Override
        public void updateNClob(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
            return null;
        }

        @Override
        public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}
