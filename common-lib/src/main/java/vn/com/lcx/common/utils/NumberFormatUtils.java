package vn.com.lcx.common.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class NumberFormatUtils {

    private static final Locale US_LOCALE = Locale.US;
    private static final Locale GERMANY_LOCALE = Locale.GERMANY;

    private NumberFormatUtils() {
    }

    public static String bigDecimalFormatter(BigDecimal inputBigDecimal) {
        NumberFormat formatter = NumberFormat.getNumberInstance(US_LOCALE);
        return formatter.format(inputBigDecimal);
    }
}
