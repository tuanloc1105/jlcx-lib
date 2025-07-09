package vn.com.lcx.common.database.pool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.lcx.common.database.DatabaseProperty;
import vn.com.lcx.common.database.pool.entry.ConnectionEntry;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.thread.SimpleExecutor;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LCXDataSourceTest {
    LCXDataSource dataSource;
    Connection mockConnection1;
    Connection mockConnection2;
    ConnectionEntry entry1;
    ConnectionEntry entry2;
    ConcurrentLinkedQueue<ConnectionEntry> mockPool;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Mock các Connection và ConnectionEntry
        mockConnection1 = mock(Connection.class);
        mockConnection2 = mock(Connection.class);
        when(mockConnection1.isValid(anyInt())).thenReturn(true);
        when(mockConnection2.isValid(anyInt())).thenReturn(true);
        when(mockConnection1.getAutoCommit()).thenReturn(true);
        when(mockConnection2.getAutoCommit()).thenReturn(true);

        // Giả sử DBTypeEnum.ORACLE tồn tại
        entry1 = ConnectionEntry.init(mockConnection1, DBTypeEnum.ORACLE, "conn-1");
        entry2 = ConnectionEntry.init(mockConnection2, DBTypeEnum.ORACLE, "conn-2");
        DatabaseProperty mockProperty = mock(DatabaseProperty.class);
        when(mockProperty.getConnectionString()).thenReturn("jdbc:h2:mem:testdb");
        when(mockProperty.getUsername()).thenReturn("sa");
        when(mockProperty.getPassword()).thenReturn("");
        when(mockProperty.getMaxTimeout()).thenReturn(1);

        // 2. Khởi tạo DataSource (không qua init() để không tạo connection thật)
        dataSource = new LCXDataSource(
                "test-pool",
                "oracle.jdbc.driver.OracleDriver",
                "SELECT 1 FROM DUAL",
                mockProperty, // Mock dependency không cần thiết
                mock(SimpleExecutor.class),   // Mock dependency không cần thiết
                DBTypeEnum.ORACLE,            // Sử dụng một enum hợp lệ
                1000,
                null // Pool sẽ được inject sau
        );

        // 3. Tạo mock pool và inject vào dataSource bằng Java reflection
        mockPool = new ConcurrentLinkedQueue<>();
        mockPool.add(entry1);
        mockPool.add(entry2);
        Field poolField = LCXDataSource.class.getDeclaredField("pool");
        poolField.setAccessible(true);
        poolField.set(dataSource, mockPool);
    }

    @Test
    void testPoolInitialization() {
        assertEquals(2, dataSource.getTotalConnections());
        assertEquals(2, dataSource.getIdleConnections());
        assertEquals(0, dataSource.getActiveConnections());
    }

    @Test
    void testGetConnection() {
        try (Connection conn = dataSource.get()) {
            assertNotNull(conn);
            assertEquals(1, dataSource.getActiveConnections());
            assertEquals(1, dataSource.getIdleConnections());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        assertEquals(0, dataSource.getActiveConnections());
        assertEquals(2, dataSource.getIdleConnections());
    }

    @Test
    void testValidateEntry() {
        assertDoesNotThrow(() -> dataSource.validateEntry(entry1));
        ConnectionEntry fakeEntry = ConnectionEntry.init(mock(Connection.class), DBTypeEnum.ORACLE, "fake");
        assertThrows(IllegalArgumentException.class, () -> dataSource.validateEntry(fakeEntry));
    }

    @Test
    void testGetConnection_RecreateIfInvalid() throws SQLException {
        when(mockConnection1.isValid(anyInt())).thenReturn(false);
        Connection conn = dataSource.get();
        assertNotNull(conn);
    }
} 
