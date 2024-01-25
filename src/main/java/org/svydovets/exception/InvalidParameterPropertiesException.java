package org.svydovets.exception;

/**
 * Exception class for signaling invalid parameter properties issues.
 * This runtime exception is thrown when there are problems related to parameter properties,
 * such as missing required properties or errors during property processing.
 */
public class InvalidParameterPropertiesException extends RuntimeException {

    public InvalidParameterPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
