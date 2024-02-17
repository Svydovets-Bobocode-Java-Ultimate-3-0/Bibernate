package org.svydovets.queryLanguage;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.svydovets.baseEntity.Note;
import org.svydovets.baseEntity.Person;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QueryManagerTest {

    @Test
    public void shouldReturnSelectNativeQueryById() {
        final String jpQuery = "select p from Person p where id = :id";
        final String expectedQuery = "select * from persons p where id = ?";
        QueryManager<Person> queryManager = QueryManager.of(jpQuery, Person.class);
        queryManager.setParameters("id", 1L);

        assertThat(expectedQuery).isEqualTo(queryManager.toSqlString());

        Object[] parameters = queryManager.getParameters();

        assertThat(1).isEqualTo(parameters.length);
        assertThat(1L).isEqualTo(parameters[0]);
    }

    @Test
    public void shouldReturnSelectNativeQueryByColumns() {
        final String jpQuery = "select p from Person p where p.firstName = :firstName and p.lastName = :lastName and p.age = :age";
        final String expectedQuery = "select * from persons p where p.first_name = ? and p.last_name = ? and p.age = ?";
        QueryManager<Person> queryManager = QueryManager.of(jpQuery, Person.class);
        queryManager.setParameters("lastName", "lastName");
        queryManager.setParameters("age", 20);
        queryManager.setParameters("firstName", "firstName");

        String sqlString = queryManager.toSqlString();
        assertThat(expectedQuery).isEqualTo(sqlString);
    }

    @Test
    public void shouldReturnArrayOfParametersInSequenceByQuery() {
        final String jpQuery = "select p from Person p where p.firstName = :firstName and p.lastName = :lastName and p.age = :age";
        QueryManager<Person> queryManager = QueryManager.of(jpQuery, Person.class);
        queryManager.setParameters("lastName", "lastName");
        queryManager.setParameters("age", 20);
        queryManager.setParameters("firstName", "firstName");
        Object[] parameters = queryManager.getParameters();

        assertThat(3).isEqualTo(parameters.length);
        assertThat("firstName").isEqualTo(parameters[0]);
        assertThat("lastName").isEqualTo(parameters[1]);
        assertThat(20).isEqualTo(parameters[2]);
    }

    @Test
    public void shouldReturnUpdateNativeQueryByColumns() {
        final String jpQuery = "update Person p set "
                + "p.firstName = :newFirstName, "
                + "p.lastName = :newLastName, "
                + "p.age = :newAge, "
                + "p.male = :newMale "
                + "where p.firstName = :firstName and p.lastName = :lastName and p.age = :age";
        final String expectedQuery = "update persons p set "
                + "p.first_name = ?, "
                + "p.last_name = ?, "
                + "p.age = ?, "
                + "p.male = ? "
                + "where p.first_name = ? and p.last_name = ? and p.age = ?";

        QueryManager<Person> queryManager = QueryManager.of(jpQuery, Person.class);
        queryManager.setParameters("newFirstName", "newFirstName");
        queryManager.setParameters("newAge", 25);
        queryManager.setParameters("newLastName", "newLastName");
        queryManager.setParameters("newMale", "M");
        queryManager.setParameters("age", 20);
        queryManager.setParameters("firstName", "firstName");
        queryManager.setParameters("lastName", "lastName");

        String sqlString = queryManager.toSqlString();
        assertThat(expectedQuery).isEqualTo(sqlString);
    }

    @Test
    public void shouldReturnUpdateArrayOfParametersInSequenceByQuery() {
        final String jpQuery = "update Person p set"
                + " p.firstName = :newFirstName, "
                + " p.lastName = :newLastName, "
                + " p.age = :newAge, "
                + " p.male = :newMale "
                + "where p.firstName = :firstName and p.lastName = :lastName and p.age = :age";
        QueryManager<Person> queryManager = QueryManager.of(jpQuery, Person.class);
        queryManager.setParameters("newFirstName", "newFirstName");
        queryManager.setParameters("firstName", "firstName");
        queryManager.setParameters("age", 20);
        queryManager.setParameters("newAge", 25);
        queryManager.setParameters("newMale", "M");
        queryManager.setParameters("newLastName", "newLastName");
        queryManager.setParameters("lastName", "lastName");

        Object[] parameters = queryManager.getParameters();
        assertThat(7).isEqualTo(parameters.length);
        assertThat("newFirstName").isEqualTo(parameters[0]);
        assertThat("newLastName").isEqualTo(parameters[1]);
        assertThat(25).isEqualTo(parameters[2]);
        assertThat("M").isEqualTo(parameters[3]);
        assertThat("firstName").isEqualTo(parameters[4]);
        assertThat("lastName").isEqualTo(parameters[5]);
        assertThat(20).isEqualTo(parameters[6]);
    }

    @Test
    public void shouldReturnDeleteNativeQueryByColumns() {
        final String jpQuery = "delete from Person p "
                + "where p.firstName = :firstName and p.lastName = :lastName and p.age = :age";
        final String expectedQuery = "delete from persons p "
                + "where p.first_name = ? and p.last_name = ? and p.age = ?";

        QueryManager<Person> queryManager = QueryManager.of(jpQuery, Person.class);
        queryManager.setParameters("age", 20);
        queryManager.setParameters("firstName", "firstName");
        queryManager.setParameters("lastName", "lastName");

        String sqlString = queryManager.toSqlString();
        assertThat(expectedQuery).isEqualTo(sqlString);
    }

    @Test
    public void shouldReturnDeleteArrayOfParametersInSequenceByQuery() {
        final String jpQuery = "delete from Person p "
                + "where p.firstName = :firstName and p.lastName = :lastName and p.age = :age";
        QueryManager<Person> queryManager = QueryManager.of(jpQuery, Person.class);
        queryManager.setParameters("firstName", "firstName");
        queryManager.setParameters("age", 20);
        queryManager.setParameters("lastName", "lastName");

        Object[] parameters = queryManager.getParameters();
        assertThat(3).isEqualTo(parameters.length);
        assertThat("firstName").isEqualTo(parameters[0]);
        assertThat("lastName").isEqualTo(parameters[1]);
        assertThat(20).isEqualTo(parameters[2]);
    }

    @Test
    public void shouldReturnInsertNativeQueryByColumns() {
        final String jpQuery = "insert into Person p "
                + "(p.firstName, p.lastName, p.age, p.male) "
                + "values "
                + "(:firstName, :lastName, :age, :male)";
        final String expectedQuery = "insert into persons p (p.first_name, p.last_name, p.age, p.male) values (?, ?, ?, ?)";

        QueryManager<Person> queryManager = QueryManager.of(jpQuery, Person.class);
        queryManager.setParameters("male", "M");
        queryManager.setParameters("age", 20);
        queryManager.setParameters("lastName", "lastName");
        queryManager.setParameters("firstName", "firstName");

        String sqlString = queryManager.toSqlString();
        assertThat(expectedQuery).isEqualTo(sqlString);
    }

    @Test
    public void shouldReturnInsertArrayOfParametersInSequenceByQuery() {
        final String jpQuery = "insert into Person p "
                + "(p.firstName, p.lastName, p.age, p.male) "
                + "values "
                + "(:firstName, :lastName, :age, :male)";

        QueryManager<Person> queryManager = QueryManager.of(jpQuery, Person.class);
        queryManager.setParameters("male", "M");
        queryManager.setParameters("age", 20);
        queryManager.setParameters("lastName", "lastName");
        queryManager.setParameters("firstName", "firstName");

        Object[] parameters = queryManager.getParameters();
        assertThat(4).isEqualTo(parameters.length);
        assertThat("firstName").isEqualTo(parameters[0]);
        assertThat("lastName").isEqualTo(parameters[1]);
        assertThat(20).isEqualTo(parameters[2]);
        assertThat("M").isEqualTo(parameters[3]);
    }

    @Test
    public void shouldReturnSelectNativeQueryWithLeftJoinByColumns() {
        final String jpQuery = "select n from Note n where n.Person.id = :personId";
        QueryManager<Note> queryManager = QueryManager.of(jpQuery, Note.class);
        queryManager.setParameters("personId", 1L);

        final String expectedQuery = "select * from notes n "
                + "left join persons join_0 on join_0.id = n.person_id "
                + "where n.person_id = ?";
        String sqlString = queryManager.toSqlString();
        assertThat(expectedQuery).isEqualToIgnoringCase(sqlString);
    }

    @Test
    public void shouldReturnSelectNativeQueryWithLeftJoinByColumnsWithoutAlias() {
        final String jpQuery = "select * from Note where Person.id = :personId";
        QueryManager<Note> queryManager = QueryManager.of(jpQuery, Note.class);
        queryManager.setParameters("personId", 1L);

        final String expectedQuery = "select * from notes "
                + "left join persons join_0 on join_0.id = person_id "
                + "where person_id = ?";
        String sqlString = queryManager.toSqlString();
        assertThat(expectedQuery).isEqualToIgnoringCase(sqlString);
    }

    @Test
    public void shouldReturnEntityTypeFromQueryManager() {
        QueryManager<Note> queryManager = new QueryManager<>(Note.class);
        assertThat(Note.class).isEqualTo(queryManager.getEntityType());
    }
}
