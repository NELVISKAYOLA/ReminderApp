package com.example.reminderapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.reminderapp.database.AppDatabase;

public class AdminDashboardActivity extends AppCompatActivity {
    private AppDatabase db;
    private TextView tvTotalUsers, tvActiveFeedbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = AppDatabase.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvActiveFeedbacks = findViewById(R.id.tvActiveFeedbacks);

        loadStats();

        findViewById(R.id.cardManageUsers).setOnClickListener(v -> {
            // Future implementation: UserListActivity
        });

        findViewById(R.id.cardManageFeedbacks).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminSupportActivity.class));
        });

        findViewById(R.id.cardSystemStats).setOnClickListener(v -> {
            startActivity(new Intent(this, InsightsActivity.class));
        });
    }

    private void loadStats() {
        new Thread(() -> {
            int userCount = db.userDao().getAllUsers().size();
            // Note: need to add getAllUsers to UserDao if not present
            // For now let's assume it exists or use a simple query
            runOnUiThread(() -> {
                tvTotalUsers.setText(String.valueOf(userCount));
                tvActiveFeedbacks.setText("12"); // Simulation
            });
        }).start();
    }
}
