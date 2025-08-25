package com.example.fitapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores AI interaction logs with success/error tracking.
 */
@Entity(tableName = "ai_log")
data class AiLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val provider: String,
    val requestType: String, // "text", "vision", "plan", "recipe"
    val prompt: String,
    val response: String,
    val success: Boolean,
    val errorMessage: String? = null,
    val confidenceScore: Float? = null, // For vision API results
    val timestamp: Long = System.currentTimeMillis()
)