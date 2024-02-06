package org.svydovets.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.svydovets.connectionPool.datasource.ConnectionAttributes;
import org.svydovets.connectionPool.datasource.ConnectionHandler;
import org.svydovets.session.actionQueue.executor.ActionQueue;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionManagerImplTest {

    @Mock
    private ConnectionHandler connectionHandler;
    @Mock
    private ActionQueue actionQueue;
    @Mock
    private Connection connection;
    @Mock
    private ConnectionAttributes connectionAttributes;
    private TransactionManagerImpl transactionManager;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(connectionHandler.getConnection()).thenReturn(connection);
        when(connectionHandler.getConnectionAttributes()).thenReturn(connectionAttributes);
        transactionManager = new TransactionManagerImpl(connectionHandler, actionQueue);
    }

    @Test
    void shouldBeginTransactionWhenNoneIsActive() throws SQLException {
        transactionManager.begin();
        verify(connectionHandler, times(1)).getConnectionAttributes();
        verify(connectionAttributes, times(1)).setTransactionActivated(true);
        verify(connection, times(1)).setAutoCommit(false);
        assertTrue(transactionManager.isActive());
    }

    @Test
    void shouldThrowExceptionWhenBeginningTransactionIfAlreadyActive() {
        transactionManager.begin();
        Exception exception = assertThrows(TransactionException.class, () -> transactionManager.begin());
        assertEquals("Transaction was started", exception.getMessage());
    }

    @Test
    void shouldCommitTransactionWhenActive() throws SQLException {
        transactionManager.begin();
        transactionManager.commit();

        verify(actionQueue, times(1)).performAccumulatedActions();
        verify(connection, times(1)).commit();
        assertFalse(transactionManager.isActive());
    }

    @Test
    void shouldThrowExceptionWhenTransactionIsNotActive() {
        Exception exception = assertThrows(TransactionException.class, transactionManager::commit);
        assertEquals("Transaction is not started", exception.getMessage());
    }

    @Test
    void shouldRollbackTransactionWhenActive() throws SQLException {
        transactionManager.begin();
        transactionManager.callback();

        verify(connection, times(1)).rollback();
        assertFalse(transactionManager.isActive());
    }

    @Test
    void shouldThrowExceptionInRollbackWhenTransactionIsNotActive() {
        Exception exception = assertThrows(TransactionException.class, transactionManager::callback);
        assertEquals("Transaction is not started", exception.getMessage());
    }

    @Test
    void shouldIsActiveReflectsCurrentTransactionState() {
        assertFalse(transactionManager.isActive());
        transactionManager.begin();
        assertTrue(transactionManager.isActive());
        transactionManager.commit();
        assertFalse(transactionManager.isActive());
    }
}

