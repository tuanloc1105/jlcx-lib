package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AESUtilsTest {

    // AES-128 requires a 16-byte key
    private static final String KEY_16 = "0123456789abcdef";
    // A different 16-byte key for cross-key tests
    private static final String KEY_16_ALT = "fedcba9876543210";

    @Test
    void encryptCBC_and_decryptCBC_roundTrip() throws Exception {
        String plainText = "Hello, AES CBC encryption!";

        String encrypted = AESUtils.encryptCBC(plainText, KEY_16);
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());
        assertNotEquals(plainText, encrypted);

        String decrypted = AESUtils.decryptCBC(encrypted, KEY_16);
        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptECB_and_decryptECB_roundTrip() throws Exception {
        String plainText = "Hello, AES ECB encryption!";

        String encrypted = AESUtils.encrypt(plainText, KEY_16);
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());
        assertNotEquals(plainText, encrypted);

        String decrypted = AESUtils.decrypt(encrypted, KEY_16);
        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptCBC_emptyInput() throws Exception {
        String plainText = "";

        String encrypted = AESUtils.encryptCBC(plainText, KEY_16);
        assertNotNull(encrypted);

        String decrypted = AESUtils.decryptCBC(encrypted, KEY_16);
        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptCBC_differentKeys_differentOutput() throws Exception {
        String plainText = "Same plain text for both keys";

        String encrypted1 = AESUtils.encryptCBC(plainText, KEY_16);
        String encrypted2 = AESUtils.encryptCBC(plainText, KEY_16_ALT);

        assertNotEquals(encrypted1, encrypted2,
                "Encrypting the same text with different keys should produce different ciphertext");
    }

    @Test
    void decryptCBC_wrongKey() {
        String plainText = "Secret message";

        assertThrows(Exception.class, () -> {
            String encrypted = AESUtils.encryptCBC(plainText, KEY_16);
            // Decrypting with a different key should either throw an exception
            // (BadPaddingException) or produce incorrect output
            String decrypted = AESUtils.decryptCBC(encrypted, KEY_16_ALT);
            // If no exception is thrown, the decrypted text must differ from the original
            assertNotEquals(plainText, decrypted);
        });
    }
}
