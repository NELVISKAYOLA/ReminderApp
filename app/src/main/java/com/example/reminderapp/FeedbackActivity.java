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
            String feedback = ((EditText)findViewById(R.id.etFeedback)).getText().toString().trim();
            if (feedback.isEmpty()) {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.feedback_success), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}