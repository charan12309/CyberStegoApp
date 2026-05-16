package com.example.stegoapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stegoapp.data.AppDatabase;
import com.example.stegoapp.data.User;
import com.example.stegoapp.data.UserDao;
import com.example.stegoapp.databinding.ActivitySignupBinding;

import com.example.stegoapp.util.ExecutorProvider;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private UserDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dao = AppDatabase.get(this).userDao();
        binding.btnCreateAccount.setOnClickListener(v -> {
            String u = binding.inputSignupUsername.getText() != null ? binding.inputSignupUsername.getText().toString().trim() : "";
            String p = binding.inputSignupPassword.getText() != null ? binding.inputSignupPassword.getText().toString() : "";
            ExecutorProvider.get().execute(() -> {
                try {
                    dao.insert(new User(u, p));
                    runOnUiThread(() -> {
                        Toast.makeText(SignupActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Username exists", Toast.LENGTH_SHORT).show());
                }
            });
        });
    }
}
