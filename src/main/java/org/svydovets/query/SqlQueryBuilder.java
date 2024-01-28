package org.svydovets.query;

import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;

/**
 * Class helper for build query
 * It gets values from annotations, converts them into table or column names, and prepares a query on the values.
 *
 * @author Renat Safarov, Alexandr Navozenko
 */
@Log4j2
public class SqlQueryBuilder {
    private static final String SELECT_BY_ID_SQL = "select * from %s where %s = ?";

    private static final String UPDATE_BY_ID_SQL = "update %s set %s where %s = ?";

    private static final String DELETE_BY_ID_SQL = "delete from %s where %s = ?";

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
        String columnsForUpdate = resolveColumnsForUpdate(clazz, idColumnName);

        return String.format(UPDATE_BY_ID_SQL, tableName, columnsForUpdate, idColumnName);
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

    private static String resolveColumnsForUpdate(Class<?> clazz, String idColumnName) {
        StringBuilder stringBuilder = new StringBuilder();
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String columnName = ParameterNameResolver.resolveColumnName(fields[i]);
            if (!columnName.equals(idColumnName)) {
                stringBuilder.append(columnName).append(" = ").append("?");
                if (i < fields.length - 1) {
                    stringBuilder.append(", ");
                }
            }
        }

        return stringBuilder.toString();
    }
}
