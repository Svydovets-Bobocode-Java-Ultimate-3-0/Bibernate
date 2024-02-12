package org.svydovets.util;

import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.JoinColumn;
import org.svydovets.annotation.ManyToOne;
import org.svydovets.annotation.OneToMany;
import org.svydovets.annotation.OneToOne;
import org.svydovets.annotation.Version;
import org.svydovets.exception.AnnotationMappingException;
import org.svydovets.exception.BibernateException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Provides utility methods for reflection-based operations on entity classes.
 * This includes identifying fields annotated for database mapping, accessing field values,
 * and instantiating entity classes. It is designed to support the framework's ORM capabilities.
 */
public class EntityReflectionUtils {

    private EntityReflectionUtils() {
    }

    /**
     * Retrieves all fields of an entity class that are mapped to database columns or relationships,
     * sorted alphabetically by field name.
     *
     * @param entityType The class of the entity.
     * @return An array of {@link Field} objects representing the sorted fields.
     */
    public static Field[] getEntityFieldsSortedByName(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> isColumnField(field) || isEntityField(field))
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    /**
     * Retrieves all fields of an entity class that can be updated in the database.
     * This excludes fields annotated with {@link Id} and collection fields representing relationships.
     *
     * @param entityType The class of the entity.
     * @return An array of {@link Field} objects representing the updatable fields.
     */
    public static Field[] getUpdatableFields(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Id.class) && !isEntityCollectionField(field))
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    /**
     * Finds the field annotated with {@link Id} in an entity class, which represents the entity's primary key.
     *
     * @param entityType The class of the entity.
     * @return The {@link Field} annotated with {@link Id}.
     * @throws AnnotationMappingException if the entity class is not annotated with {@link Entity} or does not have an Id field.
     */
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

    /**
     * Retrieves all fields that are considered insertable for an entity class. This typically includes all updatable fields.
     *
     * @param entityType The class of the entity.
     * @return An array of {@link Field} objects representing the insertable fields.
     */
    public static Field[] getInsertableFieldsForIdentityGenerationType(Class<?> entityType) {
        return getUpdatableFields(entityType);
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

    /**
     * Sets the value of a field for a given entity object.
     *
     * @param entity The target entity object.
     * @param entityField The field to set the value for.
     * @param value The value to set.
     * @throws BibernateException if an IllegalAccessException occurs.
     */
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

    /**
     * Retrieves the value of a field from a given entity object.
     *
     * @param entity The entity object.
     * @param entityField The field to retrieve the value from.
     * @return The value of the field.
     * @throws BibernateException if an IllegalAccessException occurs.
     */
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

    /**
     * Retrieves the ID value of an entity object using the field annotated with {@link Id}.
     *
     * @param entity The entity object.
     * @param <T> The type of the entity.
     * @return The ID value of the entity.
     */
    public static <T> Object getEntityIdValue(T entity) {
        return getFieldValue(entity, getIdField(entity.getClass()));
    }

    /**
     * Instantiates a new object of the specified entity class using its no-argument constructor.
     *
     * @param entityType The class to instantiate.
     * @return A new instance of the specified class.
     * @throws BibernateException if instantiation fails or the constructor is not accessible.
     */
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

    /**
     * Determines whether a field is mapped to a column in the database (and is not a relationship field).
     *
     * @param field The field to check.
     * @return {@code true} if the field is mapped to a column, {@code false} otherwise.
     */
    public static boolean isColumnField(final Field field) {
        return !isEntityCollectionField(field) && !isEntityField(field);
    }

    /**
     * Determines whether a field represents an entity relationship (ManyToOne or OneToOne with JoinColumn).
     *
     * @param field The field to check.
     * @return {@code true} if the field represents an entity relationship, {@code false} otherwise.
     */
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

    /**
     * Returns all fields that represent an entity relationship (ManyToOne or OneToOne with JoinColumn).
     *
     * @param entityType The class of the entity.
     * @return List fields represents an entity relationship.
     */
    public static List<Field> getEntityFields(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(EntityReflectionUtils::isEntityField)
                .toList();
    }

    /**
     * Determines whether a field represents a collection of entities in a relationship (OneToMany).
     *
     * @param field The field to check.
     * @return {@code true} if the field represents a collection of entities, {@code false} otherwise.
     */
    public static boolean isEntityCollectionField(final Field field) {
        return field.isAnnotationPresent(OneToMany.class);
    }

    /**
     * Determines the entity type of elements in a collection field representing an entity relationship.
     * This method is useful for resolving the generic type of a collection field, such as those
     * annotated with {@code @OneToMany}.
     *
     * @param field The collection field whose generic type is to be determined.
     * @return The {@code Class} representing the type of entities contained in the collection.
     * @throws ClassCastException if the field's generic type is not a {@code ParameterizedType} or
     *         if the actual type argument is not a {@code Class}.
     */
    public static Class<?> getJoinCollectionEntityType(Field field) {
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Type actualTypeArgument = actualTypeArguments[0];

        return (Class<?>) actualTypeArgument;
    }

    /**
     * Searches for a field in a given class that is of a specified type, typically used to find
     * the field that establishes a relationship between two entities.
     *
     * @param clazz The class type to search for within the fields of {@code joinClazz}.
     * @param joinClazz The class containing fields that potentially reference {@code clazz}.
     * @return The field in {@code joinClazz} that is of type {@code clazz}.
     * @throws AnnotationMappingException if no such field can be found.
     */
    public static <T> Field getJoinClazzField(Class<T> clazz, Class<?> joinClazz) {
        return Arrays.stream(joinClazz.getDeclaredFields())
                .filter(field -> field.getType().equals(clazz))
                .findAny()
                .orElseThrow(() -> new AnnotationMappingException(String
                        .format("Cannon find related field [%s] in %s", joinClazz, clazz)));
    }
}
