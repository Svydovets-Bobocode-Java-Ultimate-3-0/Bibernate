package org.svydovets.session.actionQueue.action;

import org.svydovets.session.EntityEntry;

/**
 * Abstract base class for actions related to entities, such as persisting, merging, or removing.
 * It defines common properties and functionality that all entity actions share, including
 * the entity entry and action priority.
 */
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
