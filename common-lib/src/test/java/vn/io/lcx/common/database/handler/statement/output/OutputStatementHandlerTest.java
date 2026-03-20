package vn.io.lcx.common.database.handler.statement.output;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Output Statement Handlers")
@ExtendWith(MockitoExtension.class)
class OutputStatementHandlerTest {

    @Mock
    private CallableStatement callableStatement;

    // ========== StringSqlStatementHandler ==========

    @Nested
    @DisplayName("StringSqlStatementHandler")
    class StringTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(StringSqlStatementHandler.getInstance(), StringSqlStatementHandler.getInstance());
        }

        @Test
        void handle_returnsString() throws Exception {
            when(callableStatement.getString(1)).thenReturn("hello");
            String result = StringSqlStatementHandler.getInstance().handle(1, callableStatement);
            assertEquals("hello", result);
        }

        @Test
        void handle_nullValue_returnsNull() throws Exception {
            when(callableStatement.getString(1)).thenReturn(null);
            assertNull(StringSqlStatementHandler.getInstance().handle(1, callableStatement));
        }
    }

    // ========== IntegerSqlStatementHandler ==========

    @Nested
    @DisplayName("IntegerSqlStatementHandler")
    class IntegerTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(IntegerSqlStatementHandler.getInstance(), IntegerSqlStatementHandler.getInstance());
        }

        @Test
        void handle_returnsInt() throws Exception {
            when(callableStatement.getInt(2)).thenReturn(42);
            Integer result = IntegerSqlStatementHandler.getInstance().handle(2, callableStatement);
            assertEquals(42, result);
        }

        @Test
        void handle_zeroValue_returnsZero() throws Exception {
            when(callableStatement.getInt(1)).thenReturn(0);
            assertEquals(0, IntegerSqlStatementHandler.getInstance().handle(1, callableStatement));
        }
    }

    // ========== LongSqlStatementHandler ==========

    @Nested
    @DisplayName("LongSqlStatementHandler")
    class LongTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(LongSqlStatementHandler.getInstance(), LongSqlStatementHandler.getInstance());
        }

        @Test
        void handle_returnsLong() throws Exception {
            when(callableStatement.getLong(1)).thenReturn(9876543210L);
            Long result = LongSqlStatementHandler.getInstance().handle(1, callableStatement);
            assertEquals(9876543210L, result);
        }
    }

    // ========== DoubleSqlStatementHandler ==========

    @Nested
    @DisplayName("DoubleSqlStatementHandler")
    class DoubleTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(DoubleSqlStatementHandler.getInstance(), DoubleSqlStatementHandler.getInstance());
        }

        @Test
        void handle_returnsDouble() throws Exception {
            when(callableStatement.getDouble(1)).thenReturn(3.14);
            Double result = DoubleSqlStatementHandler.getInstance().handle(1, callableStatement);
            assertEquals(3.14, result, 0.001);
        }
    }

    // ========== FloatSqlStatementHandler ==========

    @Nested
    @DisplayName("FloatSqlStatementHandler")
    class FloatTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(FloatSqlStatementHandler.getInstance(), FloatSqlStatementHandler.getInstance());
        }

        @Test
        void handle_returnsFloat() throws Exception {
            when(callableStatement.getFloat(1)).thenReturn(2.5f);
            Float result = FloatSqlStatementHandler.getInstance().handle(1, callableStatement);
            assertEquals(2.5f, result, 0.001f);
        }
    }

    // ========== BooleanSqlStatementHandler ==========

    @Nested
    @DisplayName("BooleanSqlStatementHandler")
    class BooleanTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(BooleanSqlStatementHandler.getInstance(), BooleanSqlStatementHandler.getInstance());
        }

        @Test
        void handle_returnsTrue() throws Exception {
            when(callableStatement.getBoolean(1)).thenReturn(true);
            assertTrue(BooleanSqlStatementHandler.getInstance().handle(1, callableStatement));
        }

        @Test
        void handle_returnsFalse() throws Exception {
            when(callableStatement.getBoolean(1)).thenReturn(false);
            assertFalse(BooleanSqlStatementHandler.getInstance().handle(1, callableStatement));
        }
    }

    // ========== BigDecimalSqlStatementHandler ==========

    @Nested
    @DisplayName("BigDecimalSqlStatementHandler")
    class BigDecimalTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(BigDecimalSqlStatementHandler.getInstance(), BigDecimalSqlStatementHandler.getInstance());
        }

        @Test
        void handle_returnsBigDecimal() throws Exception {
            BigDecimal expected = new BigDecimal("999.99");
            when(callableStatement.getBigDecimal(1)).thenReturn(expected);
            BigDecimal result = BigDecimalSqlStatementHandler.getInstance().handle(1, callableStatement);
            assertEquals(expected, result);
        }

        @Test
        void handle_nullValue_returnsNull() throws Exception {
            when(callableStatement.getBigDecimal(1)).thenReturn(null);
            assertNull(BigDecimalSqlStatementHandler.getInstance().handle(1, callableStatement));
        }
    }

    // ========== DateSqlStatementHandler ==========

    @Nested
    @DisplayName("DateSqlStatementHandler")
    class DateTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(DateSqlStatementHandler.getInstance(), DateSqlStatementHandler.getInstance());
        }

        @Test
        void handle_validTimestamp_returnsLocalDateTime() throws Exception {
            LocalDateTime expected = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            when(callableStatement.getTimestamp(1)).thenReturn(Timestamp.valueOf(expected));
            LocalDateTime result = DateSqlStatementHandler.getInstance().handle(1, callableStatement);
            assertEquals(expected, result);
        }

        @Test
        void handle_nullTimestamp_returnsNull() throws Exception {
            when(callableStatement.getTimestamp(1)).thenReturn(null);
            assertNull(DateSqlStatementHandler.getInstance().handle(1, callableStatement));
        }
    }

    // ========== ResultSetSqlStatementHandler ==========

    @Nested
    @DisplayName("ResultSetSqlStatementHandler")
    class ResultSetTest {

        @Mock
        private ResultSet resultSet;

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(ResultSetSqlStatementHandler.getInstance(), ResultSetSqlStatementHandler.getInstance());
        }

        @Test
        void handle_returnsResultSet() throws Exception {
            when(callableStatement.getObject(1)).thenReturn(resultSet);
            ResultSet result = ResultSetSqlStatementHandler.getInstance().handle(1, callableStatement);
            assertSame(resultSet, result);
        }

        @Test
        void handle_nullObject_returnsNull() throws Exception {
            when(callableStatement.getObject(1)).thenReturn(null);
            assertNull(ResultSetSqlStatementHandler.getInstance().handle(1, callableStatement));
        }
    }
}
