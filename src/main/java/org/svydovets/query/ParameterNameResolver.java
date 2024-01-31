package org.svydovets.query;

import lombok.extern.log4j.Log4j2;
import org.svydovets.annotation.Column;
import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.Table;
import org.svydovets.util.EntityReflectionUtils;

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
     * @param entityType method annotated or not as @Table
     * @return table name value from annotation or if annotation name is empty - return class name
     * @see Table
     */
    public static String resolveTableName(Class<?> entityType) {
        log.trace("Call resolveTableName({}) for class base entity", entityType);

        Table tableAnnotation = entityType.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            return entityType.getSimpleName();
        }

        return tableAnnotation.name().isEmpty()
                ? entityType.getSimpleName()
                : tableAnnotation.name();
    }

    /**
     * This method helps to define name of declared <strong>entity field</strong> by name from annotation
     *
     * @param field - field entity class annotated or not as @Column
     * @return column name value from annotation or if annotation name is empty - return field name
     * @see Column
     * @see Id
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

    /**
     * This method helps to define list name of declared <strong>entity field</strong> by name from annotation
     *
     * @param entityType - entity class annotated as @Id
     * @return primary key column name from annotation entity class
     * @see Id
     */
    public static String getIdFieldName(Class<?> entityType) {
        log.trace("Call getIdFieldName({}) for  entity class", entityType);

        return resolveColumnName(EntityReflectionUtils.getIdField(entityType));
    }

    /**
     * This method helps determine whether a class is marked with an annotation @Entity
     *
     * @param entityType - entity class annotated or not as @Entity
     * @return
     * @see Entity
     */
    public static boolean isEntity(Class<?> entityType) {
        log.trace("Call isEntity({}) for  entity class", entityType);

        return entityType.isAnnotationPresent(Entity.class);
    }
}
