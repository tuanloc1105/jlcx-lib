package vn.io.lcx.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    // --- CacheException ---

    @Test
    void cacheException_messageConstructor() {
        CacheException ex = new CacheException("cache error");
        assertEquals("cache error", ex.getMessage());
        assertNull(ex.getCause());
        assertInstanceOf(RuntimeException.class, ex);
    }

    // --- ConnectionEntryException ---

    @Test
    void connectionEntryException_messageConstructor() {
        ConnectionEntryException ex = new ConnectionEntryException("connection failed");
        assertEquals("connection failed", ex.getMessage());
        assertNull(ex.getCause());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void connectionEntryException_causeConstructor() {
        Throwable cause = new RuntimeException("root cause");
        ConnectionEntryException ex = new ConnectionEntryException(cause);
        assertEquals(cause, ex.getCause());
        assertInstanceOf(RuntimeException.class, ex);
    }

    // --- DuplicateInstancesException ---

    @Test
    void duplicateInstancesException_messageConstructor() {
        DuplicateInstancesException ex = new DuplicateInstancesException("duplicate found");
        assertEquals("duplicate found", ex.getMessage());
        assertNull(ex.getCause());
        assertInstanceOf(RuntimeException.class, ex);
    }

    // --- HikariLcxDataSourceException ---

    @Test
    void hikariLcxDataSourceException_messageConstructor() {
        HikariLcxDataSourceException ex = new HikariLcxDataSourceException("hikari error");
        assertEquals("hikari error", ex.getMessage());
        assertNull(ex.getCause());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void hikariLcxDataSourceException_causeConstructor() {
        Throwable cause = new IllegalStateException("pool exhausted");
        HikariLcxDataSourceException ex = new HikariLcxDataSourceException(cause);
        assertEquals(cause, ex.getCause());
        assertInstanceOf(RuntimeException.class, ex);
    }

    // --- LCXDataSourceException ---

    @Test
    void lcxDataSourceException_messageConstructor() {
        LCXDataSourceException ex = new LCXDataSourceException("datasource error");
        assertEquals("datasource error", ex.getMessage());
        assertNull(ex.getCause());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void lcxDataSourceException_causeConstructor() {
        Throwable cause = new RuntimeException("connection timeout");
        LCXDataSourceException ex = new LCXDataSourceException(cause);
        assertEquals(cause, ex.getCause());
        assertInstanceOf(RuntimeException.class, ex);
    }

    // --- LCXDataSourcePropertiesException ---

    @Test
    void lcxDataSourcePropertiesException_messageConstructor() {
        LCXDataSourcePropertiesException ex = new LCXDataSourcePropertiesException("invalid properties");
        assertEquals("invalid properties", ex.getMessage());
        assertNull(ex.getCause());
        assertInstanceOf(RuntimeException.class, ex);
    }

    // --- ValidationException ---

    @Test
    void validationException_messageConstructor() {
        ValidationException ex = new ValidationException("validation failed");
        assertEquals("validation failed", ex.getMessage());
        assertNull(ex.getCause());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void validationException_causeConstructor() {
        Throwable cause = new IllegalArgumentException("bad input");
        ValidationException ex = new ValidationException(cause);
        assertEquals(cause, ex.getCause());
        assertInstanceOf(RuntimeException.class, ex);
    }
}
