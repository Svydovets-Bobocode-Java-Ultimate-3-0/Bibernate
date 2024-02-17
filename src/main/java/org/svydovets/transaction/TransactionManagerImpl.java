package org.svydovets.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svydovets.connectionPool.datasource.ConnectionHandler;
import org.svydovets.session.actionQueue.executor.ActionQueue;

import java.sql.SQLException;

/**
 * Implementation of the {@link TransactionManager} interface.
 * This class manages database transactions by handling the beginning, committing, and rollback of transactions.
 * It also provides a method to check if a transaction is currently active.
 */
public class TransactionManagerImpl implements TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManagerImpl.class);
    private boolean isActive;
    private final ConnectionHandler connectionHandler;
    private final ActionQueue actionQueue;

    /**
     * Constructs a new {@code TransactionManagerImpl} with the specified {@link ConnectionHandler}.
     *
     * @param connectionHandler the connection handler used for managing database connections.
     */
    public TransactionManagerImpl(ConnectionHandler connectionHandler, ActionQueue actionQueue) {
        this.connectionHandler = connectionHandler;
        this.actionQueue = actionQueue;
    }

    /**
     * Begins a new transaction. If a transaction is already active, this method throws a {@link TransactionException}.
     * It sets the auto-commit mode of the database connection to false, indicating the start of a transaction.
     */
    @Override
    public void begin() {
        if (isActive) {
            throw new TransactionException("Transaction was started");
        }

        if (log.isInfoEnabled()) {
            log.info("Transaction was started");
        }

        try {
            isActive = true;
            connectionHandler.getConnectionAttributes().setTransactionActivated(true);
            connectionHandler.getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            throw new TransactionException(e.getMessage(), e);
        }
    }

    /**
     * Commits the current transaction. If there is no active transaction, this method throws a {@link TransactionException}.
     * It also sets the transaction state as inactive and resets the connection's auto-commit mode.
     */
    @Override
    public void commit() {
        if (!isActive) {
            throw new TransactionException("Transaction is not started");
        }

        try {
            actionQueue.performAccumulatedActions();
            connectionHandler.getConnection().commit();
            connectionHandler.closeConnectionByThreadName();
            isActive = false;
            connectionHandler.getConnectionAttributes().setTransactionActivated(false);

            if (log.isInfoEnabled()) {
                log.info("Transaction was commit");
            }
        } catch (SQLException e) {
            throw new TransactionException(e.getMessage(), e);
        }
    }

    /**
     * Rolls back the current transaction. If there is no active transaction, this method throws a {@link TransactionException}.
     * It also sets the transaction state as inactive and resets the connection's auto-commit mode.
     */
    @Override
    public void callback() {
        if (!isActive) {
            throw new TransactionException("Transaction is not started");
        }

        try {
            connectionHandler.getConnection().rollback();
            isActive = false;
            connectionHandler.getConnectionAttributes().setTransactionActivated(false);

            if (log.isInfoEnabled()) {
                log.info("Transaction was rollback");
            }
        } catch (SQLException e) {
            throw new TransactionException(e.getMessage(), e);
        }
    }

    /**
     * Checks if a transaction is currently active.
     *
     * @return {@code true} if a transaction is currently active, {@code false} otherwise.
     */
    @Override
    public boolean isActive() {
        return isActive;
    }
}
