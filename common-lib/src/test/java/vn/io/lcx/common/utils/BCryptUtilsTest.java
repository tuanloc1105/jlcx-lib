package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptUtilsTest {

    @Test
    void hashPassword_returnsNonNullHash() {
        String password = "mySecurePassword123";

        String hash = BCryptUtils.hashPassword(password);

        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertNotEquals(password, hash);
    }

    @Test
    void hashPassword_sameInput_differentHash() {
        String password = "mySecurePassword123";

        String hash1 = BCryptUtils.hashPassword(password);
        String hash2 = BCryptUtils.hashPassword(password);

        assertNotEquals(hash1, hash2,
                "BCrypt should produce different hashes for the same password due to random salt");
    }

    @Test
    void comparePassword_correctPassword_returnsTrue() {
        String password = "correctPassword";
        String hash = BCryptUtils.hashPassword(password);

        // comparePassword does not return a value; it throws if wrong.
        // No exception means the password matches.
        assertDoesNotThrow(() -> BCryptUtils.comparePassword(password, hash));
    }

    @Test
    void comparePassword_wrongPassword_returnsFalse() {
        String password = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hash = BCryptUtils.hashPassword(password);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BCryptUtils.comparePassword(wrongPassword, hash)
        );
        assertEquals("password or passHash is not correct", exception.getMessage());
    }
}
