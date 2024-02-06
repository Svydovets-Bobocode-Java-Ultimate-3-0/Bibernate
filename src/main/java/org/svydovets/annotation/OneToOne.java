package org.svydovets.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code OneToOne} annotation is utilized to establish a one-to-one
 * relationship between two entity classes. This type of relationship indicates
 * that an instance of the entity where this annotation is applied is associated
 * with precisely one instance of the referenced entity, and vice versa. This
 * annotation is particularly useful for modeling database relationships where
 * two tables are linked by a unique, bidirectional connection.
 *
 * <p>This annotation should be placed on a field that references another entity
 * with which there is a one-to-one association. The {@code mappedBy} attribute
 * is of significant importance for bidirectional relationships as it specifies
 * the field in the target entity that owns the relationship. This establishes
 * the directionality of the relationship and clarifies which side is responsible
 * for managing it.</p>
 *
 * <p>Applying the {@code OneToOne} annotation helps ORM frameworks to correctly
 * manage the lifecycle of the associated entities, ensuring that operations such
 * as fetching, updating, and deleting are properly synchronized between the two
 * entities involved in the one-to-one relationship.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Entity
 * public class User {
 *
 *     @Id
 *     private Long id;
 *
 *     @OneToOne(mappedBy = "user")
 *     private UserProfile profile;
 *
 *     // Other fields, constructors, getters, and setters
 * }
 *
 * @Entity
 * public class UserProfile {
 *
 *     @Id
 *     private Long id;
 *
 *     @OneToOne
 *     @JoinColumn(name = "user_id")
 *     private User user;
 *
 *     // Other fields, constructors, getters, and setters
 * }
 * }</pre>
 *
 * <p>In this example, {@code UserProfile} is the owner of the relationship as
 * indicated by the {@code JoinColumn} annotation, and the {@code mappedBy}
 * attribute in {@code User} specifies that the relationship is managed by
 * the {@code user} field in {@code UserProfile}.</p>
 *
 * @see JoinColumn
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToOne {

    /**
     * Specifies the field that owns the relationship on the referenced entity
     * side. This attribute is crucial for bidirectional relationships, defining
     * which side is responsible for managing the relationship's persistence context.
     *
     * <p>In bidirectional relationships, the {@code mappedBy} attribute denotes
     * the entity field that contains the foreign key linking back to the owning
     * entity. If the relationship is unidirectional, this attribute should not
     * be set, and the foreign key is usually defined by a {@link JoinColumn}
     * annotation on the owning side.</p>
     *
     * @return the name of the field in the referenced entity that owns the
     * relationship.
     */
    String mappedBy() default "";
}
