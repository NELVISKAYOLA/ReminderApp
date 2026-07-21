package com.example.reminderapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CallReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String phoneNumber = intent.getStringExtra("phone");
        if (phoneNumber == null) phoneNumber = "";

        if (NotificationHelper.NORMAL_CALL_ACTION.equals(action)) {
            CallHelper.makeNormalCall(context, phoneNumber);
        } else if (NotificationHelper.ONLINE_CALL_ACTION.equals(action)) {
            CallHelper.makeWhatsAppCall(context, phoneNumber);
        } else {
            // This is the alarm trigger
            int id = intent.getIntExtra("id", 0);
            String name = intent.getStringExtra("name");
            if (name == null) name = "Contact";
            String notes = intent.getStringExtra("notes");
            NotificationHelper.showCallNotification(context, id, name, phoneNumber, notes);
        }
    }
}
