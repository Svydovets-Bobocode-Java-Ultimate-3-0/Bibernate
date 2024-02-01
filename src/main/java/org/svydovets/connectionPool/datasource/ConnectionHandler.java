package org.svydovets.connectionPool.datasource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;

public class ConnectionHandler {

    private final DataSource dataSource;
    private final ConnectionAttributes connectionAttributes = new ConnectionAttributes();
    private final Map<String, Connection> connections;

    public ConnectionHandler(DataSource dataSource) {
        this.dataSource = dataSource;
        connections = new WeakHashMap<>();
    }

    public Connection getConnection() throws SQLException {
        if (connectionAttributes.isTransactionActivated()) {
            String threadName = Thread.currentThread().getName();

            Connection connection = connections.get(threadName);

            if (connection == null) {
                connection = dataSource.getConnection();
                connections.put(threadName, connection);
            }
            return connection;
        } else {
            return dataSource.getConnection();
        }
    }

    public ConnectionAttributes getConnectionAttributes() {
        return connectionAttributes;
    }
}
