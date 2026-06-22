package com.example.reminderapp;

import android.content.Intent;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private ListView lvReminders;
    private TextView tvDayOfWeek, tvDay, tvMonthYear;
    private AppDatabase db;
    private int activeUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        NavigationHelper.setupNavigation(this);
        db = AppDatabase.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        activeUserId = prefs.getInt("active_user_id", -1);

        // Initialize Views
        lvReminders = findViewById(R.id.lvReminders);
        tvDayOfWeek = findViewById(R.id.tvDayOfWeek);
        tvDay = findViewById(R.id.tvDay);
        tvMonthYear = findViewById(R.id.tvMonthYear);

        // Set Current Date
        setCurrentDate();

        lvReminders.setOnItemClickListener((parent, view, position, id) -> {
            ReminderEntity reminder = (ReminderEntity) parent.getItemAtPosition(position);
            if (reminder != null) {
                Intent intent = new Intent(DashboardActivity.this, ReminderDetailActivity.class);
                intent.putExtra("reminder_id", reminder.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }

    private void loadReminders() {
        if (activeUserId == -1) return;

        List<ReminderEntity> reminders = db.reminderDao().getRemindersForUser(activeUserId);
        
        ArrayAdapter<ReminderEntity> adapter = new ArrayAdapter<ReminderEntity>(this,
                R.layout.item_card, R.id.tvItemContent, reminders) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(R.id.tvItemContent);
                ReminderEntity reminder = getItem(position);
                if (reminder != null) {
                    text.setText(reminder.getTitle() + " (" + reminder.getPriority() + ")");
                }
                return view;
            }
        };
        lvReminders.setAdapter(adapter);
    }

    private void setCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        tvDayOfWeek.setText(dayOfWeekFormat.format(calendar.getTime()));
        tvDay.setText(dayFormat.format(calendar.getTime()));
        tvMonthYear.setText(monthYearFormat.format(calendar.getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }
        if (NavigationHelper.handleOptionsMenu(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
