package org.svydovets.dao;

import lombok.extern.log4j.Log4j2;
import org.svydovets.collection.LazyList;
import org.svydovets.connectionPool.datasource.ConnectionHandler;
import org.svydovets.exception.DaoOperationException;
import org.svydovets.exception.ResultSetParseException;
import org.svydovets.query.ParameterNameResolver;
import org.svydovets.query.SqlQueryBuilder;
import org.svydovets.session.EntityEntry;
import org.svydovets.session.EntityKey;
import org.svydovets.util.EntityReflectionUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A generic JDBC Data Access Object (DAO) that provides common database operations.
 * This class abstracts the boilerplate JDBC code required to interact with the database,
 * making it easier to perform CRUD (Create, Read, Update, Delete) operations on entity classes.
 */
@Log4j2
public class GenericJdbcDAO {

    public static final String THE_RESULT_FOR_ENTITY_CONTAINS_MORE_THAN_ONE_LINE = "The result for entity [%s] contains more than one line";
    public static final String ERROR_LOADING_ENTITIES_FROM_THE_DB = "Error loading entities from the DB: %s";
    private final ConnectionHandler connectionHandler;

    /**
     * Constructs a new GenericJdbcDAO with a specified connection handler.
     *
     * @param connectionHandler the connection handler responsible for providing database connections
     */
    public GenericJdbcDAO(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    /**
     * Saves an entity to the database.
     * This method automatically generates and executes an INSERT SQL statement based on the entity's class definition.
     *
     * @param entity the entity to save
     * @return the generated key of the saved entity
     * @throws DaoOperationException if there is an error saving the entity
     */
    public Object saveToDB(Object entity) {
        try (Connection connection = connectionHandler.getConnection()) {
            return save(entity, connection);
        } catch (SQLException exception) {
            throw new DaoOperationException(String.format(
                    "Error saving entity to the DB: %s", entity.getClass().getName()),
                    exception
            );
        }
    }

    /**
     * Loads an entity from the database by its identifier.
     * This method automatically generates and executes a SELECT SQL statement to retrieve the entity.
     *
     * @param entityKey the key identifying the entity to load
     * @param <T> the type parameter of the entity
     * @return the loaded entity, or {@code null} if not found
     * @throws DaoOperationException if there is an error loading the entity
     */
    public <T> T loadFromDB(EntityKey<T> entityKey) {
        try (Connection connection = connectionHandler.getConnection()) {
            return load(entityKey, connection);
        } catch (SQLException exception) {
            throw new DaoOperationException(String.format(
                    "Error loading entity from the DB: %s", entityKey.entityType().getName()),
                    exception
            );
        }
    }

    /**
     * Updates an existing entity in the database by its identifier.
     * This method automatically generates and executes an UPDATE SQL statement based on the provided entity data.
     *
     * @param entityEntry the entity entry containing the entity to update and its identifying key
     * @throws DaoOperationException if there is an error updating the entity
     */
    public void update(EntityEntry entityEntry) {
        try (Connection connection = connectionHandler.getConnection()) {
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
     * Removes an entity from the database by its identifier.
     * This method automatically generates and executes a DELETE SQL statement for the specified entity.
     *
     * @param entityKey the key identifying the entity to remove
     * @param <T> the type parameter of the entity
     * @throws DaoOperationException if there is an error deleting the entity
     */
    public <T> void remove(EntityKey<T> entityKey) {
        Class<T> entityClass = entityKey.entityType();

        log.trace("Call remove({}) for entity class", entityClass);

        try (Connection connection = connectionHandler.getConnection()) {
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

    /**
     * Returns the one entity by the restriction field
     *
     * @param entityType  - entity class type
     * @param field       - "restriction field" of entity
     * @param columnValue - value "restriction field" of entity
     * @param <T>
     * @return selected entity
     */
    public <T> T findBy(final Class<T> entityType, final Field field, final Object columnValue) {
        log.trace("Call findBy({}, {}, {})", entityType, field, columnValue);

        List<T> result = findAllBy(entityType, field, columnValue);
        if (result.size() > 1) {
            throw new DaoOperationException(String
                    .format(THE_RESULT_FOR_ENTITY_CONTAINS_MORE_THAN_ONE_LINE, entityType.getName()));
        }

        return result.get(0);
    }

    /**
     * Returns the entity list by the restriction field
     *
     * @param entityType  - entity class type
     * @param field       - "restriction field" of entity
     * @param columnValue - value "restriction field" of entity
     * @param <T>
     * @return selected list entities
     */
    public <T> List<T> findAllBy(final Class<T> entityType, final Field field, final Object columnValue) {
        log.trace("Call findAllBy({}, {}, {})", entityType, field, columnValue);

        List<T> resultList = new ArrayList<>();
        try (Connection connection = connectionHandler.getConnection()) {
            var selectByColumnStatement = prepareSelectStatement(connection, entityType, field, columnValue);
            ResultSet resultSet = selectByColumnStatement.executeQuery();
            while (resultSet.next()) {
                resultList.add(createEntityFromResultSet(entityType, resultSet));
            }

            return resultList;
        } catch (SQLException exception) {
            throw new DaoOperationException(String
                    .format(ERROR_LOADING_ENTITIES_FROM_THE_DB, entityType.getName()), exception);
        }
    }

    /**
     * Returns a single entity by native query, base entity and values
     *
     * @param query  - native query
     * @param entityType  - entity class type
     * @param columnValues - values for query
     * @param <T>
     * @return single entity
     */
    public <T> T nativeQueryBy(final String query, final Class<T> entityType, final Object[] columnValues) {
        log.trace("Call nativeQueryBy({}, {}, {})", query, entityType, columnValues);

        List<T> result = nativeQueryAllBy(query, entityType, columnValues);
        if (result.size() > 1) {
            throw new DaoOperationException(String
                    .format(THE_RESULT_FOR_ENTITY_CONTAINS_MORE_THAN_ONE_LINE, entityType.getName()));
        }

        return result.get(0);
    }

    /**
     * Returns a list of entities by native query, base entity and values
     *
     * @param query  - native query
     * @param entityType  - entity class type
     * @param columnValues - values for query
     * @param <T>
     * @return list entities
     */
    public <T> List<T> nativeQueryAllBy(final String query, final Class<T> entityType, final Object[] columnValues) {
        log.trace("Call nativeQueryAllBy({}, {}, {})", query, entityType, columnValues);

        try (Connection connection = connectionHandler.getConnection()) {
            final List<T> resultList = new ArrayList<>();
            final var selectByColumnStatement = prepareNativeQueryStatement(connection, query, columnValues);
            ResultSet resultSet = selectByColumnStatement.executeQuery();
            while (resultSet.next()) {
                resultList.add(createEntityFromResultSet(entityType, resultSet));
            }

            return resultList;
        } catch (SQLException exception) {
            throw new DaoOperationException(String
                    .format(ERROR_LOADING_ENTITIES_FROM_THE_DB, entityType.getName()), exception);
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
        String insertQuery = SqlQueryBuilder.buildInsertQuery(entity.getClass());
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
            return createEntityFromResultSet(entityKey.entityType(), resultSet);
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

    private <T> T createEntityFromResultSet(Class<T> entityType, ResultSet resultSet) {
        try {
            T entity = entityType.getConstructor().newInstance();
            parseResultSetForEntity(entityType, resultSet, entity);

            return entity;
        } catch (Exception exception) {
            throw new DaoOperationException(String.format(
                    "Error creating entity from result set: %s", entityType.getName()), exception);
        }
    }

    private <T> void parseResultSetForEntity(Class<T> entityType, ResultSet resultSet, T entity) {
        try {
            for (Field field : entityType.getDeclaredFields()) {
                Object fieldValue = parseResultSetForField(entityType, resultSet, field);
                EntityReflectionUtils.setFieldValue(entity, field, fieldValue);
            }
        } catch (SQLException exception) {
            throw new ResultSetParseException(String
                    .format("Error parsing result set for entity of type: %s",
                            entity.getClass().getName()), exception);
        }
    }

    private Object parseResultSetForField(Class<?> entityType, ResultSet resultSet, Field field) throws SQLException {
        if (EntityReflectionUtils.isEntityField(field)) {
            var joinClazz = field.getType();
            var joinColumnName = ParameterNameResolver.resolveJoinColumnName(field);
            var joinColumnValue = resultSet.getObject(joinColumnName);
            var entityKey = new EntityKey<>(joinClazz, joinColumnValue);
            return loadFromDB(entityKey);
        } else if (EntityReflectionUtils.isEntityCollectionField(field)) {
            var joinClazz = EntityReflectionUtils.getJoinCollectionEntityType(field);
            var entityFieldInJoinClazz = EntityReflectionUtils.getJoinClazzField(entityType, joinClazz);
            var joinEntityId = resultSet.getObject(ParameterNameResolver.getIdFieldName(entityType));
            return createLazyList(joinClazz, entityFieldInJoinClazz, joinEntityId);
        } else if (EntityReflectionUtils.isColumnField(field)) {
            String columnName = ParameterNameResolver.resolveColumnName(field);
            return resultSet.getObject(columnName);
        }
        return null;
    }

    private PreparedStatement prepareSelectStatement(final Connection connection,
                                                     final Class<?> entityType,
                                                     final Field field,
                                                     final Object columnValue) {
        try {
            var tableName = ParameterNameResolver.resolveTableName(entityType);
            var fieldName = ParameterNameResolver.resolveJoinColumnOrColumnName(field);
            String selectQuery = SqlQueryBuilder.buildSelectByColumnQuery(tableName, fieldName);

            if (log.isInfoEnabled()) {
                log.info("Select by column name: {}", selectQuery);
            }

            PreparedStatement selectByColumnStatement = connection.prepareStatement(selectQuery);
            selectByColumnStatement.setObject(1, columnValue);

            return selectByColumnStatement;
        } catch (SQLException exception) {
            throw new DaoOperationException(String
                    .format("Error preparing select statement for entity: %s", entityType.getName()), exception);
        }
    }

    private <T> LazyList<T> createLazyList(Class<T> joinEntityType, Field entityFieldInJoinClazz, Object entityId) {
        Supplier<List<T>> listSupplier = () -> findAllBy(joinEntityType, entityFieldInJoinClazz, entityId);

        return new LazyList<>(listSupplier);
    }

    private PreparedStatement prepareNativeQueryStatement(final Connection connection,
                                                          final String query,
                                                          final Object[] columnValues) {
        try {
            PreparedStatement selectByColumnStatement = connection.prepareStatement(query);
            for (int i = 0; i < columnValues.length - 1; i++) {
                selectByColumnStatement.setObject(i + 1, columnValues[i]);
            }

            return selectByColumnStatement;
        } catch (SQLException exception) {
            throw new DaoOperationException(String.format("Error preparing native query: %s", query), exception);
        }
    }

}
