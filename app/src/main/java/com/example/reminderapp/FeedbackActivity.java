package com.example.reminderapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FeedbackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        NavigationHelper.setupNavigation(this);

        Button btnSubmit = findViewById(R.id.btnSubmitFeedback);
        btnSubmit.setOnClickListener(v -> {
            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}