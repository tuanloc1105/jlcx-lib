package vn.com.lcx.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BCryptUtilsTest {
    @Test
    void testHashPasswordAndComparePassword_Success() {
        String password = "My-Super-Secret-Password-123";
        String hash = BCryptUtils.hashPassword(password);
        BCryptUtils.comparePassword(password, hash);
    }

    @Test
    void testHashPassword_BlankPassword() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> BCryptUtils.hashPassword("")
        );
        Assertions.assertThrows(
                NullPointerException.class,
                () -> BCryptUtils.hashPassword(null)
        );
    }

    @Test
    void testComparePassword_BlankPasswordOrHash() {
        String hash = BCryptUtils.hashPassword("My-Super-Secret-Password-123");
        Assertions.assertThrows(
                NullPointerException.class,
                () -> BCryptUtils.comparePassword("", hash)
        );
        Assertions.assertThrows(
                NullPointerException.class,
                () -> BCryptUtils.comparePassword(null, hash)
        );
        Assertions.assertThrows(
                NullPointerException.class,
                () -> BCryptUtils.comparePassword("My-Super-Secret-Password-123", "")
        );
        Assertions.assertThrows(
                NullPointerException.class,
                () -> BCryptUtils.comparePassword("My-Super-Secret-Password-123", null)
        );
    }

    @Test
    void testComparePassword_WrongPassword() {
        String hash = BCryptUtils.hashPassword("My-Super-Secret-Password-123");
        Assertions.assertThrows(IllegalArgumentException.class, () -> BCryptUtils.comparePassword("wrongpass", hash));
    }
}
