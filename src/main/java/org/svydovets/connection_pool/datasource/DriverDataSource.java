package org.svydovets.connection_pool.datasource;


import org.svydovets.connection_pool.config.DataSourceConfig;
import org.svydovets.connection_pool.exception.PooledDataSourceCreationException;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Logger;

/**
 * A {@code DataSource} implementation that delegates to an underlying JDBC {@code Driver}.
 * This class is used to create connections to a database using the provided {@code DataSourceConfig}.
 */
public class DriverDataSource implements DataSource {

    private final Driver delegateDriver;
    private final DataSourceConfig config;

    /**
     * Constructs a {@code DriverDataSource} using the provided {@code DataSourceConfig}.
     * Attempts to retrieve a {@code Driver} instance using the JDBC URL from the config.
     *
     * @param config The {@code DataSourceConfig} to use for obtaining database connection details.
     * @throws PooledDataSourceCreationException if unable to obtain a {@code Driver} instance.
     */
    public DriverDataSource(DataSourceConfig config) {
        try {
            this.delegateDriver = DriverManager.getDriver(config.getUrl());
            this.config = config;
        } catch (SQLException e) {
            throw new PooledDataSourceCreationException(String.format(
                    "Failed to get driver instance for jdbcUrl=%s", config.getUrl()),
                    e
            );
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegateDriver.connect(config.getUrl(), config.getProperties());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return delegateDriver.connect(config.getUrl(), DataSourceConfig.initProperties(username, password));
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegateDriver.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
