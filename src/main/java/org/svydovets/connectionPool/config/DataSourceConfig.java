package org.svydovets.connectionPool.config;

import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class DataSourceConfig {

    private final String url;
    private final String username;
    private final String password;
    private final Properties properties;

    public DataSourceConfig(String url, String username, String password) {
        this.url = requireNonNull(url);
        this.username = requireNonNull(username);
        this.password = requireNonNull(password);
        this.properties = initProperties(username, password);
    }

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
