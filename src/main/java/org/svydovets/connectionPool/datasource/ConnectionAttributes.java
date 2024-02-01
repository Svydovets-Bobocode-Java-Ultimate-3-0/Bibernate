package org.svydovets.connectionPool.datasource;

public class ConnectionAttributes {

    private boolean isTransactionActivated;

    public boolean isTransactionActivated() {
        return isTransactionActivated;
    }

    public void setTransactionActivated(boolean transactionActivated) {
        isTransactionActivated = transactionActivated;
    }
}
