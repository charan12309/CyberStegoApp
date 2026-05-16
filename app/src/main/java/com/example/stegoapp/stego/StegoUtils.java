package com.example.stegoapp.stego;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.nio.ByteBuffer;

public class StegoUtils {
    private static boolean[] bytesToBits(byte[] data) {
        boolean[] bits = new boolean[data.length * 8];
        int idx = 0;
        for (byte b : data) {
            for (int i = 7; i >= 0; i--) {
                bits[idx++] = ((b >> i) & 1) == 1;
            }
        }
        return bits;
    }

    private static byte[] bitsToBytes(boolean[] bits) {
        int lenBytes = (bits.length + 7) / 8;
        byte[] out = new byte[lenBytes];
        int idx = 0;
        for (int i = 0; i < lenBytes; i++) {
            int value = 0;
            for (int j = 0; j < 8; j++) {
                value <<= 1;
                int bit = (idx < bits.length && bits[idx]) ? 1 : 0;
                value |= bit;
                idx++;
            }
            out[i] = (byte) value;
        }
        return out;
    }

    private static int capacityInBits(Bitmap bitmap) {
        return bitmap.getWidth() * bitmap.getHeight();
    }

    public static Bitmap embedCiphertext(Bitmap src, byte[] ciphertext) {
        int size = Math.min(src.getWidth(), src.getHeight());
        int left = (src.getWidth() - size) / 2, top = (src.getHeight() - size) / 2;
        Bitmap square = (size == src.getWidth() && size == src.getHeight()) ? src : Bitmap.createBitmap(src, left, top, size, size);
        boolean[] payloadBits = bytesToBits(ciphertext);
        byte[] lengthPrefix = ByteBuffer.allocate(4).putInt(payloadBits.length).array();
        boolean[] headerBits = bytesToBits(lengthPrefix);
        int totalBits = headerBits.length + payloadBits.length;
        if (totalBits > capacityInBits(square)) {
            throw new IllegalArgumentException("Ciphertext too large for image capacity");
        }
        Bitmap out = square.copy(Bitmap.Config.ARGB_8888, true);
        int w = out.getWidth();
        int h = out.getHeight();
        int[] pixels = new int[w * h];
        out.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < totalBits; i++) {
            int color = pixels[i];
            int blue = Color.blue(color);
            int bit = (i < headerBits.length ? (headerBits[i] ? 1 : 0) : (payloadBits[i - headerBits.length] ? 1 : 0));
            int newBlue = (blue & 0xFE) | bit;
            pixels[i] = Color.argb(Color.alpha(color), Color.red(color), Color.green(color), newBlue);
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        return out;
    }

    public static byte[] extractCiphertext(Bitmap src) {
        int size = Math.min(src.getWidth(), src.getHeight());
        int left = (src.getWidth() - size) / 2, top = (src.getHeight() - size) / 2;
        Bitmap square = (size == src.getWidth() && size == src.getHeight()) ? src : Bitmap.createBitmap(src, left, top, size, size);
        Bitmap safe = square.getConfig() == Bitmap.Config.HARDWARE ? square.copy(Bitmap.Config.ARGB_8888, false) : square;
        int w = safe.getWidth();
        int h = safe.getHeight();
        int[] pixels = new int[w * h];
        safe.getPixels(pixels, 0, w, 0, 0, w, h);
        int header = 0;
        for (int i = 0; i < 32; i++) {
            header = (header << 1) | (Color.blue(pixels[i]) & 1);
        }
        int payloadBitsLen = header;
        int capacity = w * h - 32;
        if (payloadBitsLen <= 0 || payloadBitsLen > capacity) throw new IllegalArgumentException("No hidden data (image may be recompressed)");
        boolean[] payloadBits = new boolean[payloadBitsLen];
        for (int i = 0; i < payloadBitsLen; i++) {
            payloadBits[i] = ((Color.blue(pixels[32 + i]) & 1) == 1);
        }
        return bitsToBytes(payloadBits);
    }
}
