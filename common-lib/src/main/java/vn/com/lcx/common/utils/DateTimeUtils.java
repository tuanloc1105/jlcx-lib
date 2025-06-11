package vn.com.lcx.common.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    private static long toUnix(LocalDateTime time, String... timeZone) {
        final var zone = ZoneId.of(timeZone.length == 1 ? ZoneId.SHORT_IDS.get(timeZone[0]) : ZoneId.SHORT_IDS.get(TimezoneEnum.VST.name()));
        return time.atZone(zone).toEpochSecond();
    }

    public static long toUnixMil(LocalDateTime time, String... timeZone) {
        final var zone = ZoneId.of(timeZone.length == 1 ? ZoneId.SHORT_IDS.get(timeZone[0]) : ZoneId.SHORT_IDS.get(TimezoneEnum.VST.name()));
        return time.atZone(zone).toInstant().toEpochMilli();
    }

    public static LocalDateTime unixToLocalDateTime(Long unix) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(unix),
                TimeZone.getDefault().toZoneId()
        );
    }

    public static LocalDateTime generateCurrentTimeDefault() {
        return ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get(TimezoneEnum.VST.name()))).toLocalDateTime();
    }

    public static LocalTime generateCurrentLocalTimeDefault() {
        return ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get(TimezoneEnum.VST.name()))).toLocalTime();
    }

    public static LocalDate generateCurrentLocalDateDefault() {
        return ZonedDateTime.now().toLocalDate();
    }

    public static Calendar localDateTimeToCalendar(LocalDateTime localDateTime, TimezoneEnum... timeZone) {
        if (localDateTime == null) {
            throw new IllegalArgumentException("localDateTime cannot be null");
        }

        // Convert LocalDateTime to ZonedDateTime
        ZonedDateTime zonedDateTime = localDateTime.atZone(
                timeZone.length == 1 ?
                        ZoneId.of(ZoneId.SHORT_IDS.get(timeZone[0].name())) :
                        ZoneId.of(ZoneId.SHORT_IDS.get(TimezoneEnum.VST.name()))
        );

        // Convert ZonedDateTime to Date
        Date date = Date.from(zonedDateTime.toInstant());

        // Convert Date to Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime, TimezoneEnum... timeZone) {
        final Calendar calendar = localDateTimeToCalendar(localDateTime, timeZone);
        return calendar.getTime();
    }

    @AllArgsConstructor
    @Getter
    public enum TimezoneEnum {
        ACT("ACT"), // Australia/Darwin
        AET("AET"), // Australia/Sydney
        AGT("AGT"), // America/Argentina/Buenos_Aires
        ART("ART"), // Africa/Cairo
        AST("AST"), // America/Anchorage
        BET("BET"), // America/Sao_Paulo
        BST("BST"), // Asia/Dhaka
        CAT("CAT"), // Africa/Harare
        CNT("CNT"), // America/St_Johns
        CST("CST"), // America/Chicago
        CTT("CTT"), // Asia/Shanghai
        EAT("EAT"), // Africa/Addis_Ababa
        ECT("ECT"), // Europe/Paris
        IET("IET"), // America/Indiana/Indianapolis
        IST("IST"), // Asia/Kolkata
        JST("JST"), // Asia/Tokyo
        MIT("MIT"), // Pacific/Apia
        NET("NET"), // Asia/Yerevan
        NST("NST"), // Pacific/Auckland
        PLT("PLT"), // Asia/Karachi
        PNT("PNT"), // America/Phoenix
        PRT("PRT"), // America/Puerto_Rico
        PST("PST"), // America/Los_Angeles
        SST("SST"), // Pacific/Guadalcanal
        VST("VST"), // Asia/Ho_Chi_Minh
        EST("EST"), // -05:00
        MST("MST"), // -07:00
        HST("HST"), // -10:00
        ;
        private final String value;
    }

}
