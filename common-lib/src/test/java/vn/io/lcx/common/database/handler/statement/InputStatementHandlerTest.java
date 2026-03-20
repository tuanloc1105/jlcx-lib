package vn.io.lcx.common.database.handler.statement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.lcx.common.constant.CommonConstant;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Input Statement Handlers")
@ExtendWith(MockitoExtension.class)
class InputStatementHandlerTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private Statement plainStatement;

    // ========== Common validation tests ==========

    @Nested
    @DisplayName("Common Validation")
    class CommonValidation {

        @Test
        void handle_nullStatement_throwsNullPointerException() {
            assertThrows(NullPointerException.class, () ->
                    StringHandler.getInstance().handle(1, "test", null));
        }

        @Test
        void handle_nonPreparedStatement_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    StringHandler.getInstance().handle(1, "test", plainStatement));
        }
    }

    // ========== StringHandler ==========

    @Nested
    @DisplayName("StringHandler")
    class StringHandlerTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(StringHandler.getInstance(), StringHandler.getInstance());
        }

        @Test
        void handle_validString_setsString() throws Exception {
            StringHandler.getInstance().handle(1, "hello", preparedStatement);
            verify(preparedStatement).setString(1, "hello");
        }

        @Test
        void handle_nullInput_setsEmptyString() throws Exception {
            StringHandler.getInstance().handle(1, null, preparedStatement);
            verify(preparedStatement).setString(1, CommonConstant.EMPTY_STRING);
        }

        @Test
        void handle_emptyString_setsEmptyString() throws Exception {
            StringHandler.getInstance().handle(1, "", preparedStatement);
            verify(preparedStatement).setString(1, "");
        }

        @Test
        void handle_wrongType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    StringHandler.getInstance().handle(1, 123, preparedStatement));
        }
    }

    // ========== IntegerHandler ==========

    @Nested
    @DisplayName("IntegerHandler")
    class IntegerHandlerTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(IntegerHandler.getInstance(), IntegerHandler.getInstance());
        }

        @Test
        void handle_validInt_setsInt() throws Exception {
            IntegerHandler.getInstance().handle(1, 42, preparedStatement);
            verify(preparedStatement).setInt(1, 42);
        }

        @Test
        void handle_nullInput_setsZero() throws Exception {
            IntegerHandler.getInstance().handle(1, null, preparedStatement);
            verify(preparedStatement).setInt(1, 0);
        }

        @Test
        void handle_integerWrapper_setsInt() throws Exception {
            Integer value = Integer.valueOf(100);
            IntegerHandler.getInstance().handle(1, value, preparedStatement);
            verify(preparedStatement).setInt(1, 100);
        }

        @Test
        void handle_wrongType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    IntegerHandler.getInstance().handle(1, "notAnInt", preparedStatement));
        }
    }

    // ========== LongHandler ==========

    @Nested
    @DisplayName("LongHandler")
    class LongHandlerTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(LongHandler.getInstance(), LongHandler.getInstance());
        }

        @Test
        void handle_validLong_setsLong() throws Exception {
            LongHandler.getInstance().handle(1, 123456789L, preparedStatement);
            verify(preparedStatement).setLong(1, 123456789L);
        }

        @Test
        void handle_nullInput_setsZero() throws Exception {
            LongHandler.getInstance().handle(1, null, preparedStatement);
            verify(preparedStatement).setLong(1, 0);
        }

        @Test
        void handle_wrongType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    LongHandler.getInstance().handle(1, 42, preparedStatement));
        }
    }

    // ========== DoubleHandler ==========

    @Nested
    @DisplayName("DoubleHandler")
    class DoubleHandlerTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(DoubleHandler.getInstance(), DoubleHandler.getInstance());
        }

        @Test
        void handle_validDouble_setsDouble() throws Exception {
            DoubleHandler.getInstance().handle(1, 3.14, preparedStatement);
            verify(preparedStatement).setDouble(1, 3.14);
        }

        @Test
        void handle_nullInput_setsZero() throws Exception {
            DoubleHandler.getInstance().handle(1, null, preparedStatement);
            verify(preparedStatement).setDouble(1, 0d);
        }

        @Test
        void handle_wrongType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    DoubleHandler.getInstance().handle(1, 3.14f, preparedStatement));
        }
    }

    // ========== FloatHandler ==========

    @Nested
    @DisplayName("FloatHandler")
    class FloatHandlerTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(FloatHandler.getInstance(), FloatHandler.getInstance());
        }

        @Test
        void handle_validFloat_setsFloat() throws Exception {
            FloatHandler.getInstance().handle(1, 2.5f, preparedStatement);
            verify(preparedStatement).setFloat(1, 2.5f);
        }

        @Test
        void handle_nullInput_setsZero() throws Exception {
            FloatHandler.getInstance().handle(1, null, preparedStatement);
            verify(preparedStatement).setFloat(1, 0f);
        }

        @Test
        void handle_wrongType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    FloatHandler.getInstance().handle(1, 2.5, preparedStatement));
        }
    }

    // ========== BooleanHandler ==========

    @Nested
    @DisplayName("BooleanHandler")
    class BooleanHandlerTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(BooleanHandler.getInstance(), BooleanHandler.getInstance());
        }

        @Test
        void handle_true_setsTrue() throws Exception {
            BooleanHandler.getInstance().handle(1, true, preparedStatement);
            verify(preparedStatement).setBoolean(1, true);
        }

        @Test
        void handle_false_setsFalse() throws Exception {
            BooleanHandler.getInstance().handle(1, false, preparedStatement);
            verify(preparedStatement).setBoolean(1, false);
        }

        @Test
        void handle_nullInput_setsFalse() throws Exception {
            BooleanHandler.getInstance().handle(1, null, preparedStatement);
            verify(preparedStatement).setBoolean(1, false);
        }

        @Test
        void handle_wrongType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    BooleanHandler.getInstance().handle(1, "true", preparedStatement));
        }
    }

    // ========== BigDecimalHandler ==========

    @Nested
    @DisplayName("BigDecimalHandler")
    class BigDecimalHandlerTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(BigDecimalHandler.getInstance(), BigDecimalHandler.getInstance());
        }

        @Test
        void handle_validBigDecimal_setsBigDecimal() throws Exception {
            BigDecimal value = new BigDecimal("123.45");
            BigDecimalHandler.getInstance().handle(1, value, preparedStatement);
            verify(preparedStatement).setBigDecimal(1, value);
        }

        @Test
        void handle_nullInput_setsNull() throws Exception {
            BigDecimalHandler.getInstance().handle(1, null, preparedStatement);
            verify(preparedStatement).setBigDecimal(1, null);
        }

        @Test
        void handle_wrongType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    BigDecimalHandler.getInstance().handle(1, 123.45, preparedStatement));
        }
    }

    // ========== DateHandler ==========

    @Nested
    @DisplayName("DateHandler")
    class DateHandlerTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(DateHandler.getInstance(), DateHandler.getInstance());
        }

        @Test
        void handle_validDate_setsDate() throws Exception {
            Date date = Date.valueOf("2024-01-15");
            DateHandler.getInstance().handle(1, date, preparedStatement);
            verify(preparedStatement).setDate(1, date);
        }

        @Test
        void handle_nullInput_setsNull() throws Exception {
            DateHandler.getInstance().handle(1, null, preparedStatement);
            verify(preparedStatement).setDate(1, null);
        }

        @Test
        void handle_wrongType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    DateHandler.getInstance().handle(1, "2024-01-15", preparedStatement));
        }
    }

    // ========== LocalDateHandler ==========

    @Nested
    @DisplayName("LocalDateHandler")
    class LocalDateHandlerTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(LocalDateHandler.getInstance(), LocalDateHandler.getInstance());
        }

        @Test
        void handle_validLocalDate_setsSqlDate() throws Exception {
            LocalDate localDate = LocalDate.of(2024, 6, 15);
            LocalDateHandler.getInstance().handle(1, localDate, preparedStatement);
            verify(preparedStatement).setDate(1, Date.valueOf(localDate));
        }

        @Test
        void handle_nullInput_setsTimestampNull() throws Exception {
            LocalDateHandler.getInstance().handle(1, null, preparedStatement);
            verify(preparedStatement).setTimestamp(1, null);
        }

        @Test
        void handle_wrongType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    LocalDateHandler.getInstance().handle(1, "2024-06-15", preparedStatement));
        }
    }

    // ========== LocalDateTimeHandler ==========

    @Nested
    @DisplayName("LocalDateTimeHandler")
    class LocalDateTimeHandlerTest {

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(LocalDateTimeHandler.getInstance(), LocalDateTimeHandler.getInstance());
        }

        @Test
        void handle_validLocalDateTime_setsTimestamp() throws Exception {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            LocalDateTimeHandler.getInstance().handle(1, dateTime, preparedStatement);
            verify(preparedStatement).setTimestamp(1, Timestamp.valueOf(dateTime));
        }

        @Test
        void handle_nullInput_setsTimestampNull() throws Exception {
            LocalDateTimeHandler.getInstance().handle(1, null, preparedStatement);
            verify(preparedStatement).setTimestamp(1, null);
        }

        @Test
        void handle_wrongType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    LocalDateTimeHandler.getInstance().handle(1, "2024-06-15T10:30:00", preparedStatement));
        }
    }

    // ========== ClobHandler ==========

    @Nested
    @DisplayName("ClobHandler")
    class ClobHandlerTest {

        @Mock
        private Clob clob;

        @Test
        void getInstance_returnsSameInstance() {
            assertSame(ClobHandler.getInstance(), ClobHandler.getInstance());
        }

        @Test
        void handle_validClob_setsClob() throws Exception {
            ClobHandler.getInstance().handle(1, clob, preparedStatement);
            verify(preparedStatement).setClob(1, clob);
        }

        @Test
        void handle_nullInput_setsEmptyReader() throws Exception {
            ClobHandler.getInstance().handle(1, null, preparedStatement);
            verify(preparedStatement).setClob(eq(1), any(java.io.Reader.class));
        }

        @Test
        void handle_wrongType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    ClobHandler.getInstance().handle(1, "notAClob", preparedStatement));
        }
    }
}
