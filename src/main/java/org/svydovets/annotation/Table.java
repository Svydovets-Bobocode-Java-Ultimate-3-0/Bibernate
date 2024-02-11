package org.svydovets.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code Table} annotation is used to specify the table name
 * for an entity in the database. This annotation can be applied to
 * classes to indicate that instances of the class are stored in a
 * specific database table.
 *
 * <p>It is typically used in the context of an ORM (Object Relational Mapping)
 * framework to associate a class with a corresponding database table. When
 * used, the ORM framework can automatically map instances of the class to rows
 * in the specified table, facilitating database operations like create, read,
 * update, and delete.</p>
 *
 * <p>This annotation must be placed on the class declaration. It has a single
 * parameter, {@code name}, which specifies the name of the table in the database.
 * If the {@code name} parameter is not provided, it is up to the ORM framework
 * to determine the appropriate table name based on its own naming conventions or
 * configuration.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Table(name = "users")
 * public class User {
 *     // class body
 * }
 * }</pre>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

    /**
     * Specifies the name of the table in the database to which the entity is mapped.
     *
     * <p>If not specified, the name of the table will be determined based on the
     * class name or through the ORM framework's configuration and naming conventions.</p>
     *
     * @return the name of the table
     */
    String name() default "";
}
