package com.example.reminderapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity {

    private ListView lvSearchResults;
    private AppDatabase db;
    private int activeUserId;
    private List<ReminderEntity> allReminders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = AppDatabase.getInstance(this);
        activeUserId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("active_user_id", -1);

        EditText etSearchQuery = findViewById(R.id.etSearchQuery);
        lvSearchResults = findViewById(R.id.lvSearchResults);

        loadAllReminders();
        performSearch(""); // Show all reminders initially

        etSearchQuery.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        lvSearchResults.setOnItemClickListener((parent, view, position, id) -> {
            ReminderEntity reminder = (ReminderEntity) parent.getItemAtPosition(position);
            Intent intent = new Intent(this, ReminderDetailActivity.class);
            intent.putExtra("reminder_id", reminder.getId());
            startActivity(intent);
        });
    }

    private void loadAllReminders() {
        if (activeUserId != -1) {
            allReminders = db.reminderDao().getRemindersForUser(activeUserId);
        }
    }

    private void performSearch(String query) {
        String lowerQuery = query.toLowerCase();
        List<ReminderEntity> filtered = allReminders.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(lowerQuery) || r.getDate().contains(lowerQuery))
                .collect(Collectors.toList());

        ArrayAdapter<ReminderEntity> adapter = new ArrayAdapter<ReminderEntity>(this,
                R.layout.item_card, R.id.tvItemContent, filtered) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(R.id.tvItemContent);
                ReminderEntity reminder = getItem(position);
                if (reminder != null) {
                    text.setText(reminder.getTitle() + " (" + reminder.getDate() + ")");
                }
                return view;
            }
        };
        lvSearchResults.setAdapter(adapter);
    }
}
