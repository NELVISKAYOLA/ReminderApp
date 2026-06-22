package com.example.reminderapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;

import java.util.List;

public class ReminderDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvNotes, tvDate, tvTime, tvPriority, tvRepeat, tvDuration;
    private int reminderId;
    private ReminderEntity currentReminder;
    private AppDatabase db;
    private int activeUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_detail);

        NavigationHelper.setupNavigation(this);
        db = AppDatabase.getInstance(this);

        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        activeUserId = prefs.getInt("active_user_id", -1);

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvNotes = findViewById(R.id.tvDetailNotes);
        tvDate = findViewById(R.id.tvDetailDate);
        tvTime = findViewById(R.id.tvDetailTime);
        tvPriority = findViewById(R.id.tvDetailPriority);
        tvRepeat = findViewById(R.id.tvDetailRepeat);
        tvDuration = findViewById(R.id.tvDetailDuration);

        Button btnEdit = findViewById(R.id.btnEditReminder);
        Button btnDelete = findViewById(R.id.btnDeleteReminder);

        reminderId = getIntent().getIntExtra("reminder_id", -1);
        loadReminder();

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddeventActivity.class);
            intent.putExtra("reminder_id", reminderId);
            startActivity(intent);
            finish(); // Close detail view after starting edit
        });

        btnDelete.setOnClickListener(v -> {
            if (currentReminder != null) {
                db.reminderDao().delete(currentReminder);
                Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadReminder() {
        if (activeUserId == -1 || reminderId == -1) return;

        List<ReminderEntity> reminders = db.reminderDao().getRemindersForUser(activeUserId);
        for (ReminderEntity r : reminders) {
            if (r.getId() == reminderId) {
                currentReminder = r;
                break;
            }
        }

        if (currentReminder != null) {
            tvTitle.setText(currentReminder.getTitle());
            tvNotes.setText(currentReminder.getNotes());
            tvDate.setText(currentReminder.getDate());
            tvTime.setText(currentReminder.getTime());
            tvPriority.setText("Priority: " + currentReminder.getPriority());
            tvRepeat.setText("Repeat: " + currentReminder.getRepeat());
            tvDuration.setText("Duration: " + currentReminder.getDuration());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (NavigationHelper.handleOptionsMenu(this, item)) return true;
        return super.onOptionsItemSelected(item);
    }
}
