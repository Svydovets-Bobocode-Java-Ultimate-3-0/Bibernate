package org.svydovets.exception;

/**
 * A runtime exception that indicates an issue with annotation mapping on entity classes.
 * This exception is typically thrown when there are problems related to processing or
 * interpreting annotations used to define entity mappings, such as {@code @Entity},
 * {@code @Id}, or relationship annotations.
 *
 * Examples of scenarios where this exception might be thrown include missing required
 * annotations on an entity class, incorrect configuration of relationship mappings, or
 * attempting to use unsupported annotations.
 */
public class AnnotationMappingException extends RuntimeException {

    /**
     * Constructs a new {@code AnnotationMappingException} with the specified detail message.
     * The message provides a description of the annotation mapping issue that caused the exception.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the
     *                {@link Throwable#getMessage()} method.
     */
    public AnnotationMappingException(String message) {
        super(message);
    }
}
