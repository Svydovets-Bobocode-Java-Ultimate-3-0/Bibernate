package org.svydovets.query;


import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.svydovets.annotation.Column;
import org.svydovets.annotation.Table;

import java.lang.reflect.Field;

/**
 * Class helper for resolving names of table and column
 * Main goal - work with values of annotations for fields, classes or methods
 * It gets values from annotations, convert into name of field/variable
 *
 * @author Renat Safarov, Alexandr Navozenko
 */
@Log4j2
public class ParameterNameResolver {

    /**
     * This method helps to define name of declared <strong>entity class</strong> by name from annotation
     *
     * @param clazz method annotated or not as @Table
     * @return table name value from annotation or if annotation name is empty - return class name
     * @see Table
     */
    public static String resolveTableName(Class<?> clazz) {
        log.trace("Call resolveTableName({}) for class base entity", clazz);

        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            return clazz.getSimpleName();
        }

        return tableAnnotation.name().isEmpty()
                ? clazz.getSimpleName()
                : tableAnnotation.name();
    }

    /**
     * This method helps to define name of declared <strong>entity field</strong> by name from annotation
     *
     * @param field - field entity class annotated or not as @Column
     * @return column name value from annotation or if annotation name is empty - return field name
     * @see Column
     */
    public static String resolveColumnName(Field field) {
        log.trace("Call resolveColumnName({}) for field base entity", field);

        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation == null) {
            return field.getName();
        }

        return columnAnnotation.name().isEmpty()
                ? field.getName()
                : columnAnnotation.name();
    }
}
