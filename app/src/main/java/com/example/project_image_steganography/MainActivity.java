package com.example.project_image_steganography;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Find all the buttons
        Button btnEnc = findViewById(R.id.btn_goto_encrypt);
        Button btnEmbed = findViewById(R.id.btn_goto_embed);
        Button btnExtract = findViewById(R.id.btn_goto_extract);
        Button btnDec = findViewById(R.id.btn_goto_decrypt);
        Button btnLogs = findViewById(R.id.btn_view_logs); // Make sure this ID matches the XML
        Button btnLogout = findViewById(R.id.btn_logout);

        // 2. Set the click listeners
        btnEnc.setOnClickListener(v -> startActivity(new Intent(this, EncryptionActivity.class)));
        btnEmbed.setOnClickListener(v -> startActivity(new Intent(this, EmbedActivity.class)));
        btnExtract.setOnClickListener(v -> startActivity(new Intent(this, ExtractActivity.class)));
        btnDec.setOnClickListener(v -> startActivity(new Intent(this, DecryptionActivity.class)));

        // --- THIS IS THE CRITICAL PART FOR LOGS ---
        btnLogs.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, LogViewerActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                // If it fails, this toast will tell us why!
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // 3. Logout Logic
        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("SpyPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("is_logged_in", false).apply();

            ActivityLogger.logEvent(this, "AGENT LOGOUT");

            Toast.makeText(this, "Logged Out.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}