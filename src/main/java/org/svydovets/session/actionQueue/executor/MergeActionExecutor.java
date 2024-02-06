package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.actionQueue.action.MergeAction;

public class MergeActionExecutor extends EntityActionExecutor<MergeAction> {

    public MergeActionExecutor(GenericJdbcDAO jdbcDAO) {
        super(jdbcDAO);
    }

    @Override
    protected void execute(MergeAction entityAction) {
        super.jdbcDAO.update(entityAction.getEntityEntry());
    }
}
