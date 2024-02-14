package org.svydovets.exception;

/**
 * Represents exceptions that occur during DAO operations, such as CRUD actions.
 */
public class DaoOperationException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param e       the cause of the exception.
     */
    public DaoOperationException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public DaoOperationException(String message) {
        super(message);
    }
}
