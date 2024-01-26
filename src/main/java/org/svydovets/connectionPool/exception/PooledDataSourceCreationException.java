package org.svydovets.connectionPool.exception;

/**
 * Exception class representing issues encountered during the creation of a {@code PooledDataSource}.
 * This exception extends {@code RuntimeException} and is used to indicate problems such as
 * inability to create initial connections for the pool or other issues related to data source initialization.
 */
public class PooledDataSourceCreationException extends RuntimeException {

    public PooledDataSourceCreationException(String message) {
        super(message);
    }

    public PooledDataSourceCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
