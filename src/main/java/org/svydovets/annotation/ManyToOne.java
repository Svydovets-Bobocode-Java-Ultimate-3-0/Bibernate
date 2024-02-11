package org.svydovets.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code ManyToOne} annotation is used to establish a many-to-one
 * relationship between two entities. In a many-to-one relationship, multiple
 * entities (or instances of the class where this annotation is applied) are
 * associated with a single instance of the referenced entity. This type of
 * relationship is commonly used to model the database relationships where
 * multiple rows in a table are linked to a single row in another table.
 *
 * <p>This annotation should be placed on the field that references the entity
 * that many instances of this class are related to. It is often accompanied by
 * the {@link JoinColumn} annotation to specify the foreign key column used for
 * the association.</p>
 *
 * <p>Applying the {@code ManyToOne} annotation to a field tells the ORM framework
 * that the field represents the "many" side of a many-to-one relationship. This
 * informs the framework's strategy for fetching, caching, and managing the lifecycle
 * of the associated entities.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Entity
 * public class Employee {
 *
 *     @Id
 *     private Long id;
 *
 *     @ManyToOne
 *     @JoinColumn(name = "department_id")
 *     private Department department;
 *
 *     // Other fields, constructors, getters, and setters
 * }
 * }</pre>
 *
 * <p>This annotation does not have parameters. The details of the relationship
 * and the foreign key mapping are specified through other annotations like
 * {@link JoinColumn}.</p>
 *
 * @see Target
 * @see JoinColumn
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToOne {
}
