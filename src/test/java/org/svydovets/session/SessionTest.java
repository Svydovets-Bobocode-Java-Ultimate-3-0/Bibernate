package org.svydovets.session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mockito;
import org.svydovets.baseEntity.PersonSessionTest;
import org.svydovets.connectionPool.datasource.ConnectionHandler;
import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.util.EntityReflectionUtils;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

class SessionTest {

    private Session sessionTestable;
    private GenericJdbcDAO mockJdbcDAO;
    private final Map<EntityKey<?>, Object> entitiesCacheExpected = new HashMap<>();
    private final Map<EntityKey<?>, Object[]> entitiesSnapshotsExpected = new HashMap<>();
    private final AtomicInteger personIdSequence = new AtomicInteger(0);

    @BeforeEach
    public void initData() throws SQLException {
        mockJdbcDAO = Mockito.mock(GenericJdbcDAO.class);
        ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        sessionTestable = new Session(mockJdbcDAO, connectionHandler);
        Mockito.when(connectionHandler.getConnection()).thenReturn(Mockito.mock(Connection.class));
    }

    @AfterEach
    public void cleanData() {
        entitiesCacheExpected.clear();
        entitiesSnapshotsExpected.clear();
    }

    @Test
    void shouldReturnPersonById_positive() throws Exception {
        PersonSessionTest person1 = generateRandomPerson();
        PersonSessionTest person2 = generateRandomPerson();

        addPersonToTastableScope(person1);
        addPersonToTastableScope(person2);

        Mockito.when(mockJdbcDAO.loadFromDB(any())).thenReturn(person1);
        PersonSessionTest personById = sessionTestable.findById(PersonSessionTest.class, 1);
        Assertions.assertEquals(person1, personById);
    }


    @Test
    void shouldCloseSession() throws Exception {
        addPersonToTastableScope(generateRandomPerson());

        doNothing().when(mockJdbcDAO).update(any());

        Assertions.assertNotEquals(0, entitiesCacheExpected.size());
        Assertions.assertNotEquals(0, entitiesSnapshotsExpected.size());

        sessionTestable.close();

        Assertions.assertEquals(0, entitiesCacheExpected.size());
        Assertions.assertEquals(0, entitiesSnapshotsExpected.size());
        Mockito.verify(mockJdbcDAO, times(0)).update(any());

        System.out.println();
    }

    @Test
    void shouldCloseSessionWithNullInEntityField() throws Exception {
        PersonSessionTest person = generateRandomPerson();
        person.setLastName(null);
        addPersonToTastableScope(person);

        doNothing().when(mockJdbcDAO).update(any());

        Assertions.assertNotEquals(0, entitiesCacheExpected.size());
        Assertions.assertNotEquals(0, entitiesSnapshotsExpected.size());

        sessionTestable.close();

        Assertions.assertEquals(0, entitiesCacheExpected.size());
        Assertions.assertEquals(0, entitiesSnapshotsExpected.size());

        Mockito.verify(mockJdbcDAO, times(0)).update(any());
    }

    @Test
    void shouldCloseSessionWithUpdateRecord() throws Exception {
        PersonSessionTest person = generateRandomPerson();
        addPersonToTastableScope(person);


        PersonSessionTest updatedPerson = generateRandomPerson();
        updatedPerson.setId(person.getId());

        EntityKey<PersonSessionTest> entityKeyByPerson = getEntityKeyByPerson(updatedPerson);
        addEntityToSnapshots(entityKeyByPerson, updatedPerson);

        doNothing().when(mockJdbcDAO).update(any());

        Assertions.assertNotEquals(0, entitiesCacheExpected.size());
        Assertions.assertNotEquals(0, entitiesSnapshotsExpected.size());

        sessionTestable.close();

        Assertions.assertEquals(0, entitiesCacheExpected.size());
        Assertions.assertEquals(0, entitiesSnapshotsExpected.size());

        Mockito.verify(mockJdbcDAO, times(1)).update(any());
    }

