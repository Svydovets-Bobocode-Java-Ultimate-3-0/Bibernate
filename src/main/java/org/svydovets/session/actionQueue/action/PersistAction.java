package org.svydovets.session.actionQueue.action;

/**
 * Represents an action to persist an entity into the database.
 * This class extends {@link EntityAction} and includes additional logic to handle
 * immediate or deferred persistence based on the specified flag at construction.
 * The action encapsulates the entity to be persisted and indicates whether the
 * persistence should occur immediately or be queued for batch execution.
 */
public class PersistAction extends EntityAction {

    private final boolean instantPersist;

    /**
     * Constructs a new PersistAction for the given entity with a flag indicating
     * whether to persist immediately.
     *
     * @param entity the entity object that this action will operate on.
     * @param instantPersist {@code true} if the entity should be persisted immediately;
     *                       {@code false} if it should be queued for later persistence.
     */
    public PersistAction(Object entity, boolean instantPersist) {
        super(entity);
        this.instantPersist = instantPersist;
        this.priority = ActionPriority.PERSIST;
    }

    /**
     * Determines whether this action is marked for instant persistence.
     *
     * @return {@code true} if the action should be executed immediately,
     *         {@code false} if it should be queued for batch execution.
     */
    public boolean isInstantPersist() {
        return instantPersist;
    }

    /**
     * Retrieves the entity associated with this persist action.
     *
     * @return The entity to be persisted.
     */
    public Object entity() {
        return this.entityEntry.entity();
    }
}
