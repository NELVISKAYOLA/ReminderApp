package com.example.reminderapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddReminderActivity extends AppCompatActivity {

    private EditText etTitle, etNotes, etDate, etTime, etDuration, etContactName, etPhone;
    private Spinner spRepeat, spPriority;
    private MaterialSwitch swPrivate;
    private MaterialButtonToggleGroup toggleGroupType;
    private LinearLayout layoutContact;
    private AppDatabase db;
    private int activeUserId;
    private int editReminderId = -1;
    private Calendar selectedDateTime = Calendar.getInstance();
    private String selectedType = "Normal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        db = AppDatabase.getInstance(this);
        activeUserId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("active_user_id", -1);

        initViews();
        prefillDateTime();
        setupSpinners();
        setupTypeToggle();
        
        NavigationHelper.setupNavigation(this);

        editReminderId = getIntent().getIntExtra("reminder_id", -1);
        String prefillDate = getIntent().getStringExtra("prefill_date");

        if (editReminderId != -1) {
            loadReminderForEdit();
        } else if (prefillDate != null && !prefillDate.isEmpty()) {
            etDate.setText(prefillDate);
            try {
                String[] parts = prefillDate.split("-");
                selectedDateTime.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            } catch (Exception ignored) {}
        }

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveReminder());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void prefillDateTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        etDate.setText(sdfDate.format(selectedDateTime.getTime()));
        etTime.setText(sdfTime.format(selectedDateTime.getTime()));
    }

    private void loadReminderForEdit() {
        new Thread(() -> {
            ReminderEntity r = db.reminderDao().getReminderById(editReminderId);
            if (r != null) {
                runOnUiThread(() -> {
                    etTitle.setText(r.getTitle());
                    etNotes.setText(r.getNotes());
                    etDate.setText(r.getDate());
                    etTime.setText(r.getTime());
                    etDuration.setText(r.getDuration());
                    etContactName.setText(r.getContactName());
                    etPhone.setText(r.getPhoneNumber());
                    
                    setSpinnerSelection(spRepeat, r.getRepeat());
                    setSpinnerSelection(spPriority, r.getPriority());
                    swPrivate.setChecked(r.isPrivate());
                    
                    selectedType = r.getType();
                    updateTypeSelectionUI();

                    selectedDateTime.setTimeInMillis(r.getDateTime());
                });
            }
        }).start();
    }

    private void updateTypeSelectionUI() {
        int buttonId = R.id.btnTypeNormal;
        if ("Call".equals(selectedType)) buttonId = R.id.btnTypeCall;
        else if ("Meeting".equals(selectedType)) buttonId = R.id.btnTypeMeeting;
        
        toggleGroupType.check(buttonId);
        layoutContact.setVisibility("Call".equals(selectedType) ? View.VISIBLE : View.GONE);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etNotes = findViewById(R.id.etNotes);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etDuration = findViewById(R.id.etDuration);
        etContactName = findViewById(R.id.etContactName);
        etPhone = findViewById(R.id.etPhone);
        spRepeat = findViewById(R.id.spRepeat);
        spPriority = findViewById(R.id.spPriority);
        swPrivate = findViewById(R.id.swPrivate);
        toggleGroupType = findViewById(R.id.toggleGroupType);
        layoutContact = findViewById(R.id.layoutContact);
    }

    private void setupTypeToggle() {
        toggleGroupType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTypeNormal) {
                    selectedType = "Normal";
                    layoutContact.setVisibility(View.GONE);
                } else if (checkedId == R.id.btnTypeCall) {
                    selectedType = "Call";
                    layoutContact.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.btnTypeMeeting) {
                    selectedType = "Meeting";
                    layoutContact.setVisibility(View.GONE);
                }
            }
        });
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

    private void showDatePicker() {
        int year = selectedDateTime.get(Calendar.YEAR);
        int month = selectedDateTime.get(Calendar.MONTH);
        int day = selectedDateTime.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDateTime.set(Calendar.YEAR, y);
            selectedDateTime.set(Calendar.MONTH, m);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, d);
            etDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d));
        }, year, month, day).show();
    }

    private void showTimePicker() {
        int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDateTime.get(Calendar.MINUTE);
        new TimePickerDialog(this, (view, h, m) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, h);
            selectedDateTime.set(Calendar.MINUTE, m);
            selectedDateTime.set(Calendar.SECOND, 0);
            etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
        }, hour, minute, true).show();
    }

    private void saveReminder() {
        String title = etTitle.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        String contact = etContactName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Title, Date and Time are required", Toast.LENGTH_SHORT).show();
            return;
        }

        ReminderEntity reminder = new ReminderEntity(
                activeUserId,
                title,
                notes,
                date,
                time,
                duration,
                spRepeat.getSelectedItem().toString(),
                spPriority.getSelectedItem().toString(),
                swPrivate.isChecked(),
                selectedType,
                contact.isEmpty() ? null : contact,
                phone.isEmpty() ? null : phone,
                selectedDateTime.getTimeInMillis(),
                false
        );

        if (editReminderId != -1) {
            reminder.setId(editReminderId);
        }

        new Thread(() -> {
            try {
                if (editReminderId != -1) {
                    db.reminderDao().update(reminder);
                } else {
                    long id = db.reminderDao().insert(reminder);
                    reminder.setId((int) id);
                }
                runOnUiThread(() -> {
                    AlarmHelper.scheduleAlarm(this, reminder);
                    // Send broadcast to refresh dashboard
                    sendBroadcast(new Intent(DashboardActivity.ACTION_REMINDER_ADDED));
                    Toast.makeText(this, "Reminder saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
