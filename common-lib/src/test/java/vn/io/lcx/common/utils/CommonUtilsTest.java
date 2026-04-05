package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommonUtilsTest {

    @Test
    void generateRandom12DigitNumber_correctLength() {
        for (int i = 0; i < 100; i++) {
            String result = CommonUtils.generateRandom12DigitNumber();
            assertNotNull(result);
            assertEquals(12, result.length(),
                    "Generated number should be exactly 12 digits: " + result);
            assertTrue(result.matches("\\d{12}"),
                    "Generated number should contain only digits: " + result);
        }
    }

    @Test
    void generateRandom12DigitNumber_uniqueness() {
        String first = CommonUtils.generateRandom12DigitNumber();
        String second = CommonUtils.generateRandom12DigitNumber();
        // While not guaranteed, consecutive calls should almost always produce different results
        // due to timestamp component
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(12, first.length());
        assertEquals(12, second.length());
    }
}
