package com.example.project_image_steganography;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. AUTO-LOGIN CHECK ---
        SharedPreferences prefs = getSharedPreferences("SpyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            // Agent is already authorized. Skip login screen!
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return; // Stop running the rest of the code
        }
        // ---------------------------

        setContentView(R.layout.activity_login);

        EditText user = findViewById(R.id.login_user);
        EditText pass = findViewById(R.id.login_pass);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView goRegister = findViewById(R.id.tv_go_register);

        btnLogin.setOnClickListener(v -> {
            String u = user.getText().toString();
            String p = pass.getText().toString();

            String savedUser = prefs.getString("username", null);
            String savedPass = prefs.getString("password", null);

            if (savedUser == null) {
                Toast.makeText(this, "No agent found. Register first!", Toast.LENGTH_SHORT).show();
            } else if (u.equals(savedUser) && p.equals(savedPass)) {

                // --- 2. LOGIN SUCCESS ---
                // Set the flag to TRUE so we remember them next time
                prefs.edit().putBoolean("is_logged_in", true).apply();

                // Write in the Diary
                ActivityLogger.logEvent(this, "AGENT LOGIN (" + u + ")");

                Toast.makeText(this, "Access Granted", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                // ------------------------

            } else {
                Toast.makeText(this, "Access Denied!", Toast.LENGTH_SHORT).show();
            }
        });

        goRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}