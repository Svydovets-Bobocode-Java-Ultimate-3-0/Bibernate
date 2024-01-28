package org.svydovets.util;

import org.svydovets.query.ParameterNameResolver;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SqlQueryUtil {

    public static String resolveColumnNamesForInsert(Class<?> clazz) {
        Field[] insertableFields = ReflectionUtils.getInsertableFieldsForIdentityGenerationType(clazz);
        return Arrays.stream(insertableFields)
                .map(ParameterNameResolver::resolveColumnName)
                .collect(Collectors.joining(", "));
    }

    public static String resolveColumnValuesForInsert(Class<?> clazz) {
        Field[] insertableFields = ReflectionUtils.getInsertableFieldsForIdentityGenerationType(clazz);

        return Arrays.stream(insertableFields)
                .map(field -> "?")
                .collect(Collectors.joining(", "));
    }

    public static String resolveUpdatableColumnsWithValues(Class<?> clazz) {
        StringBuilder updateBuilder = new StringBuilder();
        Field[] entityFields = ReflectionUtils.getUpdatableFields(clazz);
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
