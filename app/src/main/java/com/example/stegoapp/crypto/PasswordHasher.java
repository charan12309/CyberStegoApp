package com.example.stegoapp.crypto;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Salted PBKDF2 hashing for account credentials.
 *
 * Stored form is "iterations:saltHex:hashHex" so the work factor can be raised later
 * without invalidating existing accounts. Verification is constant-time.
 */
public final class PasswordHasher {
    private static final String KDF = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 120000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) throws Exception {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return ITERATIONS + ":" + toHex(salt) + ":" + toHex(pbkdf2(password, salt, ITERATIONS));
    }

    public static boolean verify(String password, String stored) {
        if (stored == null) {
            return false;
        }
        String[] parts = stored.split(":");
        if (parts.length != 3) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = fromHex(parts[1]);
            byte[] expected = fromHex(parts[2]);
            return MessageDigest.isEqual(expected, pbkdf2(password, salt, iterations));
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(KDF).generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.US, "%02x", b));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }
}
