package com.example.fitapp.data.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper for storing and retrieving AI provider API keys from SharedPreferences.
 * Supports Gemini and Perplexity as the primary providers.
 */
object ApiKeys {
    private const val PREFS_NAME = "api_keys"
    private const val KEY_GEMINI = "gemini_api_key"
    private const val KEY_PERPLEXITY = "perplexity_api_key"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveGeminiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_GEMINI, key).apply()
    }

    fun getGeminiKey(context: Context): String {
        return getPrefs(context).getString(KEY_GEMINI, "") ?: ""
    }

    fun savePerplexityKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_PERPLEXITY, key).apply()
    }

    fun getPerplexityKey(context: Context): String {
        return getPrefs(context).getString(KEY_PERPLEXITY, "") ?: ""
    }

    // Legacy methods for migration compatibility
    fun saveOpenAiKey(context: Context, key: String) {
        // For migration purposes, if someone sets an OpenAI key, we'll use it as Gemini key
        saveGeminiKey(context, key)
    }

    fun getOpenAiKey(context: Context): String {
        // For migration compatibility, return Gemini key
        return getGeminiKey(context)
    }

    fun getStoredOpenAiKey(context: Context): String {
        return getGeminiKey(context)
    }

    /**
     * Check if both primary providers are available
     */
    fun isPrimaryProviderAvailable(context: Context): Boolean {
        return getGeminiKey(context).isNotBlank() && getPerplexityKey(context).isNotBlank()
    }

    /**
     * Check if specific provider is available
     */
    fun isProviderAvailable(context: Context, provider: com.example.fitapp.ai.AiProvider): Boolean {
        return when (provider) {
            com.example.fitapp.ai.AiProvider.Gemini -> getGeminiKey(context).isNotBlank()
            com.example.fitapp.ai.AiProvider.Perplexity -> {
                // Perplexity is temporarily disabled by default
                // To re-enable, remove this comment and return: getPerplexityKey(context).isNotBlank()
                false
            }
        }
    }

    /**
     * Get status message for AI provider configuration
     */
    fun getConfigurationStatus(context: Context): String {
        val geminiKey = getGeminiKey(context)
        
        return buildString {
            appendLine("AI Provider Status:")
            append("- Gemini: ${if (geminiKey.isNotBlank()) "✓ Konfiguriert" else "✗ API-Schlüssel erforderlich"}")
            appendLine()
            append("- Perplexity: ⏸ Temporär deaktiviert")
            
            if (geminiKey.isBlank()) {
                appendLine()
                appendLine()
                append("Bitte Gemini API-Schlüssel unter Einstellungen → API-Schlüssel eingeben.")
            }
        }
    }
}