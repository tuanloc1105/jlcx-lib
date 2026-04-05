package vn.io.lcx.common.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class RSAUtilsTest {

    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;
    private static String publicKeyPEM;
    private static String privateKeyPEM;

    @BeforeAll
    static void generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // Build PEM strings from the generated keys
        String pubBase64 = Base64.getMimeEncoder(64, "\n".getBytes())
                .encodeToString(publicKey.getEncoded());
        publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" + pubBase64 + "\n-----END PUBLIC KEY-----";

        String privBase64 = Base64.getMimeEncoder(64, "\n".getBytes())
                .encodeToString(privateKey.getEncoded());
        privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" + privBase64 + "\n-----END PRIVATE KEY-----";
    }

    @Test
    void encrypt_and_decrypt_roundTrip() throws Exception {
        String plainText = "Hello, RSA encryption!";

        // Use the RSAPublicKey/RSAPrivateKey overloads (returns String)
        String encrypted = RSAUtils.encrypt(plainText, publicKey);
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());

        String decrypted = RSAUtils.decrypt(encrypted, privateKey);
        assertEquals(plainText, decrypted);
    }

    @Test
    void getPublicKey_validPEM_returnsKey() throws Exception {
        RSAPublicKey parsedKey = RSAUtils.getPublicKey(publicKeyPEM);

        assertNotNull(parsedKey);
        assertEquals(publicKey.getModulus(), parsedKey.getModulus());
        assertEquals(publicKey.getPublicExponent(), parsedKey.getPublicExponent());
    }

    @Test
    void getPrivateKey_validPEM_returnsKey() throws Exception {
        RSAPrivateKey parsedKey = RSAUtils.getPrivateKey(privateKeyPEM);

        assertNotNull(parsedKey);
        assertEquals(privateKey.getModulus(), parsedKey.getModulus());
        assertEquals(privateKey.getPrivateExponent(), parsedKey.getPrivateExponent());
    }

    @Test
    void encrypt_withStringKey_roundTrip() throws Exception {
        String plainText = "Testing RSA with PEM string keys";

        // Use the String key overloads: encrypt returns byte[], decrypt takes base64 String
        byte[] encryptedBytes = RSAUtils.encrypt(plainText, publicKeyPEM);
        assertNotNull(encryptedBytes);
        assertTrue(encryptedBytes.length > 0);

        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
        String decrypted = RSAUtils.decrypt(encryptedBase64, privateKeyPEM);
        assertEquals(plainText, decrypted);
    }
}
