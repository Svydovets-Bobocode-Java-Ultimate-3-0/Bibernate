package org.svydovets.session;

import org.svydovets.util.EntityReflectionUtils;

public record EntityKey<T>(Class<T> entityType, Object id) {

    @SuppressWarnings("unchecked")
    public static <T> EntityKey<T> of(T entity) {
        Object id = EntityReflectionUtils.getEntityIdValue(entity);
        return (EntityKey<T>) new EntityKey<>(entity.getClass(), id);
    }
}
