package com.example.fitapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiLogDao {
    @Insert
    suspend fun insert(log: AiLog)

    @Query("SELECT * FROM ai_log ORDER BY timestamp DESC")
    fun getAll(): Flow<List<AiLog>>

    @Query("SELECT * FROM ai_log WHERE requestType = :type ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<AiLog>>

    @Query("SELECT * FROM ai_log WHERE success = 0 ORDER BY timestamp DESC")
    fun getErrors(): Flow<List<AiLog>>

    @Query("DELETE FROM ai_log WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)
}