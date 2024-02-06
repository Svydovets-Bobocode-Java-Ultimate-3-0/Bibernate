package org.svydovets.session;

import org.svydovets.util.EntityReflectionUtils;

public record EntityKey<T>(Class<T> entityType, Object id) {

    @SuppressWarnings("unchecked")
    public static <T> EntityKey<T> of(T entity) {
        Object id = EntityReflectionUtils.getEntityIdValue(entity);
        return (EntityKey<T>) new EntityKey<>(entity.getClass(), id);
    }

    public static EntityKey<?> valueOf(Object entity, Object entityId) {
        return new EntityKey<>(entity.getClass(), entityId);
    }

    public static EntityKey<?> empty() {
        return new EntityKey<>(null, null);
    }
}
