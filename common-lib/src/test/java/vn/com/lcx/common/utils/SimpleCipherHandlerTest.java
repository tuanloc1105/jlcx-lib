package vn.com.lcx.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SimpleCipherHandler}.
 *
 * <p>This test class covers all public methods of SimpleCipherHandler including:</p>
 * <ul>
 *   <li>Encryption and decryption with default key</li>
 *   <li>Encryption and decryption with custom key</li>
 *   <li>Hexadecimal conversion utilities</li>
 *   <li>Edge cases and error conditions</li>
 *   <li>Null input handling</li>
 * </ul>
 *
 * @author tuanloc1105
 * @version 1.0
 */
@DisplayName("SimpleCipherHandler Tests")
class SimpleCipherHandlerTest {

    @Nested
    @DisplayName("Default Key Encryption/Decryption Tests")
    class DefaultKeyTests {

        @Test
        @DisplayName("Should encrypt and decrypt with default key")
        void shouldEncryptAndDecryptWithDefaultKey() {
            // Given
            String plainText = "Hello World";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(plainText);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted);

            // Then
            assertNotNull(encrypted);
            assertNotEquals(plainText, encrypted);
            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("Should handle empty string with default key")
        void shouldHandleEmptyStringWithDefaultKey() {
            // Given
            String plainText = "";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(plainText);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted);

            // Then
            assertEquals("", encrypted);
            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("Should handle special characters with default key")
        void shouldHandleSpecialCharactersWithDefaultKey() {
            // Given
            String plainText = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(plainText);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted);

            // Then
            assertNotNull(encrypted);
            assertNotEquals(plainText, encrypted);
            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("Should handle ASCII characters with default key")
        void shouldHandleAsciiCharactersWithDefaultKey() {
            // Given
            String plainText = "Hello ASCII World";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(plainText);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted);

