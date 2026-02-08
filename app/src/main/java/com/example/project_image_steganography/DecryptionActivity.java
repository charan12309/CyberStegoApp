package com.example.project_image_steganography;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DecryptionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decryption);

        EditText binaryInput = findViewById(R.id.input_binary);
        EditText passInput = findViewById(R.id.input_pass_dec);
        TextView output = findViewById(R.id.output_final);
        Button btn = findViewById(R.id.btn_run_decrypt);

        // Make the back button work
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btn.setOnClickListener(v -> {
            try {
                String res = AESHelper.decrypt(binaryInput.getText().toString(), passInput.getText().toString());
                output.setText(res);
            } catch (Exception e) {
                Toast.makeText(this, "Wrong Password or Corrupted Data", Toast.LENGTH_SHORT).show();
            }


            // Make the back button work

        });
    }
}
