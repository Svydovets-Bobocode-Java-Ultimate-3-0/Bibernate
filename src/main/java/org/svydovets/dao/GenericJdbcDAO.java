package org.svydovets.dao;

import lombok.extern.log4j.Log4j2;
import org.svydovets.exception.DaoOperationException;
import org.svydovets.query.SqlQueryBuilder;
import org.svydovets.session.EntityEntry;
import org.svydovets.session.EntityKey;
import org.svydovets.util.EntityReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 */
@Log4j2
public class GenericJdbcDAO {

    private final DataSource dataSource;

    public GenericJdbcDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Object saveToDB(Object entity) {
        try (Connection connection = dataSource.getConnection()) {
            return save(entity, connection);
        } catch (SQLException exception) {
            throw new DaoOperationException(String.format(
                    "Error saving entity to the DB: %s", entity.getClass().getName()),
                    exception
            );
        }
    }

    private Object save(Object entity, Connection connection) throws SQLException {
        PreparedStatement insertStatement = prepareInsertStatement(entity, connection);
        insertStatement.executeUpdate();
        ResultSet resultSet = insertStatement.getGeneratedKeys();
        if (!resultSet.next()) {
            throw new DaoOperationException(String.format("Error fetching generated id for entity: %s", entity.getClass().getName()));
        }

        return resultSet.getObject(1);
    }

    private PreparedStatement prepareInsertStatement(Object entity, Connection connection) {
        String insertQuery = SqlQueryBuilder.buildInsertQuery(entity);
        if (log.isInfoEnabled()) {
            log.info(String.format("Insert: %s", insertQuery));
        }
        try {
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            Field[] entityFields = EntityReflectionUtils.getInsertableFieldsForIdentityGenerationType(entity.getClass());
            for (int i = 0; i < entityFields.length; i++) {
                insertStatement.setObject(i + 1, EntityReflectionUtils.getFieldValue(entity, entityFields[i]));
            }
            return insertStatement;
        } catch (SQLException exception) {
            throw new DaoOperationException(String.format(
                    "Error preparing insert statement for entity: %s", entity.getClass().getName()),
                    exception
            );
        }
    }

    public <T> T loadFromDB(EntityKey<T> entityKey) {
        try (Connection connection = dataSource.getConnection()) {
            return load(entityKey, connection);
        } catch (SQLException exception) {
            throw new DaoOperationException(String.format(
                    "Error loading entity from the DB: %s", entityKey.entityType().getName()),
                    exception
            );
        }
    }

    /**
     * This method update entity by id
     *
     * @param entityEntry
     */
    public void update(EntityEntry entityEntry) {
        try (Connection connection = dataSource.getConnection()) {
            performUpdate(connection, entityEntry);
        } catch (SQLException exception) {
            String entityName = entityEntry.entityKey().entityType().getName();
            throw new DaoOperationException(
                    String.format("Error updating entity: %s", entityName),
                    exception
            );
        }
    }

    /**
     * This method remove entity by id
     *
     * @param entityKey
     * @param <T>
     */
    public <T> void remove(EntityKey<T> entityKey) {
        Class<T> entityClass = entityKey.entityType();

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

    private void performUpdate(Connection connection, EntityEntry entityEntry) throws SQLException {
        PreparedStatement updateByIdStatement = prepareUpdateStatement(connection, entityEntry);
        var updatedRowsCount = updateByIdStatement.executeUpdate();
        if (updatedRowsCount == 0) {
            String entityName = entityEntry.entityKey().entityType().getName();
            throw new DaoOperationException(String.format("Update has not been perform for entity: %s", entityName));
        }
    }

    private PreparedStatement prepareUpdateStatement(Connection connection, EntityEntry entityEntry) {
        try {
            Class<?> entityType = entityEntry.entityKey().entityType();

            String updateQuery = SqlQueryBuilder.buildUpdateByIdQuery(entityType);
            System.out.println(updateQuery);
            if (log.isInfoEnabled()) {
                log.info("Update by id: {}", updateQuery);
            }

            PreparedStatement updateByIdStatement = connection.prepareStatement(updateQuery);

            Object entity = entityEntry.entity();
            Field[] entityFields = EntityReflectionUtils.getUpdatableFields(entityType);
            for (int i = 0; i < entityFields.length; i++) {
                entityFields[i].setAccessible(true);
                updateByIdStatement.setObject(i + 1, entityFields[i].get(entity));
            }

            Object entityId = entityEntry.entityKey().id();
            updateByIdStatement.setObject(entityFields.length + 1, entityId);

            return updateByIdStatement;
        } catch (Exception exception) {
            String entityName = entityEntry.entityKey().entityType().getName();
            throw new DaoOperationException(String.format(
                    "Error preparing update statement for entity: %s", entityName),
                    exception
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
            String selectQuery = SqlQueryBuilder.buildSelectByIdQuery(entityKey.entityType());

            if (log.isInfoEnabled()) {
                log.info("Select by id: {}", selectQuery);
            }

            PreparedStatement selectByIdStatement = connection.prepareStatement(selectQuery);
            selectByIdStatement.setObject(1, entityKey.id());

            return selectByIdStatement;
        } catch (SQLException exception) {
            throw new DaoOperationException(String.format(
                    "Error preparing select statement for entity: %s", entityKey.entityType().getName()),
                    exception
            );
        }
    }

    private <T> T createEntityFromResultSet(EntityKey<T> entityKey, ResultSet resultSet) {
        try {
            T entity = entityKey.entityType().getConstructor().newInstance();
            ResultSetParser.parseForEntity(entity, resultSet);

            return entity;
        } catch (Exception exception) {
            throw new DaoOperationException(String.format(
                    "Error creating entity from result set: %s", entityKey.entityType().getName()),
                    exception
            );
        }
    }

}
