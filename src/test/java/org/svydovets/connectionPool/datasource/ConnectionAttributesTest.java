package org.svydovets.connectionPool.datasource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionAttributesTest {

    private ConnectionAttributes connectionAttributes;

    @BeforeEach
    void setUp() {
        connectionAttributes = new ConnectionAttributes();
    }

    @Test
    void isTransactionActivatedDefaultsToFalse() {
        assertFalse(connectionAttributes.isTransactionActivated());
    }

    @Test
    void setTransactionActivatedChangesStateToTrue() {
        connectionAttributes.setTransactionActivated(true);

        assertTrue(connectionAttributes.isTransactionActivated());
    }

    @Test
    void setTransactionActivatedChangesStateToFalse() {
        connectionAttributes.setTransactionActivated(true);
        connectionAttributes.setTransactionActivated(false);

        assertFalse(connectionAttributes.isTransactionActivated());
    }
}

