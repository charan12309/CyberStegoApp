package com.example.project_image_steganography;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESHelper {

    public static byte[] fixSize(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return Arrays.copyOf(bytes, 16);
    }

    public static String encrypt(String plainText, String password, String ivInput) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(fixSize(password), "AES");

        byte[] ivBytes;
        if (ivInput.isEmpty()) {
            ivBytes = new byte[16];
            new java.security.SecureRandom().nextBytes(ivBytes);
        } else {
            ivBytes = fixSize(ivInput);
        }
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[ivBytes.length + encryptedBytes.length];
        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
        System.arraycopy(encryptedBytes, 0, combined, ivBytes.length, encryptedBytes.length);

        String base64String = Base64.encodeToString(combined, Base64.NO_WRAP);
        StringBuilder binary = new StringBuilder();
        for (byte b : base64String.getBytes(StandardCharsets.US_ASCII)) {
            String bin = Integer.toBinaryString(b & 0xFF);
            while (bin.length() < 8) bin = "0" + bin;
            binary.append(bin);
        }
        return binary.toString();
    }

    public static String decrypt(String binaryString, String password) throws Exception {
        int len = binaryString.length() / 8;
        byte[] asciiBytes = new byte[len];
        for (int i = 0; i < len; i++) {
            asciiBytes[i] = (byte) Integer.parseInt(binaryString.substring(i * 8, (i + 1) * 8), 2);
        }
        String base64String = new String(asciiBytes, StandardCharsets.US_ASCII);
        byte[] combined = Base64.decode(base64String, Base64.NO_WRAP);

        byte[] ivBytes = Arrays.copyOfRange(combined, 0, 16);
        byte[] encryptedBytes = Arrays.copyOfRange(combined, 16, combined.length);

        SecretKeySpec keySpec = new SecretKeySpec(fixSize(password), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
    }
}