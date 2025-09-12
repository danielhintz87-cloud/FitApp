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

    fun saveGeminiKey(
        context: Context,
        key: String,
    ) {
        getPrefs(context).edit().putString(KEY_GEMINI, key).apply()
    }

    fun getGeminiKey(context: Context): String {
        // Erst SharedPreferences prüfen (User-Eingabe hat Priorität)
        val userKey = getPrefs(context).getString(KEY_GEMINI, "") ?: ""
        if (userKey.isNotBlank()) {
            return userKey
        }

        // Fallback auf BuildConfig wenn nichts in SharedPreferences
        return try {
            com.example.fitapp.BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun savePerplexityKey(
        context: Context,
        key: String,
    ) {
        getPrefs(context).edit().putString(KEY_PERPLEXITY, key).apply()
    }

    fun getPerplexityKey(context: Context): String {
        // Erst SharedPreferences prüfen (User-Eingabe hat Priorität)
        val userKey = getPrefs(context).getString(KEY_PERPLEXITY, "") ?: ""
        if (userKey.isNotBlank()) {
            return userKey
        }

        // Fallback auf BuildConfig wenn nichts in SharedPreferences
        return try {
            com.example.fitapp.BuildConfig.PERPLEXITY_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    // Legacy methods for migration compatibility
    fun saveOpenAiKey(
        context: Context,
        key: String,
    ) {
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
     * Updated for cost optimization: Gemini is sufficient for most operations
     */
    fun isPrimaryProviderAvailable(context: Context): Boolean {
        val geminiKey = getGeminiKey(context)

        // Gemini allein ist ausreichend für die meisten App-Funktionen
        return geminiKey.isNotBlank() && !geminiKey.startsWith("demo_")
    }

    /**
     * Check if specific provider is available
     */
    fun isProviderAvailable(
        context: Context,
        provider: com.example.fitapp.ai.AiProvider,
    ): Boolean {
        return when (provider) {
            com.example.fitapp.ai.AiProvider.Gemini -> {
                val key = getGeminiKey(context)
                key.isNotBlank() && !key.startsWith("demo_")
            }
            com.example.fitapp.ai.AiProvider.Perplexity -> {
                val key = getPerplexityKey(context)
                key.isNotBlank() && !key.startsWith("demo_")
            }
        }
    }

    /**
     * Get status message for AI provider configuration
     */
    fun getConfigurationStatus(context: Context): String {
        val geminiKey = getGeminiKey(context)
        val perplexityKey = getPerplexityKey(context)

        return buildString {
            appendLine("AI Provider Status:")

            when {
                geminiKey.isBlank() -> append("- Gemini: ❌ API-Schlüssel fehlt")
                geminiKey.startsWith("demo_") -> append("- Gemini: ❌ Demo-Schlüssel (ungültig)")
                geminiKey.startsWith("AIza") -> append("- Gemini: ✅ Konfiguriert und funktional")
                else -> append("- Gemini: ⚠️ Konfiguriert (Format unbekannt)")
            }
            appendLine()

            when {
                perplexityKey.isBlank() -> append("- Perplexity: ⚪ Optional (nicht erforderlich)")
                perplexityKey.startsWith("demo_") -> append("- Perplexity: ❌ Demo-Schlüssel (ungültig)")
                perplexityKey.startsWith("pplx-") -> append("- Perplexity: ⚠️ Konfiguriert (API-Änderungen möglich)")
                else -> append("- Perplexity: ⚠️ Konfiguriert (Format unbekannt)")
            }

            // Status-Quelle anzeigen
            appendLine()
            appendLine()
            val userGemini = getPrefs(context).getString(KEY_GEMINI, "")?.isNotBlank() == true
            val userPerplexity = getPrefs(context).getString(KEY_PERPLEXITY, "")?.isNotBlank() == true

            append("Schlüssel-Quelle:")
            appendLine()
            append("- Gemini: ${if (userGemini) "App-Eingabe" else "Build-Konfiguration"}")
            appendLine()
            append("- Perplexity: ${if (userPerplexity) "App-Eingabe" else "Build-Konfiguration"}")

            appendLine()
            appendLine()
            append("💡 Kosten-Optimierung:")
            appendLine()
            append("- Gemini reicht für alle Hauptfunktionen aus")
            appendLine()
            append("- Perplexity ist optional für erweiterte Web-Suche")

            if (geminiKey.isBlank() || geminiKey.startsWith("demo_")) {
                appendLine()
                appendLine()
                append("⚠️ Gemini API-Schlüssel unter Einstellungen → API-Schlüssel eingeben.")
            }
        }
    }
}
