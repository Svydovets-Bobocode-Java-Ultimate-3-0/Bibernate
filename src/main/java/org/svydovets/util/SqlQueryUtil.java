package org.svydovets.util;

import org.svydovets.query.ParameterNameResolver;
import org.svydovets.query.PessimisticLockStrategy;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility class for constructing SQL query parts dynamically based on entity class definitions.
 * This class uses reflection to determine the appropriate columns for SQL operations such as INSERT and UPDATE,
 * based on annotations and field types within the entity classes.
 */
public class SqlQueryUtil {

    private static final String POSTFIX_LOCK_FOR_SHARE = "for share";
    private static final String POSTFIX_LOCK_FOR_UPDATE = " for update";

    /**
     * Generates a comma-separated list of column names for use in an SQL INSERT statement.
     * This method identifies insertable fields based on their annotations and/or types, suitable
     * for inclusion in the column list of the INSERT statement.
     *
     * @param entityType The class of the entity for which to resolve column names.
     * @return A comma-separated list of column names suitable for an INSERT statement.
     */
    public static String resolveColumnNamesForInsert(Class<?> entityType) {
        Field[] insertableFields = EntityReflectionUtils.getInsertableFieldsForIdentityGenerationType(entityType);
        return Arrays.stream(insertableFields)
                .map(ParameterNameResolver::resolveColumnName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Creates a comma-separated list of placeholders ("?") corresponding to the insertable fields
     * of an entity. This is used to construct the values part of an SQL INSERT statement, where
     * each placeholder will be replaced with the actual value of the field when the statement is executed.
     *
     * @param entityType The class of the entity for which to resolve column values.
     * @return A comma-separated list of placeholders suitable for the values part of an INSERT statement.
     */
    public static String resolveColumnValuesForInsert(Class<?> entityType) {
        Field[] insertableFields = EntityReflectionUtils.getInsertableFieldsForIdentityGenerationType(entityType);

        return Arrays.stream(insertableFields)
                .map(field -> "?")
                .collect(Collectors.joining(", "));
    }

    /**
     * Generates a string for an SQL UPDATE statement that sets each updatable column to a new value,
     * represented by a placeholder ("?"). This string can then be used to dynamically update entity fields
     * in the database.
     *
     * @param entityType The class of the entity for which to construct the UPDATE statement part.
     * @return A string suitable for the SET part of an UPDATE statement, with placeholders for values.
     */
    public static String resolveUpdatableColumnsWithValues(Class<?> entityType) {
        StringBuilder updateBuilder = new StringBuilder();
        Field[] entityFields = EntityReflectionUtils.getUpdatableFields(entityType);
        for (int i = 0; i < entityFields.length; i++) {
            String columnName = ParameterNameResolver.resolveColumnName(entityFields[i]);
            updateBuilder.append(columnName).append(" = ").append("?");
            if (i < entityFields.length - 1) {
                updateBuilder.append(", ");
            }
        }

        return updateBuilder.toString();
    }

    public static String pessimisticLockBuildPostfixQuery(String sql, PessimisticLockStrategy lock){
        return switch (lock){
            case ENABLE_PESSIMISTIC_READ -> sql + POSTFIX_LOCK_FOR_UPDATE;
            case ENABLE_PESSIMISTIC_WRITE -> sql + " " + POSTFIX_LOCK_FOR_SHARE;
            case DISABLED -> sql;
        };
    }
}
