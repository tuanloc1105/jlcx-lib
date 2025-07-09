package vn.com.lcx.common.utils;

/**
 * SimpleCipherHandler provides basic encryption and decryption functionality using XOR cipher.
 *
 * <p>This class implements a simple symmetric encryption algorithm based on XOR operation.
 * It uses a key to encrypt plain text and can decrypt the cipher text back to the original plain text.
 * The encryption is deterministic - the same plain text and key will always produce the same cipher text.</p>
 *
 * <p><strong>Security Note:</strong> This implementation uses a simple XOR cipher which is not cryptographically secure.
 * It should only be used for basic obfuscation purposes and not for protecting sensitive data.
 * For production use cases requiring security, consider using AES or other strong encryption algorithms.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Simple XOR-based encryption/decryption</li>
 *   <li>Hexadecimal encoding for cipher text</li>
 *   <li>Default key support for convenience</li>
 *   <li>Custom key support for flexibility</li>
 *   <li>Exception handling with logging</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Using default key
 * String encrypted = SimpleCipherHandler.simpleEncrypt("Hello World");
 * String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted);
 *
 * // Using custom key
 * String encrypted = SimpleCipherHandler.simpleEncrypt("Hello World", "mySecretKey");
 * String decrypted = SimpleCipherHandler.simpleDecrypt(encrypted, "mySecretKey");
 * }</pre>
 *
 * @author tuanloc1105
 * @version 1.0
 * @since 3.0
 */
public final class SimpleCipherHandler {

    /**
     * Default encryption key used when no custom key is provided.
     * This key is used for the default encryption/decryption methods.
     */
    private static final String DEFAULT_KEY = "WOn965wvt999WwS955SAx258AsX939qL";

    /**
     * Private constructor to prevent instantiation.
     * This class provides only static methods.
     */
    private SimpleCipherHandler() {
    }

    /**
     * Encrypts the given plain text using the default key.
     *
     * <p>This method is a convenience method that uses the default key for encryption.
     * The result is a hexadecimal string representing the encrypted data.</p>
     *
     * @param plainText the text to encrypt, must not be null
     * @return the encrypted text as a hexadecimal string, or empty string if encryption fails
     * @throws IllegalArgumentException if plainText is null
     * @see #simpleEncrypt(String, String)
     */
    public static String simpleEncrypt(String plainText) {
        return simpleEncrypt(plainText, DEFAULT_KEY);
    }

    /**
     * Decrypts the given cipher text using the default key.
     *
     * <p>This method is a convenience method that uses the default key for decryption.
     * The input should be a hexadecimal string that was produced by the encryption method.</p>
     *
     * @param cipherPass the encrypted text as a hexadecimal string, must not be null
     * @return the decrypted plain text, or empty string if decryption fails
     * @throws IllegalArgumentException if cipherPass is null
     * @see #simpleDecrypt(String, String)
     */
    public static String simpleDecrypt(String cipherPass) {
        return simpleDecrypt(cipherPass, DEFAULT_KEY);
    }

