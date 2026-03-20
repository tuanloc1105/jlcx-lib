package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;
import vn.io.lcx.common.utils.DateTimeUtils.TimezoneEnum;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {

    @Test
    void toUnixMillis_returnsCorrectTimestamp() {
        // 2024-01-01T00:00:00 in VST (Asia/Ho_Chi_Minh = UTC+7)
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        long unix = DateTimeUtils.toUnixMillis(time);

        // Compute expected using Java's own ZoneId resolution for VST
        long expected = time.atZone(TimezoneEnum.VST.getZoneId()).toInstant().toEpochMilli();
        assertEquals(expected, unix);
    }

    @Test
    void toUnixMillis_withTimezone_returnsCorrectTimestamp() {
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        long unixJST = DateTimeUtils.toUnixMillis(time, TimezoneEnum.JST);

        // JST is UTC+9, so the same local time maps to an earlier instant than VST (UTC+7)
        long unixVST = DateTimeUtils.toUnixMillis(time, TimezoneEnum.VST);
        assertTrue(unixJST < unixVST,
                "JST (UTC+9) should produce a smaller epoch than VST (UTC+7) for the same local time");

        // Verify the exact value using Java's ZoneId
        long expected = time.atZone(TimezoneEnum.JST.getZoneId()).toInstant().toEpochMilli();
        assertEquals(expected, unixJST);
    }

    @Test
    void toUnixMillis_nullTime_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.toUnixMillis(null));
    }

    @Test
    void unixToLocalDateTime_roundTrip() {
        LocalDateTime original = LocalDateTime.of(2024, 6, 15, 12, 30, 45);
        long unix = DateTimeUtils.toUnixMillis(original);
        LocalDateTime restored = DateTimeUtils.unixToLocalDateTime(unix);

        assertEquals(original, restored);
    }

    @Test
    void unixToLocalDateTime_knownValue() {
        // Use a modern date to avoid historical timezone offset differences.
        // 1704067200000L = 2024-01-01T00:00:00 UTC
        LocalDateTime result = DateTimeUtils.unixToLocalDateTime(1704067200000L);
        // In VST (Asia/Ho_Chi_Minh, UTC+7): 2024-01-01T07:00:00
        assertEquals(LocalDateTime.of(2024, 1, 1, 7, 0, 0), result);
    }

    @Test
    void generateCurrentTimeDefault_notNull() {
        LocalDateTime now = DateTimeUtils.generateCurrentTimeDefault();
        assertNotNull(now);
    }

    @Test
    void generateCurrentTimeDefault_isCloseToNow() {
        LocalDateTime before = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime generated = DateTimeUtils.generateCurrentTimeDefault();
        LocalDateTime after = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        assertFalse(generated.isBefore(before.minusSeconds(1)),
                "Generated time should not be significantly before reference");
        assertFalse(generated.isAfter(after.plusSeconds(1)),
                "Generated time should not be significantly after reference");
    }

    @Test
    void generateCurrentTimeDefaultWithTimezone_notNull() {
        OffsetDateTime result = DateTimeUtils.generateCurrentTimeDefaultWithTimezone(TimezoneEnum.JST);
        assertNotNull(result);
    }

    @Test
    void generateCurrentTimeDefaultWithTimezone_nullDefaultsToVST() {
        OffsetDateTime result = DateTimeUtils.generateCurrentTimeDefaultWithTimezone(null);
        assertNotNull(result);
    }

    @Test
    void localDateTimeToCalendar_preservesTime() {
        LocalDateTime ldt = LocalDateTime.of(2024, 3, 15, 10, 30, 45);
        Calendar cal = DateTimeUtils.localDateTimeToCalendar(ldt);

        assertNotNull(cal);
        // Convert calendar back to instant and compare with the original LocalDateTime's instant in VST
        long expectedMillis = DateTimeUtils.toUnixMillis(ldt);
        assertEquals(expectedMillis, cal.getTimeInMillis());
    }

    @Test
    void localDateTimeToCalendar_withTimezone() {
        LocalDateTime ldt = LocalDateTime.of(2024, 3, 15, 10, 30, 45);
        Calendar cal = DateTimeUtils.localDateTimeToCalendar(ldt, TimezoneEnum.JST);

        assertNotNull(cal);
        long expectedMillis = DateTimeUtils.toUnixMillis(ldt, TimezoneEnum.JST);
        assertEquals(expectedMillis, cal.getTimeInMillis());
    }

    @Test
    void localDateTimeToCalendar_nullInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.localDateTimeToCalendar(null));
    }

    @Test
    void localDateTimeToDate_preservesTime() {
        LocalDateTime ldt = LocalDateTime.of(2024, 7, 20, 14, 0, 0);
        Date date = DateTimeUtils.localDateTimeToDate(ldt);

        assertNotNull(date);
        long expectedMillis = DateTimeUtils.toUnixMillis(ldt);
        assertEquals(expectedMillis, date.getTime());
    }

    @Test
    void localDateTimeToOffsetDateTime_defaultTimezone() {
        LocalDateTime ldt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        OffsetDateTime odt = DateTimeUtils.localDateTimeToOffsetDateTime(ldt);

        assertNotNull(odt);
        assertEquals(ldt.getYear(), odt.getYear());
        assertEquals(ldt.getMonth(), odt.getMonth());
        assertEquals(ldt.getDayOfMonth(), odt.getDayOfMonth());
        assertEquals(ldt.getHour(), odt.getHour());
        assertEquals(ldt.getMinute(), odt.getMinute());
    }

    @Test
    void localDateTimeToOffsetDateTime_withTimezone() {
        LocalDateTime ldt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        OffsetDateTime odt = DateTimeUtils.localDateTimeToOffsetDateTime(ldt, TimezoneEnum.JST);

        assertNotNull(odt);
        assertEquals(ldt.getHour(), odt.getHour());
        // JST is always +09:00 (no DST)
        assertEquals("+09:00", odt.getOffset().toString());
    }

    @Test
    void convertCurrentTimeToAnotherTimeZone_correct() {
        OffsetDateTime result = DateTimeUtils.convertCurrentTimeToAnotherTimeZone(
                TimezoneEnum.VST, TimezoneEnum.JST
        );

        assertNotNull(result);
        // JST is always +09:00
        assertEquals("+09:00", result.getOffset().toString());
    }

    @Test
    void convertCurrentTimeToAnotherTimeZone_sameZone_sameOffset() {
        OffsetDateTime result = DateTimeUtils.convertCurrentTimeToAnotherTimeZone(
                TimezoneEnum.VST, TimezoneEnum.VST
        );

        assertNotNull(result);
        assertEquals("+07:00", result.getOffset().toString());
    }

    @Test
    void timezoneEnum_allValuesPresent() {
        TimezoneEnum[] values = TimezoneEnum.values();
        // There should be 28 timezone values defined in the enum
        assertEquals(28, values.length);

        // Verify each enum constant maps to a valid ZoneId
        for (TimezoneEnum tz : values) {
            assertNotNull(tz.getZoneId(), "ZoneId should not be null for " + tz.name());
            assertNotNull(tz.getZoneOffset(), "ZoneOffset should not be null for " + tz.name());
        }
    }

    @Test
    void timezoneEnum_getZoneOffset_withLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
        // JST does not observe DST, so offset should always be +09:00
        assertEquals("+09:00", TimezoneEnum.JST.getZoneOffset(ldt).toString());
        // VST does not observe DST, so offset should always be +07:00
        assertEquals("+07:00", TimezoneEnum.VST.getZoneOffset(ldt).toString());
    }

    @Test
    void generateCurrentLocalTimeDefault_notNull() {
        assertNotNull(DateTimeUtils.generateCurrentLocalTimeDefault());
    }

    @Test
    void generateCurrentLocalDateDefault_notNull() {
        assertNotNull(DateTimeUtils.generateCurrentLocalDateDefault());
    }
}
