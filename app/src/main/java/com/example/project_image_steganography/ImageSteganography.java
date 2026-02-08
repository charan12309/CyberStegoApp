package com.example.project_image_steganography;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageSteganography {

    // 1. Embed Message + Length Header
    public static Bitmap embedText(Bitmap image, String binaryData) {
        Bitmap mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
        int width = mutableBitmap.getWidth();
        int height = mutableBitmap.getHeight();

        // Convert the length of the message (e.g., 512) into a 32-bit binary string
        String lengthBinary = String.format("%32s", Integer.toBinaryString(binaryData.length())).replace(' ', '0');

        // Combine Length Header + Actual Message
        String allData = lengthBinary + binaryData;

        int dataIndex = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // If we are done hiding data, stop!
                if (dataIndex >= allData.length()) break;

                int pixel = mutableBitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                // Hide in Red
                if (dataIndex < allData.length()) {
                    int bit = allData.charAt(dataIndex) - '0';
                    r = (r & 0xFE) | bit;
                    dataIndex++;
                }
                // Hide in Green
                if (dataIndex < allData.length()) {
                    int bit = allData.charAt(dataIndex) - '0';
                    g = (g & 0xFE) | bit;
                    dataIndex++;
                }
                // Hide in Blue
                if (dataIndex < allData.length()) {
                    int bit = allData.charAt(dataIndex) - '0';
                    b = (b & 0xFE) | bit;
                    dataIndex++;
                }

                mutableBitmap.setPixel(x, y, Color.rgb(r, g, b));
            }
        }
        return mutableBitmap;
    }

    // 2. Extract Message based on Header
    public static String extractText(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        StringBuilder extractedBits = new StringBuilder();
        int headerLength = 32; // First 32 bits tell us the size
        int messageLength = 0;
        boolean readingHeader = true;
        int bitsRead = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getPixel(x, y);

                // Get LSBs from R, G, B
                int[] colors = {Color.red(pixel), Color.green(pixel), Color.blue(pixel)};

                for (int color : colors) {
                    int lsb = color & 1; // Extract the last bit
                    extractedBits.append(lsb);
                    bitsRead++;

                    // Phase 1: Read the Header (First 32 bits)
                    if (readingHeader) {
                        if (bitsRead == headerLength) {
                            // We finished reading the header! Convert it to a number.
                            messageLength = Integer.parseInt(extractedBits.toString(), 2);
                            extractedBits.setLength(0); // Clear the builder to start fresh for the message
                            readingHeader = false;
                            bitsRead = 0; // Reset counter for message body
                        }
                    }
                    // Phase 2: Read the actual Message
                    else {
                        if (bitsRead == messageLength) {
                            return extractedBits.toString(); // We found the end! Return the data.
                        }
                    }
                }
                // Break outer loops if done
                if (!readingHeader && bitsRead >= messageLength) break;
            }
            if (!readingHeader && bitsRead >= messageLength) break;
        }
        return ""; // Should not reach here if image has data
    }
}