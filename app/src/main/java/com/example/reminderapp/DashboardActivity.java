package com.example.reminderapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import androidx.core.content.ContextCompat;

public class DashboardActivity extends AppCompatActivity {

    public static final String ACTION_REMINDER_ADDED = "com.example.reminderapp.ACTION_REMINDER_ADDED";

    private final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadReminders();
        }
    };

    private RecyclerView rvReminders, rvRecentReminders, rvUpcomingReminders;
    private View emptyStateLayout;
    private AppDatabase db;
    private int activeUserId;
    private ReminderAdapter adapter, recentAdapter, upcomingAdapter;

    private TextView tvFullDate, tvReminderSummary, tvRecentHeader, tvUpcomingHeader;
    private android.widget.EditText etSearch;
    private List<ReminderEntity> currentTodaysReminders = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        NavigationHelper.setupNavigation(this);
        db = AppDatabase.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        activeUserId = prefs.getInt("active_user_id", -1);

        rvReminders = findViewById(R.id.rv_reminders);
        rvUpcomingReminders = findViewById(R.id.rv_upcoming_reminders);
        rvRecentReminders = findViewById(R.id.rv_recent_reminders);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        tvFullDate = findViewById(R.id.tv_full_date);
        tvReminderSummary = findViewById(R.id.tv_reminder_summary);
        tvUpcomingHeader = findViewById(R.id.tv_upcoming_header);
        tvRecentHeader = findViewById(R.id.tv_recent_header);
        etSearch = findViewById(R.id.etSearch);
        
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter();
        rvReminders.setAdapter(adapter);

        rvUpcomingReminders.setLayoutManager(new LinearLayoutManager(this));
        upcomingAdapter = new ReminderAdapter();
        rvUpcomingReminders.setAdapter(upcomingAdapter);

        rvRecentReminders.setLayoutManager(new LinearLayoutManager(this));
        recentAdapter = new ReminderAdapter();
        rvRecentReminders.setAdapter(recentAdapter);

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddReminderActivity.class);
            startActivity(intent);
        });

        updateDateHeader();

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReminders(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterReminders(String query) {
        if (query.isEmpty()) {
            adapter.setReminders(currentTodaysReminders);
            return;
        }
        List<ReminderEntity> filtered = currentTodaysReminders.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(query.toLowerCase()) || 
                             (r.getNotes() != null && r.getNotes().toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
        adapter.setReminders(filtered);
    }

    private void updateDateHeader() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
        tvFullDate.setText(sdf.format(calendar.getTime()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(refreshReceiver, new IntentFilter(ACTION_REMINDER_ADDED), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(refreshReceiver, new IntentFilter(ACTION_REMINDER_ADDED));
        }
        loadReminders();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(refreshReceiver);
    }

    private void loadReminders() {
        if (activeUserId == -1) return;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = sdf.format(calendar.getTime());

        new Thread(() -> {
            List<ReminderEntity> allPublicReminders = db.reminderDao().getPublicRemindersForUser(activeUserId);
            
            // 1. Today's and Overdue (Uncompleted)
            currentTodaysReminders = allPublicReminders.stream()
                    .filter(r -> !r.isCompleted() && (r.getDate().equals(todayStr) || r.getDateTime() < System.currentTimeMillis()))
                    .sorted(Comparator.comparingLong(ReminderEntity::getDateTime))
                    .collect(Collectors.toList());

            // 2. Future Reminders (Uncompleted)
            List<ReminderEntity> upcomingReminders = allPublicReminders.stream()
                    .filter(r -> !r.isCompleted() && r.getDateTime() > System.currentTimeMillis() && !r.getDate().equals(todayStr))
                    .sorted((r1, r2) -> {
                        int dateComp = r1.getDate().compareTo(r2.getDate());
                        if (dateComp != 0) return dateComp;
                        return r1.getTime().compareTo(r2.getTime());
                    })
                    .collect(Collectors.toList());

            List<ReminderEntity> recentCompleted = db.reminderDao().getRecentCompletedReminders(activeUserId);

            runOnUiThread(() -> {
                // Handle Today/Overdue List
                if (currentTodaysReminders.isEmpty()) {
                    rvReminders.setVisibility(View.GONE);
                    if (upcomingReminders.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        tvReminderSummary.setText(R.string.empty_state_title);
                    } else {
                        emptyStateLayout.setVisibility(View.GONE);
                        tvReminderSummary.setText(R.string.summary_no_tasks_today_but_upcoming);
                    }
                } else {
                    rvReminders.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                    adapter.setReminders(currentTodaysReminders);
                    int count = currentTodaysReminders.size();
                    String taskWord = count == 1 ? "task" : "tasks";
                    tvReminderSummary.setText(getString(R.string.summary_tasks_today, count, taskWord));
                }

                // Handle Upcoming List
                if (upcomingReminders.isEmpty()) {
                    tvUpcomingHeader.setVisibility(View.GONE);
                    rvUpcomingReminders.setVisibility(View.GONE);
                } else {
                    tvUpcomingHeader.setVisibility(View.VISIBLE);
                    rvUpcomingReminders.setVisibility(View.VISIBLE);
                    upcomingAdapter.setReminders(upcomingReminders);
                }

                // Handle Recent List
                if (recentCompleted.isEmpty()) {
                    tvRecentHeader.setVisibility(View.GONE);
                    rvRecentReminders.setVisibility(View.GONE);
                } else {
                    tvRecentHeader.setVisibility(View.VISIBLE);
                    rvRecentReminders.setVisibility(View.VISIBLE);
                    recentAdapter.setReminders(recentCompleted);
                }
            });
        }).start();
    }

    private class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
        private List<ReminderEntity> reminders;

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
            
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String todayStr = sdf.format(calendar.getTime());

            String timeText = reminder.getTime() + (reminder.getDuration().isEmpty() ? "" : " • " + reminder.getDuration());
            
            // Add date if it's not today
            if (!Objects.equals(reminder.getDate(), todayStr)) {
                timeText = reminder.getDate() + " • " + timeText;
            }

            if (reminder.isOverdue()) {
                holder.tvContent.setTextColor(ContextCompat.getColor(DashboardActivity.this, android.R.color.holo_red_dark));
                timeText = "OVERDUE • " + timeText;
            } else {
                holder.tvContent.setTextColor(ContextCompat.getColor(DashboardActivity.this, R.color.main_text));
            }
            
            holder.tvSubtext.setText(timeText);
            holder.tvSubtext.setVisibility(View.VISIBLE);
            
            if (reminder.getPriority().equalsIgnoreCase("Urgent") || reminder.getPriority().equalsIgnoreCase("High")) {
                holder.ivPriority.setVisibility(View.VISIBLE);
            } else {
                holder.ivPriority.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, ReminderDetailActivity.class);
                intent.putExtra("reminder_id", reminder.getId());
                startActivity(intent);
            });

            // Handle completion (Double tap or long press for simulation)
            if (!reminder.isCompleted()) {
                holder.itemView.setOnLongClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(DashboardActivity.this)
                            .setTitle("Mark as Completed?")
                            .setMessage("Do you want to mark this reminder as finished?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                reminder.setCompleted(true);
                                new Thread(() -> {
                                    db.reminderDao().update(reminder);
                                    runOnUiThread(() -> {
                                        Toast.makeText(DashboardActivity.this, "Reminder finished!", Toast.LENGTH_SHORT).show();
                                        loadReminders();
                                    });
                                }).start();
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return true;
                });
            } else {
                holder.itemView.setOnLongClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return reminders == null ? 0 : reminders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent, tvSubtext;
            ImageView ivPriority;

            ViewHolder(View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tvItemContent);
                tvSubtext = itemView.findViewById(R.id.tvItemSubtext);
                ivPriority = itemView.findViewById(R.id.ivPriorityIcon);
            }
        }
    }
}
