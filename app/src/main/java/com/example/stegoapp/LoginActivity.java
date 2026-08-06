package com.example.stegoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stegoapp.crypto.PasswordHasher;
import com.example.stegoapp.data.AppDatabase;
import com.example.stegoapp.data.User;
import com.example.stegoapp.data.UserDao;
import com.example.stegoapp.databinding.ActivityLoginBinding;

import com.example.stegoapp.util.ExecutorProvider;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private UserDao dao;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (session.getLoggedInUser() != null) { startActivity(new Intent(this, MainActivity.class)); finish(); return; }
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dao = AppDatabase.get(this).userDao();
        binding.btnLogin.setOnClickListener(v -> {
            String u = binding.inputLoginUsername.getText() != null ? binding.inputLoginUsername.getText().toString().trim() : "";
            String p = binding.inputLoginPassword.getText() != null ? binding.inputLoginPassword.getText().toString() : "";
            ExecutorProvider.get().execute(() -> {
                User user = null; try { user = dao.findByUsername(u); } catch (Exception e) { String m = e.getMessage()!=null?e.getMessage():"Login failed"; runOnUiThread(() -> Toast.makeText(this, m, Toast.LENGTH_SHORT).show()); return; }
                final boolean result = user != null && PasswordHasher.verify(p, user.passwordHash);
                runOnUiThread(() -> {
                    if (result) {
                        session.setLoggedIn(u);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
        binding.btnGoSignup.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }
}
