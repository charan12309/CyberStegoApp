package com.example.project_image_steganography;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText user = findViewById(R.id.reg_user);
        EditText pass = findViewById(R.id.reg_pass);
        EditText confirm = findViewById(R.id.reg_pass_confirm);
        Button btnReg = findViewById(R.id.btn_register);
        TextView goLogin = findViewById(R.id.tv_go_login);

        btnReg.setOnClickListener(v -> {
            String u = user.getText().toString();
            String p = pass.getText().toString();
            String c = confirm.getText().toString();

            if(u.isEmpty() || p.isEmpty() || c.isEmpty()) {
                Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
            } else if (!p.equals(c)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences preferences = getSharedPreferences("SpyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", u);
                editor.putString("password", p);
                editor.apply();

                Toast.makeText(this, "Agent Registered!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        goLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}