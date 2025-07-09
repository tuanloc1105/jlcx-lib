package vn.com.lcx.common.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class for date and time operations, including conversions between different time representations,
 * working with time zones, and generating current date/time values.
 *
 * <p>This class provides static methods for:
 * <ul>
 *   <li>Converting between {@link LocalDateTime}, Unix timestamps, {@link Calendar}, and {@link Date}</li>
 *   <li>Generating current date, time, and datetime values in default or specified time zones</li>
 *   <li>Handling time zone conversions using a set of supported short IDs</li>
 * </ul>
 *
 * <p>All methods are static and the class cannot be instantiated.</p>
 *
 * <p>Default time zone is VST (Asia/Ho_Chi_Minh) unless otherwise specified.</p>
 *
 * @author LCX Team
 * @since 1.0
 */
public final class DateTimeUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private DateTimeUtils() {
    }

    /**
     * Converts a {@link LocalDateTime} to a Unix timestamp (seconds since epoch) in the specified or default time zone.
     *
     * @param time     the LocalDateTime to convert
     * @param timeZone optional short time zone ID (see {@link TimezoneEnum}); if not provided, defaults to VST
     * @return the Unix timestamp in seconds
     */
    private static long toUnix(LocalDateTime time, String... timeZone) {
        final var zone = ZoneId.of(timeZone.length == 1 ? ZoneId.SHORT_IDS.get(timeZone[0]) : ZoneId.SHORT_IDS.get(TimezoneEnum.VST.name()));
        return time.atZone(zone).toEpochSecond();
    }

    /**
     * Converts a {@link LocalDateTime} to a Unix timestamp in milliseconds in the specified or default time zone.
     *
     * @param time     the LocalDateTime to convert
     * @param timeZone optional short time zone ID (see {@link TimezoneEnum}); if not provided, defaults to VST
     * @return the Unix timestamp in milliseconds
     */
    public static long toUnixMil(LocalDateTime time, String... timeZone) {
        final var zone = ZoneId.of(timeZone.length == 1 ? ZoneId.SHORT_IDS.get(timeZone[0]) : ZoneId.SHORT_IDS.get(TimezoneEnum.VST.name()));
        return time.atZone(zone).toInstant().toEpochMilli();
    }

    /**
     * Converts a Unix timestamp in milliseconds to a {@link LocalDateTime} using the system default time zone.
     *
     * @param unix the Unix timestamp in milliseconds
     * @return the corresponding LocalDateTime
     */
    public static LocalDateTime unixToLocalDateTime(Long unix) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(unix),
                TimeZone.getDefault().toZoneId()
        );
    }

    /**
     * Generates the current {@link LocalDateTime} in the default time zone (VST).
     *
     * @return the current LocalDateTime in VST
     */
    public static LocalDateTime generateCurrentTimeDefault() {
        return ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get(TimezoneEnum.VST.name()))).toLocalDateTime();
    }

    /**
     * Generates the current {@link LocalTime} in the default time zone (VST).
     *
     * @return the current LocalTime in VST
     */
    public static LocalTime generateCurrentLocalTimeDefault() {
        return ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get(TimezoneEnum.VST.name()))).toLocalTime();
    }

    /**
     * Generates the current {@link LocalDate} in the system default time zone.
     *
     * @return the current LocalDate
     */
    public static LocalDate generateCurrentLocalDateDefault() {
        return ZonedDateTime.now().toLocalDate();
    }

    /**
     * Converts a {@link LocalDateTime} to a {@link Calendar} in the specified or default time zone.
     *
     * @param localDateTime the LocalDateTime to convert (must not be null)
     * @param timeZone      optional time zone enum; if not provided, defaults to VST
     * @return the corresponding Calendar instance
     * @throws IllegalArgumentException if localDateTime is null
     */
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

    /**
     * Converts a {@link LocalDateTime} to a {@link Date} in the specified or default time zone.
     *
     * @param localDateTime the LocalDateTime to convert
     * @param timeZone      optional time zone enum; if not provided, defaults to VST
     * @return the corresponding Date instance
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime, TimezoneEnum... timeZone) {
        final Calendar calendar = localDateTimeToCalendar(localDateTime, timeZone);
        return calendar.getTime();
    }

    /**
     * Enum of supported time zone short IDs for use with {@link ZoneId#SHORT_IDS}.
     *
     * <p>Each value maps to a region or offset, e.g. VST = Asia/Ho_Chi_Minh.</p>
     */
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

        TimezoneEnum(String value) {
            this.value = value;
        }

        /**
         * Gets the short ID value for this time zone.
         *
         * @return the short ID string
         */
        public String getValue() {
            return value;
        }
    }

}
