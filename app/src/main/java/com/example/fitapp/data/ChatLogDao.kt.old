package com.example.fitapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatLogDao {
    @Insert
    suspend fun insert(log: ChatLog)

    @Query("SELECT * FROM chat_log ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ChatLog>>
}
