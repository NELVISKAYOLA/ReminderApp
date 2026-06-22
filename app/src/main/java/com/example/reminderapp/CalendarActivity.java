package com.example.reminderapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        NavigationHelper.setupNavigation(this);

        CalendarView calendarView = findViewById(R.id.calendarView);
        final TextView tvSelectedDate = findViewById(R.id.tvSelectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvSelectedDate.setText("Selected Date: " + date);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (NavigationHelper.handleOptionsMenu(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}