    @Test
    void shouldCloseSessionWithUpdateIfNullFieldRecord() throws Exception {
        PersonSessionTest person = generateRandomPerson();
        person.setLastName(null);
        addPersonToTastableScope(person);

        PersonSessionTest updatedPerson = new PersonSessionTest();
        updatedPerson.setId(person.getId());
        updatedPerson.setFirstName(person.getFirstName());
        updatedPerson.setLastName(UUID.randomUUID().toString());
        updatedPerson.setAge(person.getAge());
        updatedPerson.setMale(person.getMale());

        EntityKey<PersonSessionTest> entityKeyByPerson = getEntityKeyByPerson(updatedPerson);
        addEntityToSnapshots(entityKeyByPerson, updatedPerson);

        doNothing().when(mockJdbcDAO).update(any());

        Assertions.assertNotEquals(0, entitiesCacheExpected.size());
        Assertions.assertNotEquals(0, entitiesSnapshotsExpected.size());

        sessionTestable.close();

        Assertions.assertEquals(0, entitiesCacheExpected.size());
        Assertions.assertEquals(0, entitiesSnapshotsExpected.size());

        Mockito.verify(mockJdbcDAO, times(1)).update(any());
    }

    @Test
    void shouldCloseSessionWithUpdateIfTwoNullsFieldRecord() throws Exception {
        PersonSessionTest person = generateRandomPerson();
        person.setLastName(null);
        addPersonToTastableScope(person);

        PersonSessionTest updatedPerson = generateRandomPerson();
        updatedPerson.setId(person.getId());
        updatedPerson.setLastName(null);

        EntityKey<PersonSessionTest> entityKeyByPerson = getEntityKeyByPerson(updatedPerson);
        addEntityToSnapshots(entityKeyByPerson, updatedPerson);

        doNothing().when(mockJdbcDAO).update(any());

        Assertions.assertNotEquals(0, entitiesCacheExpected.size());
        Assertions.assertNotEquals(0, entitiesSnapshotsExpected.size());

        sessionTestable.close();

        Assertions.assertEquals(0, entitiesCacheExpected.size());
        Assertions.assertEquals(0, entitiesSnapshotsExpected.size());

        Mockito.verify(mockJdbcDAO, times(1)).update(any());
    }


    //utils methods
    private PersonSessionTest generateRandomPerson() {
        PersonSessionTest person = new PersonSessionTest();
        person.setId(personIdSequence.incrementAndGet());
        person.setFirstName(RandomStringUtils.randomAlphabetic(3, 10));
        person.setLastName(RandomStringUtils.randomAlphabetic(3, 10));
        person.setAge(new Random().nextInt(0, 100));
        person.setMale(RandomStringUtils.randomAlphabetic(1));
        return person;
    }

    private void addPersonToTastableScope(PersonSessionTest person) throws IllegalAccessException {
        addPersonToEntityExpected(person);
        addEntityToSnapshots(getEntityKeyByPerson(person), person);
    }

    private void addPersonToEntityExpected(PersonSessionTest person) throws IllegalAccessException {
        EntityKey<PersonSessionTest> entKeyId = getEntityKeyByPerson(person);
        entitiesCacheExpected.put(entKeyId, person);
        Field entitiesCacheField = ReflectionUtils.findFields(Session.class,
                field -> field.getName().equals("entitiesCache"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).get(0);
        entitiesCacheField.setAccessible(true);
        entitiesCacheField.set(sessionTestable, entitiesCacheExpected);
    }

    private static EntityKey<PersonSessionTest> getEntityKeyByPerson(PersonSessionTest person) {
        return new EntityKey<>(PersonSessionTest.class, person.getId());
    }

    private void addEntityToSnapshots(EntityKey<?> entityKey, Object entity) throws IllegalAccessException {
        Field[] fields = EntityReflectionUtils.getEntityFieldsSortedByName(entityKey.entityType());
        Object[] snapshots = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            snapshots[i] = getFieldValue(entity, fields[i]);
        }

        entitiesSnapshotsExpected.put(entityKey, snapshots);

        Field entitiesSnapshotsField = ReflectionUtils.findFields(Session.class,
                field -> field.getName().equals("entitiesSnapshots"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).get(0);
        entitiesSnapshotsField.setAccessible(true);
        entitiesSnapshotsField.set(sessionTestable, entitiesSnapshotsExpected);
    }

    private Object getFieldValue(Object entity, Field fields) throws IllegalAccessException {
        fields.setAccessible(true);
        return fields.get(entity);
    }

}