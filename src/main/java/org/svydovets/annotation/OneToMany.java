package org.svydovets.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code OneToMany} annotation is used to denote a one-to-many relationship
 * between two entity classes. In such a relationship, a single instance of the
 * entity where this annotation is applied is associated with multiple instances
 * of the referenced entity. This relationship is typical in database schema designs
 * where a single row in one table can be linked to multiple rows in another table.
 *
 * <p>This annotation should be placed on a field that represents a collection of
 * instances of the related entity. The {@code mappedBy} attribute is crucial as it
 * specifies the field in the target entity that owns the relationship, thereby
 * establishing the direction of the relationship in bidirectional associations.</p>
 *
 * <p>Using the {@code OneToMany} annotation informs the ORM framework of the
 * relationship's nature and how to manage the collection of related entities,
 * including strategies for fetching, updating, and deleting these entities in
 * relation to their parent entity.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Entity
 * public class Department {
 *
 *     @Id
 *     private Long id;
 *
 *     @OneToMany(mappedBy = "department")
 *     private Set<Employee> employees = new HashSet<>();
 *
 *     // Other fields, constructors, getters, and setters
 * }
 * }</pre>
 *
 * <p>The {@code mappedBy} attribute indicates that the {@code Department} entity
 * does not directly manage the relationship. Instead, the {@code Employee} entity
 * contains a {@code department} field that establishes the link back to
 * {@code Department}.</p>
 *
 * @see JoinColumn
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {

    String mappedBy() default "";
}
