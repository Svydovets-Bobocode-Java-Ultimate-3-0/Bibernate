package org.svydovets.query;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.svydovets.query.baseEntity.Person1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlQueryBuilderTest {

    @Test
    public void shouldReturnSelectByIdQuery() {
        String selectByIdQuery = "select * from persons where id = ?";
        assertThat(selectByIdQuery).isEqualTo(SqlQueryBuilder.buildSelectByIdQuery(Person1.class));
    }
}
