package com.example.fitapp.infrastructure.logging

import android.content.Context
import com.example.fitapp.ai.UsageTracker
import com.example.fitapp.data.db.AiLog
import com.example.fitapp.data.db.AiLogDao
import com.example.fitapp.domain.entities.AiProvider as DomainAiProvider
import com.example.fitapp.domain.entities.TaskType

/**
 * Handles logging and usage tracking for AI operations
 */
class AiLogger(
    private val context: Context,
    private val aiLogDao: AiLogDao
) {
    
    suspend fun logSuccess(
        taskType: TaskType,
        provider: DomainAiProvider,
        prompt: String,
        response: String,
        duration: Long,
        estimatedTokens: Int
    ) {
        // Track usage - convert domain provider to original enum
        val originalProvider = when (provider) {
            DomainAiProvider.Gemini -> com.example.fitapp.ai.AiProvider.Gemini
            DomainAiProvider.Perplexity -> com.example.fitapp.ai.AiProvider.Perplexity
        }
        UsageTracker.recordUsage(context, originalProvider, estimatedTokens)
        
        // Log to database
        aiLogDao.insert(
            AiLog.success(
                taskType.name.lowercase(),
                provider.name,
                prompt,
                response,
                duration
            )
        )
    }
    
    suspend fun logError(
        taskType: TaskType,
        provider: DomainAiProvider,
        prompt: String,
        error: String,
        duration: Long
    ) {
        // Log to database
        aiLogDao.insert(
            AiLog.error(
                taskType.name.lowercase(),
                provider.name,
                prompt,
                error,
                duration
            )
        )
    }
}