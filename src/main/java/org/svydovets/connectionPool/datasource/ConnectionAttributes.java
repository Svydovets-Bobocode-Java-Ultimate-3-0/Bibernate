package org.svydovets.connectionPool.datasource;

/**
 * The {@code ConnectionAttributes} class represents various attributes related to a database connection.
 * Specifically, this class manages the state of transaction activation.
 */
public class ConnectionAttributes {

    private boolean isTransactionActivated;

    /**
     * Returns the transaction activation status.
     *
     * @return {@code true} if a transaction is currently activated, {@code false} otherwise.
     */
    public boolean isTransactionActivated() {
        return isTransactionActivated;
    }

    /**
     * Sets the transaction activation status.
     *
     * @param transactionActivated the new status of the transaction activation.
     *                             {@code true} to indicate that a transaction is activated,
     *                             {@code false} otherwise.
     */
    public void setTransactionActivated(boolean transactionActivated) {
        isTransactionActivated = transactionActivated;
    }
}
