package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.actionQueue.action.MergeAction;
import org.svydovets.session.actionQueue.action.PersistAction;
import org.svydovets.session.actionQueue.action.RemoveAction;

import java.util.ArrayList;
import java.util.List;

public class ActionQueue {

    private PersisActionExecutor persistActionExecutor;
    private MergeActionExecutor mergeActionExecutor;
    private RemoveActionExecutor removeActionExecutor;

    private List<PersistAction> persistActions;
    private List<MergeAction> mergeActions;
    private List<RemoveAction> removeActions;

    public ActionQueue(GenericJdbcDAO jdbcDAO) {
        initActionExecutors(jdbcDAO);
        initActionCollections();
    }

    private void initActionCollections() {
        this.persistActions = new ArrayList<>();
        this.mergeActions = new ArrayList<>();
        this.removeActions = new ArrayList<>();
    }

    private void initActionExecutors(GenericJdbcDAO jdbcDAO) {
        this.persistActionExecutor = new PersisActionExecutor(jdbcDAO);
        this.mergeActionExecutor = new MergeActionExecutor(jdbcDAO);
        this.removeActionExecutor = new RemoveActionExecutor(jdbcDAO);
    }

    public void addPersistAction(PersistAction persistAction) {
        if (persistAction.isInstantPersist()) {
            persistActionExecutor.execute(persistAction);
        } else {
            persistActions.add(persistAction);
        }
    }

    public void addMergeAction(MergeAction mergeAction) {
        mergeActions.add(mergeAction);
    }

    public void addRemoveAction(RemoveAction removeAction) {
        removeActions.add(removeAction);
    }

    public void performAccumulatedActions() {
        persistActions.forEach(persistActionExecutor::execute);
        mergeActions.forEach(mergeActionExecutor::execute);
        removeActions.forEach(removeActionExecutor::execute);
    }
}
