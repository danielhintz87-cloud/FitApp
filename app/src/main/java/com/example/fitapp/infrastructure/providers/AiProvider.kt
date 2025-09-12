package com.example.fitapp.infrastructure.providers

import android.graphics.Bitmap
import com.example.fitapp.domain.entities.*

/**
 * Interface for AI provider implementations
 */
interface AiProvider {
    /**
     * Provider identification
     */
    val providerType: com.example.fitapp.domain.entities.AiProvider

    /**
     * Check if this provider is available/configured
     */
    suspend fun isAvailable(): Boolean

    /**
     * Generate text content
     */
    suspend fun generateText(prompt: String): Result<String>

    /**
     * Analyze image (only supported by certain providers)
     */
    suspend fun analyzeImage(
        prompt: String,
        bitmap: Bitmap,
    ): Result<CaloriesEstimate>

    /**
     * Check if provider supports image analysis
     */
    fun supportsVision(): Boolean
}
