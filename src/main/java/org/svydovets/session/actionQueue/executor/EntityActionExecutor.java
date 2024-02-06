package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.actionQueue.action.EntityAction;

public abstract class EntityActionExecutor<T extends EntityAction> {

    protected final GenericJdbcDAO jdbcDAO;

    public EntityActionExecutor(GenericJdbcDAO jdbcDAO) {
        this.jdbcDAO = jdbcDAO;
    }

    protected abstract void execute(T entityAction);
}
