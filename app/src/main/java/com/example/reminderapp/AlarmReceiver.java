package com.example.reminderapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.User;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "reminder_alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String notes = intent.getStringExtra("notes");
        String priority = intent.getStringExtra("priority");
        String time = intent.getStringExtra("time");
        String duration = intent.getStringExtra("duration");
        String contactName = intent.getStringExtra("contact_name");
        String type = intent.getStringExtra("type");

        createNotificationChannel(context);

        StringBuilder detailBuilder = new StringBuilder();
        if (time != null) detailBuilder.append("Scheduled for ").append(time).append("\n");
        if (duration != null && !duration.isEmpty()) detailBuilder.append("Duration: ").append(duration).append("\n\n");
        if (notes != null && !notes.isEmpty()) detailBuilder.append(notes);
        String fullDetails = detailBuilder.toString();

        int iconRes = R.drawable.ic_calendar;
        String notificationTitle = "[" + (priority != null ? priority : "Reminder") + "] " + title;
        
        if ("Urgent".equalsIgnoreCase(priority)) {
            notificationTitle = "⚠️ URGENT: " + title;
            iconRes = R.drawable.ic_priority;
        }

        Intent fullScreenIntent = new Intent(context, AlarmRingingActivity.class);
        fullScreenIntent.putExtra("title", title);
        fullScreenIntent.putExtra("notes", fullDetails);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), 
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconRes)
                .setContentTitle(notificationTitle)
                .setContentText(notes != null ? notes : "Time to check your reminder") 
                .setStyle(new NotificationCompat.BigTextStyle().bigText(fullDetails))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setContentIntent(fullScreenPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true);

        // Add In-App Call and Text Actions if it's a Call or Meeting type
        if ("Call".equalsIgnoreCase(type) || "Meeting".equalsIgnoreCase(type)) {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(context);
                User contact = db.userDao().getUserByName(contactName);
                if (contact != null) {
                    // Call Action
                    Intent callIntent = new Intent(context, InAppCallActivity.class);
                    callIntent.putExtra("contact_user_id", contact.getId());
                    callIntent.putExtra("contact_name", contact.getName());
                    PendingIntent callPending = PendingIntent.getActivity(context, 101, callIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    builder.addAction(R.drawable.ic_insights, "In-App Call", callPending);

                    // Text Action
                    Intent chatIntent = new Intent(context, InAppChatActivity.class);
                    chatIntent.putExtra("contact_user_id", contact.getId());
                    chatIntent.putExtra("contact_name", contact.getName());
                    PendingIntent chatPending = PendingIntent.getActivity(context, 102, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    builder.addAction(R.drawable.ic_logout, "In-App Text", chatPending);
                    
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (nm != null) nm.notify((int) System.currentTimeMillis(), builder.build());
                }
            }).start();
        } else {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reminder Alarms";
            String description = "Critical alerts that ring like a phone alarm";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            channel.setSound(alarmUri, null);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
