package org.svydovets.dao;


import org.svydovets.exception.ResultSetParseException;
import org.svydovets.query.ParameterNameResolver;

import java.lang.reflect.Field;
import java.sql.ResultSet;

@Deprecated
public class ResultSetParser {

    public static final String ERROR_PARSING_RESULT_SET_FOR_ENTITY = "Error parsing result set for entity of type: %s";

    /**
     * @param entity
     * @param resultSet
     */
    public static void parseForEntity(Object entity, ResultSet resultSet) {
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                String columnName = ParameterNameResolver.resolveColumnName(field);

                field.setAccessible(true);
                field.set(entity, resultSet.getObject(columnName));
            }
        } catch (Exception exception) {
            throw new ResultSetParseException(String
                    .format(ERROR_PARSING_RESULT_SET_FOR_ENTITY, entity.getClass().getName()), exception);
        }
    }
}
