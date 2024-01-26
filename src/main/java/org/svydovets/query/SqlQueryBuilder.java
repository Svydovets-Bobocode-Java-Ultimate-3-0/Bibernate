package org.svydovets.query;


import lombok.extern.log4j.Log4j2;
import org.svydovets.annotation.Id;

import java.lang.reflect.Field;
import java.util.Arrays;

@Log4j2
public class SqlQueryBuilder {
    private static final String SELECT_BY_ID_SQL = "select * from %s where %s = ?";
    private static final String UPDATE_BY_ID_SQL = "update %s set %s where %s = ?";

    public static String buildSelectByIdQuery(Class<?> clazz) {
        String tableName = ParameterNameResolver.resolveTableName(clazz);

        Field idField = getIdField(clazz);
        String idColumnName = ParameterNameResolver.resolveColumnName(idField);

        return String.format(SELECT_BY_ID_SQL, tableName, idColumnName);
    }


    public static String buildUpdateByIdQuery(Class<?> clazz) {
        String tableName = ParameterNameResolver.resolveTableName(clazz);

        Field idField = getIdField(clazz);
        String idColumnName = ParameterNameResolver.resolveColumnName(idField);

        String columnsForUpdate = resolveColumnsForUpdate(clazz, idColumnName);

        return String.format(UPDATE_BY_ID_SQL, tableName, columnsForUpdate, idColumnName);
    }

    private static String resolveColumnsForUpdate(Class<?> clazz, String idColumnName) {
        StringBuilder stringBuilder = new StringBuilder();
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String columnName = ParameterNameResolver.resolveColumnName(fields[i]);
            if (!columnName.equals(idColumnName)) {
                stringBuilder.append(columnName).append("=").append("?");
                if (i < fields.length - 1) {
                    stringBuilder.append(", ");
                }
            }
        }
        return stringBuilder.toString();
    }

    private static Field getIdField(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow();
    }
}
