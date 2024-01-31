package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.actionQueue.action.EntityAction;
import org.svydovets.session.actionQueue.action.MergeAction;
import org.svydovets.session.actionQueue.action.PersistAction;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class ActionQueue {

    private final PersisActionExecutor persistActionExecutor;
    private final MergeActionExecutor mergeActionExecutor;

    private final Queue<EntityAction> entityActions;

    public ActionQueue(GenericJdbcDAO jdbcDAO) {
        this.persistActionExecutor = new PersisActionExecutor(jdbcDAO);
        this.mergeActionExecutor = new MergeActionExecutor(jdbcDAO);
        Comparator<EntityAction> entityActionComparator = Comparator.comparingInt(entityAction ->
                entityAction.getActionPriority().getPriority()
        );
        this.entityActions = new PriorityQueue<>(entityActionComparator);
    }

    public void addPersistAction(PersistAction persistAction) {
        if (persistAction.isInstantPersist()) {
            persistActionExecutor.execute(persistAction);
        }
    }

    public void addMergeAction(MergeAction mergeAction) {
        entityActions.add(mergeAction);
    }

    public void performAccumulatedActions() {
//        entityActions.forEach();
    }
}
