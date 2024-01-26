package org.svydovets.connectionPool.config;

import java.util.Properties;

import static java.util.Objects.requireNonNull;

/**
 * Configuration class for setting up a data source.
 * This class holds the configuration details necessary for establishing database connections.
 */
public class DataSourceConfig {

    private final String url;
    private final String username;
    private final String password;
    private final Properties properties;

    /**
     * Constructs a new DataSourceConfig with the specified URL, username, and password.
     *
     * @param url      The URL of the database.
     * @param username The username for the database login.
     * @param password The password for the database login.
     * @throws NullPointerException if any of the parameters are null.
     */
    public DataSourceConfig(String url, String username, String password) {
        this.url = requireNonNull(url);
        this.username = requireNonNull(username);
        this.password = requireNonNull(password);
        this.properties = initProperties(username, password);
    }

    /**
     * Initializes the properties for database connection with the provided username and password.
     *
     * @param username The database username.
     * @param password The database password.
     * @return A {@link Properties} object containing the database connection properties.
     */
    public static Properties initProperties(String username, String password) {
        Properties properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        return properties;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Properties getProperties() {
        return properties;
    }
}
