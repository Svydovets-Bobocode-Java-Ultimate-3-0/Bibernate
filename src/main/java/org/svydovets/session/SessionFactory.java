package org.svydovets.session;

import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.dao.Properties;

public class SessionFactory {
    private final GenericJdbcDAO jdbcDAO;

    public SessionFactory(Properties properties) {
        this.jdbcDAO = new GenericJdbcDAO(properties);
    }

    public Session createSession() {
        return new Session(jdbcDAO);
    }
}
