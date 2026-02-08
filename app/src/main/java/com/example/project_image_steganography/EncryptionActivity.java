package com.example.project_image_steganography;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EncryptionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption);

        EditText msg = findViewById(R.id.input_msg);
        EditText pass = findViewById(R.id.input_pass);
        EditText iv = findViewById(R.id.input_iv);
        EditText out = findViewById(R.id.output_result);
        Button btn = findViewById(R.id.btn_run_encrypt);

        btn.setOnClickListener(v -> {
            try {
                String messageText = msg.getText().toString();
                String passwordText = pass.getText().toString();
                String ivText = iv.getText().toString();

                if (passwordText.isEmpty()) {
                    Toast.makeText(this, "Password is required!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String res = AESHelper.encrypt(messageText, passwordText, ivText);
                out.setText(res);
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // inside onCreate
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish()); // This closes the current screen and goes back
    }
}