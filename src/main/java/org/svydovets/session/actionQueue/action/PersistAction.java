package org.svydovets.session.actionQueue.action;

public class PersistAction extends EntityAction {

    private Object generatedId;
    private boolean instantPersist;

    public PersistAction(Object entity, boolean instantPersist) {
        super(entity);
        this.instantPersist = instantPersist;

    }

    public Object getGeneratedId() {
        return this.generatedId;
    }

    public void setGeneratedId(Object generatedId) {
        this.generatedId = generatedId;
    }

    public boolean isInstantPersist() {
        return instantPersist;
    }

    public Object entity() {
        return this.entity;
    }
}
