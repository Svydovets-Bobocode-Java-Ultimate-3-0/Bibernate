package org.svydovets.dao;

import lombok.extern.log4j.Log4j2;
import org.svydovets.collection.LazyList;
import org.svydovets.exception.DaoOperationException;
import org.svydovets.exception.ResultSetParseException;
import org.svydovets.query.ParameterNameResolver;
import org.svydovets.query.SqlQueryBuilder;
import org.svydovets.session.EntityKey;
import org.svydovets.util.EntityReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.svydovets.util.EntityReflectionUtils.getJoinClazzField;
import static org.svydovets.util.EntityReflectionUtils.getJoinCollectionEntityType;
import static org.svydovets.util.EntityReflectionUtils.isColumnField;
import static org.svydovets.util.EntityReflectionUtils.isEntityCollectionField;
import static org.svydovets.util.EntityReflectionUtils.isEntityField;

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
     * @param keyEntityEntry
     */
    public void update(Map.Entry<EntityKey<?>, Object> keyEntityEntry) {
        try (Connection connection = dataSource.getConnection()) {
            performUpdate(connection, keyEntityEntry.getKey(), keyEntityEntry.getValue());
        } catch (SQLException exception) {
            throw new DaoOperationException(
                    String.format("Error updating entity: %s", keyEntityEntry.getKey()),
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

    private void performUpdate(Connection connection, EntityKey<?> entityKey, Object entity) throws SQLException {
        PreparedStatement updateByIdStatement = prepareUpdateStatement(connection, entityKey, entity);
        var updatedRowsCount = updateByIdStatement.executeUpdate();
        if (updatedRowsCount == 0) {
            throw new DaoOperationException(String.format("Update has not been perform for entity: %s", entityKey.entityType().getName()));
        }
    }

    private PreparedStatement prepareUpdateStatement(Connection connection, EntityKey<?> entityKey, Object entity) {
        try {
            String updateQuery = SqlQueryBuilder.buildUpdateByIdQuery(entityKey.entityType());
            if (log.isInfoEnabled()) {
                log.info("Update by id: {}", updateQuery);
            }

            PreparedStatement updateByIdStatement = connection.prepareStatement(updateQuery);
            Field[] entityFields = EntityReflectionUtils.getUpdatableFields(entityKey.entityType());
            for (int i = 0; i < entityFields.length; i++) {
                entityFields[i].setAccessible(true);
                updateByIdStatement.setObject(i + 1, entityFields[i].get(entity));
            }

            updateByIdStatement.setObject(entityFields.length + 1, entityKey.id());

            return updateByIdStatement;
        } catch (Exception exception) {
            throw new DaoOperationException(String
                    .format("Error preparing update statement for entity: %s", entityKey.entityType().getName()), exception);
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
        if (isEntityField(field)) {
            var joinClazz = field.getType();
            var joinColumnName = ParameterNameResolver.resolveJoinColumnName(field);
            var joinColumnValue = resultSet.getObject(joinColumnName);
            var entityKey = new EntityKey<>(joinClazz, joinColumnValue);
            return loadFromDB(entityKey);
        } else if (isEntityCollectionField(field)) {
            var joinClazz = getJoinCollectionEntityType(field);
            var entityFieldInJoinClazz = getJoinClazzField(entityType, joinClazz);
            var joinEntityId = resultSet.getObject(ParameterNameResolver.getIdFieldName(entityType));
            return createLazyList(joinClazz, entityFieldInJoinClazz, joinEntityId);
        } else if (isColumnField(field)) {
            String columnName = ParameterNameResolver.resolveColumnName(field);
            return resultSet.getObject(columnName);
        }
        return null;
    }

    /**
     * method returns the one entity by the restriction field
     *
     * @param entityType  - entity class type
     * @param field       - "restriction field" of entity
     * @param columnValue - value "restriction field" of entity
     * @param <T>
     * @return selected entity
     */
    public <T> T findBy(final Class<T> entityType, final Field field, final Object columnValue) {
        log.trace("Call findBy({}, {}, {})", entityType, field, columnValue);

        var result = findAllBy(entityType, field, columnValue);
        if (result.size() > 1) {
            throw new DaoOperationException(String
                    .format("The result for entity [%s] contains more than one line", entityType.getName()));
        }

        return result.get(0);
    }

    /**
     * method returns the entity list by the restriction field
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
        try (Connection connection = dataSource.getConnection()) {
            var selectByColumnStatement = prepareSelectStatement(connection, entityType, field, columnValue);
            ResultSet resultSet = selectByColumnStatement.executeQuery();
            while (resultSet.next()) {
                resultList.add(createEntityFromResultSet(entityType, resultSet));
            }

            return resultList;
        } catch (SQLException exception) {
            throw new DaoOperationException(String
                    .format("Error loading entities from the DB: %s", entityType.getName()), exception);
        }
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

}
