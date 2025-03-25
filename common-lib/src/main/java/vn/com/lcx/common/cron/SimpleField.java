package vn.com.lcx.common.cron;

import java.time.ZonedDateTime;

public class SimpleField extends BasicField {
    SimpleField(CronFieldType fieldType, String fieldExpr) {
        super(fieldType, fieldExpr);
    }

    public boolean matches(int val) {
        if (val >= fieldType.from && val <= fieldType.to) {
            for (FieldPart part : parts) {
                if (matches(val, part)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Find the next match for this field. If a match cannot be found force an overflow and increase the next
     * greatest field.
     *
     * @param dateTime {@link ZonedDateTime} array so the reference can be modified
     * @return {@code true} if a match was found for this field or {@code false} if the field overflowed
     */
    protected boolean nextMatch(ZonedDateTime[] dateTime) {
        int value = fieldType.getValue(dateTime[0]);

        for (FieldPart part : parts) {
            int nextMatch = nextMatch(value, part);
            if (nextMatch > -1) {
                if (nextMatch != value) {
                    dateTime[0] = fieldType.setValue(dateTime[0], nextMatch);
                }
                return true;
            }
        }

        dateTime[0] = fieldType.overflow(dateTime[0]);
        return false;
    }
}
