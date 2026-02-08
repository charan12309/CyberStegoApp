package com.example.project_image_steganography;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LogViewerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs); // Ensure this matches the XML filename

        // Back Button Logic
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Log Logic
        TextView tvLogs = findViewById(R.id.tv_logs);
        String history = ActivityLogger.getLogs(this);

        if (history.isEmpty()) {
            tvLogs.setText("No records found.");
        } else {
            tvLogs.setText(history);
        }
    }
}