package com.example.reminderapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply persisted theme before super.onCreate
        SharedPreferences themePrefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        int themeMode = themePrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkConnectionAndProceed();
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
