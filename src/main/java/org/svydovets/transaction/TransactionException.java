package org.svydovets.transaction;

/**
 * {@code TransactionException} is a custom exception type used to indicate errors that occur
 * during transaction processing in the {@code org.svydovets.transaction} package.
 * This class extends {@link RuntimeException}, allowing it to be used as an unchecked exception.
 */
public class TransactionException extends RuntimeException {

    /**
     * Constructs a new {@code TransactionException} with the specified detail message.
     * The detail message is saved for later retrieval by the {@link Throwable#getMessage()} method.
     *
     * @param message the detail message. The detail message is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method.
     */
    public TransactionException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code TransactionException} with the specified detail message and cause.
     * <p>Note that the detail message associated with {@code cause} is not automatically incorporated
     * in this exception's detail message.</p>
     *
     * @param message the detail message. The detail message is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method.
     * @param cause   the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     *                (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}