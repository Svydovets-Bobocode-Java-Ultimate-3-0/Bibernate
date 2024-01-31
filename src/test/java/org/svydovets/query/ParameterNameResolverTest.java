package org.svydovets.query;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.svydovets.annotation.Column;
import org.svydovets.annotation.JoinColumn;
import org.svydovets.annotation.Table;
import org.svydovets.baseEntity.Note;
import org.svydovets.baseEntity.Note2;
import org.svydovets.baseEntity.Person1;
import org.svydovets.baseEntity.Person2;
import org.svydovets.baseEntity.Person3;
import org.svydovets.baseEntity.Person4;
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
        String tableName = Person1.class.getAnnotation(Table.class).name();
        assertThat(tableName).isEqualTo(ParameterNameResolver.resolveTableName(Person1.class));
    }

    @Test
    public void shouldReturnTableAnnotationNameIfNameIsSpecifiedNotExplicitly() {
        String tableName = Person2.class.getSimpleName();
        assertThat(tableName).isEqualTo(ParameterNameResolver.resolveTableName(Person2.class));
    }

    @Test
    public void shouldReturnTableNameWithoutTableAnnotation() {
        String tableName = Person3.class.getSimpleName();
        assertThat(tableName).isEqualTo(ParameterNameResolver.resolveTableName(Person3.class));
    }

    @Test
    public void shouldReturnColumnAnnotationNameIfNameIsSpecifiedExplicitly() {
        var anyField = Arrays.stream(Person1.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class) && !field.getName().isEmpty())
                .findAny().orElseThrow();
        var column = anyField.getAnnotation(Column.class);

        assertThat(column.name()).isEqualTo(ParameterNameResolver.resolveColumnName(anyField));
    }

    @Test
    public void shouldReturnColumnAnnotationNameIfNameIsSpecifiedNotExplicitly() {
        var anyField = Arrays.stream(Person1.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class)
                        && field.getAnnotation(Column.class).name().isEmpty())
                .findAny().orElseThrow();

        assertThat(anyField.getName()).isEqualTo(ParameterNameResolver.resolveColumnName(anyField));
    }

    @Test
    public void shouldReturnColumnNameWithoutColumnAnnotation() {
        var anyField = Arrays.stream(Person1.class.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Column.class))
                .findAny().orElseThrow();

        assertThat(anyField.getName()).isEqualTo(ParameterNameResolver.resolveColumnName(anyField));
    }

    @Test
    public void returnsPresenceOfEntityAnnotationForClass() {
        assertTrue(ParameterNameResolver.isEntity(Person1.class));
        assertFalse(ParameterNameResolver.isEntity(Person4.class));
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
        var anyField = Arrays.stream(Note2.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JoinColumn.class)
                        && field.getAnnotation(JoinColumn.class).name().isEmpty())
                .findAny()
                .orElseThrow();

        assertThat(anyField.getName()).isEqualTo(ParameterNameResolver.resolveColumnName(anyField));
    }

    @Test
    public void shouldThrowAnnotationMappingExceptionWithoutJoinColumnAnnotation() {
        var anyField = Arrays.stream(Person1.class.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Column.class))
                .findAny().orElseThrow();

        String message = String.format("Field [%s] must be marked like JoinColumn annotation", anyField.getName());

        assertThatExceptionOfType(AnnotationMappingException.class)
                .isThrownBy(() -> ParameterNameResolver.resolveJoinColumnName(anyField))
                .withMessage(message);
    }
}
