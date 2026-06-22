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

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etRegName);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etRegPhone);
        etPassword = findViewById(R.id.etRegPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            AppDatabase db = AppDatabase.getInstance(this);
            if (db.userDao().getUserByEmail(email) != null) {
                Toast.makeText(this, "Email already registered!", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User(name, email, phone, password);
            db.userDao().insert(user);

            // Mark as registered for Splash screen check
            SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("is_registered", true).apply();

            // Notify user and send to Login
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        tvBackToLogin.setOnClickListener(v -> finish());
    }
}
