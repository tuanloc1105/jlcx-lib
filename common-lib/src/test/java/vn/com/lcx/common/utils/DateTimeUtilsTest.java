package vn.com.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class DateTimeUtilsTest {
    @Test
    public void testToUnixMilAndUnixToLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        long unixMil = DateTimeUtils.toUnixMil(now, "VST");
        assertTrue(unixMil > 0);
        LocalDateTime restored = DateTimeUtils.unixToLocalDateTime(unixMil);
        assertEquals(now.getYear(), restored.getYear());
        assertEquals(now.getMonth(), restored.getMonth());
        assertEquals(now.getDayOfMonth(), restored.getDayOfMonth());
    }

    @Test
    public void testGenerateCurrentTimeDefault() {
        LocalDateTime ldt = DateTimeUtils.generateCurrentTimeDefault();
        assertNotNull(ldt);
        LocalTime lt = DateTimeUtils.generateCurrentLocalTimeDefault();
        assertNotNull(lt);
        LocalDate ld = DateTimeUtils.generateCurrentLocalDateDefault();
        assertNotNull(ld);
    }

    @Test
    public void testLocalDateTimeToCalendarAndDate() {
        LocalDateTime now = LocalDateTime.now();
        Calendar cal = DateTimeUtils.localDateTimeToCalendar(now, DateTimeUtils.TimezoneEnum.VST);
        assertNotNull(cal);
        Date date = DateTimeUtils.localDateTimeToDate(now, DateTimeUtils.TimezoneEnum.VST);
        assertNotNull(date);
    }

    @Test
    public void testTimezoneEnumValues() {
        for (DateTimeUtils.TimezoneEnum tz : DateTimeUtils.TimezoneEnum.values()) {
            assertNotNull(tz.getValue());
            assertFalse(tz.getValue().isEmpty());
        }
    }

    @Test
    public void testLocalDateTimeToCalendarNull() {
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.localDateTimeToCalendar(null));
    }
} 
