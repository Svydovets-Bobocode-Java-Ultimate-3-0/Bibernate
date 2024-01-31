package org.svydovets.session.actionQueue.action;

public abstract class EntityAction {

    protected final Object entity;
    protected ActionPriority priority;

    public EntityAction(Object entity) {
        this.entity = entity;
    }

    public Object getEntity() {
        return entity;
    }

    public ActionPriority getActionPriority() {
        return priority;
    }
}
