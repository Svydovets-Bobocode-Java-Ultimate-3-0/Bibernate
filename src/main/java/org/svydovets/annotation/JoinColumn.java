package org.svydovets.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code JoinColumn} annotation is used to specify a foreign key column
 * in the entity's corresponding table in the database. This annotation is
 * primarily used in association mappings to establish the relationship between
 * two entities. When applied to a field, it indicates that the field is a
 * foreign key that references a column in another table.
 *
 * <p>Applying this annotation to a field within an entity class allows the
 * ORM framework to map the association between the entity and another entity
 * correctly. The {@code name} attribute specifies the name of the column in
 * the database table that is used as the foreign key. If the {@code name}
 * attribute is not specified, the ORM framework usually defaults to the field
 * name or applies a naming strategy defined within the framework.</p>
 *
 * <p>This annotation is crucial for defining One-To-One, Many-To-One, and
 * One-To-Many relationships in entity models, enabling the ORM framework to
 * navigate and manage associations between entities efficiently.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Entity
 * public class Order {
 *
 *     @Id
 *     private Long id;
 *
 *     @JoinColumn(name = "customer_id")
 *     private Customer customer;
 *
 *     // Other fields, constructors, getters, and setters
 * }
 * }</pre>
 *
 * <p>By specifying the {@code name} of the foreign key column, developers can
 * ensure that the ORM framework accurately maps the relationship between the
 * Order and Customer entities based on the defined database schema.</p>
 *
 * @author Your Name
 * @since 1.0
 * @see Entity
 * @see Table
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JoinColumn {

    /**
     * Specifies the name of the foreign key column in the database table.
     *
     * <p>This name is used to map the field to the column that establishes
     * the relationship between entities. If not specified, the naming
     * convention or strategy defined by the ORM framework is used.</p>
     *
     * @return the name of the foreign key column
     */
    String name() default "";
}
