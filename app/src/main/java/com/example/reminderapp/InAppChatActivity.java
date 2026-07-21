package com.example.reminderapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.Message;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InAppChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ChatAdapter adapter;
    private AppDatabase db;
    private int activeUserId;
    private int contactUserId;
    private String contactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_chat); // Reusing the same layout structure

        NavigationHelper.setupNavigation(this);
        db = AppDatabase.getInstance(this);

        activeUserId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("active_user_id", -1);
        contactUserId = getIntent().getIntExtra("contact_user_id", -1);
        contactName = getIntent().getStringExtra("contact_name");
        
        setTitle(contactName != null ? contactName : "Chat");

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter();
        rvChat.setAdapter(adapter);

        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        if (activeUserId == -1 || contactUserId == -1) return;
        new Thread(() -> {
            List<Message> messages = db.messageDao().getChatHistory(activeUserId, contactUserId);
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

        Message message = new Message(
                activeUserId,
                contactUserId,
                msgContent,
                System.currentTimeMillis(),
                "TEXT"
        );

        new Thread(() -> {
            db.messageDao().insert(message);
            runOnUiThread(() -> {
                etMessage.setText("");
                loadMessages();
            });
        }).start();
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        private List<Message> messages = new ArrayList<>();

        public void setMessages(List<Message> messages) {
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
            Message msg = messages.get(position);
            holder.tvMessage.setText(msg.getContent());
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(new Date(msg.getTimestamp())));

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.cardMessage.getLayoutParams();
            if (msg.getSenderId() != activeUserId) {
                params.gravity = Gravity.START;
                holder.cardMessage.setCardBackgroundColor(ContextCompat.getColor(InAppChatActivity.this, R.color.card_background));
                holder.tvMessage.setTextColor(ContextCompat.getColor(InAppChatActivity.this, R.color.main_text));
            } else {
                params.gravity = Gravity.END;
                holder.cardMessage.setCardBackgroundColor(ContextCompat.getColor(InAppChatActivity.this, R.color.primary_light));
                holder.tvMessage.setTextColor(ContextCompat.getColor(InAppChatActivity.this, R.color.white));
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
