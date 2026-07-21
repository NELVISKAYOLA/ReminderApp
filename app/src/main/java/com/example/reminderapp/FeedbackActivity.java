package com.example.reminderapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.FeedbackEntity;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedbackActivity extends AppCompatActivity {
    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private AppDatabase db;
    private int activeUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        
        db = AppDatabase.getInstance(this);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        activeUserId = prefs.getInt("active_user_id", -1);

        rvChat = findViewById(R.id.rvFeedbackChat);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter();
        rvChat.setAdapter(adapter);

        findViewById(R.id.btnSendFeedback).setOnClickListener(v -> {
            TextView input = findViewById(R.id.etFeedbackInput);
            String message = input.getText().toString().trim();
            if (!message.isEmpty() && activeUserId != -1) {
                FeedbackEntity feedback = new FeedbackEntity(0, activeUserId, message, "USER", System.currentTimeMillis(), false);
                new Thread(() -> {
                    db.feedbackDao().insert(feedback);
                    
                    runOnUiThread(() -> {
                        input.setText("");
                        loadMessages();
                    });
                }).start();
            }
        });

        loadMessages();
    }

    private void loadMessages() {
        if (activeUserId == -1) return;
        new Thread(() -> {
            List<FeedbackEntity> messages = db.feedbackDao().getFeedbackForUser(activeUserId);
            db.feedbackDao().markSupplierRepliesAsRead(activeUserId);
            runOnUiThread(() -> {
                adapter.setMessages(messages);
                if (messages.size() > 0) {
                    rvChat.scrollToPosition(messages.size() - 1);
                }
            });
        }).start();
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        private List<FeedbackEntity> messages = new ArrayList<>();

        public void setMessages(List<FeedbackEntity> messages) {
            this.messages = messages;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback_message, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FeedbackEntity msg = messages.get(position);
            holder.tvMessage.setText(msg.getMessage());
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(new Date(msg.getTimestamp())));

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.card.getLayoutParams();
            LinearLayout.LayoutParams timeParams = (LinearLayout.LayoutParams) holder.tvTimestamp.getLayoutParams();
            
            if (msg.getSenderType().equals("USER")) {
                params.gravity = Gravity.END;
                timeParams.gravity = Gravity.END;
                holder.card.setCardBackgroundColor(getResources().getColor(R.color.luxury_purple));
                holder.tvMessage.setTextColor(getResources().getColor(R.color.white));
            } else {
                params.gravity = Gravity.START;
                timeParams.gravity = Gravity.START;
                holder.card.setCardBackgroundColor(getResources().getColor(R.color.card_background));
                holder.tvMessage.setTextColor(getResources().getColor(R.color.main_text));
            }
            holder.card.setLayoutParams(params);
            holder.tvTimestamp.setLayoutParams(timeParams);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage, tvTimestamp;
            MaterialCardView card;

            ViewHolder(View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
                card = itemView.findViewById(R.id.cardMessage);
            }
        }
    }
}
