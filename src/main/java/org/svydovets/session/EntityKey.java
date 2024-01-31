package org.svydovets.session;

import org.svydovets.util.ReflectionUtils;

public record EntityKey<T>(Class<T> clazz, Object id) {

    @SuppressWarnings("unchecked")
    public static <T> EntityKey<T> of(T entity) {
        Object id = ReflectionUtils.getEntityIdValue(entity);
        return (EntityKey<T>) new EntityKey<>(entity.getClass(), id);
    }
}
