package org.svydovets.query;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.svydovets.baseEntity.Person1;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueryBuilderTest {

    @Test
    public void shouldReturnInsertByIdQuery() {
        String selectByIdQuery = "insert into persons (age, first_name, last_name, male) values (?, ?, ?, ?)";
        assertThat(selectByIdQuery).isEqualTo(SqlQueryBuilder.buildInsertQuery(Person1.class));
    }

    @Test
    public void shouldReturnSelectByIdQuery() {
        String selectByIdQuery = "select * from persons where id = ?";
        assertThat(selectByIdQuery).isEqualTo(SqlQueryBuilder.buildSelectByIdQuery(Person1.class));
    }

    @Test
    public void shouldReturnSelectByColumnQuery() {
        String selectByIdQuery = "select * from persons where last_name = ?";
        var tableName = ParameterNameResolver.resolveTableName(Person1.class);
        assertThat(selectByIdQuery).isEqualTo(SqlQueryBuilder.buildSelectByColumnQuery(tableName, "last_name"));
    }

    @Test
    public void shouldReturnUpdateByIdQuery() {
        String updateByIdQuery = "update persons set age = ?, first_name = ?, last_name = ?, male = ? where id = ?";
        assertThat(updateByIdQuery).isEqualTo(SqlQueryBuilder.buildUpdateByIdQuery(Person1.class));
    }

    @Test
    public void shouldReturnDeleteByIdQuery() {
        String selectByIdQuery = "delete from persons where id = ?";
        assertThat(selectByIdQuery).isEqualTo(SqlQueryBuilder.buildDeleteByIdQuery(Person1.class));
    }
}
