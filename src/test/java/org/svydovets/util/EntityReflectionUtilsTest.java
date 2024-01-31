package org.svydovets.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.ManyToOne;
import org.svydovets.annotation.OneToMany;
import org.svydovets.annotation.OneToOne;
import org.svydovets.baseEntity.Address;
import org.svydovets.baseEntity.Address1;
import org.svydovets.baseEntity.Note;
import org.svydovets.baseEntity.Note1;
import org.svydovets.baseEntity.Person;
import org.svydovets.baseEntity.Person1;
import org.svydovets.baseEntity.NotManagedPerson;
import org.svydovets.baseEntity.PersonWithoutId;
import org.svydovets.exception.AnnotationMappingException;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void shouldReturnEntityFieldWithOneToOneAnnotation() {
        var anyField = Arrays.stream(Address.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToOne.class))
                .findAny()
                .orElseThrow();

        assertTrue(EntityReflectionUtils.isEntityField(anyField));
    }

    @Test
    public void shouldReturnEntityFieldWithManyToOneAnnotation() {
        var anyField = Arrays.stream(Note.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                .findAny()
                .orElseThrow();

        assertTrue(EntityReflectionUtils.isEntityField(anyField));
    }

    @Test
    public void shouldReturnThrowForEntityFieldWithManyToOneAnnotationAndExcludeJoinColumnAnnotation() {
        var anyField = Arrays.stream(Note1.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                .findAny()
                .orElseThrow();
        String message = String.format("The entity field [%s] that is marked with the @OneToOne "
                + "or @ManyToOne annotation is missing the required @JoinColumn annotation", anyField.getName());

        assertThatExceptionOfType(AnnotationMappingException.class)
                .isThrownBy(() -> EntityReflectionUtils.isEntityField(anyField))
                .withMessage(message);
    }

    @Test
    public void shouldReturnThrowForEntityFieldWithOneToOneAnnotationAndExcludeJoinColumnAnnotation() {
        var anyField = Arrays.stream(Address1.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToOne.class))
                .findAny()
                .orElseThrow();
        String message = String.format("The entity field [%s] that is marked with the @OneToOne "
                + "or @ManyToOne annotation is missing the required @JoinColumn annotation", anyField.getName());

        assertThatExceptionOfType(AnnotationMappingException.class)
                .isThrownBy(() -> EntityReflectionUtils.isEntityField(anyField))
                .withMessage(message);
    }

    @Test
    public void shouldReturnPositiveValueWithOneToManyAnnotation() {
        var anyField = Arrays.stream(Person.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .findAny()
                .orElseThrow();
        assertTrue(EntityReflectionUtils.isEntityCollectionField(anyField));
    }

    @Test
    public void shouldReturnNegativeValueWithOneToManyAnnotation() {
        var anyField = Arrays.stream(Note.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                .findAny()
                .orElseThrow();
        assertFalse(EntityReflectionUtils.isEntityCollectionField(anyField));
    }

    @Test
    public void shouldReturnJoinCollectionEntityTypeByField() {
        var anyField = Arrays.stream(Person.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .findAny()
                .orElseThrow();
        assertThat(Note.class).isEqualTo(EntityReflectionUtils.getJoinCollectionEntityType(anyField));
    }

    @Test
    public void shouldReturnJoinClazzFieldByParentClassAndJoinClass() {
        var anyField = Arrays.stream(Note.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                .findAny()
                .orElseThrow();

        assertThat(anyField).isEqualTo(EntityReflectionUtils.getJoinClazzField(Person.class, Note.class));
    }

    @Test
    public void shouldReturnThrowAnnotationMappingExceptionForJoinClazzFieldByParentClassAndJoinClass() {
        String message = String.format("Cannon find related field [%s] in $s", Person.class, Note.class);

        assertThatExceptionOfType(AnnotationMappingException.class)
                .isThrownBy(() -> EntityReflectionUtils.getJoinClazzField(Note.class, Person.class))
                .withMessage(message);
    }
}
