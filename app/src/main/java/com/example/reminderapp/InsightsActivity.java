package com.example.reminderapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;

import java.util.List;
import java.util.stream.Collectors;

public class InsightsActivity extends AppCompatActivity {

    private TextView tvTotalTasksCount, tvUrgentTasksCount;
    private AppDatabase db;
    private int activeUserId;
    private PriorityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        NavigationHelper.setupNavigation(this);
        db = AppDatabase.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        activeUserId = prefs.getInt("active_user_id", -1);

        RecyclerView rvPriorities = findViewById(R.id.rvPriorities);
        tvTotalTasksCount = findViewById(R.id.tvTotalTasksCount);
        tvUrgentTasksCount = findViewById(R.id.tvUrgentTasksCount);

        rvPriorities.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PriorityAdapter();
        rvPriorities.setAdapter(adapter);

        loadInsights();
    }

    private void loadInsights() {
        if (activeUserId == -1) return;

        List<ReminderEntity> allReminders = db.reminderDao().getRemindersForUser(activeUserId);
        
        List<ReminderEntity> priorities = allReminders.stream()
                .filter(r -> r.getPriority().equalsIgnoreCase("Urgent") || r.getPriority().equalsIgnoreCase("High"))
                .collect(Collectors.toList());

        adapter.setReminders(priorities);

        tvTotalTasksCount.setText(String.valueOf(allReminders.size()));
        tvUrgentTasksCount.setText(String.valueOf(priorities.size()));
    }


    private class PriorityAdapter extends RecyclerView.Adapter<PriorityAdapter.ViewHolder> {
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
            holder.tvSubtext.setText(reminder.getDate() + " at " + reminder.getTime());
            holder.tvSubtext.setVisibility(View.VISIBLE);
            holder.ivPriority.setVisibility(View.VISIBLE); // These are filtered as urgent/high
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(InsightsActivity.this, ReminderDetailActivity.class);
                intent.putExtra("reminder_id", reminder.getId());
                startActivity(intent);
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
