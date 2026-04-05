package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleCipherHandlerTest {

    @Test
    void simpleEncrypt_and_simpleDecrypt_roundTrip() {
        String plainText = "Hello, World! This is a test message.";

        String encrypted = SimpleCipherHandler.simpleEncrypt(plainText);
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());
        assertNotEquals(plainText, encrypted);

        String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void simpleEncrypt_withCustomKey_roundTrip() {
        String plainText = "Custom key encryption test";
        String customKey = "myCustomSecretKey2024";

        String encrypted = SimpleCipherHandler.simpleEncrypt(plainText, customKey);
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());

        String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, customKey);
        assertEquals(plainText, decrypted);
    }

    @Test
    void simpleEncrypt_emptyInput() {
        String plainText = "";

        String encrypted = SimpleCipherHandler.simpleEncrypt(plainText);
        assertNotNull(encrypted);
        // Empty input should produce empty encrypted output
        assertTrue(encrypted.isEmpty());

        String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted);
        assertEquals(plainText, decrypted);
    }
}
