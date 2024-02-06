package org.svydovets.session;

import org.svydovets.connectionPool.datasource.ConnectionHandler;
import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.exception.SessionOperationException;
import org.svydovets.session.actionQueue.action.MergeAction;
import org.svydovets.session.actionQueue.action.PersistAction;
import org.svydovets.session.actionQueue.action.RemoveAction;
import org.svydovets.session.actionQueue.executor.ActionQueue;
import org.svydovets.transaction.TransactionManager;
import org.svydovets.transaction.TransactionManagerImpl;
import org.svydovets.util.EntityReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.svydovets.util.EntityReflectionUtils.getFieldValue;
import static org.svydovets.util.EntityReflectionUtils.isColumnField;
import static org.svydovets.util.EntityReflectionUtils.isEntityField;

/**
 * Manages a session for interacting with the database, providing functionality
 * for persisting, merging, and removing entities. It acts as a buffer between
 * the application and the database, caching entities and deferring database
 * operations to optimize performance and manage transactions.
 */
public class Session {

    private final GenericJdbcDAO jdbcDAO;
    private final ActionQueue actionQueue;
    private final Map<EntityKey<?>, Object> entitiesCache;

    private final Map<EntityKey<?>, Object[]> entitiesSnapshots;
    private final ConnectionHandler connectionHandler;

    private boolean closed;

    /**
     * Constructs a new session with the specified JDBC DAO and connection handler.
     *
     * @param jdbcDAO The DAO for database operations.
     * @param connectionHandler The handler for managing database connections.
     */
    public Session(GenericJdbcDAO jdbcDAO, ConnectionHandler connectionHandler) {
        this.jdbcDAO = jdbcDAO;
        this.connectionHandler = connectionHandler;
        this.actionQueue = new ActionQueue(jdbcDAO);
        this.entitiesCache = new HashMap<>();
        this.entitiesSnapshots = new HashMap<>();
        this.closed = false;
    }

    /**
     * Returns a transaction manager for managing transactions within this session.
     *
     * @return A {@link TransactionManager} instance.
     */
    public TransactionManager transactionManager() {
        return new TransactionManagerImpl(connectionHandler, actionQueue);
    }

    /**
     * Persists the given entity immediately or queues it for batch persistence.
     *
     * @param entity The entity to persist.
     */
    public void persist(Object entity) {
        PersistAction persistAction = new PersistAction(entity, true);
        actionQueue.addPersistAction(persistAction);

        EntityKey<?> entityKey = persistAction.getEntityEntry().entityKey();
        entitiesCache.put(entityKey, entity);
        saveEntitySnapshots(entityKey, entity);
    }

    /**
     * Retrieves an entity by its class type and identifier from the cache or database.
     *
     * @param entityType The class of the entity to retrieve.
     * @param id The identifier of the entity.
     * @param <T> The type of the entity.
     * @return The found entity or null if not found.
     */
    public <T> T findById(Class<T> entityType, Object id) {
        checkIfOpenSession();

        EntityKey<T> entityKey = new EntityKey<>(entityType, id);
        Object entity = entitiesCache.computeIfAbsent(entityKey, jdbcDAO::loadFromDB);
        saveEntitySnapshots(entityKey, entity);
        return entityType.cast(entity);
    }

    /**
     * Merges the state of the given entity with the one in the database.
     *
     * @param entity The entity to merge.
     * @param <T> The type of the entity.
     * @return The merged entity.
     */
    public <T> T merge(T entity) {
        EntityKey<T> entityKey = EntityKey.of(entity);
        if (entitiesCache.containsKey(entityKey)) {
            return entityKey.entityType().cast(entitiesCache.get(entityKey));
        }

        Object loadedEntity = jdbcDAO.loadFromDB(entityKey);
        if (loadedEntity != null) {
            saveEntitySnapshots(entityKey, loadedEntity);

            Object mergedEntity = mergeEntity(entity);
            entitiesCache.put(entityKey, mergedEntity);

            return entityKey.entityType().cast(mergedEntity);
        }

        return null;
    }

    /**
     * Removes the specified entity from the database.
     *
     * @param entity The entity to remove.
     */
    public void remove(Object entity) {
        EntityKey<?> entityKey = EntityKey.of(entity);
        if (!entitiesCache.containsKey(entityKey)) {
            throw new IllegalArgumentException(String.format("Removing a detached entity %s", entityKey.entityType().getName()));
        }

        EntityEntry entityEntry = EntityEntry.valueOf(entityKey, entity);
        actionQueue.addRemoveAction(new RemoveAction(entityEntry));
    }

    /**
     * This method close current session. Before closing the session, the following is performed:
     * - “dirty check”,
     * - clearing the first level cache
     * - clearing all snapshots.
     */
    public void close() {
        performDirtyCheck();

        flush();

        entitiesCache.clear();
        entitiesSnapshots.clear();

        closed = true;
    }

    /**
     * Flushes queued actions to the database, effectively applying changes.
     */
    public void flush() {
        actionQueue.performAccumulatedActions();
    }

    private void saveEntitySnapshots(EntityKey<?> entityKey, Object entity) {
        Field[] fields = EntityReflectionUtils.getEntityFieldsSortedByName(entityKey.entityType());
        Object[] snapshots = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            snapshots[i] = getFieldValue(entity, fields[i]);
            var field = fields[i];
            if (isColumnField(field) || isEntityField(field)) {
                snapshots[i] = getFieldValue(entity, field);
            }
        }

        entitiesSnapshots.put(entityKey, snapshots);
    }

    private void performDirtyCheck() {
        entitiesCache.entrySet()
                .stream()
                .filter(this::hasChanged)
                .map(entry -> EntityEntry.valueOf(entry.getKey(), entry.getValue()))
                .forEach(entityEntry -> actionQueue.addMergeAction(new MergeAction(entityEntry)));
    }

    private boolean hasChanged(Map.Entry<EntityKey<?>, Object> entry) {
        EntityKey<?> entityKey = entry.getKey();
        Object entity = entry.getValue();

        Field[] fields = EntityReflectionUtils.getEntityFieldsSortedByName(entityKey.entityType());
        Object[] snapshots = entitiesSnapshots.get(entityKey);
        for (int i = 0; i < snapshots.length; i++) {
            if (!Objects.equals(snapshots[i], getFieldValue(entity, fields[i]))) {
                return true;
            }
        }

        return false;
    }

    private Object mergeEntity(Object entity) {
        Class<?> entityType = entity.getClass();
        Object mergedEntity = EntityReflectionUtils.newInstanceOf(entityType);

        for (Field entityField : entityType.getDeclaredFields()) {
            Object fieldValue = getFieldValue(entity, entityField);
            EntityReflectionUtils.setFieldValue(mergedEntity, entityField, fieldValue);
        }

        return mergedEntity;
    }

    private void checkIfOpenSession() {
        if (closed) {
            throw new SessionOperationException("Current session is closed");
        }
    }
}
