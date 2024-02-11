package org.svydovets.session.actionQueue.action;

import org.svydovets.session.EntityEntry;

/**
 * Represents an action to merge the state of an entity with the persistence context.
 * This class extends {@link EntityAction} to provide functionality specifically for
 * updating an existing entity in the database. The merge action ensures that any changes
 * made to the entity object in the application are synchronized with the corresponding
 * entity in the database.
 */
public class MergeAction extends EntityAction {

    /**
     * Constructs a new MergeAction for a specified entity entry.
     *
     * @param entityEntry the entity entry that this merge action will operate on. The
     *                    entity entry encapsulates the entity to be merged and its state.
     */
    public MergeAction(EntityEntry entityEntry) {
        super(entityEntry);
    }
}
