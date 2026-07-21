package com.example.reminderapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    public static final String CHANNEL_ID = "call_reminder_channel";
    public static final String NORMAL_CALL_ACTION = "com.example.reminderapp.ACTION_NORMAL_CALL";
    public static final String ONLINE_CALL_ACTION = "com.example.reminderapp.ACTION_ONLINE_CALL";

    public static void showCallNotification(Context context, int id, String contactName, String phoneNumber, String notes) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Call Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Reminders to call someone");
            notificationManager.createNotificationChannel(channel);
        }

        // Action: Normal Call
        Intent normalCallIntent = new Intent(context, CallReminderReceiver.class);
        normalCallIntent.setAction(NORMAL_CALL_ACTION);
        normalCallIntent.putExtra("phone", phoneNumber);
        PendingIntent normalCallPending = PendingIntent.getBroadcast(context, id, normalCallIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Action: Online Call (WhatsApp)
        Intent onlineCallIntent = new Intent(context, CallReminderReceiver.class);
        onlineCallIntent.setAction(ONLINE_CALL_ACTION);
        onlineCallIntent.putExtra("phone", phoneNumber);
        PendingIntent onlineCallPending = PendingIntent.getBroadcast(context, id + 1000, onlineCallIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Content Intent (Open App)
        Intent contentIntent = new Intent(context, DashboardActivity.class);
        contentIntent.putExtra("reminder_id", id);
        contentIntent.putExtra("show_call_dialog", true);
        PendingIntent contentPending = PendingIntent.getActivity(context, id + 2000, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle("Time to call " + contactName)
            .setContentText(notes != null ? notes : "Don't forget to call!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPending)
            .addAction(R.drawable.ic_home, "📞 Normal Call", normalCallPending)
            .addAction(R.drawable.ic_insights, "🌐 Online Call", onlineCallPending);

        notificationManager.notify(id, builder.build());
    }
}
