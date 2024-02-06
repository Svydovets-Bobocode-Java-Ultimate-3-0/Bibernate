package org.svydovets.exception;

/**
 * Exception class for signaling invalid parameter properties issues.
 * This runtime exception is thrown when there are problems related to parameter properties,
 * such as missing required properties or errors during property processing.
 */
public class InvalidParameterPropertiesException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause of the exception.
     */
    public InvalidParameterPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
