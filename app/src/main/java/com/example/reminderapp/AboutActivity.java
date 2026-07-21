package com.example.reminderapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        NavigationHelper.setupNavigation(this);

        findViewById(R.id.btnPrivacyPolicy).setOnClickListener(v -> 
            startActivity(new Intent(this, PrivacyPolicyActivity.class))
        );

        findViewById(R.id.btnTerms).setOnClickListener(v -> 
            startActivity(new Intent(this, TermsPolicyActivity.class))
        );
    }
}
