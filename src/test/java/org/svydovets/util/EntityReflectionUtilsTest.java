package org.svydovets.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.svydovets.annotation.Id;
import org.svydovets.baseEntity.Person1;
import org.svydovets.baseEntity.NotManagedPerson;
import org.svydovets.baseEntity.PersonWithoutId;
import org.svydovets.exception.AnnotationMappingException;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityReflectionUtilsTest {

    @Test
    public void shouldReturnIdFieldWhenIdIsSpecified() {
        Field idField = Arrays.stream(Person1.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny().orElseThrow();

        assertThat(idField).isEqualTo(EntityReflectionUtils.getIdField(Person1.class));
    }

    @Test
    public void shouldThrowAnnotationMappingExceptionWhenClassDoesNotHaveAnIdentifier() {
        assertThrows(
                AnnotationMappingException.class,
                () -> EntityReflectionUtils.getIdField(PersonWithoutId.class),
                String.format("Identifier is not specified for type: %s (Each entity must have field marked as '@Id')", PersonWithoutId.class.getName())
        );
    }
    @Test
    public void shouldThrowAnnotationMappingExceptionWhenClassIsNotMarkedAsEntity() {
        assertThrows(
                AnnotationMappingException.class,
                () -> EntityReflectionUtils.getIdField(NotManagedPerson.class),
                String.format("Not a managed type. Class must be marked as'@Entity': %s", NotManagedPerson.class)
        );
    }

    @Test
    public void shouldReturnEntityFieldsSortedByName() throws Exception {
        Class<Person1> personClass = Person1.class;
        Field[] expected = new Field[] {
                personClass.getDeclaredField("age"),
                personClass.getDeclaredField("firstName"),
                personClass.getDeclaredField("id"),
                personClass.getDeclaredField("lastName"),
                personClass.getDeclaredField("male"),
        };
        Assertions.assertThat(EntityReflectionUtils.getEntityFieldsSortedByName(personClass))
                .isEqualTo(expected);
    }

    @Test
    public void shouldReturnEntityFieldsWithoutIdSortedByName() throws Exception {
        Class<Person1> personClass = Person1.class;
        Field[] expected = new Field[] {
                personClass.getDeclaredField("age"),
                personClass.getDeclaredField("firstName"),
                personClass.getDeclaredField("lastName"),
                personClass.getDeclaredField("male"),
        };
        Assertions.assertThat(EntityReflectionUtils.getUpdatableFields(personClass))
                .isEqualTo(expected);
    }
}
