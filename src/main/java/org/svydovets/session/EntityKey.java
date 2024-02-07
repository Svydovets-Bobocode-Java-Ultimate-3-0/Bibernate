package org.svydovets.session;

import org.svydovets.util.EntityReflectionUtils;

/**
 * Represents a unique key for an entity, combining the entity's class type with its identifier.
 * This record is utilized within a persistence context to uniquely identify, track, and manage
 * entities, especially for operations like fetching, updating, and deleting entities in a database.
 *
 * @param <T> the type of the entity this key represents
 * @param entityType the class of the entity
 * @param id the unique identifier of the entity
 */
public record EntityKey<T>(Class<T> entityType, Object id) {

    /**
     * Constructs an {@code EntityKey} for a given entity by extracting its identifier
     * using reflection utilities. This method is useful for quickly obtaining an entity's
     * key when only the entity instance is available.
     *
     * @param <T> the type of the entity
     * @param entity the entity instance
     * @return an {@code EntityKey<T>} representing the entity's unique key
     */
    @SuppressWarnings("unchecked")
    public static <T> EntityKey<T> of(T entity) {
        Object id = EntityReflectionUtils.getEntityIdValue(entity);
        return (EntityKey<T>) new EntityKey<>(entity.getClass(), id);
    }

    /**
     * Constructs an {@code EntityKey} using the provided entity and its identifier.
     * This method allows for explicit creation of an entity key when both the entity
     * and its identifier are known.
     *
     * @param entity the entity object
     * @param entityId the identifier of the entity
     * @return an {@code EntityKey} representing the unique key of the entity
     */
    public static EntityKey<?> valueOf(Object entity, Object entityId) {
        return new EntityKey<>(entity.getClass(), entityId);
    }

    /**
     * Generates an empty {@code EntityKey}, representing a null or uninitialized entity key.
     * This can be used in contexts where an entity key is required but not yet available.
     *
     * @return an empty {@code EntityKey} with both the entity type and id set to {@code null}
     */
    public static EntityKey<?> empty() {
        return new EntityKey<>(null, null);
    }
}
