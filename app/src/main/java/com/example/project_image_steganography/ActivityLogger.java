package com.example.project_image_steganography;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityLogger {

    private static final String PREF_NAME = "SpyLogs";
    private static final String KEY_LOGS = "access_logs";

    public static void logEvent(Context context, String event) {
        // 1. Get the current time
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // 2. Open the diary
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String oldLogs = prefs.getString(KEY_LOGS, "");

        // 3. Write the new line on top: "Logged In at 10:00 PM"
        String newLogEntry = event + " at " + timeStamp + "\n\n" + oldLogs;

        // 4. Save it back
        prefs.edit().putString(KEY_LOGS, newLogEntry).apply();
    }

    public static String getLogs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LOGS, "No activity yet.");
    }
}
