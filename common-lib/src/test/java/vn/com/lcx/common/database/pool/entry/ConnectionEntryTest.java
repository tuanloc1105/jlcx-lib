package vn.com.lcx.common.database.pool.entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.lcx.common.database.type.DBTypeEnum;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConnectionEntryTest {
    Connection mockConn;
    ConnectionEntry entry;

    @BeforeEach
    void setUp() {
        mockConn = mock(Connection.class);
        entry = ConnectionEntry.init(mockConn, DBTypeEnum.ORACLE, "test-conn");
    }

    @Test
    void testActivateAndDeactivate() {
        assertTrue(entry.getIdle().get());
        entry.activate();
        assertFalse(entry.getIdle().get());
        entry.deactivate();
        assertTrue(entry.getIdle().get());
    }

    @Test
    void testActivateWhenNotIdleThrows() {
        entry.activate();
        assertThrows(RuntimeException.class, entry::activate);
    }

    @Test
    void testDeactivateWhenIdleThrows() {
        assertThrows(RuntimeException.class, entry::deactivate);
    }

    @Test
    void testTransactionIsOpen() throws SQLException {
        when(mockConn.getAutoCommit()).thenReturn(false);
        assertTrue(entry.transactionIsOpen());
        when(mockConn.getAutoCommit()).thenReturn(true);
        assertFalse(entry.transactionIsOpen());
    }

    @Test
    void testOpenTransaction() throws SQLException {
        when(mockConn.getAutoCommit()).thenReturn(true);
        entry.openTransaction();
        verify(mockConn).setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        verify(mockConn).setAutoCommit(false);
    }

    @Test
    void testOpenTransactionWithIsolation() throws SQLException {
        when(mockConn.getAutoCommit()).thenReturn(true);
        entry.openTransaction(ConnectionEntry.TransactionIsolation.TRANSACTION_SERIALIZABLE);
        verify(mockConn).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        verify(mockConn).setAutoCommit(false);
    }

    @Test
    void testCommit() throws SQLException {
        when(mockConn.getAutoCommit()).thenReturn(false);
        entry.commit();
        verify(mockConn).commit();
        verify(mockConn).setAutoCommit(true);
    }

    @Test
    void testCommitNoClose() throws SQLException {
        when(mockConn.getAutoCommit()).thenReturn(false);
        entry.commitNoClose();
        verify(mockConn).commit();
        verify(mockConn, never()).setAutoCommit(true);
    }

    @Test
    void testRollback() throws SQLException {
        when(mockConn.getAutoCommit()).thenReturn(false);
        entry.rollback();
        verify(mockConn).rollback();
        verify(mockConn).setAutoCommit(true);
    }

    @Test
    void testIsValid() throws SQLException {
        when(mockConn.isValid(anyInt())).thenReturn(true);
        assertTrue(entry.isValid());
        when(mockConn.isValid(anyInt())).thenReturn(false);
        assertFalse(entry.isValid());
    }

    @Test
    void testShutdown() throws SQLException {
        when(mockConn.isClosed()).thenReturn(false);
        when(mockConn.isValid(anyInt())).thenReturn(true);
        entry.shutdown();
        verify(mockConn).close();
        assertNull(entry.getConnection());
    }

    @Test
    void testCloseCallsDeactivate() {
        entry.activate();
        entry.close();
        assertTrue(entry.getIdle().get());
    }
} 
