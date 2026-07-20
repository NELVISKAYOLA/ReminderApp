package com.example.reminderapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feedback")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val message: String,
    val senderType: String, // "USER" or "SUPPLIER"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
