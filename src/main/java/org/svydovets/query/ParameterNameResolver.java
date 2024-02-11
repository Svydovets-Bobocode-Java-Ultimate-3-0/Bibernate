package org.svydovets.query;

import lombok.extern.log4j.Log4j2;
import org.svydovets.annotation.Column;
import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.JoinColumn;
import org.svydovets.annotation.Table;
import org.svydovets.exception.AnnotationMappingException;
import org.svydovets.util.EntityReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
     * This method helps to define name of declared <strong>entity field</strong> by name from annotations @Column or @JoinColumn
     *
     * @param field - field entity class annotated or @JoinColumn or annotated not as @Column
     * @return column name value from annotations or if annotation name is empty - return field name
     * @see Column
     * @see JoinColumn
     */
    public static String resolveJoinColumnOrColumnName(Field field) {
        log.trace("Call resolveJoinColumnOrColumnName({}) for field base entity", field);

        JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);

        return joinColumnAnnotation != null ? resolveJoinColumnName(field) : resolveColumnName(field);
    }

    /**
     * This method helps to define join column name of declared <strong>entity field</strong> by name from annotation
     *
     * @param field - field entity class annotated or not as @JoinColumn
     * @return join column name value from annotation or if annotation name is empty - return field name
     * @see JoinColumn
     */
    public static String resolveJoinColumnName(Field field) {
        log.trace("Call resolveJoinColumnName({}) for field base entity", field);

        JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
        if (joinColumnAnnotation == null) {
            throw new AnnotationMappingException(String
                    .format("Field [%s] must be marked like JoinColumn annotation", field.getName()));
        }

        return joinColumnAnnotation.name().isEmpty()
                ? field.getName()
                : joinColumnAnnotation.name();
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

    public static String getVersionFieldName(Class<?> entityType) {
        log.trace("Call getVersionFieldName({}) for  entity class", entityType);

        Field versionField = EntityReflectionUtils.getVersionField(entityType);
        if (versionField == null){
            return "";
        } else {
            return resolveColumnName(versionField);
        }
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


    /**
     * This method returns a map with dependencies of the field name and column name
     *
     * @param entityType
     * @return map there key - field name, value - column name
     */
    public static Map<String, String> getColumnNameByFieldNameMap(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> !EntityReflectionUtils.isEntityCollectionField(field))
                .collect(Collectors.toMap(Field::getName, ParameterNameResolver::resolveJoinColumnOrColumnName));
    }
}
