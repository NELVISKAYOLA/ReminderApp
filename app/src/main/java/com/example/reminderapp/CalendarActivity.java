package com.example.reminderapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarActivity extends AppCompatActivity {

    private RecyclerView rvDayReminders;
    private TextView tvSelectedDate;
    private AppDatabase db;
    private int activeUserId;
    private List<ReminderEntity> allReminders = new ArrayList<>();
    private CalendarAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        NavigationHelper.setupNavigation(this);
        db = AppDatabase.getInstance(this);
        activeUserId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("active_user_id", -1);

        CalendarView calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        rvDayReminders = findViewById(R.id.rvDayReminders);

        rvDayReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CalendarAdapter();
        rvDayReminders.setAdapter(adapter);

        loadAllReminders();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Standardize format to YYYY-MM-DD
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvSelectedDate.setText("Events for: " + date);
            showRemindersForDate(date);
        });
    }

    private void loadAllReminders() {
        if (activeUserId != -1) {
            // Only show public reminders on the public calendar
            allReminders = db.reminderDao().getPublicRemindersForUser(activeUserId);
        }
    }

    private void showRemindersForDate(String date) {
        List<ReminderEntity> dayReminders = allReminders.stream()
                .filter(r -> r.getDate().equals(date))
                .collect(Collectors.toList());

        adapter.setReminders(dayReminders);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    private class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
        private List<ReminderEntity> reminders = new ArrayList<>();

        public void setReminders(List<ReminderEntity> reminders) {
            this.reminders = reminders;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ReminderEntity reminder = reminders.get(position);
            holder.tvContent.setText(reminder.getTitle());
            holder.tvSubtext.setText(reminder.getTime());
            holder.tvSubtext.setVisibility(View.VISIBLE);
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(CalendarActivity.this, ReminderDetailActivity.class);
                intent.putExtra("reminder_id", reminder.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return reminders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent, tvSubtext;
            ViewHolder(View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tvItemContent);
                tvSubtext = itemView.findViewById(R.id.tvItemSubtext);
            }
        }
    }
}
