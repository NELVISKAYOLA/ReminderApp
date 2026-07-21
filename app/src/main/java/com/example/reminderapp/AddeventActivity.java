package com.example.reminderapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.materialswitch.MaterialSwitch;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddeventActivity extends AppCompatActivity {

    private EditText etEventTitle, etEventNotes, etEventDate, etEventTime, etDuration;
    private Spinner spRepeat, spPriority;
    private MaterialSwitch swPrivate;
    private Button btnSave, btnCancel;
    private AppDatabase db;
    private int activeUserId;
    private int editReminderId = -1;
    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        db = AppDatabase.getInstance(this);

        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        activeUserId = prefs.getInt("active_user_id", -1);

        // Initialize Views
        etEventTitle = findViewById(R.id.etEventTitle);
        etEventNotes = findViewById(R.id.etEventNotes);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        etDuration = findViewById(R.id.etDuration);
        spRepeat = findViewById(R.id.spRepeat);
        spPriority = findViewById(R.id.spPriority);
        swPrivate = findViewById(R.id.swPrivate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        setupSpinners();

        // Check for Edit Mode or Prefill Date
        editReminderId = getIntent().getIntExtra("reminder_id", -1);
        String prefillDate = getIntent().getStringExtra("prefill_date");

        if (editReminderId != -1) {
            setTitle("Edit Reminder");
            loadReminderForEdit();
        } else {
            setTitle("Add Reminder");
            if (prefillDate != null && !prefillDate.isEmpty()) {
                etEventDate.setText(prefillDate);
                try {
                    String[] parts = prefillDate.split("-");
                    selectedDateTime.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
                } catch (Exception ignored) {}
            }
        }

        NavigationHelper.setupNavigation(this);

        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());

        spRepeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (Objects.equals(selected, "Custom")) showDatePicker();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> saveReminder());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadReminderForEdit() {
        List<ReminderEntity> reminders = db.reminderDao().getRemindersForUser(activeUserId);
        ReminderEntity targetReminder = null;
        for(ReminderEntity r : reminders) {
            if(r.getId() == editReminderId) {
                targetReminder = r;
                break;
            }
        }

        if (targetReminder != null) {
            etEventTitle.setText(targetReminder.getTitle());
            etEventNotes.setText(targetReminder.getNotes());
            etEventDate.setText(targetReminder.getDate());
            etEventTime.setText(targetReminder.getTime());
            etDuration.setText(targetReminder.getDuration());
            
            setSpinnerSelection(spRepeat, targetReminder.getRepeat());
            setSpinnerSelection(spPriority, targetReminder.getPriority());
            swPrivate.setChecked(targetReminder.isPrivate());

            try {
                String[] dateParts = targetReminder.getDate().split("-");
                String[] timeParts = targetReminder.getTime().split(":");
                selectedDateTime.set(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[2]),
                        Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
            } catch (Exception ignored) {}
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveReminder() {
        String title = etEventTitle.getText().toString().trim();
        String notes = etEventNotes.getText().toString().trim();
        String date = etEventDate.getText().toString().trim();
        String time = etEventTime.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        String repeat = spRepeat.getSelectedItem().toString();
        String priority = spPriority.getSelectedItem().toString();
        boolean isPrivate = swPrivate.isChecked();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title is mandatory!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (activeUserId == -1) {
            Toast.makeText(this, "User error", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar now = Calendar.getInstance();
        if (selectedDateTime.getTimeInMillis() < now.getTimeInMillis()) {
            Toast.makeText(this, "Set time in the future!", Toast.LENGTH_LONG).show();
            return;
        }

        ReminderEntity reminder = new ReminderEntity(activeUserId, title, notes, date, time, duration, repeat, priority, isPrivate);
        reminder.setDateTime(selectedDateTime.getTimeInMillis());
        if (editReminderId != -1) {
            reminder.setId(editReminderId);
            db.reminderDao().update(reminder);
        } else {
            long id = db.reminderDao().insert(reminder);
            reminder.setId((int) id);
        }

        AlarmHelper.scheduleAlarm(this, reminder);
        playNotificationSound();
        Toast.makeText(this, "Reminder saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showTimePicker() {
        int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDateTime.get(Calendar.MINUTE);
        new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour);
            selectedDateTime.set(Calendar.MINUTE, selectedMinute);
            selectedDateTime.set(Calendar.SECOND, 0);
            etEventTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
        }, hour, minute, true).show();
    }

    private void showDatePicker() {
        int year = selectedDateTime.get(Calendar.YEAR);
        int month = selectedDateTime.get(Calendar.MONTH);
        int day = selectedDateTime.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            selectedDateTime.set(Calendar.YEAR, selectedYear);
            selectedDateTime.set(Calendar.MONTH, selectedMonth);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, selectedDay);
            String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            etEventDate.setText(formattedDate);
        }, year, month, day).show();
    }

    private void setupSpinners() {
        String[] repeatOptions = {"None", "Daily", "Weekly", "Custom"};
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, repeatOptions);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepeat.setAdapter(repeatAdapter);

        String[] priorityOptions = {"Personal", "Work", "Urgent"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorityOptions);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(priorityAdapter);
    }

    private void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            RingtoneManager.getRingtone(getApplicationContext(), notification).play();
        } catch (Exception e) { e.printStackTrace(); }
    }

}
