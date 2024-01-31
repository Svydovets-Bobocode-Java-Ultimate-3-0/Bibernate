package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.EntityEntry;
import org.svydovets.session.EntityKey;
import org.svydovets.session.actionQueue.action.PersistAction;
import org.svydovets.util.EntityReflectionUtils;

import java.lang.reflect.Field;

public class PersisActionExecutor extends EntityActionExecutor<PersistAction> {

    public PersisActionExecutor(GenericJdbcDAO jdbcDAO) {
        super(jdbcDAO);
    }

    @Override
    protected void execute(PersistAction persistAction) {
        Object entity = persistAction.entity();

        Object generatedId = this.jdbcDAO.saveToDB(entity);
        Field idField = EntityReflectionUtils.getIdField(entity.getClass());
        EntityReflectionUtils.setFieldValue(entity, idField, generatedId);

        EntityKey<?> entityKey = EntityKey.valueOf(entity, generatedId);
        persistAction.updateEntityEntry(EntityEntry.valueOf(entityKey, entity));
    }
}
