package org.svydovets.session;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.util.EntityReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Session {

    private final GenericJdbcDAO jdbcDAO;
    private final Map<EntityKey<?>, Object> entitiesCache;
    private final Map<EntityKey<?>, Object[]> entitiesSnapshots;

    public Session(GenericJdbcDAO jdbcDAO) {
        this.jdbcDAO = jdbcDAO;
        this.entitiesCache = new HashMap<>();
        this.entitiesSnapshots = new HashMap<>();
    }

    public void persist(Object entity) {
        Class<?> entityType = entity.getClass();

        Object generatedId = jdbcDAO.saveToDB(entity);
        Field idField = EntityReflectionUtils.getIdField(entityType);
        EntityReflectionUtils.setFieldValue(entity, idField, generatedId);

        EntityKey<?> entityKey = new EntityKey<>(entityType, generatedId);
        entitiesCache.put(entityKey, entity);
        saveEntitySnapshots(entityKey, entity);
    }

    /**
     * This method load entity form DB by entity type and primary key
     *
     * @param entityType
     * @param id - entity id
     * @param <T> - type of entity
     */
    public <T> T findById(Class<T> entityType, Object id) {
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

    /**
     * This method close current session. Before closing the session, the following is performed:
     *  - “dirty check”,
     *  - clearing the first level cache
     *  - clearing all snapshots.
     *
     */
    public void close() {
        performDirtyCheck();

        entitiesCache.clear();
        entitiesSnapshots.clear();
    }

    private void saveEntitySnapshots(EntityKey<?> entityKey, Object entity) {
        Field[] fields = EntityReflectionUtils.getEntityFieldsSortedByName(entityKey.entityType());
        Object[] snapshots = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            snapshots[i] = EntityReflectionUtils.getFieldValue(entity, fields[i]);
        }
        entitiesSnapshots.put(entityKey, snapshots);
    }

    private void performDirtyCheck() {
        entitiesCache.entrySet()
                .stream()
                .filter(this::hasChanged)
                .forEach(jdbcDAO::update);
    }

    private boolean hasChanged(Map.Entry<EntityKey<?>, Object> entry) {
        EntityKey<?> entityKey = entry.getKey();
        Object entity = entry.getValue();

        Field[] fields = EntityReflectionUtils.getEntityFieldsSortedByName(entityKey.entityType());
        Object[] snapshots = entitiesSnapshots.get(entityKey);
        for (int i = 0; i < snapshots.length; i++) {
            if (!Objects.equals(snapshots[i], EntityReflectionUtils.getFieldValue(entity, fields[i]))) {
                return true;
            }
        }
        return false;
    }

    private Object mergeEntity(Object entity) {
        Class<?> entityType = entity.getClass();
        Object mergedEntity = EntityReflectionUtils.newInstanceOf(entityType);

        for (Field entityField : entityType.getDeclaredFields()) {
            Object fieldValue = EntityReflectionUtils.getFieldValue(entity, entityField);
            EntityReflectionUtils.setFieldValue(mergedEntity, entityField, fieldValue);
        }

        return mergedEntity;
    }
}
