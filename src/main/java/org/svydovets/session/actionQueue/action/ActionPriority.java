package org.svydovets.session.actionQueue.action;

public enum ActionPriority {
    PERSIST(1),
    MERGE(2),
    REMOVE(3);

    final int priority;

    ActionPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
