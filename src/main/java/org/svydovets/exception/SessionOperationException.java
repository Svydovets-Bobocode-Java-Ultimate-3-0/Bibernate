package org.svydovets.exception;

public class SessionOperationException extends RuntimeException {
    public SessionOperationException(String message, Exception e) {
        super(message, e);
    }

    public SessionOperationException(String message) {
        super(message);
    }
}
