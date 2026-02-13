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

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of(ZoneId.SHORT_IDS.get(TimezoneEnum.VST.name()));

    /**
     * Private constructor to prevent instantiation.
     */
    private DateTimeUtils() {
    }

    /**
     * Converts a {@link LocalDateTime} to a Unix timestamp in milliseconds using the default time zone (VST).
     *
     * @param time the LocalDateTime to convert (must not be null)
     * @return the Unix timestamp in milliseconds
     * @throws IllegalArgumentException if time is null
     */
    public static long toUnixMillis(LocalDateTime time) {
        return toUnixMillis(time, TimezoneEnum.VST);
    }

    /**
     * Converts a {@link LocalDateTime} to a Unix timestamp in milliseconds in the specified time zone.
     *
     * @param time     the LocalDateTime to convert (must not be null)
     * @param timezone the target time zone
     * @return the Unix timestamp in milliseconds
     * @throws IllegalArgumentException if time is null
     */
    public static long toUnixMillis(LocalDateTime time, TimezoneEnum timezone) {
        if (time == null) {
            throw new IllegalArgumentException("time must not be null");
        }
        return time.atZone(timezone.getZoneId()).toInstant().toEpochMilli();
    }

    /**
     * Converts a {@link LocalDateTime} to a Unix timestamp in milliseconds in the specified or default time zone.
     *
     * @param time     the LocalDateTime to convert
     * @param timeZone optional short time zone ID (see {@link TimezoneEnum}); if not provided, defaults to VST
     * @return the Unix timestamp in milliseconds
     * @deprecated Use {@link #toUnixMillis(LocalDateTime)} or {@link #toUnixMillis(LocalDateTime, TimezoneEnum)} instead.
     */
    @Deprecated(forRemoval = true)
    public static long toUnixMil(LocalDateTime time, String... timeZone) {
        if (time == null) {
            throw new IllegalArgumentException("time must not be null");
        }
        final var zone = timeZone.length == 1 ? ZoneId.of(ZoneId.SHORT_IDS.get(timeZone[0])) : DEFAULT_ZONE_ID;
        return time.atZone(zone).toInstant().toEpochMilli();
    }

    /**
     * Converts a Unix timestamp in milliseconds to a {@link LocalDateTime} using the default time zone (VST).
     *
     * @param unix the Unix timestamp in milliseconds
     * @return the corresponding LocalDateTime in VST
     */
    public static LocalDateTime unixToLocalDateTime(Long unix) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(unix),
                DEFAULT_ZONE_ID
        );
    }

    /**
     * Generates the current {@link LocalDateTime} in the default time zone (VST).
     *
     * @return the current LocalDateTime in VST
     */
    public static LocalDateTime generateCurrentTimeDefault() {
        return ZonedDateTime.now(DEFAULT_ZONE_ID).toLocalDateTime();
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
        final ZoneId zoneId = timezone.getZoneId();
        final ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return zonedDateTime.toOffsetDateTime();
    }

    /**
     * Generates the current {@link LocalTime} in the default time zone (VST).
     *
     * @return the current LocalTime in VST
     */
    public static LocalTime generateCurrentLocalTimeDefault() {
        return ZonedDateTime.now(DEFAULT_ZONE_ID).toLocalTime();
    }

    /**
     * Generates the current {@link LocalDate} in the default time zone (VST).
     *
     * @return the current LocalDate in VST
     */
    public static LocalDate generateCurrentLocalDateDefault() {
        return ZonedDateTime.now(DEFAULT_ZONE_ID).toLocalDate();
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
                timeZone.length == 1 ? timeZone[0].getZoneId() : DEFAULT_ZONE_ID
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
     * corresponding to Vietnam Standard Time
     */
    public static OffsetDateTime localDateTimeToOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTimeToOffsetDateTime(localDateTime, TimezoneEnum.VST);
    }

    /**
     * Converts a {@link LocalDateTime} to an {@link OffsetDateTime} using the provided {@link TimezoneEnum}.
     * <p>
     * The method resolves the {@link ZoneId} based on the given timezone enum, then uses
     * {@link LocalDateTime#atZone(ZoneId)} to correctly determine the offset for the given
     * local date-time, including proper handling of Daylight Saving Time (DST) transitions.
     * </p>
     *
     * @param localDateTime the {@code LocalDateTime} instance to be converted, must not be {@code null}
     * @param timezone      the target {@link TimezoneEnum}, must not be {@code null}
     * @return an {@code OffsetDateTime} representing the same local date-time with the offset
     * corresponding to the given timezone
     */
    public static OffsetDateTime localDateTimeToOffsetDateTime(LocalDateTime localDateTime, TimezoneEnum timezone) {
        return localDateTime.atZone(timezone.getZoneId()).toOffsetDateTime();
    }

    /**
     * Converts the current time from one timezone to another.
     *
     * <p>This method first generates the current {@link OffsetDateTime}
     * in the source timezone ({@code fromTimeZone}), and then converts
     * it to the target timezone ({@code toTimeZone}) while preserving
     * the exact instant in time.</p>
     *
     * <h3>Usage example:</h3>
     * <pre>{@code
     * OffsetDateTime tokyoTime = DateTimeUtils.convertCurrentTimeToAnotherTimeZone(
     *         TimezoneEnum.VST, // from Vietnam Standard Time
     *         TimezoneEnum.JST  // to Japan Standard Time
     * );
     * }</pre>
     *
     * @param fromTimeZone the source timezone enum (must not be {@code null})
     * @param toTimeZone   the target timezone enum (must not be {@code null})
     * @return an {@link OffsetDateTime} representing the current instant,
     * expressed in the target timezone
     */
    public static OffsetDateTime convertCurrentTimeToAnotherTimeZone(TimezoneEnum fromTimeZone, TimezoneEnum toTimeZone) {
        return generateCurrentTimeDefaultWithTimezone(fromTimeZone).withOffsetSameInstant(toTimeZone.getZoneOffset());
    }

    public static OffsetDateTime convertCurrentTimeToAnotherTimeZone(TimezoneEnum fromTimeZone, ZoneOffset toTimeZone) {
        return generateCurrentTimeDefaultWithTimezone(fromTimeZone).withOffsetSameInstant(toTimeZone);
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
        ACT, // Australia/Darwin
        AET, // Australia/Sydney
        AGT, // America/Argentina/Buenos_Aires
        ART, // Africa/Cairo
        AST, // America/Anchorage
        BET, // America/Sao_Paulo
        BST, // Asia/Dhaka
        CAT, // Africa/Harare
        CNT, // America/St_Johns
        CST, // America/Chicago
        CTT, // Asia/Shanghai
        EAT, // Africa/Addis_Ababa
        ECT, // Europe/Paris
        IET, // America/Indiana/Indianapolis
        IST, // Asia/Kolkata
        JST, // Asia/Tokyo
        MIT, // Pacific/Apia
        NET, // Asia/Yerevan
        NST, // Pacific/Auckland
        PLT, // Asia/Karachi
        PNT, // America/Phoenix
        PRT, // America/Puerto_Rico
        PST, // America/Los_Angeles
        SST, // Pacific/Guadalcanal
        VST, // Asia/Ho_Chi_Minh
        EST, // -05:00
        MST, // -07:00
        HST; // -10:00

        /**
         * Resolves the {@link ZoneId} for this timezone using {@link ZoneId#SHORT_IDS}.
         *
         * @return the ZoneId corresponding to this timezone
         */
        public ZoneId getZoneId() {
            return ZoneId.of(ZoneId.SHORT_IDS.get(this.name()));
        }

        /**
         * Gets the current {@link ZoneOffset} for this timezone based on the current instant.
         *
         * <p><b>Note:</b> For regions with Daylight Saving Time (DST), the result
         * may vary depending on the current date and time. For a specific date/time,
         * use {@link #getZoneOffset(LocalDateTime)} instead.</p>
         *
         * @return the current ZoneOffset for this timezone
         */
        public ZoneOffset getZoneOffset() {
            return getZoneId().getRules().getOffset(Instant.now());
        }

        /**
         * Gets the {@link ZoneOffset} for this timezone at a specific local date-time.
         *
         * @param localDateTime the local date-time to evaluate
         * @return the ZoneOffset valid at the given local date-time
         */
        public ZoneOffset getZoneOffset(LocalDateTime localDateTime) {
            final var zoneId = getZoneId();
            return zoneId.getRules().getOffset(localDateTime.atZone(zoneId).toInstant());
        }

    }

}
