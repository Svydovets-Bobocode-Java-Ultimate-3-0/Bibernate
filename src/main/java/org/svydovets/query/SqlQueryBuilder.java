package org.svydovets.query;

import lombok.extern.log4j.Log4j2;
import org.svydovets.util.SqlQueryUtil;

/**
 * Class helper for build query
 * It gets values from annotations, converts them into table or column names, and prepares a query on the values.
 *
 * @author Renat Safarov, Alexandr Navozenko
 */
@Log4j2
public class SqlQueryBuilder {
    private static final String SELECT_BY_ID_SQL = "select * from %s where %s = ?";

    private static final String INSERT_SQL = "insert into %s (%s) values (%s)";

    private static final String UPDATE_BY_ID_SQL = "update %s set %s where %s = ?";

    private static final String UPDATE_OPT_LOCK_VERSION_POSTFIX = " and %s = ?";

    private static final String DELETE_BY_ID_SQL = "delete from %s where %s = ?";

    /**
     * This method helps to build a INSERT QUERY based on the primary key.
     *
     * @param entityType - entity type
     */
    public static String buildInsertQuery(Class<?> entityType) {
        String tableName = ParameterNameResolver.resolveTableName(entityType);
        String columnNames = SqlQueryUtil.resolveColumnNamesForInsert(entityType);
        String columnValues = SqlQueryUtil.resolveColumnValuesForInsert(entityType);

        return String.format(INSERT_SQL, tableName, columnNames, columnValues);
    }

    /**
     * This method helps to build a SELECT QUERY based on the primary key.
     *
     * @param entityType - entity class with annotation @Id
     */
    public static String buildSelectByIdQuery(Class<?> entityType) {
        return buildSelectByIdQuery(entityType, PessimisticLockStrategy.DISABLED);
    }
    public static String buildSelectByIdQuery(Class<?> entityType, PessimisticLockStrategy lock) {
        log.trace("Call buildSelectByIdQuery({}) for class base entity with PessimisticLock is ({})", entityType, lock);

        String tableName = ParameterNameResolver.resolveTableName(entityType);
        String idColumnName = ParameterNameResolver.getIdFieldName(entityType);

        return buildSelectByColumnQuery(tableName, idColumnName, lock);
    }

    /**
     * This method helps to build a SELECT QUERY based on the column name.
     *
     * @param tableName - entity table name
     * @param columnName - entity column name
     * @return prepared select query
     */
    public static String buildSelectByColumnQuery(final String tableName, final String columnName) {
        return buildSelectByColumnQuery(tableName, columnName, PessimisticLockStrategy.DISABLED);
    }

    public static String buildSelectByColumnQuery(final String tableName, final String columnName, PessimisticLockStrategy lock) {
        log.trace("Call buildSelectByColumnQuery({}, {}) for class base entity ({}) and PessimisticLock ({})", tableName, columnName, lock);
        String sql = String.format(SELECT_BY_ID_SQL, tableName, columnName);
        return SqlQueryUtil.pessimisticLockBuildPostfixQuery(sql, lock);
    }

    /**
     * This method helps to build a UPDATE QUERY based on the primary key.
     *
     * @param entityType - entity class with annotation @Id
     */
    public static String buildUpdateByIdQuery(Class<?> entityType) {
        return buildUpdateByIdQuery(entityType, PessimisticLockStrategy.DISABLED);
    }

    public static String buildUpdateByIdQuery(Class<?> entityType, PessimisticLockStrategy lock) {
        log.trace("Call buildUpdateByIdQuery({}) for  entity class and PessimisticLock ({})", entityType, lock);

        String tableName = ParameterNameResolver.resolveTableName(entityType);
        String idColumnName = ParameterNameResolver.getIdFieldName(entityType);
        String updatableColumns = SqlQueryUtil.resolveUpdatableColumnsWithValues(entityType);
        String versionOptLockColumnName = ParameterNameResolver.getVersionFieldName(entityType);
        if (versionOptLockColumnName != null && !versionOptLockColumnName.isBlank()){
            String sql = String.format(UPDATE_BY_ID_SQL + UPDATE_OPT_LOCK_VERSION_POSTFIX, tableName, updatableColumns, idColumnName, versionOptLockColumnName);
            return SqlQueryUtil.pessimisticLockBuildPostfixQuery(sql, lock);
        } else {
            String sql = String.format(UPDATE_BY_ID_SQL, tableName, updatableColumns, idColumnName);
            return SqlQueryUtil.pessimisticLockBuildPostfixQuery(sql, lock);
        }
    }

    /**
     * This method helps to build a DELETE QUERY based on the primary key.
     *
     * @param entityType - entity class with annotation @Id
     */
    public static String buildDeleteByIdQuery(Class<?> entityType) {
        log.trace("Call buildDeleteByIdQuery({}) for  entity class", entityType);

        String tableName = ParameterNameResolver.resolveTableName(entityType);
        String idColumnName = ParameterNameResolver.getIdFieldName(entityType);

        return String.format(DELETE_BY_ID_SQL, tableName, idColumnName);
    }
}
