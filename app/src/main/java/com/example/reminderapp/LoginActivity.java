package com.example.reminderapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.User;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply persisted theme before super.onCreate
        SharedPreferences themePrefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        int themeMode = themePrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                AppDatabase db = AppDatabase.getInstance(this);

                new Thread(() -> {
                    // 1. Ensure Admin exists before checking
                    User existingAdmin = db.userDao().getUserByEmail("admin@reminder.com");
                    if (existingAdmin == null) {
                        User admin = new User("System Admin", "admin@reminder.com", "0000000000", "admin123");
                        admin.setRole("ADMIN");
                        db.userDao().insert(admin);
                    }

                    // 2. Perform Login Check
                    User user = db.userDao().getUserByEmail(email);

                    runOnUiThread(() -> {
                        if (user != null && user.getPassword().equals(password)) {
                            // Save to current session
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("active_user_id", user.getId());
                            editor.putString("active_name", user.getName());
                            editor.putString("active_email", user.getEmail());
                            editor.putString("active_phone", user.getPhone());
                            editor.putString("active_role", user.getRole());
                            editor.putBoolean("is_logged_in", true);
                            editor.apply();

                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Invalid credentials or user not registered!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
        });

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        });

        findViewById(R.id.btnGoogleLogin).setOnClickListener(v -> 
            Toast.makeText(this, "Login with Google (Simulation)", Toast.LENGTH_SHORT).show());
            
        findViewById(R.id.btnAppleLogin).setOnClickListener(v -> 
            Toast.makeText(this, "Login with Apple (Simulation)", Toast.LENGTH_SHORT).show());
    }
}