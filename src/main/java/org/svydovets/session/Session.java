package org.svydovets.session;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Session {

    private final GenericJdbcDAO jdbcDAO;
    private final Map<EntityKey<?>, Object> entitiesCache;
    private final Map<EntityKey<?>, Object[]> entitiesSnapshots;

    public Session(GenericJdbcDAO jdbcDAO) {
        this.jdbcDAO = jdbcDAO;
        this.entitiesCache = new HashMap<>();
        this.entitiesSnapshots = new HashMap<>();
    }

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
            snapshots[i] = getFieldValue(entity, fields[i]);
        }
        entitiesSnapshots.put(entityKey, snapshots);
    }


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

            Object snapshotValue = snapshots[i];
            Object fieldValue = getFieldValue(entity, fields[i]);

            if (snapshotValue == null && fieldValue == null){
                return false;
            }  else if((snapshotValue != null && fieldValue == null) || (snapshotValue == null && fieldValue != null)){
                return true;
            } else if (!snapshotValue.equals(fieldValue)) {
                return true;
            }
        }
        return false;
    }

    private Object getFieldValue(Object entity, Field fields) {
        try {
            fields.setAccessible(true);
            return fields.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error creating snapshots for entity: " + entity, e);
        }
    }
}
