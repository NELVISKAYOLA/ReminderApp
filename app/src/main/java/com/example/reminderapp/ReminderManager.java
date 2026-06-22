package com.example.reminderapp;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReminderManager {
    private static final String PREFS_NAME = "reminder_prefs";
    private static final String KEY_REMINDERS = "reminders_list";

    public static void saveReminder(Context context, Reminder reminder) {
        List<Reminder> reminders = getReminders(context);
        reminders.add(reminder);
        saveList(context, reminders);
    }

    public static List<Reminder> getReminders(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_REMINDERS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Reminder>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public static Reminder getReminderById(Context context, String id) {
        List<Reminder> reminders = getReminders(context);
        for (Reminder r : reminders) {
            if (r.getId().equals(id)) return r;
        }
        return null;
    }

    public static void updateReminder(Context context, Reminder updatedReminder) {
        List<Reminder> reminders = getReminders(context);
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(updatedReminder.getId())) {
                reminders.set(i, updatedReminder);
                break;
            }
        }
        saveList(context, reminders);
    }

    public static void deleteReminder(Context context, String id) {
        List<Reminder> reminders = getReminders(context);
        reminders.removeIf(r -> r.getId().equals(id));
        saveList(context, reminders);
    }

    private static void saveList(Context context, List<Reminder> reminders) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(reminders);
        editor.putString(KEY_REMINDERS, json);
        editor.apply();
    }
}
