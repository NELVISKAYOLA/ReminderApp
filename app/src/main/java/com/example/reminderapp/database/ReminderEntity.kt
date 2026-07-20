package com.example.reminderapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity @JvmOverloads constructor(
    var userId: Int,
    var title: String,
    var notes: String? = null,
    var date: String,
    var time: String,
    var duration: String = "",
    var repeat: String = "None",
    var priority: String = "Personal",
    var isPrivate: Boolean = false,
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var type: String = "Normal",
    var contactName: String? = null,
    var phoneNumber: String? = null,
    var dateTime: Long = 0,
    var isCompleted: Boolean = false
) {
    val isOverdue: Boolean
        get() = !isCompleted && dateTime > 0 && dateTime < System.currentTimeMillis()
}

typealias Reminder = ReminderEntity