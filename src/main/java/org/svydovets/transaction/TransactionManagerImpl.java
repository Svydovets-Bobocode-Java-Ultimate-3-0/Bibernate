package org.svydovets.transaction;

import org.svydovets.connectionPool.datasource.ConnectionHandler;

import java.sql.SQLException;

public class TransactionManagerImpl implements TransactionManager {

    private boolean isActive;
    private final ConnectionHandler connectionHandler;

    public TransactionManagerImpl(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    @Override
    public void begin() {
        if (isActive) {
            throw new TransactionException("Transaction was started");
        }
        try {
            connectionHandler.getConnection().setAutoCommit(false);
            connectionHandler.getConnectionAttributes().setTransactionActivated(true);
            isActive = true;
        } catch (SQLException e) {
            throw new TransactionException(e.getMessage(), e);
        }
    }

    @Override
    public void commit() {
        if (!isActive) {
            throw new TransactionException("Transaction is not started");
        }
        try {
            connectionHandler.getConnection().commit();
            isActive = false;
            connectionHandler.getConnectionAttributes().setTransactionActivated(false);
        } catch (SQLException e) {
            throw new TransactionException(e.getMessage(), e);
        }
    }

    @Override
    public void callback() {
        if (!isActive) {
            throw new TransactionException("Transaction is not started");
        }
        try {
            connectionHandler.getConnection().rollback();
            isActive = false;
            connectionHandler.getConnectionAttributes().setTransactionActivated(false);
        } catch (SQLException e) {
            throw new TransactionException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }
}
