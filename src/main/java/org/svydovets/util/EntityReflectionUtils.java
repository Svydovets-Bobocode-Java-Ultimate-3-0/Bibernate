package org.svydovets.util;

import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.JoinColumn;
import org.svydovets.annotation.ManyToOne;
import org.svydovets.annotation.OneToMany;
import org.svydovets.annotation.OneToOne;
import org.svydovets.exception.AnnotationMappingException;
import org.svydovets.exception.BibernateException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;

public class EntityReflectionUtils {

    public static Field[] getEntityFieldsSortedByName(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    public static Field[] getUpdatableFields(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    public static Field getIdField(Class<?> entityType) {
        if (!entityType.isAnnotationPresent(Entity.class)) {
            throw new AnnotationMappingException(String.format(
                    "Not a managed type. Class must be marked as'@Entity': %s",
                    entityType.getName())
            );
        }
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow(() -> new AnnotationMappingException(String.format(
                        "Identifier is not specified for type: %s (Each entity must have field marked as '@Id')",
                        entityType.getName()))
                );
    }

    public static Field[] getInsertableFieldsForIdentityGenerationType(Class<?> entityType) {
        return getUpdatableFields(entityType);
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

    public static <T> Object getEntityIdValue(T entity) {
        return getFieldValue(entity, getIdField(entity.getClass()));
    }

    public static Object newInstanceOf(Class<?> entityType) {
        try {
            Constructor<?> constructor = entityType.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new BibernateException(String.format(
                    "Error creating instance of type %s. Each entity must have a default no-args constructor",
                    entityType.getName()),
                    e
            );
        }
    }

    public static boolean isEntityField(final Field field) {
        boolean isEntityAnnotation = field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToOne.class);
        if (!field.isAnnotationPresent(JoinColumn.class)) {
            throw new AnnotationMappingException(String.format("The entity field [%s] that is marked with the @OneToOne "
                    + "or @ManyToOne annotation is missing the required @JoinColumn annotation", field.getName()));
        }

        return isEntityAnnotation;
    }

    public static boolean isEntityCollectionField(final Field field) {
        return field.isAnnotationPresent(OneToMany.class);
    }

    public static Class<?> getJoinCollectionEntityType(Field field) {
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Type actualTypeArgument = actualTypeArguments[0];

        return (Class<?>) actualTypeArgument;
    }

    public static <T> Field getJoinClazzField(Class<T> clazz, Class<?> joinClazz) {
        return Arrays.stream(joinClazz.getDeclaredFields())
                .filter(field -> field.getType().equals(clazz))
                .findAny()
                .orElseThrow(() -> new AnnotationMappingException(String
                        .format("Cannon find related field [%s] in $s", joinClazz, clazz)));
    }
}
