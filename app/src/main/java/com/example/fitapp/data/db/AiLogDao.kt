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

    @Query("SELECT * FROM ai_log WHERE success = 1 ORDER BY timestamp DESC LIMIT :limit")
    fun getSuccessful(limit: Int = 50): Flow<List<AiLog>>

    @Query("SELECT * FROM ai_log WHERE success = 0 ORDER BY timestamp DESC LIMIT :limit")
    fun getFailed(limit: Int = 50): Flow<List<AiLog>>

    @Query("SELECT * FROM ai_log WHERE provider = :provider ORDER BY timestamp DESC LIMIT :limit")
    fun getByProvider(provider: String, limit: Int = 50): Flow<List<AiLog>>

    @Query("DELETE FROM ai_log WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldLogs(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM ai_log")
    suspend fun getCount(): Int
}