package com.project.manager.finance_management.infrastructure.security;

import com.project.manager.finance_management.infrastructure.midlewares.exceptions.CryptographyException;
import com.project.manager.finance_management.infrastructure.midlewares.exceptions.SecretKeyNotSetException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Component
public class JceCryptography {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH_BYTE = 16;

    @Value("${infrastructure.security.jce.secret-key}")
    private static String encryptionKey;

    private static void validateKey() {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            throw new SecretKeyNotSetException("The encryption key has not been configured.");
        }
    }

    public static String encrypt(String plainText) {
        validateKey();

        try {
            SecretKey key = generateKeyFromString(encryptionKey);
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedIVAndText = new byte[IV_LENGTH_BYTE + cipherText.length];
            System.arraycopy(iv, 0, encryptedIVAndText, 0, IV_LENGTH_BYTE);
            System.arraycopy(cipherText, 0, encryptedIVAndText, IV_LENGTH_BYTE, cipherText.length);

            return Base64.getEncoder().encodeToString(encryptedIVAndText);
        } catch (GeneralSecurityException exception) {
            throw new CryptographyException("Error during encryption");
        }
    }

    public static String decrypt(String encryptedTextWithIV) {
        validateKey();

        try {
            SecretKey key = generateKeyFromString(encryptionKey);
            byte[] encryptedIVAndText = Base64.getDecoder().decode(encryptedTextWithIV);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(encryptedIVAndText, 0, iv, 0, IV_LENGTH_BYTE);
            byte[] cipherText = new byte[encryptedIVAndText.length - IV_LENGTH_BYTE];
            System.arraycopy(encryptedIVAndText, IV_LENGTH_BYTE, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] plainTextBytes = cipher.doFinal(cipherText);

            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new CryptographyException("Error during decryption");
        }
    }

    private static SecretKey generateKeyFromString(String keyString) {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    private static byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH_BYTE];
        java.security.SecureRandom random = new java.security.SecureRandom();
        random.nextBytes(iv);
        return iv;
    }
}