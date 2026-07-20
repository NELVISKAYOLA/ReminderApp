package com.example.reminderapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    const val CHANNEL_ID = "call_reminder_channel"
    const val NORMAL_CALL_ACTION = "com.example.reminderapp.ACTION_NORMAL_CALL"
    const val ONLINE_CALL_ACTION = "com.example.reminderapp.ACTION_ONLINE_CALL"

    fun showCallNotification(context: Context, id: Int, contactName: String, phoneNumber: String, notes: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Call Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders to call someone"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action: Normal Call
        val normalCallIntent = Intent(context, CallReminderReceiver::class.java).apply {
            action = NORMAL_CALL_ACTION
            putExtra("phone", phoneNumber)
        }
        val normalCallPending = PendingIntent.getBroadcast(context, id, normalCallIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Action: Online Call (WhatsApp)
        val onlineCallIntent = Intent(context, CallReminderReceiver::class.java).apply {
            action = ONLINE_CALL_ACTION
            putExtra("phone", phoneNumber)
        }
        val onlineCallPending = PendingIntent.getBroadcast(context, id + 1000, onlineCallIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Content Intent (Open App)
        val contentIntent = Intent(context, DashboardActivity::class.java).apply {
            putExtra("reminder_id", id)
            putExtra("show_call_dialog", true)
        }
        val contentPending = PendingIntent.getActivity(context, id + 2000, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle("Time to call $contactName")
            .setContentText(notes ?: "Don't forget to call!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPending)
            .addAction(R.drawable.ic_home, "📞 Normal Call", normalCallPending) // Using home icon for now, should use phone
            .addAction(R.drawable.ic_insights, "🌐 Online Call", onlineCallPending) // Using insights icon for now

        notificationManager.notify(id, builder.build())
    }
}