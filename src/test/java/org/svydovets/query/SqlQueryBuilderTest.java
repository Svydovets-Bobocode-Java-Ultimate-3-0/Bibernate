package org.svydovets.query;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.svydovets.baseEntity.PersonWithValidAnnotations;
import org.svydovets.baseEntity.PersonWithVersionAnnotation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueryBuilderTest {

    @Test
    public void shouldReturnInsertByIdQuery() {
        String selectByIdQuery = "insert into persons (age, first_name, last_name, male) values (?, ?, ?, ?)";
        assertThat(selectByIdQuery).isEqualTo(SqlQueryBuilder.buildInsertQuery(PersonWithValidAnnotations.class));
    }

    @Test
    public void shouldReturnSelectByIdQuery() {
        String selectByIdQuery = "select * from persons where id = ?";
        assertThat(selectByIdQuery).isEqualTo(SqlQueryBuilder.buildSelectByIdQuery(PersonWithValidAnnotations.class));
    }

    @Test
    public void shouldReturnSelectByIdQueryWithPessimistickLockForWrite() {
        String selectByIdQuery = "select * from persons where id = ? for share";
        assertThat(selectByIdQuery).isEqualTo(SqlQueryBuilder.buildSelectByIdQuery(PersonWithValidAnnotations.class, PessimisticLockStrategy.ENABLE_PESSIMISTIC_WRITE));
    }
    @Test
    public void shouldReturnSelectByIdQueryWithPessimistickLockForRead() {
        String selectByIdQuery = "select * from persons where id = ? for update";
        assertThat(selectByIdQuery).isEqualTo(SqlQueryBuilder.buildSelectByIdQuery(PersonWithValidAnnotations.class, PessimisticLockStrategy.ENABLE_PESSIMISTIC_READ));
    }

    @Test
    public void shouldReturnSelectByColumnQuery() {
        String selectByIdQuery = "select * from persons where last_name = ?";
        var tableName = ParameterNameResolver.resolveTableName(PersonWithValidAnnotations.class);
        assertThat(selectByIdQuery).isEqualTo(SqlQueryBuilder.buildSelectByColumnQuery(tableName, "last_name"));
    }

    @Test
    public void shouldReturnUpdateByIdQuery() {
        String updateByIdQuery = "update persons set age = ?, first_name = ?, last_name = ?, male = ? where id = ?";
        assertThat(updateByIdQuery).isEqualTo(SqlQueryBuilder.buildUpdateByIdQuery(PersonWithValidAnnotations.class));
    }

    @Test
    public void shouldReturnUpdateByIdQueryWitVersionOptLock() {
        String updateByVersionQuery = "update persons set age = ?, first_name = ?, last_name = ?, male = ?, version = ? where id = ? and version = ?";
        assertThat(updateByVersionQuery).isEqualTo(SqlQueryBuilder.buildUpdateByIdQuery(PersonWithVersionAnnotation.class));
    }

    @Test
    public void shouldReturnDeleteByIdQuery() {
        String selectByIdQuery = "delete from persons where id = ?";
        assertThat(selectByIdQuery).isEqualTo(SqlQueryBuilder.buildDeleteByIdQuery(PersonWithValidAnnotations.class));
    }
}
