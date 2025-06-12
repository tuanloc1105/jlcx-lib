package vn.com.lcx.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AESUtilsTest {

    private static final String KEY_16 = "1234567890abcdef"; // 16 bytes key for AES-128
    private static final String DATA = "Hello, AES!";

    @Test
    void testEncryptDecryptCBC() throws Exception {
        String encrypted = AESUtils.encryptCBC(DATA, KEY_16);
        String decrypted = AESUtils.decryptCBC(encrypted, KEY_16);
        Assertions.assertEquals(DATA, decrypted);
    }

    @Test
    void testEncryptDecryptECB() throws Exception {
        String encrypted = AESUtils.encrypt(DATA, KEY_16);
        String decrypted = AESUtils.decrypt(encrypted, KEY_16);
        Assertions.assertEquals(DATA, decrypted);
    }

    @Test
    void testEncryptCBC_InvalidKey() {
        String invalidKey = "shortkey";
        Assertions.assertThrows(Exception.class, () -> AESUtils.encryptCBC(DATA, invalidKey));
    }

    @Test
    void testDecryptCBC_InvalidKey() throws Exception {
        String encrypted = AESUtils.encryptCBC(DATA, KEY_16);
        String invalidKey = "shortkey";
        Assertions.assertThrows(Exception.class, () -> AESUtils.decryptCBC(encrypted, invalidKey));
    }

    @Test
    void testEncryptECB_InvalidKey() {
        String invalidKey = "shortkey";
        Assertions.assertThrows(Exception.class, () -> AESUtils.encrypt(DATA, invalidKey));
    }

    @Test
    void testDecryptECB_InvalidKey() throws Exception {
        String encrypted = AESUtils.encrypt(DATA, KEY_16);
        String invalidKey = "shortkey";
        Assertions.assertThrows(Exception.class, () -> AESUtils.decrypt(encrypted, invalidKey));
    }
}
