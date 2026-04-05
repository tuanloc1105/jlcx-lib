package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NumberFormatUtilsTest {

    @Test
    void bigDecimalFormatter_formatsCorrectly() {
        // US locale uses comma as thousands separator and dot as decimal separator
        assertEquals("1,234.56", NumberFormatUtils.bigDecimalFormatter(new BigDecimal("1234.56")));
        assertEquals("0", NumberFormatUtils.bigDecimalFormatter(BigDecimal.ZERO));
        assertEquals("1,000,000", NumberFormatUtils.bigDecimalFormatter(new BigDecimal("1000000")));
        assertEquals("999.999", NumberFormatUtils.bigDecimalFormatter(new BigDecimal("999.999")));
        assertEquals("-1,234.56", NumberFormatUtils.bigDecimalFormatter(new BigDecimal("-1234.56")));
    }
}
