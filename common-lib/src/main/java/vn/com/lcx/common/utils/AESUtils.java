package vn.com.lcx.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class AESUtils {

    private AESUtils() {
    }

    public static String encryptCBC(String data, String key) throws Exception {
        String iv = key.substring(0, 16);
        // Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/OFB/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/OFB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        int plaintextLength = dataBytes.length;
        byte[] plaintext = new byte[plaintextLength];
        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
        SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

        cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
        byte[] encrypted = cipher.doFinal(plaintext);
        return new String(Base64.getEncoder().encode(encrypted));
    }

    public static String decryptCBC(String encrypted, String key) throws Exception {
        String iv = key.substring(0, 16);

        byte[] encrypt = Base64.getDecoder().decode(encrypted.getBytes(StandardCharsets.UTF_8));

        // Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/OFB/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/OFB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

        cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

        byte[] original = cipher.doFinal(encrypt);
        return new String(original, StandardCharsets.UTF_8);
    }

    public static String encrypt(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        // Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/OFB/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/OFB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] byteOriginal = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(byteOriginal);
    }

    public static String decrypt(String encrypted, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        // Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/OFB/NoPadding");
        // Cipher cipher = Cipher.getInstance("AES/OFB/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
    }

}
