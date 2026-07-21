package com.example.reminderapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class CallHelper {

    public static void makeNormalCall(Context context, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(context, "Phone number is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (SecurityException e) {
            Toast.makeText(context, "Permission denied for calling", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Could not start call", Toast.LENGTH_SHORT).show();
        }
    }

    public static void makeWhatsAppCall(Context context, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(context, "Phone number is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // Clean number for WhatsApp
        String cleanNumber = phoneNumber.replace("+", "").replace(" ", "").replace("-", "");
        Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + cleanNumber);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.whatsapp");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp not installed or error occurred", Toast.LENGTH_SHORT).show();
        }
    }
}
