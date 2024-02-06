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

public class Session {

    private final GenericJdbcDAO jdbcDAO;
    private final ActionQueue actionQueue;
    private final Map<EntityKey<?>, Object> entitiesCache;

    private final Map<EntityKey<?>, Object[]> entitiesSnapshots;
    private final ConnectionHandler connectionHandler;

    private boolean closed;

    public Session(GenericJdbcDAO jdbcDAO, ConnectionHandler connectionHandler) {
        this.jdbcDAO = jdbcDAO;
        this.connectionHandler = connectionHandler;
        this.actionQueue = new ActionQueue(jdbcDAO);
        this.entitiesCache = new HashMap<>();
        this.entitiesSnapshots = new HashMap<>();
        this.closed = false;
    }

    public TransactionManager transactionManager() {
        return new TransactionManagerImpl(connectionHandler, actionQueue);
    }

    public void persist(Object entity) {
        PersistAction persistAction = new PersistAction(entity, true);
        actionQueue.addPersistAction(persistAction);

        EntityKey<?> entityKey = persistAction.getEntityEntry().entityKey();
        entitiesCache.put(entityKey, entity);
        saveEntitySnapshots(entityKey, entity);
    }

    /**
     * This method load entity form DB by entity type and primary key
     *
     * @param entityType
     * @param id         - entity id
     * @param <T>        - type of entity
     */
    public <T> T findById(Class<T> entityType, Object id) {
        checkIfOpenSession();

        EntityKey<T> entityKey = new EntityKey<>(entityType, id);
        Object entity = entitiesCache.computeIfAbsent(entityKey, jdbcDAO::loadFromDB);
        saveEntitySnapshots(entityKey, entity);
        return entityType.cast(entity);
    }

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
