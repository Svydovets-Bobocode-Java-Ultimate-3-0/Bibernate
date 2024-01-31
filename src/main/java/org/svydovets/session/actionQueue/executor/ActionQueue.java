package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.actionQueue.action.EntityAction;
import org.svydovets.session.actionQueue.action.PersistAction;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class ActionQueue {

    private final GenericJdbcDAO jdbcDAO;

    private final PersisActionExecutor persistActionExecutor;

    private final Queue<EntityAction> entityActionQueue;

    public ActionQueue(GenericJdbcDAO jdbcDAO) {
        this.jdbcDAO = jdbcDAO;
        this.persistActionExecutor = new PersisActionExecutor(jdbcDAO);
        Comparator<EntityAction> entityActionComparator = Comparator.comparingInt(o -> o.getActionPriority().getPriority());
        this.entityActionQueue = new PriorityQueue<>(entityActionComparator);
    }

    public void addPersistAction(PersistAction persistAction) {
        if (persistAction.isInstantPersist()) {
            persistActionExecutor.execute(persistAction);
        }
    }
}
