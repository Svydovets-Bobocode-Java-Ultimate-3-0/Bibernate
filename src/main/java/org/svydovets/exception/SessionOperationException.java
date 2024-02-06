package org.svydovets.exception;

/**
 * Represents errors that occur during the management and operation of a session.
 */
public class SessionOperationException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param e the cause of the exception.
     */
    public SessionOperationException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public SessionOperationException(String message) {
        super(message);
    }
}
