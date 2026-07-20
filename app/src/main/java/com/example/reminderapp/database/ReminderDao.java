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
    void insert(ReminderEntity reminder);

    @Update
    void update(ReminderEntity reminder);

    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY date ASC")
    List<ReminderEntity> getRemindersForUser(int userId);

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isPrivate = 1 ORDER BY date ASC")
    List<ReminderEntity> getPrivateRemindersForUser(int userId);

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isPrivate = 0 ORDER BY date ASC")
    List<ReminderEntity> getPublicRemindersForUser(int userId);

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 1 ORDER BY date DESC")
    List<ReminderEntity> getCompletedRemindersForUser(int userId);

    @Delete
    void delete(ReminderEntity reminder);
}
