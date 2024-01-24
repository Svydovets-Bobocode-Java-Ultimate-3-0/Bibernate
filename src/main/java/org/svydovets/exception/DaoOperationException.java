package org.svydovets.exception;

public class DaoOperationException extends RuntimeException {
    public DaoOperationException(String message, Exception e) {
        super(message, e);
    }

    public DaoOperationException(String message) {
        super(message);
    }
}
