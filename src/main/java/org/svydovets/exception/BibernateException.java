package org.svydovets.exception;

/**
 * A general-purpose exception for the Bibernate framework, indicating issues
 * related to its configuration, operation, or other runtime problems.
 */
public class BibernateException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public BibernateException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of the exception.
     */
    public BibernateException(String message, Throwable cause) {
        super(message, cause);
    }
}
