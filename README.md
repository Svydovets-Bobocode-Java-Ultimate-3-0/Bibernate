# Bibernate

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

4. **You can download the Bibernate demo project to simplify setup:** [DEMO Project](https://github.com/JJJazl/Bibernate-demo/)
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
- **[Session](#session)**
- **[Column](#column)**
- **[Id](#id)**
- **[JoinColumn](#joinColumn)**
- **[ManyToOne](#manyToOne)**
- **[OneToMany](#oneToMany)**
- **[OneToOne](#oneToOne)**
- **[Table](#table)**

