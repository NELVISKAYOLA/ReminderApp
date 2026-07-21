package com.example.reminderapp.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "feedback_messages")
public class FeedbackMessage {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private String senderName;
    private String message;
    private long timestamp;
    private boolean isAdmin; // true if sent by admin, false if sent by user

    public FeedbackMessage(int userId, String senderName, String message, long timestamp, boolean isAdmin) {
        this.userId = userId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
        this.isAdmin = isAdmin;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}
