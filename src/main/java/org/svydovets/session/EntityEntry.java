package org.svydovets.session;

import org.svydovets.util.EntityReflectionUtils;

public record EntityEntry(EntityKey<?> entityKey, Object entity) {
    public static EntityEntry valueOf(EntityKey<?> entityKey, Object entity) {
        return new EntityEntry(entityKey, entity);
    }

    public static EntityEntry valueOf(Object entity) {
        Object entityId = EntityReflectionUtils.getEntityIdValue(entity);
        return entityId == null
                ? EntityEntry.valueOf(EntityKey.empty(), entity)
                : EntityEntry.valueOf(EntityKey.valueOf(entity, entityId), entity);
    }
}
