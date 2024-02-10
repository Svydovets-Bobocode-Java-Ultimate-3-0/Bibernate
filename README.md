# Bibernate

<img src="./img/bibernate-logo.png" alt="bibernate">

# Requirements

- **Java**: Version 17.
- **Maven**: Make sure Maven is installed on your system. You can download it
  from [here](https://maven.apache.org/download.cgi).

# Getting Started

Follow these steps to get started with our project:

To start working on our project in the demo app, follow these steps:

1. **Clone the Repository Bibernate app:**
   ```bash
   git clone https://github.com/Svydovets-Bobocode-Java-Ultimate-3-0/Bibernate.git
   cd Bibernate
    ```

2. **Build the Project:**
   ```bash
    mvn clean install
    ```
3. **To test Bring Application, create new Maven Project and add dependency:**
    ```xml
           <dependency>
            <groupId>org.svydovets</groupId>
            <artifactId>Bibernate</artifactId>
            <version>1.0</version>
        </dependency>
   ```

4. **You can download the Bibernate demo project to simplify setup:
   ** [DEMO Project](https://github.com/JJJazl/Bibernate-demo/)
5. **Run the application:**
   ```java
   public class DemoApp {

    public static void main(String[] args) {
        SessionFactory sessionFactory = new SessionFactory();
        Session session = sessionFactory.createSession();

        System.out.println("Select note by id=2");
        Note note = session.findById(Note.class, 2L);
        System.out.println(note);

        System.out.println("Select person by id=1");
        Person person = session.findById(Person.class, 1L);
        System.out.println(person);

        System.out.println("Select all notes by person id=1");
        person.getNotes().forEach(System.out::println);

        session.close();
    }
   }
   ```
6. **Enjoy :)**

# Functionality

- **[SessionFactory](#sessionFactory)**
    - Initialization
    - Creating a Session
- **[Session](#session)**
    - Transaction Management
    - Persisting Entities
    - Retrieving Entities
        - findById
        - findAllBy
        - findBy
        - nativeQueryBy
        - nativeQueryAllBy
    - Merging and Removing Entities
    - Closing the Session
    - Flushing the Session
- **[Column](#column)**
- **[Id](#id)**
- **[Table](#table)**
- **[JoinColumn](#joinColumn)**
- **[ManyToOne](#manyToOne)**
- **[OneToMany](#oneToMany)**
- **[OneToOne](#oneToOne)**
- **[TransactionManager](#transactionManager)**
    - Transaction functionality
        - Starting a Transaction
        - Committing a Transaction
        - Rolling Back a Transaction
        - Checking Transaction State
    - Exception handling
    - Best Practices
- **[Database configuration](#database-configuration)**

#### SessionFactory

Factory class for creating sessions for database operations. This class handles the initialization of the necessary
components for database connectivity,
including loading database properties and setting up a data source.

1. Initialization: Create an instance of SessionFactory using either the default database properties loaded from
   application.properties or by providing a custom Properties object with the required database connection details.
    ```java
    SessionFactory sessionFactory = new SessionFactory(); // Using default properties

    Properties customProperties = new Properties("jdbc:db_url", "db_user", "db_password");
    SessionFactory sessionFactory = new SessionFactory(customProperties); // Using custom properties
    ```

2. Creating a Session: Use the createSession() method to obtain a new Session instance for performing database
   operations.
    ```java
    Session session = sessionFactory.createSession();
    ```

### Session

Manages a session for interacting with the database, providing functionality for persisting, merging, and removing
entities. It acts as a buffer between the application and the database, caching entities and deferring database
operations to optimize performance and manage transactions.

1. Transaction Management

   To manage transactions within a session:
    ```java
        Session session = sessionFactory.createSession();     
        TransactionManager transactionManager = session.transactionManager();
        transactionManager.begin();
        // Perform database operations
        transactionManager.commit();
    ```

2. Persisting Entities

   To persist an entity immediately or queue it for batch persistence:

    ```java
        MyEntity entity = new MyEntity();
        session.persist(entity);
   ```

3. Retrieving Entities

   #### findById

   Retrieve an entity by its identifier:
    ```java
        MyEntity foundEntity = session.findById(MyEntity.class, entityId);
    ```

   #### findAllBy

   Perform a more complex retrieval using a field and value:
    ```java
    List<MyEntity> entities = session.findAllBy(MyEntity.class, fieldName, fieldValue);
    ```

   #### findBy

   Retrieves an entity based on a specified field and its value, utilizing the cache for efficiency.
   ```java
      MyEntity entity = session.findBy(MyEntity.class, someField, fieldValue);
   ```

   #### nativeQueryBy

   Executes a native SQL query to retrieve a single entity of a specified class that matches the query criteria.
   ```java
      MyEntity entity = session.nativeQueryBy("SELECT * FROM MyTable WHERE name = ?", MyEntity.class, new Object[]{"Name"});
   ```

   #### nativeQueryAllBy

   Executes a native SQL query to retrieve a list of entities of a specified class that match the query criteria.
   ```java
      List<MyEntity> entities = session.nativeQueryAllBy("SELECT * FROM MyTable", MyEntity.class, new Object[]{});
   ```

4. Merging and Removing Entities

   To merge the state of an entity with the one in the database:

    ```java
    MyEntity mergedEntity = session.merge(entity);
    ```

   To remove an entity from the database:

    ```java
        session.remove(entity);
    ```

5. Closing the Session

   It's crucial to close the session when done to release resources:

    ```java
        session.close();
    ```

6. Flushing the Session

   #### flush

   This method is crucial for applying all pending changes to the database. It triggers the execution of all actions
   queued during the session, such as persisting, merging, or removing entities. This operation ensures that all
   modifications made to entities within the session are synchronized with the database according to the current
   transaction's boundaries. It is implicitly called when committing a transaction but can also be explicitly invoked to
   force the application of changes at a specific point within a transaction.

   ```java
      // Example usage of flush within a session
      TransactionManager tm = session.transactionManager();
      tm.begin();
      
      // Perform some operations on entities
      session.persist(newEntity);
      session.merge(existingEntity);
      session.remove(toRemoveEntity);
      
      // Explicitly flush changes to the database
      session.flush();
      
      // Continue with other operations or commit the transaction
      tm.commit();
   ```

### Column

The Column annotation is used to specify the mapping between a class field and a column in a database table.
When a class is annotated as an Entity, its fields can be mapped to columns of a database table using this annotation.
This allows ORM frameworks to understand how to map the fields of an object to the columns in the database table.

This annotation should be applied to the field level. The name attribute is used to specify the exact name of the column
in the database table that the field should be mapped to. If the name attribute is not specified, the field name is
typically
used as the column name by default, following the ORM framework's naming conventions.

```java

import org.svydovets.annotation.Entity;

@Entity
public class User {

    @Id
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name")
    private String name;

    // Other fields, constructors, getters, and setters
}
```

### Id

The Id annotation is used to designate a field as the primary key of the entity's corresponding table in the database.
This annotation indicates
that the field uniquely identifies an instance of an entity. In the context of ORM frameworks, the Id annotation is
crucial for defining how an entity is uniquely recognized within the persistence context and the database.

Applying this annotation to a field within a class annotated as an Entity specifies that field as the unique
identifier (primary key) for instances of the class.
This is essential for ORM operations that involve entity retrieval, updates, and deletions, where the primary key is
used to precisely locate records in the database.

It is a common practice to use this annotation on fields that represent the entity's primary key in the database. The
field type can be any primitive data type, a String, or a wrapper of a primitive data type.
The choice of the field as an ID should align with the database schema's primary key constraints.

```java
import org.svydovets.annotation.Entity;

@Entity
public class User {

    @Id
    private Long id;

    // Other fields, constructors, getters, and setters
}
```

### Table

The Table annotation is used to specify the table name for an entity in the database. This annotation can be applied to
classes to indicate that instances of the class are stored in a specific database table.

It is typically used in the context of an ORM (Object Relational Mapping) framework to associate a class with a
corresponding database table. When used, the ORM framework can automatically map instances of
the class to rows in the specified table, facilitating database operations like create, read, update, and delete.

This annotation must be placed on the class declaration. It has a single parameter, name, which specifies the name of
the table in the database. If the name parameter is not provided, it is up to the ORM framework to determine the
appropriate table name based on its own naming conventions or configuration.

```java

@Table(name = "users")
public class User {
    // class body
}
```

### JoinColumn

The JoinColumn annotation is used to specify a foreign key column in the entity's corresponding table in the database.
This annotation is primarily used in association mappings to establish the relationship between two entities. When
applied to a field, it indicates that the field is a foreign key that references a column in another table.

Applying this annotation to a field within an entity class allows the ORM framework to map the association between the
entity and another entity correctly. The name attribute specifies the name of the column in the database table that is
used as the foreign key. If the name attribute is not specified, the ORM framework usually defaults to the field name or
applies a naming strategy defined within the framework.

This annotation is crucial for defining One-To-One, Many-To-One, and One-To-Many relationships in entity models,
enabling the ORM framework to navigate and manage associations between entities efficiently.

```java

@Entity
public class Order {

    @Id
    private Long id;

    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Other fields, constructors, getters, and setters
}
```

### ManyToOne

The ManyToOne annotation is used to establish a many-to-one relationship between two entities. In a many-to-one
relationship, multiple entities (or instances of the class where this annotation is applied) are associated with a
single instance of the referenced entity. This type of relationship is commonly used to model the database relationships
where multiple rows in a table are linked to a single row in another table.

This annotation should be placed on the field that references the entity that many instances of this class are related
to. It is often accompanied by the JoinColumn annotation to specify the foreign key column used for the association.

Applying the ManyToOne annotation to a field tells the ORM framework that the field represents the "many" side of a
many-to-one relationship. This informs the framework's strategy for fetching, caching, and managing the lifecycle of the
associated entities.

```java

@Entity
public class Employee {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    // Other fields, constructors, getters, and setters
}
```

### OneToMany

The OneToMany annotation is used to denote a one-to-many relationship between two entity classes. In such a
relationship, a single instance of the entity where this annotation is applied is associated with multiple instances of
the referenced entity. This relationship is typical in database schema designs where a single row in one table can be
linked to multiple rows in another table.

This annotation should be placed on a field that represents a collection of instances of the related entity. The
mappedBy attribute is crucial as it specifies the field in the target entity that owns the relationship, thereby
establishing the direction of the relationship in bidirectional associations.

Using the OneToMany annotation informs the ORM framework of the relationship's nature and how to manage the collection
of related entities, including strategies for fetching, updating, and deleting these entities in relation to their
parent entity.

```java

@Entity
public class Department {

    @Id
    private Long id;

    @OneToMany(mappedBy = "department")
    private Set<Employee> employees = new HashSet<>();

    // Other fields, constructors, getters, and setters
}
```

### OneToOne

The OneToOne annotation is utilized to establish a one-to-one relationship between two entity classes. This type of
relationship indicates that an instance of the entity where this annotation is applied is associated with precisely one
instance of the referenced entity, and vice versa. This annotation is particularly useful for modeling database
relationships where two tables are linked by a unique, bidirectional connection.

This annotation should be placed on a field that references another entity with which there is a one-to-one association.
The mappedBy attribute is of significant importance for bidirectional relationships as it specifies the field in the
target entity that owns the relationship. This establishes the directionality of the relationship and clarifies which
side is responsible for managing it.

Applying the OneToOne annotation helps ORM frameworks to correctly manage the lifecycle of the associated entities,
ensuring that operations such as fetching, updating, and deleting are properly synchronized between the two entities
involved in the one-to-one relationship.

```java

@Entity
public class User {

    @Id
    private Long id;

    @OneToOne(mappedBy = "user")
    private UserProfile profile;

    // Other fields, constructors, getters, and setters
}

@Entity
public class UserProfile {

    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Other fields, constructors, getters, and setters
}
```

### TransactionManager

Transaction manager is designed to manage database transactions within the application. It controls the lifecycle of
transactions, including their start, commit, and rollback processes. By managing transactions at this level, it ensures
data integrity and consistency across database operations.

#### Transaction functionality

Transaction Control: Offers explicit methods to begin, commit, and roll back transactions, providing fine-grained
control over database operations.

Transaction State Management: Tracks the active state of a transaction to prevent illegal operation states.

Integration with Connection Handling: Works closely with ConnectionHandler to manage database connection states,
including toggling auto-commit mode based on transaction boundaries.

1. Starting a Transaction

   To start a new transaction, ensuring no transaction is already active:

   ```java
        Session session = sessionFactory.createSession();     
        TransactionManager transactionManager = session.transactionManager();
        transactionManager.begin();
   ```

2. Committing a Transaction

   Once the database operations are successfully completed within the transaction scope, commit the transaction:

   ```java
        Session session = sessionFactory.createSession();     
        TransactionManager transactionManager = session.transactionManager();
        transactionManager.begin();
        //some actions
        transactionManager.commit();
   ```

3. Rolling Back a Transaction

   In case of an error or when the business logic dictates, roll back any changes made during the transaction:

   ```java
        Session session = sessionFactory.createSession();     
        TransactionManager transactionManager = session.transactionManager();
       try {
           transactionManager.begin();
           //some actions
           transactionManager.commit();
       } catch (Exception exception) {
           transactionManager.callback();
       }
   ```
4. Checking Transaction State

   To verify if a transaction is currently active:

   ```java
      boolean isActive = transactionManager.isActive();
   ```

#### Exception handling

`TransactionException`: This custom exception is thrown to indicate issues related to transaction management, such as
attempting to start a new transaction when one is already active, or trying to commit or roll back when no transaction
is active.

#### Best Practices

Always ensure that transactions are properly committed or rolled back to maintain data integrity.

Use try-catch-finally blocks or try-with-resources statements to handle transactions, ensuring that resources are
properly released and transactions are either committed or rolled back in case of exceptions.

### Database configuration

To configure database need to add the `application.properties` file with the following lines:

   ```properties
      db.url=url_cred
db.user=user_cred
db.password=password_cred
   ```

`URL` - responsible for the database url
`user` - a user for your database
`password` - needed password to connect to database 