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
import vn.com.lcx.common.exception.LCXDataSourcePropertiesException;
import vn.com.lcx.common.thread.RejectMode;
import vn.com.lcx.common.thread.SimpleExecutor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LCXDataSourceTest {

    private LCXDataSource dataSource;
    private DatabaseProperty mockProperty;
    private SimpleExecutor<Boolean> mockExecutor;
    private ConcurrentLinkedQueue<ConnectionEntry> mockPool;

    @BeforeEach
    void setUp() {
        mockProperty = mock(DatabaseProperty.class);
        mockExecutor = mock(SimpleExecutor.class);
        mockPool = mock(ConcurrentLinkedQueue.class);

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
                mockPool
        );
    }

    @Test
    void testInit_WithValidParameters_ShouldCreateDataSource() {
        // Arrange
        String host = "localhost";
        int port = 5432;
        String username = "testuser";
        String password = "testpass";
        String dbName = "testdb";
        String driverClass = "org.postgresql.Driver";
        int initialPoolSize = 2;
        int maxPoolSize = 5;
        int maxTimeout = 30;
        DBTypeEnum dbType = DBTypeEnum.POSTGRESQL;

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class);
             MockedStatic<SimpleExecutor> executorMock = Mockito.mockStatic(SimpleExecutor.class)) {

            Connection mockConnection = mock(Connection.class);
            SimpleExecutor<Boolean> mockSimpleExecutor = mock(SimpleExecutor.class);

            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            executorMock.when(() -> SimpleExecutor.init(anyInt(), anyInt(), any(RejectMode.class), anyInt(), any(TimeUnit.class)))
                    .thenReturn(mockSimpleExecutor);

            // Act
            LCXDataSource result = LCXDataSource.init(host, port, username, password, dbName, driverClass,
                    initialPoolSize, maxPoolSize, maxTimeout, dbType);

            // Assert
            assertNotNull(result);
            assertEquals(dbName, result.getPoolName());
            assertEquals(dbType, result.getDbType());
        }
    }

    @Test
    void testInit_WithInvalidProperties_ShouldThrowException() {
        // Arrange
        when(mockProperty.propertiesIsAllSet()).thenReturn(false);

        // Act & Assert
        assertThrows(LCXDataSourcePropertiesException.class, () -> {
            LCXDataSource.init("localhost", 5432, "user", "pass", "db", "driver", 2, 5, 30, DBTypeEnum.POSTGRESQL);
        });
    }

    @Test
    void testGet_WithIdleConnection_ShouldReturnConnection() throws SQLException {
        // Arrange
        ConnectionEntry mockEntry = mock(ConnectionEntry.class);
        Connection mockConnection = mock(Connection.class);

        when(mockEntry.isActive()).thenReturn(false);
        when(mockEntry.isCriticalLock()).thenReturn(false);
        when(mockEntry.isValid()).thenReturn(true);
        when(mockEntry.getConnection()).thenReturn(mockConnection);
        when(mockPool.iterator()).thenReturn(java.util.Arrays.asList(mockEntry).iterator());

        // Act
        Connection result = dataSource.get();

        // Assert
        assertNotNull(result);
        verify(mockEntry).activate();
    }

    @Test
    void testGet_WithInvalidConnection_ShouldRecreateConnection() throws SQLException, ClassNotFoundException {
        // Arrange
        ConnectionEntry mockEntry = mock(ConnectionEntry.class);
        Connection mockOldConnection = mock(Connection.class);
        Connection mockNewConnection = mock(Connection.class);

        when(mockEntry.isActive()).thenReturn(false);
        when(mockEntry.isCriticalLock()).thenReturn(false);
        when(mockEntry.isValid()).thenReturn(false);
        when(mockEntry.getConnection()).thenReturn(mockOldConnection);
        when(mockPool.iterator()).thenReturn(java.util.Arrays.asList(mockEntry).iterator());

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockNewConnection);

            // Act
            Connection result = dataSource.get();

            // Assert
            assertNotNull(result);
            verify(mockEntry).setConnection(mockNewConnection);
        }
    }

    @Test
    void testGet_WithPoolNotFull_ShouldCreateNewConnection() throws SQLException, ClassNotFoundException {
        // Arrange
        ConnectionEntry mockEntry = mock(ConnectionEntry.class);
        Connection mockConnection = mock(Connection.class);

        when(mockEntry.isActive()).thenReturn(true);
        when(mockPool.size()).thenReturn(1);
        when(mockProperty.getMaxPoolSize()).thenReturn(5);
        when(mockPool.iterator()).thenReturn(java.util.Arrays.asList(mockEntry).iterator());

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class);
             MockedStatic<ConnectionEntry> connectionEntryMock = Mockito.mockStatic(ConnectionEntry.class)) {

            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            connectionEntryMock.when(() -> ConnectionEntry.init(any(Connection.class), any(DBTypeEnum.class), anyString()))
                    .thenReturn(mockEntry);

            // Act
            Connection result = dataSource.get();

            // Assert
            assertNotNull(result);
            verify(mockPool).add(mockEntry);
        }
    }

    @Test
    void testGet_WithFullPool_ShouldWaitForAvailableConnection() throws SQLException {
        // Arrange
        ConnectionEntry mockEntry = mock(ConnectionEntry.class);
        Connection mockConnection = mock(Connection.class);

        when(mockEntry.isActive()).thenReturn(true);
        when(mockPool.size()).thenReturn(5);
        when(mockProperty.getMaxPoolSize()).thenReturn(5);
        when(mockPool.iterator()).thenReturn(java.util.Arrays.asList(mockEntry).iterator());
        when(mockPool.stream()).thenReturn(java.util.stream.Stream.of(mockEntry));
        when(mockEntry.getLastActiveTime()).thenReturn(java.time.LocalDateTime.now().minusSeconds(35));
        when(mockEntry.isCriticalLock()).thenReturn(false);

        // Act & Assert
        assertThrows(LCXDataSourceException.class, () -> {
            dataSource.get();
        });
    }

    @Test
    void testShowDBVersion_ShouldReturnVersion() throws SQLException, ClassNotFoundException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getString(1)).thenReturn("PostgreSQL 13.0");

            // Act
            String result = dataSource.showDBVersion();

            // Assert
            assertEquals("PostgreSQL 13.0", result);
        }
    }

    @Test
    void testShowDBVersion_WithException_ShouldThrowException() throws SQLException, ClassNotFoundException {
        // Arrange
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(new SQLException("Connection failed"));

            // Act & Assert
            assertThrows(LCXDataSourceException.class, () -> {
                dataSource.showDBVersion();
            });
        }
    }

    @Test
    void testGetTotalConnections() {
        // Arrange
        when(mockPool.size()).thenReturn(5);

        // Act
        int result = dataSource.getTotalConnections();

        // Assert
        assertEquals(5, result);
    }

    @Test
    void testGetActiveConnections() {
        // Arrange
        ConnectionEntry mockEntry1 = mock(ConnectionEntry.class);
        ConnectionEntry mockEntry2 = mock(ConnectionEntry.class);

        when(mockEntry1.isActive()).thenReturn(true);
        when(mockEntry2.isActive()).thenReturn(false);
        when(mockPool.stream()).thenReturn(java.util.stream.Stream.of(mockEntry1, mockEntry2));

        // Act
        int result = dataSource.getActiveConnections();

        // Assert
        assertEquals(1, result);
    }

    @Test
    void testGetIdleConnections() {
        // Arrange
        ConnectionEntry mockEntry1 = mock(ConnectionEntry.class);
        ConnectionEntry mockEntry2 = mock(ConnectionEntry.class);

        when(mockEntry1.getIdle()).thenReturn(new java.util.concurrent.atomic.AtomicBoolean(true));
        when(mockEntry2.getIdle()).thenReturn(new java.util.concurrent.atomic.AtomicBoolean(false));
        when(mockPool.stream()).thenReturn(java.util.stream.Stream.of(mockEntry1, mockEntry2));

        // Act
        int result = dataSource.getIdleConnections();

        // Assert
        assertEquals(1, result);
    }

    @Test
    void testValidateEntry_WithValidEntry_ShouldNotThrowException() throws SQLException, ClassNotFoundException {
        // Arrange
        ConnectionEntry mockEntry = mock(ConnectionEntry.class);
        Connection mockConnection = mock(Connection.class);

        when(mockEntry.getConnectionName()).thenReturn("test-connection");
        when(mockEntry.isValid()).thenReturn(true);
        when(mockPool.stream()).thenReturn(java.util.stream.Stream.of(mockEntry));

        // Act & Assert
        assertDoesNotThrow(() -> dataSource.validateEntry(mockEntry));
    }

    @Test
    void testValidateEntry_WithInvalidEntry_ShouldRecreateConnection() throws SQLException, ClassNotFoundException {
        // Arrange
        ConnectionEntry mockEntry = mock(ConnectionEntry.class);
        Connection mockOldConnection = mock(Connection.class);
        Connection mockNewConnection = mock(Connection.class);

        when(mockEntry.getConnectionName()).thenReturn("test-connection");
        when(mockEntry.isValid()).thenReturn(false);
        when(mockEntry.getConnection()).thenReturn(mockOldConnection);
        when(mockPool.stream()).thenReturn(java.util.stream.Stream.of(mockEntry));

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
    void testValidateEntry_WithInvalidConnectionName_ShouldThrowException() {
        // Arrange
        ConnectionEntry mockEntry = mock(ConnectionEntry.class);
        when(mockEntry.getConnectionName()).thenReturn("invalid-connection");
        when(mockPool.stream()).thenReturn(java.util.stream.Stream.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dataSource.validateEntry(mockEntry);
        });
    }

    @Test
    void testGetConnection_DeprecatedMethod_ShouldWork() throws SQLException {
        // Arrange
        ConnectionEntry mockEntry = mock(ConnectionEntry.class);
        Connection mockConnection = mock(Connection.class);

        when(mockEntry.isActive()).thenReturn(false);
        when(mockEntry.isCriticalLock()).thenReturn(false);
        when(mockEntry.isValid()).thenReturn(true);
        when(mockEntry.getConnection()).thenReturn(mockConnection);
        when(mockPool.iterator()).thenReturn(java.util.Arrays.asList(mockEntry).iterator());

        // Act
        ConnectionEntry result = dataSource.getConnection();

        // Assert
        assertNotNull(result);
        verify(mockEntry).activate();
    }

    @Test
    void testCreateConnection_WithValidParameters_ShouldReturnConnection() throws SQLException, ClassNotFoundException {
        // Arrange
        String url = "jdbc:postgresql://localhost:5432/testdb";
        String user = "testuser";
        String password = "testpass";
        int timeout = 30;

        Connection mockConnection = mock(Connection.class);

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(url, user, password))
                    .thenReturn(mockConnection);

            // Act
            Connection result = LCXDataSource.createConnection(url, user, password, timeout);

            // Assert
            assertNotNull(result);
            assertEquals(mockConnection, result);
        }
    }

    @Test
    void testCreateConnection_WithException_ShouldThrowException() throws SQLException, ClassNotFoundException {
        // Arrange
        String url = "jdbc:postgresql://localhost:5432/testdb";
        String user = "testuser";
        String password = "testpass";
        int timeout = 30;

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(url, user, password))
                    .thenThrow(new SQLException("Connection failed"));

            // Act & Assert
            assertThrows(SQLException.class, () -> {
                LCXDataSource.createConnection(url, user, password, timeout);
            });
        }
    }
} 
