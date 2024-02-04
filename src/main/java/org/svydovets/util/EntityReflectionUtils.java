package org.svydovets.util;

import org.svydovets.annotation.*;
import org.svydovets.exception.AnnotationMappingException;
import org.svydovets.exception.BibernateException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class EntityReflectionUtils {

    public static Field[] getEntityFieldsSortedByName(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> isColumnField(field) || isEntityField(field))
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    public static Field[] getUpdatableFields(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Id.class) && !isEntityCollectionField(field))
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    public static Field getIdField(Class<?> entityType) {
        if (!entityType.isAnnotationPresent(Entity.class)) {
            throw new AnnotationMappingException(String.format(
                    "Not a managed type. Class must be marked as '@Entity': %s",
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

    public static Field getVersionField(Class<?> entityType) {
        if (!entityType.isAnnotationPresent(Entity.class)) {
            throw new AnnotationMappingException(String.format(
                    "Not a managed type. Class must be marked as '@Entity': %s",
                    entityType.getName())
            );
        }
        List<Field> versions = Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> isVesionOptLockField(field))
                .toList();
        if (versions.size() > 1) {
            throw new AnnotationMappingException(
                    String.format("Entity '%s' has more than 1 '@Version' annotated field. Annotated fields: %s"
                    , entityType.getName() ,versions.stream().map(Field::getName).toList()));
        } else if (versions.size() < 1){
            return null;
        } else {
            return versions.get(0);
        }
    }

    public static Object incrementVersionField(Field field, Object entity) throws IllegalAccessException {
        field.setAccessible(true);
        if (field.getType().isAssignableFrom(Integer.class)){
            return ((Integer) field.get(entity)) + 1;
        } else if (field.getType().isAssignableFrom(Long.class)){
            return ((Long) field.get(entity)) + 1;
        } else if (field.getType().isAssignableFrom(int.class)) {
            return ((int) field.get(entity)) + 1;
        } else if (field.getType().isAssignableFrom(long.class)) {
            return ((long) field.get(entity)) + 1;
        }
        else {
            throw new AnnotationMappingException(String.format(
                    "In entity %S not a managed type '%s' for '@Version', supported types Integer, Long, int, long",
                    entity.getClass().getName(),  field.getType()));
        }
    }

    public static boolean isVesionOptLockField(Field field){
        return field.isAnnotationPresent(Version.class);
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
            if (isEntityField(entityField)) {
                Class<?> fieldType = entityField.getType();
                var joinEntity = fieldType.cast(entityField.get(entity));
                var idFieldJoinEntity = getIdField(fieldType);

                idFieldJoinEntity.setAccessible(true);

                return idFieldJoinEntity.get(joinEntity);
            } else if (isColumnField(entityField)) {
                return entityField.get(entity);
            }

            throw new BibernateException(String.format("Invalid relation for field [%s] of entity [%s]",
                            entityField.getName(), entity.getClass().getName()));
        } catch (IllegalAccessException exception) {
            throw new BibernateException(String.format("Error getting value of field %s of entity %s",
                    entityField.getName(), entity.getClass().getName()), exception);
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
            throw new BibernateException(String
                    .format("Error creating instance of type %s. Each entity must have a default no-args constructor",
                    entityType.getName()),
                    e
            );
        }
    }

    public static boolean isColumnField(final Field field) {
        return !isEntityCollectionField(field) && !isEntityField(field);
    }

    public static boolean isEntityField(final Field field) {
        boolean isEntityAnnotation = field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToOne.class);

        if (isEntityAnnotation) {
            if (!field.isAnnotationPresent(JoinColumn.class)) {
                throw new AnnotationMappingException(String.format("The entity field [%s] that is marked with @OneToOne "
                        + "or @ManyToOne annotation is missing the required @JoinColumn annotation", field.getName()));
            }
        } else {
            if (field.isAnnotationPresent(JoinColumn.class)) {
                throw new AnnotationMappingException(String.format("The entity field [%s] that is marked with the "
                        + "@JoinColumn annotation is missing @OneToOne or @ManyToOne annotation", field.getName()));
            }
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
                        .format("Cannon find related field [%s] in %s", joinClazz, clazz)));
    }
}
