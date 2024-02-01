package org.svydovets.transaction;

/**
 * The {@code TransactionManager} interface defines the basic operations for transaction management.
 * It allows the initiation, commitment, and rollback of transactions, as well as querying the transaction status.
 */
public interface TransactionManager {

    /**
     * Begins a new transaction. If a transaction is already active, this method may throw an exception
     * or behave as a no-op, depending on the implementation.
     */
    void begin();

    /**
     * Commits the current transaction, making all changes made during the transaction permanent.
     * If there is no active transaction, this method may throw an exception.
     */
    void commit();

    /**
     * Rolls back the current transaction, reverting all changes made during the transaction.
     * If there is no active transaction, this method may throw an exception.
     */
    void callback();

    /**
     * Checks if there is an active transaction.
     *
     * @return {@code true} if a transaction is currently active, {@code false} otherwise.
     */
    boolean isActive();

}
