package org.svydovets.session.actionQueue.action;

import org.svydovets.session.EntityEntry;

public abstract class EntityAction {

    protected EntityEntry entityEntry;
    protected ActionPriority priority;

    public EntityAction(EntityEntry entityEntry) {
        this.entityEntry = entityEntry;
    }

    public EntityAction(Object entity) {
        this.entityEntry = EntityEntry.valueOf(entity);
    }

    public void updateEntityEntry(EntityEntry entityEntry) {
        this.entityEntry = entityEntry;
    }

    public EntityEntry getEntityEntry() {
        return entityEntry;
    }

    public ActionPriority getActionPriority() {
        return priority;
    }

    public void setPriority(ActionPriority priority) {
        this.priority = priority;
    }
}
