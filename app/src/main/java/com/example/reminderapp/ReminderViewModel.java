package com.example.reminderapp;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.reminderapp.database.AppDatabase;
import com.example.reminderapp.database.ReminderDao;
import com.example.reminderapp.database.ReminderEntity;

import java.util.List;

public class ReminderViewModel extends AndroidViewModel {
    private final ReminderDao dao;
    private final MutableLiveData<List<ReminderEntity>> _reminders = new MutableLiveData<>();

    public ReminderViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        dao = db.reminderDao();
    }

    public LiveData<List<ReminderEntity>> getReminders() {
        return _reminders;
    }

    public void loadReminders(int userId) {
        _reminders.setValue(dao.getRemindersForUser(userId));
    }

    public void insert(ReminderEntity reminder, InsertionCallback callback) {
        long id = dao.insert(reminder);
        if (callback != null) {
            callback.onInserted(id);
        }
    }

    public void delete(ReminderEntity reminder) {
        dao.delete(reminder);
    }

    public interface InsertionCallback {
        void onInserted(long id);
    }
}
