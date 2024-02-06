package org.svydovets.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code Id} annotation is used to designate a field as the primary key
 * of the entity's corresponding table in the database. This annotation indicates
 * that the field uniquely identifies an instance of an entity. In the context of
 * ORM frameworks, the {@code Id} annotation is crucial for defining how an entity
 * is uniquely recognized within the persistence context and the database.
 *
 * <p>Applying this annotation to a field within a class annotated as an {@link Entity}
 * specifies that field as the unique identifier (primary key) for instances of the class.
 * This is essential for ORM operations that involve entity retrieval, updates, and deletions,
 * where the primary key is used to precisely locate records in the database.</p>
 *
 * <p>It is a common practice to use this annotation on fields that represent the entity's
 * primary key in the database. The field type can be any primitive data type, a String, or
 * a wrapper of a primitive data type. The choice of the field as an ID should align with
 * the database schema's primary key constraints.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Entity
 * public class User {
 *
 *     @Id
 *     private Long id;
 *
 *     // Other fields, constructors, getters, and setters
 * }
 * }</pre>
 *
 * <p>This annotation does not require parameters. The mere presence of {@code Id} on a field
 * is sufficient to mark it as the primary key of the entity.</p>
 *
 * @see Entity
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {
}
