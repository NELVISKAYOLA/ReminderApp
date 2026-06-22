package com.example.reminderapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_policy); // Reusing terms layout for privacy
        NavigationHelper.setupNavigation(this);
    }
}