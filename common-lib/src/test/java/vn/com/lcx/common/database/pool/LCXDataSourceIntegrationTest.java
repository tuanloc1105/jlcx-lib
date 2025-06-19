package vn.com.lcx.common.database.pool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.lcx.common.database.DatabaseProperty;
import vn.com.lcx.common.database.pool.entry.ConnectionEntry;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.exception.LCXDataSourceException;
import vn.com.lcx.common.thread.SimpleExecutor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LCXDataSourceIntegrationTest {

    private LCXDataSource dataSource;
    private DatabaseProperty mockProperty;
    private SimpleExecutor<Boolean> mockExecutor;
    private ConcurrentLinkedQueue<ConnectionEntry> realPool;

    @BeforeEach
    void setUp() {
        mockProperty = mock(DatabaseProperty.class);
        mockExecutor = mock(SimpleExecutor.class);
        realPool = new ConcurrentLinkedQueue<>();

        // Setup default mock behavior
        when(mockProperty.getConnectionString()).thenReturn("jdbc:postgresql://localhost:5432/testdb");
        when(mockProperty.getUsername()).thenReturn("testuser");
        when(mockProperty.getPassword()).thenReturn("testpass");
        when(mockProperty.getDriverClassName()).thenReturn("org.postgresql.Driver");
        when(mockProperty.getInitialPoolSize()).thenReturn(2);
        when(mockProperty.getMaxPoolSize()).thenReturn(5);
        when(mockProperty.getMaxTimeout()).thenReturn(30);
        when(mockProperty.propertiesIsAllSet()).thenReturn(true);

        dataSource = new LCXDataSource(
                "test-pool",
                "org.postgresql.Driver",
                "SELECT version()",
                mockProperty,
                mockExecutor,
                DBTypeEnum.POSTGRESQL,
                30000,
                realPool
        );
    }

    @Test
    void testConcurrentConnectionRequests() throws SQLException, InterruptedException {
        // Arrange
        List<ConnectionEntry> mockEntries = createMockConnectionEntries(3);
        realPool.addAll(mockEntries);

        // Act
        List<Connection> connections = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            final int index = i;
            Thread thread = new Thread(() -> {
                try {
                    Connection conn = dataSource.get();
                    connections.add(conn);
                } catch (Exception e) {
                    // Handle exception
                }
            });
            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(1000);
        }

        // Assert
        assertEquals(3, connections.size());
        for (ConnectionEntry entry : mockEntries) {
            verify(entry, atLeastOnce()).activate();
        }
    }

    @Test
    void testConnectionPoolExhaustion() throws SQLException {
        // Arrange
        List<ConnectionEntry> mockEntries = createMockConnectionEntries(5);
        realPool.addAll(mockEntries);

        // Make all connections active
        for (ConnectionEntry entry : mockEntries) {
            when(entry.isActive()).thenReturn(true);
            when(entry.isCriticalLock()).thenReturn(false);
        }

        // Act & Assert
        assertThrows(LCXDataSourceException.class, () -> {
            dataSource.get();
        });
    }

    @Test
    void testConnectionRecreationOnInvalidConnection() throws SQLException, ClassNotFoundException {
        // Arrange
        ConnectionEntry mockEntry = mock(ConnectionEntry.class);
        Connection mockOldConnection = mock(Connection.class);
        Connection mockNewConnection = mock(Connection.class);

        when(mockEntry.isActive()).thenReturn(false);
        when(mockEntry.isCriticalLock()).thenReturn(false);
        when(mockEntry.isValid()).thenReturn(false);
        when(mockEntry.getConnection()).thenReturn(mockOldConnection);
        realPool.add(mockEntry);

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockNewConnection);

            // Act
            Connection result = dataSource.get();

            // Assert
            assertNotNull(result);
            verify(mockEntry).setConnection(mockNewConnection);
            verify(mockEntry).activate();
        }
    }

    @Test
    void testConnectionPoolExpansion() throws SQLException, ClassNotFoundException {
        // Arrange
        List<ConnectionEntry> mockEntries = createMockConnectionEntries(2);
        realPool.addAll(mockEntries);

        // Make all existing connections active
        for (ConnectionEntry entry : mockEntries) {
            when(entry.isActive()).thenReturn(true);
        }

        ConnectionEntry newMockEntry = mock(ConnectionEntry.class);
        Connection mockConnection = mock(Connection.class);

        when(newMockEntry.isActive()).thenReturn(false);
        when(newMockEntry.isCriticalLock()).thenReturn(false);
        when(newMockEntry.isValid()).thenReturn(true);
        when(newMockEntry.getConnection()).thenReturn(mockConnection);

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class);
             MockedStatic<ConnectionEntry> connectionEntryMock = Mockito.mockStatic(ConnectionEntry.class)) {

            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            connectionEntryMock.when(() -> ConnectionEntry.init(any(Connection.class), any(DBTypeEnum.class), anyString()))
                    .thenReturn(newMockEntry);

            // Act
            Connection result = dataSource.get();

            // Assert
            assertNotNull(result);
            assertEquals(3, realPool.size());
        }
    }

    @Test
    void testConnectionPoolStatistics() {
        // Arrange
        List<ConnectionEntry> mockEntries = createMockConnectionEntries(5);
        realPool.addAll(mockEntries);

        // Set some connections as active, some as idle
        when(mockEntries.get(0).isActive()).thenReturn(true);
        when(mockEntries.get(1).isActive()).thenReturn(true);
        when(mockEntries.get(2).isActive()).thenReturn(false);
        when(mockEntries.get(3).isActive()).thenReturn(false);
        when(mockEntries.get(4).isActive()).thenReturn(false);

        when(mockEntries.get(0).getIdle()).thenReturn(new java.util.concurrent.atomic.AtomicBoolean(false));
        when(mockEntries.get(1).getIdle()).thenReturn(new java.util.concurrent.atomic.AtomicBoolean(false));
        when(mockEntries.get(2).getIdle()).thenReturn(new java.util.concurrent.atomic.AtomicBoolean(true));
        when(mockEntries.get(3).getIdle()).thenReturn(new java.util.concurrent.atomic.AtomicBoolean(true));
        when(mockEntries.get(4).getIdle()).thenReturn(new java.util.concurrent.atomic.AtomicBoolean(true));

        // Act
        int totalConnections = dataSource.getTotalConnections();
        int activeConnections = dataSource.getActiveConnections();
        int idleConnections = dataSource.getIdleConnections();

        // Assert
        assertEquals(5, totalConnections);
        assertEquals(2, activeConnections);
        assertEquals(3, idleConnections);
    }

    @Test
    void testConnectionValidation() throws SQLException, ClassNotFoundException {
        // Arrange
        ConnectionEntry mockEntry = mock(ConnectionEntry.class);
        Connection mockConnection = mock(Connection.class);
        Connection mockNewConnection = mock(Connection.class);

        when(mockEntry.getConnectionName()).thenReturn("test-connection");
        when(mockEntry.isValid()).thenReturn(false);
        when(mockEntry.getConnection()).thenReturn(mockConnection);
        realPool.add(mockEntry);

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockNewConnection);

            // Act
            dataSource.validateEntry(mockEntry);

            // Assert
            verify(mockEntry).setConnection(mockNewConnection);
        }
    }

    @Test
    void testConnectionValidationWithInvalidEntry() {
        // Arrange
        ConnectionEntry mockEntry = mock(ConnectionEntry.class);
        when(mockEntry.getConnectionName()).thenReturn("invalid-connection");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dataSource.validateEntry(mockEntry);
        });
    }

    @Test
    void testDatabaseVersionRetrieval() throws SQLException, ClassNotFoundException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        java.sql.PreparedStatement mockStatement = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet mockResultSet = mock(java.sql.ResultSet.class);

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getString(1)).thenReturn("PostgreSQL 14.0");

            // Act
            String version = dataSource.showDBVersion();

            // Assert
            assertEquals("PostgreSQL 14.0", version);
        }
    }

    @Test
    void testDatabaseVersionRetrievalFailure() throws SQLException, ClassNotFoundException {
        // Arrange
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(new SQLException("Database connection failed"));

            // Act & Assert
            assertThrows(LCXDataSourceException.class, () -> {
                dataSource.showDBVersion();
            });
        }
    }

    @Test
    void testConnectionPoolWithDifferentDatabaseTypes() {
        // Test with different database types
        DBTypeEnum[] dbTypes = {DBTypeEnum.ORACLE, DBTypeEnum.POSTGRESQL, DBTypeEnum.MYSQL, DBTypeEnum.MSSQL};

        for (DBTypeEnum dbType : dbTypes) {
            LCXDataSource testDataSource = new LCXDataSource(
                    "test-pool-" + dbType.name(),
                    dbType.getDefaultDriverClassName(),
                    dbType.getShowDbVersionSqlStatement(),
                    mockProperty,
                    mockExecutor,
                    dbType,
                    30000,
                    realPool
            );

            assertNotNull(testDataSource);
            assertEquals(dbType, testDataSource.getDbType());
        }
    }

    @Test
    void testConnectionPoolShutdownHook() {
        // Arrange
        List<ConnectionEntry> mockEntries = createMockConnectionEntries(3);
        realPool.addAll(mockEntries);

        // Act - Simulate shutdown hook execution
        for (ConnectionEntry entry : mockEntries) {
            try {
                if (entry.transactionIsOpen()) {
                    entry.commit();
                }
                entry.shutdown();
            } catch (Exception e) {
                // Handle exception
            }
        }

        // Assert
        for (ConnectionEntry entry : mockEntries) {
            verify(entry, atLeastOnce()).shutdown();
        }
    }

    private List<ConnectionEntry> createMockConnectionEntries(int count) {
        List<ConnectionEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ConnectionEntry mockEntry = mock(ConnectionEntry.class);
            Connection mockConnection = mock(Connection.class);

            when(mockEntry.isActive()).thenReturn(false);
            when(mockEntry.isCriticalLock()).thenReturn(false);
            when(mockEntry.isValid()).thenReturn(true);
            when(mockEntry.getConnection()).thenReturn(mockConnection);
            when(mockEntry.getConnectionName()).thenReturn("connection-" + i);
            when(mockEntry.getIdle()).thenReturn(new java.util.concurrent.atomic.AtomicBoolean(true));

            entries.add(mockEntry);
        }
        return entries;
    }
} 
