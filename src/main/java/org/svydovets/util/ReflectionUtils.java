package org.svydovets.util;

import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.exception.AnnotationMappingException;
import org.svydovets.exception.BibernateException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

public class ReflectionUtils {

    public static Field[] getEntityFieldsSortedByName(Class<?> entityClazz) {
        return Arrays.stream(entityClazz.getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    public static Field[] getUpdatableFields(Class<?> entityClazz) {
        return Arrays.stream(entityClazz.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    public static Field getIdField(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new AnnotationMappingException(String.format(
                    "Not a managed type. Class must be marked as'@Entity': %s",
                    clazz.getName())
            );
        }
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow(() -> new AnnotationMappingException(String.format(
                        "Identifier is not specified for type: %s (Each entity must have field marked as '@Id')",
                        clazz.getName()))
                );
    }

    public static Field[] getInsertableFieldsForIdentityGenerationType(Class<?> clazz) {
        return getUpdatableFields(clazz);
    }

    public static void setFieldValue(Object entity, Field entityField, Object value) {
        try {
            entityField.setAccessible(true);
            entityField.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new BibernateException(String.format(
                    "Error setting value to field %s of entity %s",
                    entityField.getName(),
                    entity.getClass().getName())
            );
        }
    }

    public static Object getFieldValue(Object entity, Field entityField) {
        try {
            entityField.setAccessible(true);
            return entityField.get(entity);
        } catch (IllegalAccessException e) {
            throw new BibernateException(String.format(
                    "Error getting value of field %s of entity %s",
                    entityField.getName(),
                    entity.getClass().getName())
            );
        }
    }
}
