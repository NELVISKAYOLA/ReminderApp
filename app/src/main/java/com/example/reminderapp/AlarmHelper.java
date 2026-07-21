package com.example.reminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import com.example.reminderapp.database.ReminderEntity;

public class AlarmHelper {

    public static void scheduleAlarm(Context context, ReminderEntity reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Check for Exact Alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                new Handler(Looper.getMainLooper()).post(() -> 
                    Toast.makeText(context, "Please grant permission to schedule exact alarms", Toast.LENGTH_LONG).show()
                );
                return;
            }
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", reminder.getTitle());
        intent.putExtra("notes", reminder.getNotes());
        intent.putExtra("priority", reminder.getPriority());
        intent.putExtra("time", reminder.getTime());
        intent.putExtra("duration", reminder.getDuration());
        intent.putExtra("contact_name", reminder.getContactName());
        intent.putExtra("type", reminder.getType());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                reminder.getId(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (reminder.getDateTime() > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, 
                    reminder.getDateTime(), 
                    pendingIntent
            );
        }
    }

    public static void cancelAlarm(Context context, ReminderEntity reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                reminder.getId(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}
