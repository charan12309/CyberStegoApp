package com.example.stegoapp.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    private static byte[] normalizeKey(String input, int size) {
        byte[] k = new byte[size]; byte[] s = input.getBytes();
        System.arraycopy(s, 0, k, 0, Math.min(s.length, size)); return k;
    }

    public static byte[] encryptAES(String message, String key) throws Exception {
        SecretKeySpec secret = new SecretKeySpec(normalizeKey(key, 16), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        return cipher.doFinal(message.getBytes());
    }

    public static String decryptAES(byte[] ciphertext, String key) throws Exception {
        SecretKeySpec secret = new SecretKeySpec(normalizeKey(key, 16), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        byte[] plain = cipher.doFinal(ciphertext);
        return new String(plain);
    }
}
