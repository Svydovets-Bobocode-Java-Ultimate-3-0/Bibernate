package org.svydovets.dao;

import lombok.extern.log4j.Log4j2;
import org.svydovets.exception.DaoOperationException;
import org.svydovets.query.SqlQueryBuilder;
import org.svydovets.session.EntityKey;
import org.svydovets.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Log4j2
public class GenericJdbcDAO {

    private final DataSource dataSource;

    public GenericJdbcDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T loadFromDB(EntityKey<T> entityKey) {
        try (Connection connection = dataSource.getConnection()) {
            return load(entityKey, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format(
                    "Error loading entity from the DB: %s", entityKey.clazz().getName()),
                    e
            );
        }
    }

    public void update(Map.Entry<EntityKey<?>, Object> keyEntityEntry) {
        try (Connection connection = dataSource.getConnection()) {
            performUpdate(connection, keyEntityEntry);
        } catch (SQLException e) {
            throw new DaoOperationException(
                    String.format("Error updating entity: %s", keyEntityEntry.getKey()),
                    e
            );
        }
    }

    private void performUpdate(Connection connection, Map.Entry<EntityKey<?>, Object> entry) throws SQLException {
        PreparedStatement updateByIdStatement = prepareUpdateStatement(connection, entry);
        var updatedRowsCount = updateByIdStatement.executeUpdate();
        if (updatedRowsCount == 0) {
            throw new DaoOperationException(String.format("Update has not been perform for entity: %s", entry.getKey()));
        }
    }

    private PreparedStatement prepareUpdateStatement(Connection connection, Map.Entry<EntityKey<?>, Object> entry) {
        try {
            EntityKey<?> entityKey = entry.getKey();

            String updateQuery = SqlQueryBuilder.buildUpdateByIdQuery(entityKey.clazz());

            if (log.isInfoEnabled()) {
                log.info("Update by id: {}", updateQuery);
            }

            PreparedStatement updateByIdStatement = connection.prepareStatement(updateQuery);
            Field[] fields = ReflectionUtils.getEntityFieldsWithoutIdFieldSortedByName(entityKey.clazz());
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                updateByIdStatement.setObject(i + 1, fields[i].get(entry.getValue()));
            }
            updateByIdStatement.setObject(fields.length + 1, entityKey.id());
            return updateByIdStatement;
        } catch (Exception e) {
            throw new DaoOperationException(
                    String.format("Error preparing update statement for entity: %s", entry.getKey().clazz()),
                    e
            );
        }
    }

    private <T> T load(EntityKey<T> entityKey, Connection connection) throws SQLException {
        PreparedStatement selectByIdStatement = prepareSelectStatement(entityKey, connection);
        ResultSet resultSet = selectByIdStatement.executeQuery();
        if (resultSet.next()) {
            return createEntityFromResultSet(entityKey, resultSet);
        }
        return null;
    }

    private PreparedStatement prepareSelectStatement(EntityKey<?> entityKey, Connection connection) {
        try {
            String selectQuery = SqlQueryBuilder.buildSelectByIdQuery(entityKey.clazz());

            if (log.isInfoEnabled()) {
                log.info("Select by id: {}", selectQuery);
            }

            PreparedStatement selectByIdStatement = connection.prepareStatement(selectQuery);
            selectByIdStatement.setObject(1, entityKey.id());
            return selectByIdStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format(
                    "Error preparing select statement for entity: %s", entityKey.clazz().getName()),
                    e
            );
        }
    }

    private <T> T createEntityFromResultSet(EntityKey<T> entityKey, ResultSet resultSet) {
        try {
            T entity = entityKey.clazz().getConstructor().newInstance();
            ResultSetParser.parseForEntity(entity, resultSet);
            return entity;
        } catch (Exception e) {
            throw new DaoOperationException(String.format(
                    "Error creating entity from result set: %s", entityKey.clazz().getName()),
                    e
            );
        }
    }

}
