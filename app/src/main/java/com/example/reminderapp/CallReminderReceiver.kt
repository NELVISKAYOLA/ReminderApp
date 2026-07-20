package com.example.reminderapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val phoneNumber = intent.getStringExtra("phone") ?: ""

        when (action) {
            NotificationHelper.NORMAL_CALL_ACTION -> {
                CallHelper.makeNormalCall(context, phoneNumber)
            }
            NotificationHelper.ONLINE_CALL_ACTION -> {
                CallHelper.makeWhatsAppCall(context, phoneNumber)
            }
            else -> {
                // This is the alarm trigger
                val id = intent.getIntExtra("id", 0)
                val name = intent.getStringExtra("name") ?: "Contact"
                val notes = intent.getStringExtra("notes")
                NotificationHelper.showCallNotification(context, id, name, phoneNumber, notes)
            }
        }
    }
}