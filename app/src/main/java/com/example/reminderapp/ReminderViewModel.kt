package com.example.reminderapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.reminderapp.database.Reminder
import com.example.reminderapp.database.ReminderDatabase

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val db = ReminderDatabase.getInstance(application)
    private val dao = db.reminderDao()

    private val _reminders = MutableLiveData<List<Reminder>>()
    val reminders: LiveData<List<Reminder>> get() = _reminders

    fun loadReminders(userId: Int) {
        _reminders.value = dao.getRemindersForUser(userId)
    }

    fun insert(reminder: Reminder, callback: (Long) -> Unit) {
        val id = dao.insert(reminder)
        callback(id)
    }

    fun delete(reminder: Reminder) {
        dao.delete(reminder)
    }
}