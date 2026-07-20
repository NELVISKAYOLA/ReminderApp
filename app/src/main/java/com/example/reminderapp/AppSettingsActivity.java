package com.example.reminderapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class AppSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        SwitchMaterial switchNotifications = findViewById(R.id.switchNotificationsSetting);
        switchNotifications.setChecked(prefs.getBoolean("notifications_enabled", true));
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
        });

        findViewById(R.id.cardThemeSetting).setOnClickListener(v -> {
            startActivity(new Intent(this, ThemeActivity.class));
        });

        findViewById(R.id.cardFeedbackSetting).setOnClickListener(v -> {
            startActivity(new Intent(this, FeedbackActivity.class));
        });

        findViewById(R.id.btnAboutSetting).setOnClickListener(v -> {
            // Reusing Terms/Policy logic or creating a simple dialog
            startActivity(new Intent(this, TermsPolicyActivity.class));
        });
    }
}
