package org.svydovets.session.actionQueue.action;

import org.svydovets.session.EntityEntry;

/**
 * Represents an action to remove an entity from the database. This class extends {@link EntityAction}
 * to provide functionality specifically for deleting entities. The removal action ensures that the
 * specified entity is deleted from the database, effectively managing the lifecycle of persisted entities.
 */
public class RemoveAction extends EntityAction {


    /**
     * Constructs a new RemoveAction for a specified entity entry.
     *
     * @param entityEntry The entity entry that this removal action will operate on. The
     *                    entity entry encapsulates the entity to be removed and its state.
     */
    public RemoveAction(EntityEntry entityEntry) {
        super(entityEntry);
    }
}
