package org.svydovets.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code Entity} annotation is used to mark a class as an entity in a
 * persistence context. An entity represents a table stored in a database and
 * each instance of an entity corresponds to a row in that table. This annotation
 * signifies that the class is an integral part of the object-relational mapping
 * (ORM) framework and should be treated as such for CRUD (Create, Read, Update,
 * Delete) operations.
 *
 * <p>This annotation should be placed on the class level. By marking a class as
 * an entity, it becomes manageable by the ORM framework, allowing the framework
 * to automatically handle database operations for objects of this class. It is
 * commonly used in applications that require database interactions and is a key
 * component of Java Persistence API (JPA) and similar technologies.</p>
 *
 * <p>Note: Classes annotated with {@code Entity} typically also use other annotations
 * to define mappings between the class's fields and the columns of the database table,
 * relationships with other entities, and queries. However, {@code Entity} itself does
 * not require any parameters and acts as a marker to identify relevant classes to the
 * ORM framework.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Entity
 * public class User {
 *     // fields, constructors, methods
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
}
