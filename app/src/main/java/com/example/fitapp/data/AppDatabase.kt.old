package com.example.fitapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChatLog::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatLogDao(): ChatLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "logs.db"
                ).build().also { INSTANCE = it }
            }
    }
}
