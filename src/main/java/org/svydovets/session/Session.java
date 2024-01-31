package org.svydovets.session;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.util.ReflectionUtils;

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
        Class<?> clazz = entity.getClass();

        Object generatedId = jdbcDAO.saveToDB(entity);
        Field idField = ReflectionUtils.getIdField(clazz);
        ReflectionUtils.setFieldValue(entity, idField, generatedId);

        EntityKey<?> entityKey = new EntityKey<>(clazz, generatedId);
        entitiesCache.put(entityKey, entity);
        saveEntitySnapshots(entityKey, entity);
    }

    /**
     * This method load entity form DB by entity type and primary key
     *
     * @param clazz
     * @param id - entity id
     * @param <T> - type of entity
     */
    public <T> T findById(Class<T> clazz, Object id) {
        EntityKey<T> entityKey = new EntityKey<>(clazz, id);
        Object entity = entitiesCache.computeIfAbsent(entityKey, jdbcDAO::loadFromDB);
        saveEntitySnapshots(entityKey, entity);
        return clazz.cast(entity);
    }

    private void saveEntitySnapshots(EntityKey<?> entityKey, Object entity) {
        Field[] fields = ReflectionUtils.getEntityFieldsSortedByName(entityKey.clazz());
        Object[] snapshots = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            snapshots[i] = ReflectionUtils.getFieldValue(entity, fields[i]);
        }
        entitiesSnapshots.put(entityKey, snapshots);
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

    private void performDirtyCheck() {
        entitiesCache.entrySet()
                .stream()
                .filter(this::hasChanged)
                .forEach(jdbcDAO::update);
    }

    private boolean hasChanged(Map.Entry<EntityKey<?>, Object> entry) {
        EntityKey<?> entityKey = entry.getKey();
        Object entity = entry.getValue();

        Field[] fields = ReflectionUtils.getEntityFieldsSortedByName(entityKey.clazz());
        Object[] snapshots = entitiesSnapshots.get(entityKey);
        for (int i = 0; i < snapshots.length; i++) {
            if (!Objects.equals(snapshots[i], ReflectionUtils.getFieldValue(entity, fields[i]))) {
                return true;
            }
        }
        return false;
    }
}
