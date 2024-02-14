package org.svydovets.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code Column} annotation is used to specify the mapping between
 * a class field and a column in a database table. When a class is annotated
 * as an {@link Entity}, its fields can be mapped to columns of a database table
 * using this annotation. This allows ORM frameworks to understand how to map
 * the fields of an object to the columns in the database table.
 *
 * <p>This annotation should be applied to the field level. The {@code name}
 * attribute is used to specify the exact name of the column in the database
 * table that the field should be mapped to. If the {@code name} attribute is
 * not specified, the field name is typically used as the column name by default,
 * following the ORM framework's naming conventions.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Entity
 * public class User {
 *
 *     @Column(name = "user_id")
 *     private Long id;
 *
 *     @Column(name = "user_name")
 *     private String name;
 *
 *     // Other fields, constructors, getters, and setters
 * }
 * }</pre>
 *
 * <p>Using the {@code Column} annotation helps in making the database schema
 * configuration clear and concise directly within the Java class definitions,
 * improving the readability and maintainability of the ORM mapping configuration.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * Specifies the name of the database column that the field is mapped to.
     *
     * <p>If not specified, the ORM framework typically defaults to using the
     * field name as the column name, possibly applying naming strategies
     * defined within the framework.</p>
     *
     * @return the name of the database column
     */
    String name() default "";
}
