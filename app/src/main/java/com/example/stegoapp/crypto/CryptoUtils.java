package com.example.stegoapp.crypto;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Authenticated encryption for the steganography payload.
 *
 * The passphrase is stretched with PBKDF2 into a 256-bit key using a fresh random
 * salt per message, and the message is sealed with AES-GCM under a fresh random IV.
 * Salt and IV are not secret, so they are prefixed to the ciphertext:
 *
 *     salt (16 bytes) || iv (12 bytes) || ciphertext+tag
 *
 * Extraction therefore still needs nothing but the passphrase. A wrong passphrase
 * or a modified image fails the GCM authentication tag instead of silently
 * returning garbage.
 */
public class CryptoUtils {
    private static final String KDF = "PBKDF2WithHmacSHA1";
    private static final int KEY_BITS = 256;
    private static final int ITERATIONS = 120000;
    private static final int SALT_BYTES = 16;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static SecretKey deriveKey(String passphrase, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_BITS);
        try {
            byte[] key = SecretKeyFactory.getInstance(KDF).generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, "AES");
        } finally {
            spec.clearPassword();
        }
    }

    public static byte[] encryptAES(String message, String key) throws Exception {
        byte[] salt = new byte[SALT_BYTES];
        byte[] iv = new byte[IV_BYTES];
        RANDOM.nextBytes(salt);
        RANDOM.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(key, salt), new GCMParameterSpec(TAG_BITS, iv));
        byte[] sealed = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream out = new ByteArrayOutputStream(salt.length + iv.length + sealed.length);
        out.write(salt);
        out.write(iv);
        out.write(sealed);
        return out.toByteArray();
    }

    public static String decryptAES(byte[] payload, String key) throws Exception {
        if (payload == null || payload.length <= SALT_BYTES + IV_BYTES) {
            throw new IllegalArgumentException("Payload too short to be valid");
        }
        byte[] salt = Arrays.copyOfRange(payload, 0, SALT_BYTES);
        byte[] iv = Arrays.copyOfRange(payload, SALT_BYTES, SALT_BYTES + IV_BYTES);
        byte[] sealed = Arrays.copyOfRange(payload, SALT_BYTES + IV_BYTES, payload.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(key, salt), new GCMParameterSpec(TAG_BITS, iv));
        return new String(cipher.doFinal(sealed), StandardCharsets.UTF_8);
    }
}
