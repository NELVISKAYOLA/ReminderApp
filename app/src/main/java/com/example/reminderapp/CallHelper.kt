package com.example.reminderapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object CallHelper {

    fun makeNormalCall(context: Context, phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            Toast.makeText(context, "Phone number is empty", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission denied for calling", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not start call", Toast.LENGTH_SHORT).show()
        }
    }

    fun makeWhatsAppCall(context: Context, phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            Toast.makeText(context, "Phone number is empty", Toast.LENGTH_SHORT).show()
            return
        }
        // Clean number for WhatsApp
        val cleanNumber = phoneNumber.replace("+", "").replace(" ", "").replace("-", "")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            `package` = "com.whatsapp"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not installed or error occurred", Toast.LENGTH_SHORT).show()
        }
    }
}