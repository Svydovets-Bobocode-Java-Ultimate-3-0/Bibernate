package org.svydovets.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.svydovets.annotation.Column;
import org.svydovets.annotation.JoinColumn;
import org.svydovets.annotation.Table;
import org.svydovets.baseEntity.Note;
import org.svydovets.baseEntity.NoteWithJoinColumnAnnotationAndWithoutNameValue;
import org.svydovets.baseEntity.PersonWithValidAnnotations;
import org.svydovets.baseEntity.PersonWithoutTableAnnotation;
import org.svydovets.baseEntity.PersonWithoutTableAnnotationNameValue;
import org.svydovets.baseEntity.PersonWithoutTableAndEntityAnnotations;
import org.svydovets.baseEntity.PersonWithVersionAnnotation;
import org.svydovets.exception.AnnotationMappingException;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParameterNameResolverTest {

    @Test
    public void shouldReturnTableAnnotationNameIfNameIsSpecifiedExplicitly() {
        String tableName = PersonWithValidAnnotations.class.getAnnotation(Table.class).name();
        assertThat(tableName).isEqualTo(ParameterNameResolver.resolveTableName(PersonWithValidAnnotations.class));
    }

    @Test
    public void shouldReturnTableAnnotationNameIfNameIsSpecifiedNotExplicitly() {
        String tableName = PersonWithoutTableAnnotation.class.getSimpleName();
        assertThat(tableName).isEqualTo(ParameterNameResolver.resolveTableName(PersonWithoutTableAnnotation.class));
    }

    @Test
    public void shouldReturnTableNameWithoutTableAnnotation() {
        String tableName = PersonWithoutTableAnnotationNameValue.class.getSimpleName();
        assertThat(tableName).isEqualTo(ParameterNameResolver.resolveTableName(PersonWithoutTableAnnotationNameValue.class));
    }

    @Test
    public void shouldReturnColumnAnnotationNameIfNameIsSpecifiedExplicitly() {
        var anyField = Arrays.stream(PersonWithValidAnnotations.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class) && !field.getName().isEmpty())
                .findAny().orElseThrow();
        var column = anyField.getAnnotation(Column.class);

        assertThat(column.name()).isEqualTo(ParameterNameResolver.resolveColumnName(anyField));
    }

    @Test
    public void shouldReturnColumnAnnotationNameIfNameIsSpecifiedNotExplicitly() {
        var anyField = Arrays.stream(PersonWithValidAnnotations.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class)
                        && field.getAnnotation(Column.class).name().isEmpty())
                .findAny().orElseThrow();

        assertThat(anyField.getName()).isEqualTo(ParameterNameResolver.resolveColumnName(anyField));
    }

    @Test
    public void shouldReturnColumnNameWithoutColumnAnnotation() {
        var anyField = Arrays.stream(PersonWithValidAnnotations.class.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Column.class))
                .findAny().orElseThrow();

        assertThat(anyField.getName()).isEqualTo(ParameterNameResolver.resolveColumnName(anyField));
    }

    @Test
    public void returnsPresenceOfEntityAnnotationForClass() {
        assertTrue(ParameterNameResolver.isEntity(PersonWithValidAnnotations.class));
        assertFalse(ParameterNameResolver.isEntity(PersonWithoutTableAndEntityAnnotations.class));
    }

    @Test
    public void shouldReturnJoinColumnAnnotationNameIfNameIsSpecifiedExplicitly() {
        var anyField = Arrays.stream(Note.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class))
                .findAny()
                .orElseThrow();
        var joinColumn = anyField.getAnnotation(JoinColumn.class);

        assertThat(joinColumn.name()).isEqualTo(ParameterNameResolver.resolveJoinColumnName(anyField));
    }

    @Test
    public void shouldReturnJoinColumnAnnotationNameIfNameIsSpecifiedNotExplicitly() {
        var anyField = Arrays.stream(NoteWithJoinColumnAnnotationAndWithoutNameValue.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class)
                        && field.getAnnotation(JoinColumn.class).name().isEmpty())
                .findAny()
                .orElseThrow();

        assertThat(anyField.getName()).isEqualTo(ParameterNameResolver.resolveColumnName(anyField));
    }

    @Test
    public void shouldThrowAnnotationMappingExceptionWithoutJoinColumnAnnotation() {
        var anyField = Arrays.stream(PersonWithValidAnnotations.class.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Column.class))
                .findAny().orElseThrow();

        String message = String.format("Field [%s] must be marked like JoinColumn annotation", anyField.getName());

        assertThatExceptionOfType(AnnotationMappingException.class)
                .isThrownBy(() -> ParameterNameResolver.resolveJoinColumnName(anyField))
                .withMessage(message);
    }

    @Test
    public void shouldReturnVersionColumnAnnotationFieldName() {
        Assertions.assertEquals("version", ParameterNameResolver.getVersionFieldName(PersonWithVersionAnnotation.class));
    }

}
