package com.example.reminderapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.FeedbackEntity;
import com.example.reminderapp.database.User;

import java.util.ArrayList;
import java.util.List;

public class AdminSupportActivity extends AppCompatActivity {
    private RecyclerView rvThreads;
    private ThreadAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_support);

        db = AppDatabase.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvThreads = findViewById(R.id.rvSupportThreads);
        rvThreads.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ThreadAdapter();
        rvThreads.setAdapter(adapter);

        loadThreads();
    }

    private void loadThreads() {
        new Thread(() -> {
            List<FeedbackEntity> threads = db.feedbackDao().getAllFeedbackThreads();
            runOnUiThread(() -> adapter.setThreads(threads));
        }).start();
    }

    private class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder> {
        private List<FeedbackEntity> threads = new ArrayList<>();

        public void setThreads(List<FeedbackEntity> threads) {
            this.threads = threads;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_support_thread, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FeedbackEntity thread = threads.get(position);
            
            // Get user name for the thread
            new Thread(() -> {
                User user = db.userDao().getUserById(thread.getUserId());
                if (user != null) {
                    runOnUiThread(() -> holder.tvUser.setText(user.getName()));
                }
            }).start();

            holder.tvLastMsg.setText(thread.getMessage());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AdminSupportActivity.this, AdminChatActivity.class);
                intent.putExtra("target_user_id", thread.getUserId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return threads.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvUser, tvLastMsg;
            ViewHolder(View itemView) {
                super(itemView);
                tvUser = itemView.findViewById(R.id.tvSupportUser);
                tvLastMsg = itemView.findViewById(R.id.tvSupportLastMsg);
            }
        }
    }
}
