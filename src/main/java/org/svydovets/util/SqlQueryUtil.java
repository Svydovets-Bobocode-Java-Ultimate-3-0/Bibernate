package org.svydovets.util;

import org.svydovets.query.ParameterNameResolver;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SqlQueryUtil {

    public static String resolveColumnNamesForInsert(Class<?> entityType) {
        Field[] insertableFields = EntityReflectionUtils.getInsertableFieldsForIdentityGenerationType(entityType);
        return Arrays.stream(insertableFields)
                .map(ParameterNameResolver::resolveColumnName)
                .collect(Collectors.joining(", "));
    }

    public static String resolveColumnValuesForInsert(Class<?> entityType) {
        Field[] insertableFields = EntityReflectionUtils.getInsertableFieldsForIdentityGenerationType(entityType);

        return Arrays.stream(insertableFields)
                .map(field -> "?")
                .collect(Collectors.joining(", "));
    }

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
}
