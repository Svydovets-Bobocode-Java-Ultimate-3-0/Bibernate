package org.svydovets.query;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.svydovets.annotation.Column;
import org.svydovets.annotation.Table;
import org.svydovets.query.baseEntity.Person1;
import org.svydovets.query.baseEntity.Person2;
import org.svydovets.query.baseEntity.Person3;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
}
