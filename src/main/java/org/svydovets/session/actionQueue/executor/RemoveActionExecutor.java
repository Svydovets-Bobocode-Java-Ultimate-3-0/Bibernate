package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.EntityKey;
import org.svydovets.session.actionQueue.action.RemoveAction;

/**
 * Executes {@link RemoveAction}s by removing entities from the database. This class extends
 * {@link EntityActionExecutor} to provide specialized functionality for handling removal
 * actions. It leverages the {@link GenericJdbcDAO} passed during construction to access
 * the database and perform the delete operations.
 */
public class RemoveActionExecutor extends EntityActionExecutor<RemoveAction> {

    /**
     * Constructs a {@code RemoveActionExecutor} with a specified {@link GenericJdbcDAO}.
     * This JDBC DAO is used for accessing the database to remove entities.
     *
     * @param jdbcDAO The {@code GenericJdbcDAO} instance to be used for removing entities.
     */
    public RemoveActionExecutor(GenericJdbcDAO jdbcDAO) {
        super(jdbcDAO);
    }

    /**
     * Executes the specified {@link RemoveAction} by removing the targeted entity from
     * the database. The entity to be removed is identified by the {@code EntityKey}
     * contained within the action's {@code EntityEntry}.
     *
     * @param entityAction The {@code RemoveAction} to execute, encapsulating the entity
     *                     to be removed and its identifying key.
     */
    @Override
    protected void execute(RemoveAction entityAction) {
        EntityKey<?> entityKey = entityAction.getEntityEntry().entityKey();
        super.jdbcDAO.remove(entityKey);
    }
}
