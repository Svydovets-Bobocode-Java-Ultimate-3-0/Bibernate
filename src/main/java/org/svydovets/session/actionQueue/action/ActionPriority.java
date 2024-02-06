package org.svydovets.session.actionQueue.action;

/**
 * Enumerates priority levels for database actions, providing a way to manage the execution
 * order of persist, merge, and remove operations.
 * <p>
 * Each action type is assigned a numeric priority, with lower numbers indicating higher priority.
 * This allows actions to be ordered and executed based on their defined priorities, facilitating
 * a controlled execution sequence for database operations.
 */
public enum ActionPriority {
    /**
     * Priority for persist actions.
     * Persist actions are typically given higher priority to ensure that new entities
     * are saved to the database before other operations that might depend on their existence.
     */
    PERSIST(1),
    /**
     * Priority for merge actions.
     * Merge actions usually follow persist actions to update existing entities with new values.
     */
    MERGE(2),
    /**
     * Priority for remove actions.
     * Remove actions are generally assigned the lowest priority to ensure that entities
     * are only removed after all other operations have been completed.
     */
    REMOVE(3);

    final int priority;

    ActionPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
