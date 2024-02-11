package org.svydovets.session.actionQueue.executor;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.session.EntityEntry;
import org.svydovets.session.EntityKey;
import org.svydovets.session.actionQueue.action.PersistAction;
import org.svydovets.util.EntityReflectionUtils;

import java.lang.reflect.Field;

/**
 * Executes {@link PersistAction}s by saving entities to the database. This class extends
 * {@link EntityActionExecutor} to provide functionality specifically for handling
 * persistence actions. It uses the provided {@link GenericJdbcDAO} for database access
 * to persist entities and manage their generated identifiers.
 */
public class PersisActionExecutor extends EntityActionExecutor<PersistAction> {

    /**
     * Constructs a {@code PersisActionExecutor} with the specified {@link GenericJdbcDAO}.
     *
     * @param jdbcDAO The {@code GenericJdbcDAO} instance to be used for persisting entities.
     */
    public PersisActionExecutor(GenericJdbcDAO jdbcDAO) {
        super(jdbcDAO);
    }

    /**
     * Executes a given {@link PersistAction} by persisting the encapsulated entity into the database.
     * After saving the entity, this method also updates the entity's identifier field with the generated
     * identifier from the database. Finally, it updates the {@code PersistAction}'s entity entry
     * with the new entity state.
     *
     * @param persistAction The {@code PersistAction} to be executed, encapsulating the entity to be persisted.
     */
    @Override
    protected void execute(PersistAction persistAction) {
        Object entity = persistAction.entity();

        Object generatedId = super.jdbcDAO.saveToDB(entity);
        Field idField = EntityReflectionUtils.getIdField(entity.getClass());
        EntityReflectionUtils.setFieldValue(entity, idField, generatedId);

        EntityKey<?> entityKey = EntityKey.valueOf(entity, generatedId);
        persistAction.updateEntityEntry(EntityEntry.valueOf(entityKey, entity));
    }
}
