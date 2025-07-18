package vn.com.lcx.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class RSAUtils {

    private RSAUtils() {
    }

    public static String readKeyFromResource(String resourcePath, boolean isPrivate) throws IOException {
        final var classLoader = RSAUtils.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("File not found in resources: " + resourcePath);
            }
            return convertToPEM(inputStream, isPrivate);
        }
    }

    public static String readKey(String resourcePath, boolean isPrivate) throws IOException {
        try (InputStream inputStream = new FileInputStream(resourcePath)) {
            return convertToPEM(inputStream, isPrivate);
        }
    }

    private static String convertToPEM(InputStream inputStream, boolean isPrivate) throws IOException {
        byte[] keyBytes = inputStream.readAllBytes();
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyBytes);
        final var headerName = isPrivate ? "PRIVATE KEY" : "PUBLIC KEY";
        return "-----BEGIN " + headerName + "-----\n" + base64 + "\n-----END " + headerName + "-----";
    }

    public static RSAPublicKey getPublicKey(String key) throws Exception {
        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    public static RSAPrivateKey getPrivateKey(String key) throws Exception {
        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    public static byte[] encrypt(String data, String publicKey) throws Exception {
        // Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        // Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaepParameterSpecJCE = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey), oaepParameterSpecJCE);
        return cipher.doFinal(data.getBytes());
    }

    public static String decrypt(String encrypted, String privateKey) throws Exception {
        byte[] data = Base64.getDecoder().decode(encrypted.getBytes());
        // Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        // Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaepParameterSpecJCE = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKey), oaepParameterSpecJCE);
        return new String(cipher.doFinal(data));
    }

    public static String encrypt(String data, RSAPublicKey publicKey) throws Exception {
        // Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        // Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaepParameterSpecJCE = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParameterSpecJCE);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decrypt(String encrypted, RSAPrivateKey privateKey) throws Exception {
        byte[] data = Base64.getDecoder().decode(encrypted.getBytes());
        // Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        // Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaepParameterSpecJCE = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParameterSpecJCE);
        return new String(cipher.doFinal(data));
    }

}
