package vn.com.lcx.common.cron;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;

@SuppressWarnings("ListIndexOfReplaceableByContains")
public class DayOfWeekField extends BasicField {

    DayOfWeekField(String fieldExpr) {
        super(CronFieldType.DAY_OF_WEEK, fieldExpr);
    }

    boolean matches(LocalDate dato) {
        for (FieldPart part : parts) {
            if ("L".equals(part.getModifier())) {
                YearMonth ym = YearMonth.of(dato.getYear(), dato.getMonth().getValue());
                return dato.getDayOfWeek() == DayOfWeek.of(part.getFrom()) && dato.getDayOfMonth() > (ym.lengthOfMonth() - 7);
            } else if ("#".equals(part.getIncrementModifier())) {
                if (dato.getDayOfWeek() == DayOfWeek.of(part.getFrom())) {
                    int num = dato.getDayOfMonth() / 7;
                    return part.getIncrement() == (dato.getDayOfMonth() % 7 == 0 ? num : num + 1);
                }
                return false;
            } else if (matches(dato.getDayOfWeek().getValue(), part)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected int mapValue(String value) {
        // Use 1-7 for weedays, but 0 will also represent sunday (linux practice)
        return "0".equals(value) ? 7 : super.mapValue(value);
    }

    @Override
    protected boolean matches(int val, FieldPart part) {
        return "?".equals(part.getModifier()) || super.matches(val, part);
    }

    @Override
    protected void validatePart(FieldPart part) {
        if (part.getModifier() != null && Arrays.asList("L", "?").indexOf(part.getModifier()) == -1) {
            throw new IllegalArgumentException(String.format("Invalid modifier [%s]", part.getModifier()));
        } else if (part.getIncrementModifier() != null && Arrays.asList("/", "#").indexOf(part.getIncrementModifier()) == -1) {
            throw new IllegalArgumentException(String.format("Invalid increment modifier [%s]", part.getIncrementModifier()));
        }
    }
}
