package com.example.reminderapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.Message;

public class InAppCallActivity extends AppCompatActivity {

    private TextView tvCallStatus, tvCallerName;
    private ImageView ivEndCall;
    private int contactUserId;
    private String contactName;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_call);

        contactUserId = getIntent().getIntExtra("contact_user_id", -1);
        contactName = getIntent().getStringExtra("contact_name");
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("active_user_id", -1);

        tvCallerName = findViewById(R.id.tvCallerName);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        ivEndCall = findViewById(R.id.ivEndCall);

        tvCallerName.setText(contactName != null ? contactName : "Unknown");
        tvCallStatus.setText("Calling...");

        ivEndCall.setOnClickListener(v -> endCall());

        // Simulate connecting and call duration
        new Handler().postDelayed(() -> tvCallStatus.setText("00:01"), 2000);
    }

    private void endCall() {
        tvCallStatus.setText("Call Ended");
        
        // Log the call in internal messages
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Message log = new Message(currentUserId, contactUserId, "In-app call to " + contactName, System.currentTimeMillis(), "CALL_LOG");
            db.messageDao().insert(log);
            runOnUiThread(() -> finish());
        }).start();
    }
}
