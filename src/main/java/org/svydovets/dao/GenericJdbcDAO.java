package org.svydovets.dao;

import lombok.extern.log4j.Log4j2;
import org.svydovets.exception.DaoOperationException;
import org.svydovets.query.ParameterNameResolver;
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

/**
 *
 */
@Log4j2
public class GenericJdbcDAO {

    private final DataSource dataSource;

    public GenericJdbcDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * This method load entity by id
     *
     * @param entityKey
     * @return entity form DB
     * @param <T>
     */
    public <T> T loadFromDB(EntityKey<T> entityKey) {
        try (Connection connection = dataSource.getConnection()) {
            return load(entityKey, connection);
        } catch (SQLException exception) {
            throw new DaoOperationException(String
                    .format("Error loading entity from the DB: %s", entityKey.clazz().getName()), exception);
        }
    }

    /**
     * This method update entity by id
     *
     * @param keyEntityEntry
     */
    public void update(Map.Entry<EntityKey<?>, Object> keyEntityEntry) {
        try (Connection connection = dataSource.getConnection()) {
            performUpdate(connection, keyEntityEntry);
        } catch (SQLException exception) {
            throw new DaoOperationException(String
                    .format("Error updating entity: %s", keyEntityEntry.getKey()), exception);
        }
    }

    /**
     * This method remove entity by id
     *
     * @param entityKey
     * @param <T>
     */
    public <T> void remove(EntityKey<T> entityKey) {
        Class<T> entityClass = entityKey.clazz();

        log.trace("Call remove({}) for entity class", entityClass);

        try (Connection connection = dataSource.getConnection()) {
            String deleteQuery = SqlQueryBuilder.buildDeleteByIdQuery(entityClass);
            if (log.isInfoEnabled()) {
                log.info("Remove by id: {}", deleteQuery);
            }

            PreparedStatement deleteByIdStatement = connection.prepareStatement(deleteQuery);
            deleteByIdStatement.setObject(1, entityKey.id());
            var deleteRowsCount = deleteByIdStatement.executeUpdate();
            if (deleteRowsCount == 0) {
                throw new DaoOperationException(String
                        .format("Delete has not been perform for entity: %s", entityClass));
            }
        } catch (SQLException exception) {
            throw new DaoOperationException(String
                    .format("Error delete entity: %s", entityClass), exception);
        }
    }

    private void performUpdate(Connection connection, Map.Entry<EntityKey<?>, Object> entry) throws SQLException {
        PreparedStatement updateByIdStatement = prepareUpdateStatement(connection, entry);
        var updatedRowsCount = updateByIdStatement.executeUpdate();
        if (updatedRowsCount == 0) {
            throw new DaoOperationException(String
                    .format("Update has not been perform for entity: %s", entry.getKey()));
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
        } catch (Exception exception) {
            throw new DaoOperationException(String
                    .format("Error preparing update statement for entity: %s", entry.getKey().clazz()), exception);
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
        } catch (SQLException exception) {
            throw new DaoOperationException(String.format("Error preparing select statement for entity: %s",
                            entityKey.clazz().getName()), exception);
        }
    }

    private <T> T createEntityFromResultSet(EntityKey<T> entityKey, ResultSet resultSet) {
        try {
            T entity = entityKey.clazz().getConstructor().newInstance();
            ResultSetParser.parseForEntity(entity, resultSet);

            return entity;
        } catch (Exception exception) {
            throw new DaoOperationException(String
                    .format("Error creating entity from result set: %s", entityKey.clazz().getName()), exception);
        }
    }

}
