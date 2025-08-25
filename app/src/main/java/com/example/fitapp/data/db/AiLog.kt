package com.example.fitapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_logs")
data class AiLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long,
    val type: String,
    val provider: String,
    val prompt: String,
    val result: String,
    val success: Boolean,
    val tookMs: Long
) {
    companion object {
        fun success(type: String, provider: String, prompt: String, result: String, took: Long) =
            AiLog(ts = System.currentTimeMillis(), type = type, provider = provider, prompt = prompt, result = result, success = true, tookMs = took)

        fun error(type: String, provider: String, prompt: String, error: String, took: Long) =
            AiLog(ts = System.currentTimeMillis(), type = type, provider = provider, prompt = prompt, result = "ERROR: $error", success = false, tookMs = took)
    }
}