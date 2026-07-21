package com.example.reminderapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderEntity;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
            case "android.intent.action.QUICKBOOT_POWERON":
            case "com.htc.intent.action.QUICKBOOT_POWERON":
                AppDatabase db = AppDatabase.getInstance(context);
                new Thread(() -> {
                    // Get all uncompleted reminders for all users
                    List<ReminderEntity> allReminders = db.reminderDao().getAllUncompletedReminders();
                    for (ReminderEntity reminder : allReminders) {
                        if (reminder.getDateTime() > System.currentTimeMillis()) {
                            AlarmHelper.scheduleAlarm(context, reminder);
                        }
                    }
                }).start();
                break;
        }
    }
}
