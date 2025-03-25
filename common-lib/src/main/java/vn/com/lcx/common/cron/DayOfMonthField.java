package vn.com.lcx.common.cron;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;

@SuppressWarnings("ListIndexOfReplaceableByContains")
public class DayOfMonthField extends BasicField {
    DayOfMonthField(String fieldExpr) {
        super(CronFieldType.DAY_OF_MONTH, fieldExpr);
    }

    boolean matches(LocalDate dato) {
        for (FieldPart part : parts) {
            if ("L".equals(part.getModifier())) {
                YearMonth ym = YearMonth.of(dato.getYear(), dato.getMonth().getValue());
                return dato.getDayOfMonth() == (ym.lengthOfMonth() - (part.getFrom() == -1 ? 0 : part.getFrom()));
            } else if ("W".equals(part.getModifier())) {
                if (dato.getDayOfWeek().getValue() <= 5) {
                    if (dato.getDayOfMonth() == part.getFrom()) {
                        return true;
                    } else if (dato.getDayOfWeek().getValue() == 5) {
                        return dato.plusDays(1).getDayOfMonth() == part.getFrom();
                    } else if (dato.getDayOfWeek().getValue() == 1) {
                        return dato.minusDays(1).getDayOfMonth() == part.getFrom();
                    }
                }
            } else if (matches(dato.getDayOfMonth(), part)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void validatePart(FieldPart part) {
        if (part.getModifier() != null && Arrays.asList("L", "W", "?").indexOf(part.getModifier()) == -1) {
            throw new IllegalArgumentException(String.format("Invalid modifier [%s]", part.getModifier()));
        } else if (part.getIncrementModifier() != null && !"/".equals(part.getIncrementModifier())) {
            throw new IllegalArgumentException(String.format("Invalid increment modifier [%s]", part.getIncrementModifier()));
        }
    }

    @Override
    protected boolean matches(int val, FieldPart part) {
        return "?".equals(part.getModifier()) || super.matches(val, part);
    }
}
