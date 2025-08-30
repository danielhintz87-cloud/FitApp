package com.example.fitapp.infrastructure.repositories

import android.graphics.Bitmap
import com.example.fitapp.ai.UsageTracker
import com.example.fitapp.domain.entities.*
import com.example.fitapp.domain.repositories.AiProviderRepository
import com.example.fitapp.infrastructure.logging.AiLogger
import com.example.fitapp.infrastructure.providers.AiProvider
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Implementation of AiProviderRepository that coordinates multiple AI providers
 */
class AiProviderRepositoryImpl(
    private val providers: Map<com.example.fitapp.domain.entities.AiProvider, AiProvider>,
    private val logger: AiLogger
) : AiProviderRepository {
    
    override suspend fun generateText(request: AiRequest): Result<String> {
        val provider = getProvider(request.provider)
            ?: return Result.failure(IllegalStateException("Provider ${request.provider} not available"))
        
        val startTime = System.currentTimeMillis()
        
        return withRetry {
            provider.generateText(request.prompt)
        }.onSuccess { response ->
            val duration = System.currentTimeMillis() - startTime
            val estimatedTokens = UsageTracker.estimateTokens(request.prompt + response)
            logger.logSuccess(request.taskType, request.provider, request.prompt, response, duration, estimatedTokens)
        }.onFailure { error ->
            val duration = System.currentTimeMillis() - startTime
            logger.logError(request.taskType, request.provider, request.prompt, error.message ?: "Unknown error", duration)
        }
    }
    
    override suspend fun analyzeImage(
        prompt: String,
        bitmap: Bitmap,
        provider: com.example.fitapp.domain.entities.AiProvider
    ): Result<CaloriesEstimate> {
        val aiProvider = getProvider(provider)
            ?: return Result.failure(IllegalStateException("Provider $provider not available"))
        
        if (!aiProvider.supportsVision()) {
            return Result.failure(IllegalStateException("Provider $provider does not support image analysis"))
        }
        
        val startTime = System.currentTimeMillis()
        
        return withRetry {
            aiProvider.analyzeImage(prompt, bitmap)
        }.onSuccess { estimate ->
            val duration = System.currentTimeMillis() - startTime
            val estimatedTokens = UsageTracker.estimateVisionTokens(prompt + estimate.text)
            logger.logSuccess(TaskType.CALORIE_ESTIMATION, provider, prompt, estimate.toString(), duration, estimatedTokens)
        }.onFailure { error ->
            val duration = System.currentTimeMillis() - startTime
            logger.logError(TaskType.CALORIE_ESTIMATION, provider, prompt, error.message ?: "Unknown error", duration)
        }
    }
    
    override suspend fun isProviderAvailable(provider: com.example.fitapp.domain.entities.AiProvider): Boolean {
        return getProvider(provider)?.isAvailable() ?: false
    }
    
    override suspend fun selectOptimalProvider(
        taskType: TaskType,
        hasImage: Boolean
    ): com.example.fitapp.domain.entities.AiProvider {
        val perplexityAvailable = isProviderAvailable(
            com.example.fitapp.domain.entities.AiProvider.Perplexity
        )

        return when {
            // Multimodal tasks always require Gemini's vision support
            hasImage -> com.example.fitapp.domain.entities.AiProvider.Gemini

            // Structured fitness plans favour Gemini for longer responses
            taskType == TaskType.TRAINING_PLAN -> com.example.fitapp.domain.entities.AiProvider.Gemini

            // Route lightweight tasks to Perplexity when available
            taskType == TaskType.SHOPPING_LIST_PARSING && perplexityAvailable ->
                com.example.fitapp.domain.entities.AiProvider.Perplexity
            taskType == TaskType.RECIPE_GENERATION && perplexityAvailable ->
                com.example.fitapp.domain.entities.AiProvider.Perplexity

            // Fallback to Gemini in all other cases or when Perplexity is unavailable
            else -> com.example.fitapp.domain.entities.AiProvider.Gemini
        }
    }
    
    override suspend fun getFallbackProvider(primary: com.example.fitapp.domain.entities.AiProvider): com.example.fitapp.domain.entities.AiProvider? {
        return when (primary) {
            com.example.fitapp.domain.entities.AiProvider.Gemini -> com.example.fitapp.domain.entities.AiProvider.Perplexity
            com.example.fitapp.domain.entities.AiProvider.Perplexity -> com.example.fitapp.domain.entities.AiProvider.Gemini
        }
    }
    
    private fun getProvider(providerType: com.example.fitapp.domain.entities.AiProvider): AiProvider? {
        return providers[providerType]
    }
    
    /**
     * Retry logic with exponential backoff for failed requests
     */
    private suspend fun <T> withRetry(request: suspend () -> Result<T>): Result<T> {
        var lastException: Exception? = null
        
        for (attempt in 0..3) {
            try {
                val result = request()
                if (result.isSuccess) {
                    return result
                }
                // If it's a controlled failure (Result.failure), don't retry
                return result
            } catch (e: Exception) {
                lastException = e

                // Extract status code if available for retry logic
                val lastStatusCode = when {
                    e.message?.contains(" 429") == true || e.message?.contains("429") == true -> 429
                    e.message?.contains(" 500") == true || e.message?.contains("500") == true -> 500
                    e.message?.contains(" 502") == true || e.message?.contains("502") == true -> 502
                    e.message?.contains(" 503") == true || e.message?.contains("503") == true -> 503
                    e.message?.contains(" 504") == true || e.message?.contains("504") == true -> 504
                    else -> null
                }

                if (attempt < 3 && isRetriableError(lastStatusCode, e)) {
                    val delayMs = calculateBackoffDelay(attempt)
                    delay(delayMs)
                } else {
                    return Result.failure(e)
                }
            }
        }
        return Result.failure(lastException ?: IllegalStateException("Max retries exceeded"))
    }
    
    /**
     * Calculate exponential backoff delay with jitter for retry attempts
     */
    private fun calculateBackoffDelay(attempt: Int): Long {
        val baseDelays = listOf(800L, 1500L, 2500L, 4000L, 6000L)
        val baseDelay = baseDelays.getOrElse(attempt) { 8000L }
        val jitter = Random.nextLong(0, 400)
        return baseDelay + jitter
    }

    /**
     * Check if an exception or HTTP status code indicates a retriable error
     */
    private fun isRetriableError(statusCode: Int?, exception: Exception?): Boolean {
        return when {
            statusCode == 429 -> true  // Rate limit
            statusCode in 500..599 -> true  // Server errors
            exception?.message?.contains("timeout", ignoreCase = true) == true -> true
            exception?.message?.contains("connection", ignoreCase = true) == true -> true
            else -> false
        }
    }
}