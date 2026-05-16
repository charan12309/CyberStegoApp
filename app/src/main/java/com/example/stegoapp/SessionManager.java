package com.example.stegoapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE);
    }

    public void setLoggedIn(String username) {
        prefs.edit().putString("logged_user", username).apply();
    }

    public String getLoggedInUser() {
        return prefs.getString("logged_user", null);
    }

    public void logout() {
        prefs.edit().remove("logged_user").apply();
    }
}
