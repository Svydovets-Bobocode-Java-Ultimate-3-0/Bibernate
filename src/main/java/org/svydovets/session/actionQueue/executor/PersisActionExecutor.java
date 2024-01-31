package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.actionQueue.action.PersistAction;
import org.svydovets.util.EntityReflectionUtils;

import java.lang.reflect.Field;

public class PersisActionExecutor extends EntityActionExecutor<PersistAction> {

    public PersisActionExecutor(GenericJdbcDAO jdbcDAO) {
        super(jdbcDAO);
    }

    @Override
    protected void execute(PersistAction persistAction) {
        System.out.println("PersistExecutor start processing persist action");
        Object entity = persistAction.entity();

        Object generatedId = this.jdbcDAO.saveToDB(entity);
        Field idField = EntityReflectionUtils.getIdField(entity.getClass());
        EntityReflectionUtils.setFieldValue(entity, idField, generatedId);

        persistAction.setGeneratedId(generatedId);
        System.out.println("Generated id: " + generatedId.toString());
    }
}
