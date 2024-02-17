package org.svydovets.session;

import org.svydovets.util.EntityReflectionUtils;

/**
 * Represents an entry for an entity within a session or transaction context, combining
 * an entity's key with its current state. This record is used to manage and track the
 * lifecycle and state changes of entities during persistence operations.
 *
 * @param entityKey The key of the entity, encapsulating its type and identifier.
 * @param entity    The actual entity instance.
 */
public record EntityEntry(EntityKey<?> entityKey, Object entity) {
    /**
     * Creates a new {@code EntityEntry} with the specified entity key and entity instance.
     * This static factory method provides a convenient way to create {@code EntityEntry} instances.
     *
     * @param entityKey The key of the entity.
     * @param entity    The entity instance.
     * @return A new {@code EntityEntry} instance encapsulating the entity and its key.
     */
    public static EntityEntry valueOf(EntityKey<?> entityKey, Object entity) {
        return new EntityEntry(entityKey, entity);
    }

    /**
     * Creates an {@code EntityEntry} from an entity instance by extracting its identifier
     * and constructing an {@code EntityKey}. This method facilitates the creation of an
     * {@code EntityEntry} when only the entity instance is available, automatically handling
     * the identification of the entity.
     *
     * @param entity The entity instance from which to create an {@code EntityEntry}.
     * @return An {@code EntityEntry} representing the provided entity, with its key
     * constructed based on the entity's identifier.
     */
    public static EntityEntry valueOf(Object entity) {
        Object entityId = EntityReflectionUtils.getEntityIdValue(entity);
        return entityId == null
                ? EntityEntry.valueOf(EntityKey.empty(), entity)
                : EntityEntry.valueOf(EntityKey.valueOf(entity, entityId), entity);
    }
}
