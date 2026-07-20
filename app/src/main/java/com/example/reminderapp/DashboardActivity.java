package com.example.reminderapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView rvReminders;
    private View emptyStateLayout;
    private AppDatabase db;
    private int activeUserId;
    private ReminderAdapter adapter;

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
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter();
        rvReminders.setAdapter(adapter);

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddeventActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }

    private void loadReminders() {
        if (activeUserId == -1) return;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = sdf.format(calendar.getTime());

        List<ReminderEntity> allReminders = db.reminderDao().getPublicRemindersForUser(activeUserId);
        List<ReminderEntity> todaysReminders = allReminders.stream()
                .filter(r -> r.getDate().equals(todayStr) && !r.isCompleted())
                .collect(Collectors.toList());
        
        if (todaysReminders.isEmpty()) {
            rvReminders.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            rvReminders.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            adapter.setReminders(todaysReminders);
        }
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
            holder.tvSubtext.setText(reminder.getTime() + (reminder.getDuration().isEmpty() ? "" : " • " + reminder.getDuration()));
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
