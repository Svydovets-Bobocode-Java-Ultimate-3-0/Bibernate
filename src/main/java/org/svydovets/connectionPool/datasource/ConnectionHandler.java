package org.svydovets.connectionPool.datasource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The {@code ConnectionHandler} class is responsible for managing database connections.
 * It handles the retrieval of connections based on the current transaction state and manages a cache of connections
 * associated with different threads.
 */
public class ConnectionHandler {

    private final DataSource dataSource;
    private final ConnectionAttributes connectionAttributes = new ConnectionAttributes();
    private final Map<String, Connection> connections;

    /**
     * Constructs a new {@code ConnectionHandler} with the given {@link DataSource}.
     * It initializes an internal map to keep track of connections per thread.
     *
     * @param dataSource the data source from which connections will be obtained.
     */
    public ConnectionHandler(DataSource dataSource) {
        this.dataSource = dataSource;
        connections = new WeakHashMap<>();
    }

    /**
     * Retrieves a {@link Connection} from the data source. If a transaction is currently activated,
     * it returns a connection associated with the current thread. Otherwise, it fetches a new connection
     * from the data source.
     *
     * @return A {@link Connection} object for database operations.
     * @throws SQLException if a database access error occurs or the data source is closed.
     */
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

    /**
     * Retrieves the {@link ConnectionAttributes} object that represents the attributes of the connection,
     * including the transaction activation status.
     *
     * @return The {@link ConnectionAttributes} object associated with this handler.
     */
    public ConnectionAttributes getConnectionAttributes() {
        return connectionAttributes;
    }
}
