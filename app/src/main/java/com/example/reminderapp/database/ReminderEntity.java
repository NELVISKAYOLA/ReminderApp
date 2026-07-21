package com.example.reminderapp.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class ReminderEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private String title;
    private String notes;
    private String date;
    private String time;
    private String duration;
    private String repeat;
    private String priority;
    private boolean isPrivate;
    private String type;
    private String contactName;
    private String phoneNumber;
    private long dateTime;
    private boolean isCompleted;

    public ReminderEntity(int userId, String title, String notes, String date, String time, 
                          String duration, String repeat, String priority, boolean isPrivate, 
                          String type, String contactName, String phoneNumber, long dateTime, 
                          boolean isCompleted) {
        this.userId = userId;
        this.title = title;
        this.notes = notes;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.repeat = repeat;
        this.priority = priority;
        this.isPrivate = isPrivate;
        this.type = type;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.dateTime = dateTime;
        this.isCompleted = isCompleted;
    }

    @Ignore
    public ReminderEntity(int userId, String title, String notes, String date, String time, 
                          String duration, String repeat, String priority, boolean isPrivate) {
        this(userId, title, notes, date, time, duration, repeat, priority, isPrivate, 
             "Normal", null, null, 0, false);
    }

    @Ignore
    public boolean isOverdue() {
        return !isCompleted && dateTime > 0 && dateTime < System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getRepeat() { return repeat; }
    public void setRepeat(String repeat) { this.repeat = repeat; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public long getDateTime() { return dateTime; }
    public void setDateTime(long dateTime) { this.dateTime = dateTime; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
