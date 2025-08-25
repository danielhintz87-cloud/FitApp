package com.example.fitapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiLogDao {
    @Insert suspend fun insert(log: AiLog)
    @Query("SELECT * FROM ai_logs ORDER BY ts DESC LIMIT :limit")
    fun latest(limit: Int = 200): Flow<List<AiLog>>
}