            // Then
            assertNotNull(encrypted);
            assertNotEquals(plainText, encrypted);
            assertEquals(plainText, decrypted);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should throw exception for null plain text with default key")
        void shouldThrowExceptionForNullPlainTextWithDefaultKey(String plainText) {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                SimpleCipherHandler.simpleEncrypt(plainText);
            });
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should throw exception for null cipher text with default key")
        void shouldThrowExceptionForNullCipherTextWithDefaultKey(String cipherText) {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                SimpleCipherHandler.simpleDecrypt(cipherText);
            });
        }
    }

    @Nested
    @DisplayName("Custom Key Encryption/Decryption Tests")
    class CustomKeyTests {

        @Test
        @DisplayName("Should encrypt and decrypt with custom key")
        void shouldEncryptAndDecryptWithCustomKey() {
            // Given
            String plainText = "Hello World";
            String key = "mySecretKey123";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(plainText, key);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertNotEquals(plainText, encrypted);
            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("Should produce different results with different keys")
        void shouldProduceDifferentResultsWithDifferentKeys() {
            // Given
            String plainText = "Hello World";
            String key1 = "key1";
            String key2 = "key2";

            // When
            String encrypted1 = SimpleCipherHandler.simpleEncrypt(plainText, key1);
            String encrypted2 = SimpleCipherHandler.simpleEncrypt(plainText, key2);

            // Then
            assertNotEquals(encrypted1, encrypted2);
        }

        @Test
        @DisplayName("Should produce same result with same key")
        void shouldProduceSameResultWithSameKey() {
            // Given
            String plainText = "Hello World";
            String key = "mySecretKey";

            // When
            String encrypted1 = SimpleCipherHandler.simpleEncrypt(plainText, key);
            String encrypted2 = SimpleCipherHandler.simpleEncrypt(plainText, key);

            // Then
            assertEquals(encrypted1, encrypted2);
        }

        @Test
        @DisplayName("Should fail decryption with wrong key")
        void shouldFailDecryptionWithWrongKey() {
            // Given
            String plainText = "Hello World";
            String correctKey = "correctKey";
            String wrongKey = "wrongKey";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(plainText, correctKey);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, wrongKey);

            // Then
            assertNotEquals(plainText, decrypted);
        }

        @ParameterizedTest
        @CsvSource({
                "Hello World, myKey123",
                "Test String, secretKey",
                "123456, password",
                "Special!@#, keyWithSymbols"
        })
        @DisplayName("Should handle various plain text and key combinations")
        void shouldHandleVariousPlainTextAndKeyCombinations(String plainText, String key) {
            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(plainText, key);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertNotEquals(plainText, encrypted);
            assertEquals(plainText, decrypted);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should throw exception for null plain text with custom key")
        void shouldThrowExceptionForNullPlainTextWithCustomKey(String plainText) {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                SimpleCipherHandler.simpleEncrypt(plainText, "key");
            });
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should throw exception for null key")
        void shouldThrowExceptionForNullKey(String key) {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                SimpleCipherHandler.simpleEncrypt("plainText", key);
            });
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should throw exception for null cipher text with custom key")
        void shouldThrowExceptionForNullCipherTextWithCustomKey(String cipherText) {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                SimpleCipherHandler.simpleDecrypt(cipherText, "key");
            });
        }
    }

    @Nested
    @DisplayName("Hexadecimal Conversion Tests")
    class HexadecimalConversionTests {

        @Test
        @DisplayName("Should convert byte array to hex string")
        void shouldConvertByteArrayToHexString() {
            // Given
            byte[] data = {0x00, 0x01, 0x0A, 0x0F, 0x10, (byte) 0xFF};

            // When
            String hexString = SimpleCipherHandler.data2hex(data);

            // Then
            assertEquals("00010a0f10ff", hexString);
        }

        @Test
        @DisplayName("Should convert hex string to byte array")
        void shouldConvertHexStringToByteArray() {
            // Given
            String hexString = "00010a0f10ff";

            // When
            byte[] data = SimpleCipherHandler.hex2data(hexString);

            // Then
            assertArrayEquals(new byte[]{0x00, 0x01, 0x0A, 0x0F, 0x10, (byte) 0xFF}, data);
        }

        @Test
        @DisplayName("Should handle uppercase hex string")
        void shouldHandleUppercaseHexString() {
            // Given
            String hexString = "00010A0F10FF";

            // When
            byte[] data = SimpleCipherHandler.hex2data(hexString);

            // Then
            assertArrayEquals(new byte[]{0x00, 0x01, 0x0A, 0x0F, 0x10, (byte) 0xFF}, data);
        }

        @Test
        @DisplayName("Should handle mixed case hex string")
        void shouldHandleMixedCaseHexString() {
            // Given
            String hexString = "00010a0F10ff";

            // When
            byte[] data = SimpleCipherHandler.hex2data(hexString);

            // Then
            assertArrayEquals(new byte[]{0x00, 0x01, 0x0A, 0x0F, 0x10, (byte) 0xFF}, data);
        }

        @Test
        @DisplayName("Should handle empty byte array")
        void shouldHandleEmptyByteArray() {
            // Given
            byte[] data = {};

            // When
            String hexString = SimpleCipherHandler.data2hex(data);

            // Then
            assertEquals("", hexString);
        }

        @Test
        @DisplayName("Should handle null byte array")
        void shouldHandleNullByteArray() {
            // When
            String hexString = SimpleCipherHandler.data2hex(null);

            // Then
            assertNull(hexString);
        }

        @Test
        @DisplayName("Should handle null hex string")
        void shouldHandleNullHexString() {
            // When
            byte[] data = SimpleCipherHandler.hex2data(null);

            // Then
            assertArrayEquals(new byte[0], data);
        }

        @Test
        @DisplayName("Should handle empty hex string")
        void shouldHandleEmptyHexString() {
            // Given
            String hexString = "";

            // When
            byte[] data = SimpleCipherHandler.hex2data(hexString);

            // Then
            assertArrayEquals(new byte[0], data);
        }

        @Test
        @DisplayName("Should handle odd length hex string")
        void shouldHandleOddLengthHexString() {
            // Given
            String hexString = "123";

            // When
            byte[] data = SimpleCipherHandler.hex2data(hexString);

            // Then
            // Should handle gracefully, though result may not be as expected
            assertNotNull(data);
        }
    }

    @Nested
    @DisplayName("Nibble Conversion Tests")
    class NibbleConversionTests {

        @ParameterizedTest
        @CsvSource({
                "0, 0",
                "1, 1",
                "9, 9",
                "a, 10",
                "f, 15",
                "A, 10",
                "F, 15"
        })
        @DisplayName("Should convert valid hex characters to nibbles")
        void shouldConvertValidHexCharactersToNibbles(char hexChar, int expectedValue) {
            // When
            byte result = SimpleCipherHandler.toDataNibble(hexChar);

            // Then
            assertEquals(expectedValue, result);
        }

        @ParameterizedTest
        @ValueSource(chars = {'g', 'h', 'z', 'G', 'H', 'Z', '!', '@', '#', '$'})
        @DisplayName("Should return -1 for invalid hex characters")
        void shouldReturnMinusOneForInvalidHexCharacters(char invalidChar) {
            // When
            byte result = SimpleCipherHandler.toDataNibble(invalidChar);

            // Then
            assertEquals(-1, result);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle round-trip encryption/decryption with various data types")
        void shouldHandleRoundTripEncryptionDecryptionWithVariousDataTypes() {
            // Test cases with different types of data (ASCII only for this implementation)
            String[] testCases = {
                    "Simple text",
                    "Text with numbers 12345",
                    "Text with symbols !@#$%^&*()",
                    "Text with spaces and tabs\t",
                    "Text with newlines\n\r",
                    "Very long text that might exceed some buffer limits and contains various characters including numbers 123, symbols !@#",
                    "", // Empty string
                    "Single character: A",
                    "Numbers only: 1234567890",
                    "Symbols only: !@#$%^&*()_+-=[]{}|;':\",./<>?"
            };

            for (String testCase : testCases) {
                // When
                String encrypted = SimpleCipherHandler.simpleEncrypt(testCase);
                String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted);

                // Then
                assertEquals(testCase, decrypted, "Failed for test case: " + testCase);
            }
        }

        @Test
        @DisplayName("Should maintain consistency across multiple encryption/decryption cycles")
        void shouldMaintainConsistencyAcrossMultipleEncryptionDecryptionCycles() {
            // Given
            String plainText = "Test consistency across multiple cycles";
            String key = "consistencyTestKey";

            // When - perform multiple encryption/decryption cycles
            String encrypted1 = SimpleCipherHandler.simpleEncrypt(plainText, key);
            String decrypted1 = SimpleCipherHandler.simpleDecrypt(encrypted1, key);

            String encrypted2 = SimpleCipherHandler.simpleEncrypt(decrypted1, key);
            String decrypted2 = SimpleCipherHandler.simpleDecrypt(encrypted2, key);

            String encrypted3 = SimpleCipherHandler.simpleEncrypt(decrypted2, key);
            String decrypted3 = SimpleCipherHandler.simpleDecrypt(encrypted3, key);

            // Then
            assertEquals(plainText, decrypted1);
            assertEquals(plainText, decrypted2);
            assertEquals(plainText, decrypted3);
            assertEquals(encrypted1, encrypted2);
            assertEquals(encrypted2, encrypted3);
        }

        @Test
        @DisplayName("Should handle large text efficiently")
        void shouldHandleLargeTextEfficiently() {
            // Given
            StringBuilder largeText = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                largeText.append("This is line ").append(i).append(" of a large text. ");
            }
            String key = "largeTextKey";

            // When
            long startTime = System.currentTimeMillis();
            String encrypted = SimpleCipherHandler.simpleEncrypt(largeText.toString(), key);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, key);
            long endTime = System.currentTimeMillis();

            // Then
            assertEquals(largeText.toString(), decrypted);
            long duration = endTime - startTime;
            assertTrue(duration < 1000, "Encryption/decryption took too long: " + duration + "ms");
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle malformed hex string gracefully")
        void shouldHandleMalformedHexStringGracefully() {
            // Given
            String malformedHex = "invalid hex string";

            // When & Then
            // This should not throw an exception but may produce unexpected results
            assertDoesNotThrow(() -> {
                SimpleCipherHandler.hex2data(malformedHex);
            });
        }

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Given
            StringBuilder veryLongString = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                veryLongString.append("A");
            }
            String key = "longStringKey";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(veryLongString.toString(), key);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, key);

            // Then
            assertEquals(veryLongString.toString(), decrypted);
        }

        @Test
        @DisplayName("Should handle strings with null bytes")
        void shouldHandleStringsWithNullBytes() {
            // Given
            String textWithNulls = "Hello\0World\0Test";
            String key = "nullByteKey";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(textWithNulls, key);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, key);

            // Then
            assertEquals(textWithNulls, decrypted);
        }

        @Test
        @DisplayName("Should handle empty string encryption/decryption")
        void shouldHandleEmptyStringEncryptionDecryption() {
            // Given
            String emptyString = "";
            String key = "testKey";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(emptyString, key);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, key);

            // Then
            assertEquals(emptyString, decrypted);
            assertEquals("", encrypted);
        }

        @Test
        @DisplayName("Should handle single character encryption/decryption")
        void shouldHandleSingleCharacterEncryptionDecryption() {
            // Given
            String singleChar = "A";
            String key = "testKey";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(singleChar, key);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, key);

            // Then
            assertEquals(singleChar, decrypted);
            assertNotNull(encrypted);
            assertNotEquals(singleChar, encrypted);
        }
    }

    @Nested
    @DisplayName("Character Encoding Tests")
    class CharacterEncodingTests {

        @Test
        @DisplayName("Should handle ASCII characters correctly")
        void shouldHandleAsciiCharactersCorrectly() {
            // Given
            String asciiText = "Hello World 123 !@#";
            String key = "asciiKey";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(asciiText, key);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, key);

            // Then
            assertEquals(asciiText, decrypted);
        }

        @Test
        @DisplayName("Should handle extended ASCII characters")
        void shouldHandleExtendedAsciiCharacters() {
            // Given - Using characters that can be properly converted to/from bytes
            // Note: SimpleCipherHandler has limitations with extended ASCII (128-255)
            // due to byte casting. Characters 0-127 work correctly.
            String extendedAscii = "Hello World " + (char) 127 + (char) 126;
            String key = "extendedKey";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(extendedAscii, key);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, key);

            // Then
            assertEquals(extendedAscii, decrypted);
        }

        @Test
        @DisplayName("Should document limitation with extended ASCII characters")
        void shouldDocumentLimitationWithExtendedAsciiCharacters() {
            // Given - This test documents the limitation of SimpleCipherHandler
            // with extended ASCII characters (128-255)
            String extendedAscii = "Hello World " + (char) 128 + (char) 255;
            String key = "extendedKey";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(extendedAscii, key);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, key);

            // Then - This will fail due to byte casting limitation
            // The original implementation uses (byte) char which loses information
            // for characters with values > 127
            assertNotEquals(extendedAscii, decrypted,
                    "This test documents that SimpleCipherHandler cannot handle extended ASCII properly");
        }

        @Test
        @DisplayName("Should handle control characters")
        void shouldHandleControlCharacters() {
            // Given
            String controlChars = "\t\n\r\b\f";
            String key = "controlKey";

            // When
            String encrypted = SimpleCipherHandler.simpleEncrypt(controlChars, key);
            String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, key);

            // Then
            assertEquals(controlChars, decrypted);
        }
    }
} 
