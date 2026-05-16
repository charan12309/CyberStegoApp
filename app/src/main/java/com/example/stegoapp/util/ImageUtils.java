package com.example.stegoapp.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.OutputStream;
import android.graphics.ImageDecoder;

public class ImageUtils {
    public static Uri savePng(Context context, Bitmap bitmap, String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name == null ? "embedded.png" : name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/StegoApp");
        }
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (uri == null) return null;
        try (OutputStream stream = context.getContentResolver().openOutputStream(uri)) {
            if (stream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            }
        } catch (Exception ignored) {}
        return uri;
    }

    public static Bitmap loadPngSoftware(Context context, Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source src = ImageDecoder.createSource(context.getContentResolver(), uri);
                Bitmap bmp = ImageDecoder.decodeBitmap(src, (decoder, info, source) -> {
                    decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                    decoder.setMutableRequired(true);
                });
                return bmp.copy(Bitmap.Config.ARGB_8888, true);
            } else {
                @SuppressWarnings("deprecation")
                Bitmap legacy = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                return legacy.copy(Bitmap.Config.ARGB_8888, true);
            }
        } catch (Exception e) {
            return null;
        }
    }
}
