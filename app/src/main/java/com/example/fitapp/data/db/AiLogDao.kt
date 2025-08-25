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
    fun getAllLogs(): Flow<List<AiLog>>

    @Query("SELECT * FROM ai_log WHERE isSuccess = 1 ORDER BY timestamp DESC")
    fun getSuccessfulLogs(): Flow<List<AiLog>>

    @Query("SELECT * FROM ai_log WHERE isSuccess = 0 ORDER BY timestamp DESC")
    fun getFailedLogs(): Flow<List<AiLog>>

    @Query("SELECT * FROM ai_log WHERE provider = :provider ORDER BY timestamp DESC")
    fun getLogsByProvider(provider: String): Flow<List<AiLog>>

    @Query("SELECT * FROM ai_log WHERE requestType = :type ORDER BY timestamp DESC")
    fun getLogsByType(type: String): Flow<List<AiLog>>

    @Query("DELETE FROM ai_log WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)

    @Query("SELECT COUNT(*) FROM ai_log WHERE isSuccess = 1")
    suspend fun getSuccessCount(): Int

    @Query("SELECT COUNT(*) FROM ai_log WHERE isSuccess = 0")
    suspend fun getFailureCount(): Int
}