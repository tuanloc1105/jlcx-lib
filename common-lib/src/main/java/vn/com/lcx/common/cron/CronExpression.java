package vn.com.lcx.common.cron;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CronExpression {
    private final String expr;
    private final SimpleField secondField;
    private final SimpleField minuteField;
    private final SimpleField hourField;
    private final DayOfWeekField dayOfWeekField;
    private final SimpleField monthField;
    private final DayOfMonthField dayOfMonthField;

    public CronExpression(final String expr) {
        this(expr, true);
    }

    public CronExpression(final String expr, final boolean withSeconds) {
        if (expr == null) {
            throw new IllegalArgumentException("expr is null"); //$NON-NLS-1$
        }

        this.expr = expr;

        final int expectedParts = withSeconds ? 6 : 5;
        final String[] parts = expr.split("\\s+"); //$NON-NLS-1$
        if (parts.length != expectedParts) {
            throw new IllegalArgumentException(String.format("Invalid cron expression [%s], expected %s field, got %s", expr, expectedParts, parts.length));
        }

        int ix = withSeconds ? 1 : 0;
        this.secondField = new SimpleField(CronFieldType.SECOND, withSeconds ? parts[0] : "0");
        this.minuteField = new SimpleField(CronFieldType.MINUTE, parts[ix++]);
        this.hourField = new SimpleField(CronFieldType.HOUR, parts[ix++]);
        this.dayOfMonthField = new DayOfMonthField(parts[ix++]);
        this.monthField = new SimpleField(CronFieldType.MONTH, parts[ix++]);
        this.dayOfWeekField = new DayOfWeekField(parts[ix++]);
    }

    public static CronExpression create(final String expr) {
        return new CronExpression(expr, true);
    }

    public static CronExpression createWithoutSeconds(final String expr) {
        return new CronExpression(expr, false);
    }

    private static void checkIfDateTimeBarrierIsReached(ZonedDateTime nextTime, ZonedDateTime dateTimeBarrier) {
        if (nextTime.isAfter(dateTimeBarrier)) {
            throw new IllegalArgumentException("No next execution time could be determined that is before the limit of " + dateTimeBarrier);
        }
    }

    public ZonedDateTime nextTimeAfter(ZonedDateTime afterTime) {
        // will search for the next time within the next 4 years. If there is no
        // time matching, an InvalidArgumentException will be thrown (it is very
        // likely that the cron expression is invalid, like the February 30th).
        return nextTimeAfter(afterTime, afterTime.plusYears(4));
    }

    public LocalDateTime nextLocalDateTimeAfter(LocalDateTime dateTime) {
        return nextTimeAfter(ZonedDateTime.of(dateTime, ZoneId.systemDefault())).toLocalDateTime();
    }

    public ZonedDateTime nextTimeAfter(ZonedDateTime afterTime, long durationInMillis) {
        // will search for the next time within the next durationInMillis
        // millisecond. Be aware that the duration is specified in millis,
        // but in fact the limit is checked on a day-to-day basis.
        return nextTimeAfter(afterTime, afterTime.plus(Duration.ofMillis(durationInMillis)));
    }

    public ZonedDateTime nextTimeAfter(ZonedDateTime afterTime, ZonedDateTime dateTimeBarrier) {
        ZonedDateTime[] nextDateTime = {afterTime.plusSeconds(1).withNano(0)};

        while (true) {
            checkIfDateTimeBarrierIsReached(nextDateTime[0], dateTimeBarrier);
            if (!monthField.nextMatch(nextDateTime)) {
                continue;
            }
            if (!findDay(nextDateTime, dateTimeBarrier)) {
                continue;
            }
            if (!hourField.nextMatch(nextDateTime)) {
                continue;
            }
            if (!minuteField.nextMatch(nextDateTime)) {
                continue;
            }
            if (!secondField.nextMatch(nextDateTime)) {
                continue;
            }

            checkIfDateTimeBarrierIsReached(nextDateTime[0], dateTimeBarrier);
            return nextDateTime[0];
        }
    }

    /**
     * Find the next match for the day field.
     * <p>
     * This is handled different than all other fields because there are two ways to describe the day and it is easier
     * to handle them together in the same method.
     *
     * @param dateTime        Initial {@link ZonedDateTime} instance to start from
     * @param dateTimeBarrier At which point stop searching for next execution time
     * @return {@code true} if a match was found for this field or {@code false} if the field overflowed
     * @see {@link SimpleField#nextMatch(ZonedDateTime[])}
     */
    private boolean findDay(ZonedDateTime[] dateTime, ZonedDateTime dateTimeBarrier) {
        int month = dateTime[0].getMonthValue();

        while (!(dayOfMonthField.matches(dateTime[0].toLocalDate())
                && dayOfWeekField.matches(dateTime[0].toLocalDate()))) {
            dateTime[0] = dateTime[0].plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            if (dateTime[0].getMonthValue() != month) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + expr + ">";
    }

}
