package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.actionQueue.action.MergeAction;
import org.svydovets.session.actionQueue.action.PersistAction;
import org.svydovets.session.actionQueue.action.RemoveAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a queue of database actions including persist, merge, and remove operations.
 * Actions can be executed immediately or accumulated for batch execution, depending on
 * their configuration and the nature of the operation.
 */
public class ActionQueue {

    private PersisActionExecutor persistActionExecutor;
    private MergeActionExecutor mergeActionExecutor;
    private RemoveActionExecutor removeActionExecutor;

    private List<PersistAction> persistActions;
    private List<MergeAction> mergeActions;
    private List<RemoveAction> removeActions;

    /**
     * Initializes the action queue with specific action executors and initializes
     * collections for each action type.
     *
     * @param jdbcDAO the GenericJdbcDAO instance used for database operations, shared by all executors.
     */
    public ActionQueue(GenericJdbcDAO jdbcDAO) {
        initActionExecutors(jdbcDAO);
        initActionCollections();
    }

    /**
     * Adds a persist action to the queue. If the action is marked for instant execution,
     * it is executed immediately; otherwise, it is added to the queue for later execution.
     *
     * @param persistAction the persist action to add or execute.
     */
    public void addPersistAction(PersistAction persistAction) {
        if (persistAction.isInstantPersist()) {
            persistActionExecutor.execute(persistAction);
        } else {
            persistActions.add(persistAction);
        }
    }

    /**
     * Adds a merge action to the queue for later execution.
     *
     * @param mergeAction the merge action to add.
     */
    public void addMergeAction(MergeAction mergeAction) {
        mergeActions.add(mergeAction);
    }

    /**
     * Adds a remove action to the queue for later execution.
     *
     * @param removeAction the remove action to add.
     */
    public void addRemoveAction(RemoveAction removeAction) {
        removeActions.add(removeAction);
    }

    /**
     * Executes all accumulated actions in the queue. Each type of action is executed
     * in the order they were added.
     */
    public void performAccumulatedActions() {
        persistActions.forEach(persistActionExecutor::execute);
        mergeActions.forEach(mergeActionExecutor::execute);
        removeActions.forEach(removeActionExecutor::execute);
    }

    /**
     * Initializes collections for persist, merge, and remove actions.
     */
    private void initActionCollections() {
        this.persistActions = new ArrayList<>();
        this.mergeActions = new ArrayList<>();
        this.removeActions = new ArrayList<>();
    }

    /**
     * Initializes executors for persist, merge, and remove actions with a shared GenericJdbcDAO instance.
     *
     * @param jdbcDAO the GenericJdbcDAO instance to be used by the executors.
     */
    private void initActionExecutors(GenericJdbcDAO jdbcDAO) {
        this.persistActionExecutor = new PersisActionExecutor(jdbcDAO);
        this.mergeActionExecutor = new MergeActionExecutor(jdbcDAO);
        this.removeActionExecutor = new RemoveActionExecutor(jdbcDAO);
    }
}
