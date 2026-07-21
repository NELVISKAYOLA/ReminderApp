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
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;

import java.util.List;
import java.util.concurrent.Executor;

public class PrivateRemindersActivity extends AppCompatActivity {

    private RecyclerView rvPrivateReminders;
    private View lockScreen;
    private AppDatabase db;
    private int activeUserId;
    private PrivateAdapter adapter;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_reminders);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        db = AppDatabase.getInstance(this);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        activeUserId = prefs.getInt("active_user_id", -1);

        rvPrivateReminders = findViewById(R.id.rvPrivateReminders);
        lockScreen = findViewById(R.id.lockScreen);
        
        rvPrivateReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PrivateAdapter();
        rvPrivateReminders.setAdapter(adapter);

        findViewById(R.id.btnAuthenticate).setOnClickListener(v -> authenticate());

        setupBiometric();
        authenticate();
    }

    private void setupBiometric() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(PrivateRemindersActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                showPrivateReminders();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth_title))
                .setSubtitle(getString(R.string.auth_subtitle))
                .setNegativeButtonText(getString(R.string.auth_negative_text))
                .build();
    }

    private void authenticate() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                biometricPrompt.authenticate(promptInfo);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "No biometric hardware found", Toast.LENGTH_SHORT).show();
                showPrivateReminders(); // Fallback for testing/unsupported devices
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Biometric hardware is currently unavailable", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "No biometrics enrolled", Toast.LENGTH_SHORT).show();
                showPrivateReminders(); // Fallback
                break;
            default:
                Toast.makeText(this, "Biometric authentication error", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showPrivateReminders() {
        lockScreen.setVisibility(View.GONE);
        rvPrivateReminders.setVisibility(View.VISIBLE);
        loadPrivateReminders();
    }

    private void loadPrivateReminders() {
        if (activeUserId == -1) return;
        List<ReminderEntity> privateReminders = db.reminderDao().getPrivateRemindersForUser(activeUserId);
        adapter.setReminders(privateReminders);
    }

    private class PrivateAdapter extends RecyclerView.Adapter<PrivateAdapter.ViewHolder> {
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
            holder.tvSubtext.setText(reminder.getDate() + " " + reminder.getTime());
            holder.tvSubtext.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(R.drawable.ic_lock);
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(PrivateRemindersActivity.this, ReminderDetailActivity.class);
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
            ImageView ivIcon;
            ViewHolder(View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tvItemContent);
                tvSubtext = itemView.findViewById(R.id.tvItemSubtext);
                ivIcon = itemView.findViewById(R.id.ivItemIcon);
            }
        }
    }
}
