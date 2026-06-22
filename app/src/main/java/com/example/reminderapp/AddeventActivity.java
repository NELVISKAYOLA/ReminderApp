package com.example.reminderapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddeventActivity extends AppCompatActivity {

    private EditText etEventTitle, etEventNotes, etEventDate, etEventTime, etDuration;
    private Spinner spRepeat, spPriority;
    private Button btnSave, btnCancel;
    private AppDatabase db;
    private int activeUserId;
    private int editReminderId = -1;
    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        NavigationHelper.setupNavigation(this);
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
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        setupSpinners();

        // Check for Edit Mode
        editReminderId = getIntent().getIntExtra("reminder_id", -1);
        if (editReminderId != -1) {
            loadReminderForEdit();
        }

        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());

        spRepeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals("Custom")) showDatePicker();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> saveReminder());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadReminderForEdit() {
        ReminderEntity reminder = null;
        List<ReminderEntity> reminders = db.reminderDao().getRemindersForUser(activeUserId);
        for(ReminderEntity r : reminders) {
            if(r.getId() == editReminderId) {
                reminder = r;
                break;
            }
        }

        if (reminder != null) {
            etEventTitle.setText(reminder.getTitle());
            etEventNotes.setText(reminder.getNotes());
            etEventDate.setText(reminder.getDate());
            etEventTime.setText(reminder.getTime());
            etDuration.setText(reminder.getDuration());
            
            setSpinnerSelection(spRepeat, reminder.getRepeat());
            setSpinnerSelection(spPriority, reminder.getPriority());

            try {
                String[] dateParts = reminder.getDate().split("-");
                String[] timeParts = reminder.getTime().split(":");
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

        ReminderEntity reminder = new ReminderEntity(activeUserId, title, notes, date, time, duration, repeat, priority);
        if (editReminderId != -1) {
            reminder.setId(editReminderId);
            db.reminderDao().update(reminder);
        } else {
            db.reminderDao().insert(reminder);
        }

        scheduleAlarm(reminder);
        playNotificationSound();
        Toast.makeText(this, "Reminder saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void scheduleAlarm(ReminderEntity reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("title", reminder.getTitle());
        intent.putExtra("notes", reminder.getNotes());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, selectedDateTime.getTimeInMillis(), pendingIntent);
        }
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
            etEventDate.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);
        }, year, month, day).show();
    }

    private void setupSpinners() {
        String[] repeatOptions = {"None", "Daily", "Weekly", "Custom"};
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, repeatOptions);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepeat.setAdapter(repeatAdapter);

        String[] priorityOptions = {"Work", "Personal", "Urgent"};
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (NavigationHelper.handleOptionsMenu(this, item)) return true;
        return super.onOptionsItemSelected(item);
    }
}
