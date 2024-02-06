package org.svydovets.connectionPool.datasource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionHandlerTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;

    private ConnectionHandler connectionHandler;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        connectionHandler = new ConnectionHandler(dataSource);
    }

    @Test
    void shouldGetConnectionWhenNoTransactionActivated() throws SQLException {
        connectionHandler.getConnectionAttributes().setTransactionActivated(false);

        Connection retrievedConnection = connectionHandler.getConnection();

        verify(dataSource, times(1)).getConnection();
        assertNotNull(retrievedConnection);
    }

    @Test
    void shouldGetConnectionWithActiveTransactionForNewThread() throws SQLException {
        connectionHandler.getConnectionAttributes().setTransactionActivated(true);

        Connection firstRetrieval = connectionHandler.getConnection();

        verify(dataSource, times(1)).getConnection();
        assertNotNull(firstRetrieval);
    }

    @Test
    void shouldGetConnectionWithActiveTransactionForExistingThread() throws SQLException {
        connectionHandler.getConnectionAttributes().setTransactionActivated(true);
        Connection firstRetrieval = connectionHandler.getConnection();

        Connection secondRetrieval = connectionHandler.getConnection();

        verify(dataSource, times(1)).getConnection();
        assertSame(firstRetrieval, secondRetrieval);
    }

    @Test
    void shouldGetConnectionAttributesReturnsCorrectAttributes() {
        ConnectionAttributes attributes = connectionHandler.getConnectionAttributes();

        assertNotNull(attributes);
        assertFalse(attributes.isTransactionActivated());
    }
}
