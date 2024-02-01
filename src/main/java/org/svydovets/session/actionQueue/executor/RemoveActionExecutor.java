package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.EntityKey;
import org.svydovets.session.actionQueue.action.RemoveAction;

public class RemoveActionExecutor extends EntityActionExecutor<RemoveAction> {
    public RemoveActionExecutor(GenericJdbcDAO jdbcDAO) {
        super(jdbcDAO);
    }

    @Override
    protected void execute(RemoveAction entityAction) {
        EntityKey<?> entityKey = entityAction.getEntityEntry().entityKey();
        super.jdbcDAO.remove(entityKey);
    }
}
