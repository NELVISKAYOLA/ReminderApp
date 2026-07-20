package com.example.reminderapp.database

import androidx.room.*

@Dao
interface ReminderDao {
    @Insert
    fun insert(reminder: ReminderEntity): Long

    @Update
    fun update(reminder: ReminderEntity)

    @Delete
    fun delete(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY date ASC")
    fun getRemindersForUser(userId: Int): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isPrivate = 1 ORDER BY date ASC")
    fun getPrivateRemindersForUser(userId: Int): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isPrivate = 0 ORDER BY date ASC")
    fun getPublicRemindersForUser(userId: Int): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 1 ORDER BY date DESC")
    fun getCompletedRemindersForUser(userId: Int): List<ReminderEntity>
    
    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 1 ORDER BY date DESC, time DESC LIMIT 5")
    fun getRecentCompletedReminders(userId: Int): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND date = :date AND isPrivate = 0 ORDER BY time ASC")
    fun getPublicRemindersForDate(userId: Int, date: String): List<ReminderEntity>
}