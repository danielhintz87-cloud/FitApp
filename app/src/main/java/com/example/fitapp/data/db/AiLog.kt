package com.example.fitapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing AI interaction logs with success/error tracking.
 */
@Entity(tableName = "ai_log")
data class AiLog(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val provider: String,
    val requestType: String, // "text" or "vision"
    val prompt: String,
    val response: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val tokensUsed: Int? = null,
    val confidenceScore: Int? = null // for vision results
)