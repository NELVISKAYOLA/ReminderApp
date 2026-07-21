package com.example.reminderapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReminderDao {
    @Insert
    long insert(ReminderEntity reminder);

    @Update
    void update(ReminderEntity reminder);

    @Delete
    void delete(ReminderEntity reminder);

    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY date ASC")
    List<ReminderEntity> getRemindersForUser(int userId);

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isPrivate = 1 ORDER BY date ASC")
    List<ReminderEntity> getPrivateRemindersForUser(int userId);

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isPrivate = 0 ORDER BY date ASC")
    List<ReminderEntity> getPublicRemindersForUser(int userId);

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 1 ORDER BY date DESC")
    List<ReminderEntity> getCompletedRemindersForUser(int userId);
    
    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 1 ORDER BY date DESC, time DESC LIMIT 5")
    List<ReminderEntity> getRecentCompletedReminders(int userId);

    @Query("SELECT * FROM reminders WHERE userId = :userId AND date = :date AND isPrivate = 0 ORDER BY time ASC")
    List<ReminderEntity> getPublicRemindersForDate(int userId, String date);

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    ReminderEntity getReminderById(int id);

    @Query("SELECT * FROM reminders WHERE isCompleted = 0")
    List<ReminderEntity> getAllUncompletedReminders();
}
