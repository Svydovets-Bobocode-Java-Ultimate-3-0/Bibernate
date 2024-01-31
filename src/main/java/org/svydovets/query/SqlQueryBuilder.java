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

    private static final String INSERT_SQL = "insert into %s (%s) values(%s)";

    private static final String UPDATE_BY_ID_SQL = "update %s set %s where %s = ?";

    private static final String DELETE_BY_ID_SQL = "delete from %s where %s = ?";

    public static String buildInsertQuery(Object entity) {
        Class<?> entityType = entity.getClass();

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
        log.trace("Call buildSelectByIdQuery({}) for class base entity", entityType);

        String tableName = ParameterNameResolver.resolveTableName(entityType);
        String idColumnName = ParameterNameResolver.getIdFieldName(entityType);

        return String.format(SELECT_BY_ID_SQL, tableName, idColumnName);
    }

    /**
     * This method helps to build a UPDATE QUERY based on the primary key.
     *
     * @param entityType - entity class with annotation @Id
     */
    public static String buildUpdateByIdQuery(Class<?> entityType) {
        log.trace("Call buildUpdateByIdQuery({}) for  entity class", entityType);

        String tableName = ParameterNameResolver.resolveTableName(entityType);
        String idColumnName = ParameterNameResolver.getIdFieldName(entityType);
        String updatableColumns = SqlQueryUtil.resolveUpdatableColumnsWithValues(entityType);

        return String.format(UPDATE_BY_ID_SQL, tableName, updatableColumns, idColumnName);
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
