package com.example.reminderapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FeedbackDao {
    @Insert
    long insert(FeedbackEntity feedback);

    @Insert
    void insertFeedbackMessage(FeedbackMessage message);

    @Query("SELECT * FROM feedback_messages WHERE userId = :userId ORDER BY timestamp ASC")
    List<FeedbackMessage> getFeedbackMessages(int userId);

    @Query("SELECT * FROM feedback WHERE userId = :userId ORDER BY timestamp ASC")
    List<FeedbackEntity> getFeedbackForUser(int userId);

    @Query("UPDATE feedback SET isRead = 1 WHERE userId = :userId AND senderType = 'SUPPLIER'")
    void markSupplierRepliesAsRead(int userId);

    @Query("SELECT COUNT(*) FROM feedback WHERE userId = :userId AND senderType = 'SUPPLIER' AND isRead = 0")
    int getUnreadReplyCount(int userId);

    @Query("SELECT * FROM feedback GROUP BY userId ORDER BY timestamp DESC")
    List<FeedbackEntity> getAllFeedbackThreads();

    @Query("SELECT * FROM feedback WHERE userId = :userId ORDER BY timestamp ASC")
    List<FeedbackEntity> getFeedbackHistory(int userId);
}
