package org.svydovets.session.actionQueue.action;

public class PersistAction extends EntityAction {

    private boolean instantPersist;

    public PersistAction(Object entity, boolean instantPersist) {
        super(entity);
        this.instantPersist = instantPersist;
        this.priority = ActionPriority.PERSIST;
    }

    public boolean isInstantPersist() {
        return instantPersist;
    }

    public Object entity() {
        return this.entityEntry.entity();
    }
}
