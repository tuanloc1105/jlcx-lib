package vn.com.lcx.common.cron;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum CronFieldType {
    SECOND(0, 59, null) {
        @Override
        int getValue(ZonedDateTime dateTime) {
            return dateTime.getSecond();
        }

        @Override
        ZonedDateTime setValue(ZonedDateTime dateTime, int value) {
            return dateTime.withSecond(value).withNano(0);
        }

        @Override
        ZonedDateTime overflow(ZonedDateTime dateTime) {
            return dateTime.plusMinutes(1).withSecond(0).withNano(0);
        }
    },
    MINUTE(0, 59, null) {
        @Override
        int getValue(ZonedDateTime dateTime) {
            return dateTime.getMinute();
        }

        @Override
        ZonedDateTime setValue(ZonedDateTime dateTime, int value) {
            return dateTime.withMinute(value).withSecond(0).withNano(0);
        }

        @Override
        ZonedDateTime overflow(ZonedDateTime dateTime) {
            return dateTime.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        }
    },
    HOUR(0, 23, null) {
        @Override
        int getValue(ZonedDateTime dateTime) {
            return dateTime.getHour();
        }

        @Override
        ZonedDateTime setValue(ZonedDateTime dateTime, int value) {
            return dateTime.withHour(value).withMinute(0).withSecond(0).withNano(0);
        }

        @Override
        ZonedDateTime overflow(ZonedDateTime dateTime) {
            return dateTime.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
    },
    DAY_OF_MONTH(1, 31, null) {
        @Override
        int getValue(ZonedDateTime dateTime) {
            return dateTime.getDayOfMonth();
        }

        @Override
        ZonedDateTime setValue(ZonedDateTime dateTime, int value) {
            return dateTime.withDayOfMonth(value).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }

        @Override
        ZonedDateTime overflow(ZonedDateTime dateTime) {
            return dateTime.plusMonths(1).withDayOfMonth(0).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
    },
    MONTH(1, 12, new ArrayList<>(Arrays.asList("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"))) {
        @Override
        int getValue(ZonedDateTime dateTime) {
            return dateTime.getMonthValue();
        }

        @Override
        ZonedDateTime setValue(ZonedDateTime dateTime, int value) {
            return dateTime.withMonth(value).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }

        @Override
        ZonedDateTime overflow(ZonedDateTime dateTime) {
            return dateTime.plusYears(1).withMonth(1).withHour(0).withDayOfMonth(1).withMinute(0).withSecond(0).withNano(0);
        }
    },
    DAY_OF_WEEK(1, 7, new ArrayList<>(Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"))) {
        @Override
        int getValue(ZonedDateTime dateTime) {
            return dateTime.getDayOfWeek().getValue();
        }

        @Override
        ZonedDateTime setValue(ZonedDateTime dateTime, int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        ZonedDateTime overflow(ZonedDateTime dateTime) {
            throw new UnsupportedOperationException();
        }
    };

    final int from, to;
    final List<String> names;

    CronFieldType(int from, int to, List<String> names) {
        this.from = from;
        this.to = to;
        this.names = names;
    }

    /**
     * @param dateTime {@link ZonedDateTime} instance
     * @return The field time or date value from {@code dateTime}
     */
    abstract int getValue(ZonedDateTime dateTime);

    /**
     * @param dateTime Initial {@link ZonedDateTime} instance to use
     * @param value    to set for this field in {@code dateTime}
     * @return {@link ZonedDateTime} with {@code value} set for this field and all smaller fields cleared
     */
    abstract ZonedDateTime setValue(ZonedDateTime dateTime, int value);

    /**
     * Handle when this field overflows and the next higher field should be incremented
     *
     * @param dateTime Initial {@link ZonedDateTime} instance to use
     * @return {@link ZonedDateTime} with the next greater field incremented and all smaller fields cleared
     */
    abstract ZonedDateTime overflow(ZonedDateTime dateTime);
}
