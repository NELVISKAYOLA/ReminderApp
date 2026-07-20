package com.example.reminderapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.card.MaterialCardView;

public class ThemeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        NavigationHelper.setupNavigation(this);

        MaterialCardView cardLight = findViewById(R.id.cardLightTheme);
        MaterialCardView cardDark = findViewById(R.id.cardDarkTheme);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int currentMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Highlight currently selected theme
        updateCardSelection(cardLight, cardDark, currentMode);

        cardLight.setOnClickListener(v -> applyAndSaveTheme(AppCompatDelegate.MODE_NIGHT_NO));
        cardDark.setOnClickListener(v -> applyAndSaveTheme(AppCompatDelegate.MODE_NIGHT_YES));
    }

    private void applyAndSaveTheme(int mode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
        
        AppCompatDelegate.setDefaultNightMode(mode);
        recreate();
    }

    private void updateCardSelection(MaterialCardView light, MaterialCardView dark, int mode) {
        int selectedColor = getResources().getColor(R.color.luxury_purple);
        int transparent = android.R.color.transparent;

        light.setStrokeColor(getResources().getColor(transparent));
        dark.setStrokeColor(getResources().getColor(transparent));

        if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            dark.setStrokeColor(selectedColor);
        } else if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            light.setStrokeColor(selectedColor);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
