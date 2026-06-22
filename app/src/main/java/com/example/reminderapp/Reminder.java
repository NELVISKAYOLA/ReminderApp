package com.example.reminderapp;

import java.util.UUID;

public class Reminder {
    private String id;
    private String title;
    private String notes;
    private String date;
    private String duration;
    private String repeat;
    private String priority;

    public Reminder(String title, String notes, String date, String duration, String repeat, String priority) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.notes = notes;
        this.date = date;
        this.duration = duration;
        this.repeat = repeat;
        this.priority = priority;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getRepeat() { return repeat; }
    public void setRepeat(String repeat) { this.repeat = repeat; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    @Override
    public String toString() {
        return title + " (" + priority + ")";
    }
}
