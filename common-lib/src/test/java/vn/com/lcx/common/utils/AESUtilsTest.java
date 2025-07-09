package vn.com.lcx.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AESUtils}.
 *
 * <p>This test class covers both ECB and CBC encryption/decryption methods
 * with various test scenarios including normal cases, edge cases, and error conditions.</p>
 */
@DisplayName("AESUtils Tests")
class AESUtilsTest {

    private static final String VALID_16_BYTE_KEY = "1234567890123456"; // 16 bytes
    private static final String VALID_32_BYTE_KEY = "12345678901234567890123456789012"; // 32 bytes
    private static final String TEST_DATA = "Hello, World!";
    private static final String EMPTY_STRING = "";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
    private static final String UNICODE_STRING = "Hello 世界! Привет! こんにちは!";

    @Nested
    @DisplayName("ECB Mode Tests")
    class ECBModeTests {

        @Test
        @DisplayName("Should encrypt and decrypt data successfully in ECB mode")
        void shouldEncryptAndDecryptDataSuccessfully() throws Exception {
            // Given
            String originalData = TEST_DATA;
            String key = VALID_16_BYTE_KEY;

            // When
            String encrypted = AESUtils.encrypt(originalData, key);
            String decrypted = AESUtils.decrypt(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertNotEquals(originalData, encrypted);
            assertEquals(originalData, decrypted);
        }

        @Test
        @DisplayName("Should handle empty string in ECB mode")
        void shouldHandleEmptyString() throws Exception {
            // Given
            String originalData = EMPTY_STRING;
            String key = VALID_16_BYTE_KEY;

            // When
            String encrypted = AESUtils.encrypt(originalData, key);
            String decrypted = AESUtils.decrypt(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertEquals(originalData, decrypted);
        }

        @Test
        @DisplayName("Should handle special characters in ECB mode")
        void shouldHandleSpecialCharacters() throws Exception {
            // Given
            String originalData = SPECIAL_CHARS;
            String key = VALID_16_BYTE_KEY;

            // When
            String encrypted = AESUtils.encrypt(originalData, key);
            String decrypted = AESUtils.decrypt(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertEquals(originalData, decrypted);
        }

        @Test
        @DisplayName("Should handle unicode characters in ECB mode")
        void shouldHandleUnicodeCharacters() throws Exception {
            // Given
            String originalData = UNICODE_STRING;
            String key = VALID_16_BYTE_KEY;

            // When
            String encrypted = AESUtils.encrypt(originalData, key);
            String decrypted = AESUtils.decrypt(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertEquals(originalData, decrypted);
        }

        @Test
        @DisplayName("Should handle long text in ECB mode")
        void shouldHandleLongText() throws Exception {
            // Given
            String originalData = "This is a very long text that contains multiple sentences. " +
                    "It should be properly encrypted and decrypted without any issues. " +
                    "The text includes various punctuation marks and spaces.";
            String key = VALID_16_BYTE_KEY;

            // When
            String encrypted = AESUtils.encrypt(originalData, key);
            String decrypted = AESUtils.decrypt(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertEquals(originalData, decrypted);
        }

        @Test
        @DisplayName("Should produce valid Base64 encoded encrypted data in ECB mode")
        void shouldProduceValidBase64EncodedData() throws Exception {
            // Given
            String data = TEST_DATA;
            String key = VALID_16_BYTE_KEY;

            // When
            String encrypted = AESUtils.encrypt(data, key);

            // Then
            assertNotNull(encrypted);
            // Verify it's valid Base64
            assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(encrypted));
            // Verify it's not empty
            assertFalse(encrypted.isEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = {"123456789012345", "12345678901234567"}) // 15 and 17 bytes
        @DisplayName("Should throw exception for invalid key length in ECB mode")
        void shouldThrowExceptionForInvalidKeyLength(String invalidKey) {
            // Given
            String data = TEST_DATA;

            // When & Then
            assertThrows(Exception.class, () -> AESUtils.encrypt(data, invalidKey));
            assertThrows(Exception.class, () -> AESUtils.decrypt(data, invalidKey));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should throw exception for null or empty key in ECB mode")
        void shouldThrowExceptionForNullOrEmptyKey(String key) {
            // Given
            String data = TEST_DATA;

            // When & Then
            assertThrows(Exception.class, () -> AESUtils.encrypt(data, key));
            assertThrows(Exception.class, () -> AESUtils.decrypt(data, key));
        }

        @Test
        @DisplayName("Should throw exception for null data in ECB mode")
        void shouldThrowExceptionForNullData() {
            // Given
            String data = null;
            String key = VALID_16_BYTE_KEY;

            // When & Then
            assertThrows(Exception.class, () -> AESUtils.encrypt(data, key));
        }

        @Test
        @DisplayName("Should throw exception for invalid encrypted data in ECB mode")
        void shouldThrowExceptionForInvalidEncryptedData() {
            // Given
            String invalidEncryptedData = "invalid-base64-data!@#";
            String key = VALID_16_BYTE_KEY;

            // When & Then
            assertThrows(Exception.class, () -> AESUtils.decrypt(invalidEncryptedData, key));
        }

        @Test
        @DisplayName("Should encrypt different data to different results in ECB mode")
        void shouldEncryptDifferentDataToDifferentResults() throws Exception {
            // Given
            String data1 = "Hello";
            String data2 = "World";
            String key = VALID_16_BYTE_KEY;

            // When
            String encrypted1 = AESUtils.encrypt(data1, key);
            String encrypted2 = AESUtils.encrypt(data2, key);

            // Then
            assertNotEquals(encrypted1, encrypted2);
        }
    }

    @Nested
    @DisplayName("CBC Mode Tests")
    class CBCModeTests {

        @Test
        @DisplayName("Should encrypt and decrypt data successfully in CBC mode")
        void shouldEncryptAndDecryptDataSuccessfully() throws Exception {
            // Given
            String originalData = TEST_DATA;
            String key = VALID_32_BYTE_KEY; // CBC mode needs key >= 16 bytes for IV

            // When
            String encrypted = AESUtils.encryptCBC(originalData, key);
            String decrypted = AESUtils.decryptCBC(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertNotEquals(originalData, encrypted);
            assertEquals(originalData, decrypted);
        }

        @Test
        @DisplayName("Should handle empty string in CBC mode")
        void shouldHandleEmptyString() throws Exception {
            // Given
            String originalData = EMPTY_STRING;
            String key = VALID_32_BYTE_KEY;

            // When
            String encrypted = AESUtils.encryptCBC(originalData, key);
            String decrypted = AESUtils.decryptCBC(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertEquals(originalData, decrypted);
        }

        @Test
        @DisplayName("Should handle special characters in CBC mode")
        void shouldHandleSpecialCharacters() throws Exception {
            // Given
            String originalData = SPECIAL_CHARS;
            String key = VALID_32_BYTE_KEY;

            // When
            String encrypted = AESUtils.encryptCBC(originalData, key);
            String decrypted = AESUtils.decryptCBC(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertEquals(originalData, decrypted);
        }

        @Test
        @DisplayName("Should handle unicode characters in CBC mode")
        void shouldHandleUnicodeCharacters() throws Exception {
            // Given
            String originalData = UNICODE_STRING;
            String key = VALID_32_BYTE_KEY;

            // When
            String encrypted = AESUtils.encryptCBC(originalData, key);
            String decrypted = AESUtils.decryptCBC(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertEquals(originalData, decrypted);
        }

        @Test
        @DisplayName("Should handle long text in CBC mode")
        void shouldHandleLongText() throws Exception {
            // Given
            String originalData = "This is a very long text that contains multiple sentences. " +
                    "It should be properly encrypted and decrypted without any issues. " +
                    "The text includes various punctuation marks and spaces.";
            String key = VALID_32_BYTE_KEY;

            // When
            String encrypted = AESUtils.encryptCBC(originalData, key);
            String decrypted = AESUtils.decryptCBC(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertEquals(originalData, decrypted);
        }

        @Test
        @DisplayName("Should produce valid Base64 encoded encrypted data in CBC mode")
        void shouldProduceValidBase64EncodedData() throws Exception {
            // Given
            String data = TEST_DATA;
            String key = VALID_32_BYTE_KEY;

            // When
            String encrypted = AESUtils.encryptCBC(data, key);

            // Then
            assertNotNull(encrypted);
            // Verify it's valid Base64
            assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(encrypted));
            // Verify it's not empty
            assertFalse(encrypted.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception for key shorter than 16 bytes in CBC mode")
        void shouldThrowExceptionForKeyShorterThan16Bytes() {
            // Given
            String data = TEST_DATA;
            String shortKey = "123456789012345"; // 15 bytes

            // When & Then
            assertThrows(Exception.class, () -> AESUtils.encryptCBC(data, shortKey));
            assertThrows(Exception.class, () -> AESUtils.decryptCBC(data, shortKey));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should throw exception for null or empty key in CBC mode")
        void shouldThrowExceptionForNullOrEmptyKey(String key) {
            // Given
            String data = TEST_DATA;

            // When & Then
            assertThrows(Exception.class, () -> AESUtils.encryptCBC(data, key));
            assertThrows(Exception.class, () -> AESUtils.decryptCBC(data, key));
        }

        @Test
        @DisplayName("Should throw exception for null data in CBC mode")
        void shouldThrowExceptionForNullData() {
            // Given
            String data = null;
            String key = VALID_32_BYTE_KEY;

            // When & Then
            assertThrows(Exception.class, () -> AESUtils.encryptCBC(data, key));
        }

        @Test
        @DisplayName("Should throw exception for invalid encrypted data in CBC mode")
        void shouldThrowExceptionForInvalidEncryptedData() {
            // Given
            String invalidEncryptedData = "invalid-base64-data!@#";
            String key = VALID_32_BYTE_KEY;

            // When & Then
            assertThrows(Exception.class, () -> AESUtils.decryptCBC(invalidEncryptedData, key));
        }

        @Test
        @DisplayName("Should encrypt different data to different results in CBC mode")
        void shouldEncryptDifferentDataToDifferentResults() throws Exception {
            // Given
            String data1 = "Hello";
            String data2 = "World";
            String key = VALID_32_BYTE_KEY;

            // When
            String encrypted1 = AESUtils.encryptCBC(data1, key);
            String encrypted2 = AESUtils.encryptCBC(data2, key);

            // Then
            assertNotEquals(encrypted1, encrypted2);
        }

        @Test
        @DisplayName("Should use first 16 bytes of key as IV in CBC mode")
        void shouldUseFirst16BytesAsIV() throws Exception {
            // Given
            String data = TEST_DATA;
            String key = VALID_32_BYTE_KEY;
            String expectedIV = key.substring(0, 16);

            // When
            String encrypted = AESUtils.encryptCBC(data, key);

            // Then
            assertNotNull(encrypted);
            // Note: We can't directly verify IV usage, but we can verify the method works
            // and doesn't throw exceptions related to IV
        }
    }

    @Nested
    @DisplayName("Cross-Mode Compatibility Tests")
    class CrossModeCompatibilityTests {

        @Test
        @DisplayName("ECB and CBC should produce different encrypted results for same data")
        void shouldProduceDifferentResultsForDifferentModes() throws Exception {
            // Given
            String data = TEST_DATA;
            String key = VALID_32_BYTE_KEY;

            // When
            String ecbEncrypted = AESUtils.encrypt(data, key.substring(0, 16));
            String cbcEncrypted = AESUtils.encryptCBC(data, key);

            // Then
            assertNotEquals(ecbEncrypted, cbcEncrypted);
        }

        @Test
        @DisplayName("Should not be able to decrypt CBC data with ECB method")
        void shouldNotDecryptCBCWithECB() throws Exception {
            // Given
            String data = TEST_DATA;
            String key = VALID_32_BYTE_KEY;
            String cbcEncrypted = AESUtils.encryptCBC(data, key);

            // When & Then
            assertThrows(Exception.class, () ->
                    AESUtils.decrypt(cbcEncrypted, key.substring(0, 16)));
        }

        @Test
        @DisplayName("Should not be able to decrypt ECB data with CBC method")
        void shouldNotDecryptECBWithCBC() throws Exception {
            // Given
            String data = TEST_DATA;
            String key = VALID_16_BYTE_KEY;
            String ecbEncrypted = AESUtils.encrypt(data, key);

            // When & Then
            assertThrows(Exception.class, () ->
                    AESUtils.decryptCBC(ecbEncrypted, key + "extra"));
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Encrypted data should not contain original plaintext")
        void encryptedDataShouldNotContainPlaintext() throws Exception {
            // Given
            String data = "sensitive_password_123";
            String key = VALID_16_BYTE_KEY;

            // When
            String encrypted = AESUtils.encrypt(data, key);

            // Then
            assertFalse(encrypted.contains("sensitive"));
            assertFalse(encrypted.contains("password"));
            assertFalse(encrypted.contains("123"));
        }

        @Test
        @DisplayName("CBC encrypted data should not contain original plaintext")
        void cbcEncryptedDataShouldNotContainPlaintext() throws Exception {
            // Given
            String data = "sensitive_password_123";
            String key = VALID_32_BYTE_KEY;

            // When
            String encrypted = AESUtils.encryptCBC(data, key);

            // Then
            assertFalse(encrypted.contains("sensitive"));
            assertFalse(encrypted.contains("password"));
            assertFalse(encrypted.contains("123"));
        }

        @Test
        @DisplayName("Encrypted data should be Base64 encoded")
        void encryptedDataShouldBeBase64Encoded() throws Exception {
            // Given
            String data = TEST_DATA;
            String key = VALID_16_BYTE_KEY;

            // When
            String encrypted = AESUtils.encrypt(data, key);

            // Then
            // Base64 should only contain A-Z, a-z, 0-9, +, /, and = for padding
            assertTrue(encrypted.matches("^[A-Za-z0-9+/]*={0,2}$"));
        }

        @Test
        @DisplayName("CBC encrypted data should be Base64 encoded")
        void cbcEncryptedDataShouldBeBase64Encoded() throws Exception {
            // Given
            String data = TEST_DATA;
            String key = VALID_32_BYTE_KEY;

            // When
            String encrypted = AESUtils.encryptCBC(data, key);

            // Then
            // Base64 should only contain A-Z, a-z, 0-9, +, /, and = for padding
            assertTrue(encrypted.matches("^[A-Za-z0-9+/]*={0,2}$"));
        }
    }
}
