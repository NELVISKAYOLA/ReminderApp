package com.example.reminderapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;

import java.util.List;
import java.util.stream.Collectors;

public class InsightsActivity extends AppCompatActivity {

    private ListView lvPriorities;
    private TextView tvTotalTasks, tvUrgentTasks;
    private AppDatabase db;
    private int activeUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        NavigationHelper.setupNavigation(this);
        db = AppDatabase.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        activeUserId = prefs.getInt("active_user_id", -1);

        lvPriorities = findViewById(R.id.lvPriorities);
        tvTotalTasks = findViewById(R.id.tvTotalTasks);
        tvUrgentTasks = findViewById(R.id.tvUrgentTasks);

        loadInsights();
    }

    private void loadInsights() {
        if (activeUserId == -1) return;

        List<ReminderEntity> allReminders = db.reminderDao().getRemindersForUser(activeUserId);
        
        List<ReminderEntity> priorities = allReminders.stream()
                .filter(r -> r.getPriority().equalsIgnoreCase("Urgent"))
                .collect(Collectors.toList());

        ArrayAdapter<ReminderEntity> adapter = new ArrayAdapter<ReminderEntity>(this,
                R.layout.item_card, R.id.tvItemContent, priorities) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(R.id.tvItemContent);
                ReminderEntity reminder = getItem(position);
                if (reminder != null) {
                    text.setText(reminder.getTitle() + " (" + reminder.getDate() + ")");
                }
                return view;
            }
        };
        lvPriorities.setAdapter(adapter);

        tvTotalTasks.setText("Total Reminders: " + allReminders.size());
        tvUrgentTasks.setText("Urgent Tasks: " + priorities.size());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (NavigationHelper.handleOptionsMenu(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
