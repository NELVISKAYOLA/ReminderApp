package com.example.reminderapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String notes = intent.getStringExtra("notes");
        String priority = intent.getStringExtra("priority");
        String time = intent.getStringExtra("time");
        String duration = intent.getStringExtra("duration");

        createNotificationChannel(context);

        // Intent for when user taps the notification
        Intent dashboardIntent = new Intent(context, DashboardActivity.class);
        dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), dashboardIntent, PendingIntent.FLAG_IMMUTABLE);

        // Construct a clear, informative notification
        String notificationTitle = "Reminder: " + title;
        if ("Urgent".equalsIgnoreCase(priority)) {
            notificationTitle = "⚠️ URGENT: " + title;
        }

        StringBuilder detailBuilder = new StringBuilder();
        if (time != null) detailBuilder.append("Scheduled for ").append(time).append("\n");
        if (duration != null && !duration.isEmpty()) detailBuilder.append("Duration: ").append(duration).append("\n\n");
        detailBuilder.append(notes);

        String fullDetails = detailBuilder.toString();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_calendar)
                .setContentTitle(notificationTitle)
                .setContentText(notes) // Brief summary for collapsed view
                .setStyle(new NotificationCompat.BigTextStyle().bigText(fullDetails)) // Full details when expanded
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }

        // Handle Urgent Priority: Full Screen Alarm UI
        if ("Urgent".equalsIgnoreCase(priority)) {
            Intent alarmIntent = new Intent(context, AlarmRingingActivity.class);
            alarmIntent.putExtra("title", title);
            alarmIntent.putExtra("notes", fullDetails); // Pass the detailed text to the full screen UI
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(alarmIntent);
        } else {
            playBriefSound(context);
        }
    }

    private void playBriefSound(Context context) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ringAlarm(Context context) {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
            ringtone.play();

            // Stop ringing after 10 seconds for user comfort, but ensure it rings
            new Handler(Looper.getMainLooper()).postDelayed(ringtone::stop, 10000);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reminder Notifications";
            String description = "Channel for Reminder App alarms";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