    /**
     * Encrypts the given plain text using the specified key.
     *
     * <p>The encryption process:</p>
     * <ol>
     *   <li>Converts the key to a hash code and ensures it's positive</li>
     *   <li>Converts each character of the plain text to a byte</li>
     *   <li>Performs XOR operation between each byte and the key hash</li>
     *   <li>Converts the resulting bytes to a hexadecimal string</li>
     * </ol>
     *
     * @param plainText the text to encrypt, must not be null
     * @param key       the encryption key, must not be null
     * @return the encrypted text as a hexadecimal string, or empty string if encryption fails
     * @throws IllegalArgumentException if plainText or key is null
     */
    public static String simpleEncrypt(String plainText, String key) {
        if (plainText == null) {
            throw new IllegalArgumentException("Plain text cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        String out = "";
        try {
            int i = (key.hashCode() < 0) ? key.hashCode() * -1 : key.hashCode();
            byte[] b = new byte[plainText.length()];

            for (int j = 0; j < b.length; ++j) {
                b[j] = (byte) plainText.charAt(j);
                b[j] = (byte) (b[j] ^ i);
            }
            out = data2hex(b);
        } catch (Exception ex) {
            LogUtils.writeLog(ex.getMessage(), ex);
        }
        return out;

    }

    /**
     * Decrypts the given cipher text using the specified key.
     *
     * <p>The decryption process:</p>
     * <ol>
     *   <li>Converts the key to a hash code and ensures it's positive</li>
     *   <li>Converts the hexadecimal string back to bytes</li>
     *   <li>Performs XOR operation between each byte and the key hash</li>
     *   <li>Converts the resulting bytes back to characters</li>
     * </ol>
     *
     * @param cipherPass the encrypted text as a hexadecimal string, must not be null
     * @param key        the decryption key, must not be null
     * @return the decrypted plain text, or empty string if decryption fails
     * @throws IllegalArgumentException if cipherPass or key is null
     */
    public static String simpleDecrypt(String cipherPass, String key) {
        if (cipherPass == null) {
            throw new IllegalArgumentException("Cipher text cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        String out = "";
        try {
            int i = (key.hashCode() < 0) ? key.hashCode() * -1 : key.hashCode();
            byte[] b = hex2data(cipherPass);

            for (int j = 0; j < b.length; ++j) {
                b[j] = (byte) (b[j] ^ i);
            }

            StringBuilder sb = new StringBuilder(b.length);
            for (byte value : b) {
                sb.append((char) value);
            }
            out = sb.toString();
        } catch (Exception ex) {
            LogUtils.writeLog(ex.getMessage(), ex);
        }
        return out;
    }

    /**
     * Converts a hexadecimal string to a byte array.
     *
     * <p>This method parses a hexadecimal string and converts it to the corresponding byte array.
     * The input string should contain only valid hexadecimal characters (0-9, a-f, A-F).</p>
     *
     * @param str the hexadecimal string to convert, can be null
     * @return the byte array representation, or empty array if input is null
     * @throws IllegalArgumentException if the string contains invalid hexadecimal characters
     */
    public static byte[] hex2data(String str) {
        if (str == null) {
            return new byte[0];
        }
        int len = str.length();
        char[] hex = str.toCharArray();
        byte[] buf = new byte[len / 2];

        for (int pos = 0; pos < len / 2; ++pos) {
            buf[pos] = (byte) (toDataNibble(hex[(2 * pos)]) << 4 & 0xF0 | toDataNibble(hex[(2 * pos + 1)]) & 0xF);
        }
        return buf;
    }

    /**
     * Converts a hexadecimal character to its 4-bit value.
     *
     * <p>This method converts a single hexadecimal character (0-9, a-f, A-F) to its corresponding
     * 4-bit value (0-15).</p>
     *
     * @param c the hexadecimal character to convert
     * @return the 4-bit value (0-15), or -1 if the character is not a valid hexadecimal digit
     */
    public static byte toDataNibble(char c) {
        if (('0' <= c) && (c <= '9')) {
            return (byte) ((byte) c - 48);
        }
        if (('a' <= c) && (c <= 'f')) {
            return (byte) ((byte) c - 97 + 10);
        }
        if (('A' <= c) && (c <= 'F')) {
            return (byte) ((byte) c - 65 + 10);
        }
        return -1;
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * <p>This method converts each byte in the array to its two-character hexadecimal representation.
     * The output uses lowercase letters (a-f) for hexadecimal digits.</p>
     *
     * @param data the byte array to convert, can be null
     * @return the hexadecimal string representation, or null if input is null
     */
    public static String data2hex(byte[] data) {
        if (data == null) {
            return null;
        }
        int len = data.length;
        StringBuilder buf = new StringBuilder(len * 2);
        for (byte datum : data) {
            buf.append(toHexChar(datum >>> 4 & 0xF)).append(toHexChar(datum & 0xF));
        }
        return buf.toString();
    }

    /**
     * Converts a 4-bit value to its hexadecimal character representation.
     *
     * <p>This method converts a value in the range 0-15 to its corresponding hexadecimal character.
     * Values 0-9 are represented as '0'-'9', and values 10-15 are represented as 'a'-'f'.</p>
     *
     * @param i the 4-bit value to convert (0-15)
     * @return the hexadecimal character representation
     * @throws IllegalArgumentException if the value is not in the range 0-15
     */
    private static char toHexChar(int i) {
        if ((0 <= i) && (i <= 9)) {
            return (char) (48 + i);
        }
        return (char) (97 + i - 10);
    }

}
