package com.example.reminderapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.FeedbackMessage;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedbackChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ChatAdapter adapter;
    private AppDatabase db;
    private int activeUserId;
    private String activeUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_chat);

        NavigationHelper.setupNavigation(this);
        db = AppDatabase.getInstance(this);

        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        activeUserId = prefs.getInt("active_user_id", -1);
        activeUserName = prefs.getString("active_name", "User");

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter();
        rvChat.setAdapter(adapter);

        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        if (activeUserId == -1) return;
        new Thread(() -> {
            List<FeedbackMessage> messages = db.feedbackDao().getFeedbackMessages(activeUserId);
            runOnUiThread(() -> {
                adapter.setMessages(messages);
                if (adapter.getItemCount() > 0) {
                    rvChat.scrollToPosition(adapter.getItemCount() - 1);
                }
            });
        }).start();
    }

    private void sendMessage() {
        String msgContent = etMessage.getText().toString().trim();
        if (msgContent.isEmpty()) return;

        FeedbackMessage message = new FeedbackMessage(
                activeUserId,
                activeUserName,
                msgContent,
                System.currentTimeMillis(),
                false
        );

        new Thread(() -> {
            db.feedbackDao().insertFeedbackMessage(message);
            // Simulate Admin Response for demonstration (Prototyping)
            if (msgContent.toLowerCase().contains("problem") || msgContent.toLowerCase().contains("bug")) {
                FeedbackMessage adminReply = new FeedbackMessage(
                        activeUserId,
                        "Admin",
                        "Thank you for reporting this. Our team is looking into it.",
                        System.currentTimeMillis() + 1000,
                        true
                );
                db.feedbackDao().insertFeedbackMessage(adminReply);
            }
            
            runOnUiThread(() -> {
                etMessage.setText("");
                loadMessages();
            });
        }).start();
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        private List<FeedbackMessage> messages = new ArrayList<>();

        public void setMessages(List<FeedbackMessage> messages) {
            this.messages = messages;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FeedbackMessage msg = messages.get(position);
            holder.tvMessage.setText(msg.getMessage());
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(new Date(msg.getTimestamp())));

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.cardMessage.getLayoutParams();
            if (msg.isAdmin()) {
                params.gravity = Gravity.START;
                holder.cardMessage.setCardBackgroundColor(ContextCompat.getColor(FeedbackChatActivity.this, R.color.card_background));
            } else {
                params.gravity = Gravity.END;
                holder.cardMessage.setCardBackgroundColor(ContextCompat.getColor(FeedbackChatActivity.this, R.color.primary_light));
                holder.tvMessage.setTextColor(ContextCompat.getColor(FeedbackChatActivity.this, R.color.white));
            }
            holder.cardMessage.setLayoutParams(params);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage, tvTimestamp;
            MaterialCardView cardMessage;
            ViewHolder(View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
                cardMessage = itemView.findViewById(R.id.cardMessage);
            }
        }
    }
}
