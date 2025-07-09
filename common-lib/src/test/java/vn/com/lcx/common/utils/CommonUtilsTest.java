package vn.com.lcx.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonUtilsTest {
    @Test
    public void testGenerateRandom12DigitNumber() {
        String number = CommonUtils.generateRandom12DigitNumber();
        assertNotNull(number);
        assertEquals(12, number.length());
        assertTrue(number.matches("\\d{12}"));
    }

    @Test
    public void testGcDoesNotThrow() {
        assertDoesNotThrow(CommonUtils::gc);
    }

    @Test
    public void testBannerLoggingDoesNotThrow() {
        // No resource needed, just check no exception
        assertDoesNotThrow(() -> CommonUtils.bannerLogging("nonexistent-banner.txt"));
    }
} 
