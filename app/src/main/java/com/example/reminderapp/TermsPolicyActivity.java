package com.example.reminderapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class TermsPolicyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_policy);

        NavigationHelper.setupNavigation(this);

        Button btnAgree = findViewById(R.id.btnAgree);
        btnAgree.setOnClickListener(v -> {
            Toast.makeText(this, "You have agreed to the terms", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

}