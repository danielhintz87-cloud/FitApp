package com.example.fitapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores AI interaction logs for monitoring and debugging
 */
@Entity(tableName = "ai_log")
data class AiLog(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val provider: String,           // OpenAI, Gemini, DeepSeek
    val requestType: String,        // text, vision
    val prompt: String,
    val response: String?,          // null if failed
    val success: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0,         // milliseconds
    val error: String? = null       // error message if failed
)