package org.svydovets.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.svydovets.annotation.Column;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.JoinColumn;
import org.svydovets.annotation.ManyToOne;
import org.svydovets.annotation.OneToMany;
import org.svydovets.annotation.OneToOne;
import org.svydovets.baseEntity.Address;
import org.svydovets.baseEntity.AddressWithoutJoinColumnAnnotation;
import org.svydovets.baseEntity.AddressWithoutOneToOneAnnotation;
import org.svydovets.baseEntity.Note;
import org.svydovets.baseEntity.NoteWithoutJoinColumnAnnotation;
import org.svydovets.baseEntity.NoteWithoutManyToOneAnnotation;
import org.svydovets.baseEntity.Person;
import org.svydovets.baseEntity.PersonWithValidAnnotations;
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
        Field idField = Arrays.stream(PersonWithValidAnnotations.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny().orElseThrow();

        assertThat(idField).isEqualTo(EntityReflectionUtils.getIdField(PersonWithValidAnnotations.class));
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
        Class<PersonWithValidAnnotations> personClass = PersonWithValidAnnotations.class;
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
        Class<PersonWithValidAnnotations> personClass = PersonWithValidAnnotations.class;
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
    public void shouldReturnPositiveValueForEntityFieldIfColumnEntity() {
        var anyField = Arrays.stream(Person.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .findAny()
                .orElseThrow();

        assertTrue(EntityReflectionUtils.isColumnField(anyField));
    }

    @Test
    public void shouldReturnNegativeValueForEntityFieldIfColumnEntity() {
        var anyField = Arrays.stream(Person.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .findAny()
                .orElseThrow();

        assertFalse(EntityReflectionUtils.isColumnField(anyField));
    }

    @Test
    public void shouldReturnPositiveValueForEntityFieldWithOneToOneAnnotation() {
        var anyField = Arrays.stream(Address.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToOne.class))
                .findAny()
                .orElseThrow();

        assertTrue(EntityReflectionUtils.isEntityField(anyField));
    }

    @Test
    public void shouldReturnPositiveValueForEntityFieldWithManyToOneAnnotation() {
        var anyField = Arrays.stream(Note.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                .findAny()
                .orElseThrow();

        assertTrue(EntityReflectionUtils.isEntityField(anyField));
    }

    @Test
    public void shouldThrowForEntityFieldWithManyToOneAnnotationAndExcludeJoinColumnAnnotation() {
        var anyField = Arrays.stream(NoteWithoutJoinColumnAnnotation.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                .findAny()
                .orElseThrow();
        String message = String.format("The entity field [%s] that is marked with @OneToOne "
                + "or @ManyToOne annotation is missing the required @JoinColumn annotation", anyField.getName());

        assertThatExceptionOfType(AnnotationMappingException.class)
                .isThrownBy(() -> EntityReflectionUtils.isEntityField(anyField))
                .withMessage(message);
    }

    @Test
    public void shouldThrowForEntityFieldWithJoinColumnAnnotationAndExcludeManyToOneAnnotation() {
        var anyField = Arrays.stream(NoteWithoutManyToOneAnnotation.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .findAny()
                .orElseThrow();
        String message = String.format("The entity field [%s] that is marked with the " +
                "@JoinColumn annotation is missing @OneToOne or @ManyToOne annotation", anyField.getName());

        assertThatExceptionOfType(AnnotationMappingException.class)
                .isThrownBy(() -> EntityReflectionUtils.isEntityField(anyField))
                .withMessage(message);
    }

    @Test
    public void shouldThrowForEntityFieldWithOneToOneAnnotationAndExcludeJoinColumnAnnotation() {
        var anyField = Arrays.stream(AddressWithoutJoinColumnAnnotation.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToOne.class))
                .findAny()
                .orElseThrow();
        String message = String.format("The entity field [%s] that is marked with @OneToOne "
                + "or @ManyToOne annotation is missing the required @JoinColumn annotation", anyField.getName());

        assertThatExceptionOfType(AnnotationMappingException.class)
                .isThrownBy(() -> EntityReflectionUtils.isEntityField(anyField))
                .withMessage(message);
    }

    @Test
    public void shouldThrowForEntityFieldWithJoinColumnAnnotationAndExcludeOneToOneAnnotation() {
        var anyField = Arrays.stream(AddressWithoutOneToOneAnnotation.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .findAny()
                .orElseThrow();
        String message = String.format("The entity field [%s] that is marked with the " +
                "@JoinColumn annotation is missing @OneToOne or @ManyToOne annotation", anyField.getName());

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
    public void shouldThrowAnnotationMappingExceptionForJoinClazzFieldByParentClassAndJoinClass() {
        String message = String.format("Cannon find related field [%s] in %s", Person.class, Note.class);

        assertThatExceptionOfType(AnnotationMappingException.class)
                .isThrownBy(() -> EntityReflectionUtils.getJoinClazzField(Note.class, Person.class))
                .withMessage(message);
    }

}
