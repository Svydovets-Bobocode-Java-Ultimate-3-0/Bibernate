package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.actionQueue.action.MergeAction;

/**
 * Executes {@link MergeAction}s against the database by merging the state of entities.
 * This class extends {@link EntityActionExecutor} to provide specific functionality for
 * handling merge actions, using the associated {@link GenericJdbcDAO} for database operations.
 */
public class MergeActionExecutor extends EntityActionExecutor<MergeAction> {

    /**
     * Constructs a {@code MergeActionExecutor} with the specified {@link GenericJdbcDAO}.
     *
     * @param jdbcDAO The {@code GenericJdbcDAO} instance to be used for executing merge operations.
     */
    public MergeActionExecutor(GenericJdbcDAO jdbcDAO) {
        super(jdbcDAO);
    }

    /**
     * Executes a given {@link MergeAction} by updating the entity's state in the database.
     * This method overrides the abstract {@code execute} method defined in {@link EntityActionExecutor}
     * to specifically handle the merge action, ensuring the entity's current state is synchronized
     * with the database.
     *
     * @param entityAction The {@code MergeAction} to be executed, encapsulating the entity and its state.
     */
    @Override
    protected void execute(MergeAction entityAction) {
        super.jdbcDAO.update(entityAction.getEntityEntry());
    }
}
