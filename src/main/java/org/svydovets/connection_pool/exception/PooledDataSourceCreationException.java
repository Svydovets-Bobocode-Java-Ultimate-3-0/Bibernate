package org.svydovets.connection_pool.exception;

public class PooledDataSourceCreationException extends RuntimeException {

    public PooledDataSourceCreationException(String message) {
        super(message);
    }

    public PooledDataSourceCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
