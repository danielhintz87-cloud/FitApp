package com.example.fitapp.ai

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.roundToInt

/**
 * Tracks API usage and estimated costs for budget monitoring
 */
object UsageTracker {
    private const val PREFS_NAME = "ai_usage_tracker"
    private const val KEY_GEMINI_TOKENS = "gemini_tokens_used"
    private const val KEY_PERPLEXITY_TOKENS = "perplexity_tokens_used"
    private const val KEY_GEMINI_REQUESTS = "gemini_requests_count"
    private const val KEY_PERPLEXITY_REQUESTS = "perplexity_requests_count"
    private const val KEY_LAST_RESET = "last_reset_timestamp"
    
    // Estimated costs per 1K tokens (approximate)
    private const val GEMINI_COST_PER_1K_TOKENS = 0.075 // USD for Gemini 1.5 Flash
    private const val PERPLEXITY_COST_PER_1K_TOKENS = 0.20 // USD for Perplexity API
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Record API usage for tracking
     */
    fun recordUsage(context: Context, provider: AiProvider, estimatedTokens: Int) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        
        when (provider) {
            AiProvider.Gemini -> {
                val currentTokens = prefs.getInt(KEY_GEMINI_TOKENS, 0)
                val currentRequests = prefs.getInt(KEY_GEMINI_REQUESTS, 0)
                editor.putInt(KEY_GEMINI_TOKENS, currentTokens + estimatedTokens)
                editor.putInt(KEY_GEMINI_REQUESTS, currentRequests + 1)
            }
            AiProvider.Perplexity -> {
                val currentTokens = prefs.getInt(KEY_PERPLEXITY_TOKENS, 0)
                val currentRequests = prefs.getInt(KEY_PERPLEXITY_REQUESTS, 0)
                editor.putInt(KEY_PERPLEXITY_TOKENS, currentTokens + estimatedTokens)
                editor.putInt(KEY_PERPLEXITY_REQUESTS, currentRequests + 1)
            }
        }
        
        editor.apply()
    }
    
    /**
     * Get usage statistics for a provider
     */
    fun getUsageStats(context: Context, provider: AiProvider): UsageStats {
        val prefs = getPrefs(context)
        
        return when (provider) {
            AiProvider.Gemini -> {
                val tokens = prefs.getInt(KEY_GEMINI_TOKENS, 0)
                val requests = prefs.getInt(KEY_GEMINI_REQUESTS, 0)
                val estimatedCost = (tokens / 1000.0) * GEMINI_COST_PER_1K_TOKENS
                UsageStats(provider, tokens, requests, estimatedCost)
            }
            AiProvider.Perplexity -> {
                val tokens = prefs.getInt(KEY_PERPLEXITY_TOKENS, 0)
                val requests = prefs.getInt(KEY_PERPLEXITY_REQUESTS, 0)
                val estimatedCost = (tokens / 1000.0) * PERPLEXITY_COST_PER_1K_TOKENS
                UsageStats(provider, tokens, requests, estimatedCost)
            }
        }
    }
    
    /**
     * Get total usage across all providers
     */
    fun getTotalUsageStats(context: Context): TotalUsageStats {
        val geminiStats = getUsageStats(context, AiProvider.Gemini)
        val perplexityStats = getUsageStats(context, AiProvider.Perplexity)
        
        return TotalUsageStats(
            totalTokens = geminiStats.tokens + perplexityStats.tokens,
            totalRequests = geminiStats.requests + perplexityStats.requests,
            totalEstimatedCost = geminiStats.estimatedCost + perplexityStats.estimatedCost,
            geminiStats = geminiStats,
            perplexityStats = perplexityStats
        )
    }
    
    /**
     * Reset usage statistics (e.g., monthly reset)
     */
    fun resetUsageStats(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().clear()
            .putLong(KEY_LAST_RESET, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Estimate tokens for a given text (rough approximation)
     */
    fun estimateTokens(text: String): Int {
        // Rough estimation: ~4 characters per token for English text
        return (text.length / 4.0).roundToInt().coerceAtLeast(1)
    }
    
    /**
     * Estimate tokens for vision requests (includes image processing overhead)
     */
    fun estimateVisionTokens(text: String): Int {
        // Vision requests have higher token usage due to image processing
        val textTokens = estimateTokens(text)
        val imageProcessingTokens = 258 // Typical image processing overhead for Gemini
        return textTokens + imageProcessingTokens
    }
}

data class UsageStats(
    val provider: AiProvider,
    val tokens: Int,
    val requests: Int,
    val estimatedCost: Double
)

data class TotalUsageStats(
    val totalTokens: Int,
    val totalRequests: Int,
    val totalEstimatedCost: Double,
    val geminiStats: UsageStats,
    val perplexityStats: UsageStats
)