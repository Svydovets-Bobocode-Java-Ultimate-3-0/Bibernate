package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.actionQueue.action.EntityAction;

/**
 * Abstract base class for executing specific types of {@link EntityAction}s against the database.
 * This class provides the foundational structure for action executors by encapsulating a
 * {@link GenericJdbcDAO} instance for database access and defining a generic method to
 * execute actions.
 *
 * @param <T> The type of {@link EntityAction} this executor is capable of handling, ensuring
 *            type-safe execution of actions.
 */
public abstract class EntityActionExecutor<T extends EntityAction> {

    protected final GenericJdbcDAO jdbcDAO;

    /**
     * Constructs an {@link EntityActionExecutor} with a specified {@link GenericJdbcDAO}.
     *
     * @param jdbcDAO The {@link GenericJdbcDAO} instance for database access.
     */
    public EntityActionExecutor(GenericJdbcDAO jdbcDAO) {
        this.jdbcDAO = jdbcDAO;
    }

    /**
     * Executes the given entity action against the database.
     * This method is abstract and must be implemented by subclasses to define how
     * specific types of actions are executed.
     *
     * @param entityAction The entity action to execute, of type {@code T}.
     */
    protected abstract void execute(T entityAction);
}
