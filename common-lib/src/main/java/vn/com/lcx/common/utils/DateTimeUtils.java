package vn.com.lcx.common.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
     * Create an OffsetDateTime for the current moment in a given timezone.
     *
     * @param timezone the time zone ID
     * @return OffsetDateTime representing the current moment in that timezone
     */
    public static OffsetDateTime generateCurrentTimeDefaultWithTimezone(TimezoneEnum timezone) {
        if (timezone == null) {
            timezone = TimezoneEnum.VST;
        }
        final ZoneId zoneId = ZoneId.of(ZoneId.SHORT_IDS.get(timezone.name()));
        final ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return zonedDateTime.toOffsetDateTime();
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
     * Converts a {@link LocalDateTime} to an {@link OffsetDateTime} using the default timezone
     * {@link TimezoneEnum#VST} (Vietnam Standard Time).
     * <p>
     * This method is a shortcut for calling
     * {@link #localDateTimeToOffsetDateTime(LocalDateTime, TimezoneEnum)} with {@code TimezoneEnum.VST}.
     * </p>
     *
     * @param localDateTime the {@code LocalDateTime} instance to be converted, must not be {@code null}
     * @return an {@code OffsetDateTime} representing the same local date-time with the offset
     *         corresponding to Vietnam Standard Time
     */
    public static OffsetDateTime localDateTimeToOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTimeToOffsetDateTime(localDateTime, TimezoneEnum.VST);
    }

    /**
     * Converts a {@link LocalDateTime} to an {@link OffsetDateTime} using the provided {@link TimezoneEnum}.
     * <p>
     * The method resolves the {@link ZoneId} based on the given timezone enum, determines the current
     * {@link ZoneOffset} for that zone using {@link java.time.zone.ZoneRules}, and applies that offset to the given
     * local date-time.
     * </p>
     * <p><b>Note:</b> Because the offset is determined based on the current {@link Instant}, the result
     * may not always reflect the historical or future offset for the given {@code localDateTime} in
     * zones that observe Daylight Saving Time (DST).
     * </p>
     *
     * @param localDateTime the {@code LocalDateTime} instance to be converted, must not be {@code null}
     * @param timezone      the target {@link TimezoneEnum}, must not be {@code null}
     * @return an {@code OffsetDateTime} representing the same local date-time with the offset
     *         corresponding to the given timezone
     */
    public static OffsetDateTime localDateTimeToOffsetDateTime(LocalDateTime localDateTime, TimezoneEnum timezone) {
        final var zoneId = ZoneId.of(ZoneId.SHORT_IDS.get(timezone.name()));
        Instant now = Instant.now();
        ZoneOffset offset = zoneId.getRules().getOffset(now);
        return localDateTime.atOffset(offset);
    }

    /**
     * Enum of supported time zone short IDs for use with {@link ZoneId#SHORT_IDS}.
     *
     * <p>Each enum constant maps to a standard short ID string recognized by
     * {@link ZoneId#of(String, java.util.Map)} or {@link ZoneId#SHORT_IDS}.
     * These short IDs resolve to either a specific region-based time zone
     * (e.g. {@code VST -> Asia/Ho_Chi_Minh}) or a fixed UTC offset
     * (e.g. {@code EST -> -05:00}).</p>
     *
     * <p>This enum provides a type-safe way of working with short IDs instead of
     * hardcoding raw strings.</p>
     *
     * <h3>Usage example:</h3>
     * <pre>{@code
     * ZoneId zoneId = ZoneId.of(ZoneId.SHORT_IDS.get(TimezoneEnum.VST.name()));
     * LocalDateTime localDateTime = LocalDateTime.now();
     * OffsetDateTime odt = localDateTime.atZone(zoneId).toOffsetDateTime();
     * }</pre>
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
        HST("HST"); // -10:00

        private final String value;

        TimezoneEnum(String value) {
            this.value = value;
        }

        /**
         * Gets the short ID value for this time zone.
         *
         * <p>This value can be used with {@link ZoneId#of(String, java.util.Map)}
         * in combination with {@link ZoneId#SHORT_IDS} to resolve a proper
         * {@link ZoneId} instance.</p>
         *
         * @return the short ID string (e.g. "VST", "EST", "PST")
         */
        public String getValue() {
            return value;
        }
    }

}
