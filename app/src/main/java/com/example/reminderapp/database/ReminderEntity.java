package com.example.reminderapp.database;

import androidx.room.Entity;
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
    private boolean isCompleted;

    public ReminderEntity(int userId, String title, String notes, String date, String time, String duration, String repeat, String priority, boolean isPrivate) {
        this.userId = userId;
        this.title = title;
        this.notes = notes;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.repeat = repeat;
        this.priority = priority;
        this.isPrivate = isPrivate;
        this.isCompleted = false;
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
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
