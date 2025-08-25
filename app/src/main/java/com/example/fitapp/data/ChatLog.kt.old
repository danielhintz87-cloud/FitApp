package com.example.fitapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores a single AI interaction.
 */
@Entity(tableName = "chat_log")
data class ChatLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val provider: String,
    val prompt: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)
