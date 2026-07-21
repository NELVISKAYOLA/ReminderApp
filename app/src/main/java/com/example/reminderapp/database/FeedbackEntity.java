package com.example.reminderapp.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "feedback")
public class FeedbackEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private String message;
    private String senderType; // "USER" or "SUPPLIER"
    private long timestamp;
    private boolean isRead;

    public FeedbackEntity(int userId, String message, String senderType, long timestamp, boolean isRead) {
        this.userId = userId;
        this.message = message;
        this.senderType = senderType;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    @androidx.room.Ignore
    public FeedbackEntity(int id, int userId, String message, String senderType, long timestamp, boolean isRead) {
        this(userId, message, senderType, timestamp, isRead);
        this.id = id;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
