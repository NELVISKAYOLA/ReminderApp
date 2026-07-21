package com.example.reminderapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // We proceed regardless of whether all were granted, 
                // but specific features will check again when used.
                checkConnectionAndProceed();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply persisted theme before super.onCreate
        SharedPreferences themePrefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        int themeMode = themePrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkPermissions();
    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        // Notifications (Required for Alarms on Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        
        // Camera (For Profile Picture)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(android.Manifest.permission.CAMERA);
        }

        // Contacts (For Call Reminders)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(android.Manifest.permission.READ_CONTACTS);
        }

        if (!permissionsNeeded.isEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        } else {
            checkConnectionAndProceed();
        }
    }

    private void checkConnectionAndProceed() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            proceedToNextScreen();
        } else {
            showNoInternetDialog();
        }
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_no_internet_title)
                .setMessage(R.string.error_no_internet_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_try_again, (dialog, which) -> checkConnectionAndProceed())
                .setNegativeButton(R.string.btn_exit, (dialog, which) -> finish())
                .show();
    }

    private void proceedToNextScreen() {
        // 2.5-Second delay before moving to Next Screen
        new Handler().postDelayed(() -> {
            SharedPreferences userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            boolean isLoggedIn = userPrefs.getBoolean("is_logged_in", false);
            boolean isRegistered = userPrefs.getBoolean("is_registered", false);
            
            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(SplashActivity.this, DashboardActivity.class);
            } else if (isRegistered) {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, RegisterActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2500);
    }
}
