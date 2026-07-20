package com.example.reminderapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, ReminderEntity::class, FeedbackEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun reminderDao(): ReminderDao
    abstract fun feedbackDao(): FeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reminder_db"
                )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

typealias ReminderDatabase = AppDatabase
