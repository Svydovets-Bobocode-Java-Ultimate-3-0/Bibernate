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
        Class<?> clazz = entity.getClass();

        String tableName = ParameterNameResolver.resolveTableName(clazz);
        String columnNames = SqlQueryUtil.resolveColumnNamesForInsert(clazz);
        String columnValues = SqlQueryUtil.resolveColumnValuesForInsert(clazz);

        return String.format(INSERT_SQL, tableName, columnNames, columnValues);
    }

    /**
     * This method helps to build a SELECT QUERY based on the primary key.
     *
     * @param clazz - entity class with annotation @Id
     */
    public static String buildSelectByIdQuery(Class<?> clazz) {
        log.trace("Call buildSelectByIdQuery({}) for class base entity", clazz);

        String tableName = ParameterNameResolver.resolveTableName(clazz);
        String idColumnName = ParameterNameResolver.getIdFieldName(clazz);

        return String.format(SELECT_BY_ID_SQL, tableName, idColumnName);
    }

    /**
     * This method helps to build a UPDATE QUERY based on the primary key.
     *
     * @param clazz - entity class with annotation @Id
     */
    public static String buildUpdateByIdQuery(Class<?> clazz) {
        log.trace("Call buildUpdateByIdQuery({}) for  entity class", clazz);

        String tableName = ParameterNameResolver.resolveTableName(clazz);
        String idColumnName = ParameterNameResolver.getIdFieldName(clazz);
        String updatableColumns = SqlQueryUtil.resolveUpdatableColumnsWithValues(clazz);

        return String.format(UPDATE_BY_ID_SQL, tableName, updatableColumns, idColumnName);
    }

    /**
     * This method helps to build a DELETE QUERY based on the primary key.
     *
     * @param clazz - entity class with annotation @Id
     */
    public static String buildDeleteByIdQuery(Class<?> clazz) {
        log.trace("Call buildDeleteByIdQuery({}) for  entity class", clazz);

        String tableName = ParameterNameResolver.resolveTableName(clazz);
        String idColumnName = ParameterNameResolver.getIdFieldName(clazz);

        return String.format(DELETE_BY_ID_SQL, tableName, idColumnName);
    }
}
