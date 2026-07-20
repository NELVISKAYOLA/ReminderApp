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

public class CalendarActivity extends AppCompatActivity {

    private RecyclerView rvDayReminders;
    private TextView tvSelectedDate, tvEmptyState;
    private AppDatabase db;
    private int activeUserId;
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
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvDayReminders = findViewById(R.id.rvDayReminders);

        rvDayReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CalendarAdapter();
        rvDayReminders.setAdapter(adapter);

        // Initial date
        long currentMillis = calendarView.getDate();
        java.util.Calendar initialCal = java.util.Calendar.getInstance();
        initialCal.setTimeInMillis(currentMillis);
        String initialDate = String.format("%04d-%02d-%02d", initialCal.get(java.util.Calendar.YEAR), 
                initialCal.get(java.util.Calendar.MONTH) + 1, initialCal.get(java.util.Calendar.DAY_OF_MONTH));
        tvSelectedDate.setText("Events for: " + initialDate);
        showRemindersForDate(initialDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvSelectedDate.setText("Events for: " + date);
            showRemindersForDate(date);
        });
    }

    private void showRemindersForDate(String date) {
        if (activeUserId == -1) return;
        
        new Thread(() -> {
            List<ReminderEntity> dayReminders = db.reminderDao().getPublicRemindersForDate(activeUserId, date);
            
            runOnUiThread(() -> {
                adapter.setReminders(dayReminders);
                if (dayReminders.isEmpty()) {
                    rvDayReminders.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    rvDayReminders.setVisibility(View.VISIBLE);
                    tvEmptyState.setVisibility(View.GONE);
                }
            });
        }).start();
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
