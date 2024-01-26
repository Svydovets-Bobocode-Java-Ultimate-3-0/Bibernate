package org.svydovets.session;

import org.svydovets.connection_pool.config.DataSourceConfig;
import org.svydovets.connection_pool.datasource.PooledDataSource;
import org.svydovets.dao.GenericJdbcDAO;
import org.svydovets.dao.Properties;
import org.svydovets.exception.InvalidParameterPropertiesException;

import java.io.FileInputStream;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Factory class for creating sessions for database operations.
 * This class handles the initialization of the necessary components for database connectivity,
 * including loading database properties and setting up a data source.
 */
public class SessionFactory {

    private static final String DB_CONFIG = "src/main/resources/application.properties";
    private final GenericJdbcDAO jdbcDAO;

    /**
     * Constructs a {@code SessionFactory} with the default database properties.
     */
    public SessionFactory() {
        this(getDBProperties());
    }

    /**
     * Constructs a {@code SessionFactory} using the provided database properties.
     *
     * @param properties The {@code Properties} object containing the database connection details.
     */
    public SessionFactory(Properties properties) {
        this.jdbcDAO = new GenericJdbcDAO(createPooledDataSource(properties));
    }

    /**
     * Creates a new session for database operations.
     *
     * @return A new {@code Session} instance for interacting with the database.
     */
    public Session createSession() {
        return new Session(jdbcDAO);
    }

    /**
     * Retrieves the database properties from a configuration file.
     *
     * <p>This method loads the database connection details from a file specified by
     * the {@code DB_CONFIG} constant. It expects the file to contain properties
     * named 'db.url', 'db.user', and 'db.password'. These properties are then used
     * to create and return a new {@code Properties} object.</p>
     *
     * @return A {@code Properties} object containing the database URL, username, and password.
     * @throws InvalidParameterPropertiesException if the configuration file cannot be read
     * or if required properties are missing.
     */
    private static Properties getDBProperties() {
        java.util.Properties properties = new java.util.Properties();

        try (FileInputStream fileInputStream = new FileInputStream(DB_CONFIG)) {
            properties.load(fileInputStream);

            String url = requireNonNull(properties.getProperty("db.url"));
            String user = requireNonNull(properties.getProperty("db.user"));
            String password = requireNonNull(properties.getProperty("db.password"));

            return new Properties(url, user, password);
        } catch (IOException e) {
            throw new InvalidParameterPropertiesException(e.getMessage(), e);
        }
    }

    /**
     * Creates a pooled data source using the provided database properties.
     *
     * <p>This method initializes a {@code PooledDataSource} object using the
     * database connection details provided in the {@code properties} parameter.
     * It sets up the data source configuration and returns an instance of
     * {@code PooledDataSource} that can be used for database connections.</p>
     *
     * @param properties The {@code Properties} object containing the database URL, username, and password.
     * @return An instance of {@code PooledDataSource} configured with the provided database properties.
     */
    private PooledDataSource createPooledDataSource(Properties properties) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig(properties.url(), properties.user(), properties.password());
        return new PooledDataSource(dataSourceConfig);
    }
}
