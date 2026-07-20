package com.example.reminderapp.database

import androidx.room.*

@Dao
interface FeedbackDao {
    @Insert
    fun insert(feedback: FeedbackEntity): Long

    @Query("SELECT * FROM feedback WHERE userId = :userId ORDER BY timestamp ASC")
    fun getFeedbackForUser(userId: Int): List<FeedbackEntity>

    @Query("UPDATE feedback SET isRead = 1 WHERE userId = :userId AND senderType = 'SUPPLIER'")
    fun markSupplierRepliesAsRead(userId: Int)

    @Query("SELECT COUNT(*) FROM feedback WHERE userId = :userId AND senderType = 'SUPPLIER' AND isRead = 0")
    fun getUnreadReplyCount(userId: Int): Int

    @Query("SELECT * FROM feedback GROUP BY userId ORDER BY timestamp DESC")
    fun getAllFeedbackThreads(): List<FeedbackEntity>

    @Query("SELECT * FROM feedback WHERE userId = :userId ORDER BY timestamp ASC")
    fun getFeedbackHistory(userId: Int): List<FeedbackEntity>
}
