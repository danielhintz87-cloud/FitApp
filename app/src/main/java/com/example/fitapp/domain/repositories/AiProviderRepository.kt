package com.example.fitapp.domain.repositories

import android.graphics.Bitmap
import com.example.fitapp.domain.entities.*

/**
 * Domain repository interface for AI providers
 * Defines the contract for AI operations without implementation details
 */
interface AiProviderRepository {
    /**
     * Generate text content using AI
     */
    suspend fun generateText(request: AiRequest): Result<String>

    /**
     * Analyze image with AI vision capabilities
     */
    suspend fun analyzeImage(
        prompt: String,
        bitmap: Bitmap,
        provider: AiProvider = AiProvider.Gemini,
    ): Result<CaloriesEstimate>

    /**
     * Check if a specific AI provider is available/configured
     */
    suspend fun isProviderAvailable(provider: AiProvider): Boolean

    /**
     * Get the best provider for a specific task type
     */
    suspend fun selectOptimalProvider(
        taskType: TaskType,
        hasImage: Boolean = false,
    ): AiProvider

    /**
     * Get fallback provider if primary fails
     */
    suspend fun getFallbackProvider(primary: AiProvider): AiProvider?
}
