package com.example.fitapp.data.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper for storing and retrieving the OpenAI API key from SharedPreferences.
 * Simplified to focus exclusively on OpenAI as the primary provider.
 */
object ApiKeys {
    private const val PREFS_NAME = "api_keys"
    private const val KEY_OPENAI = "openai_api_key"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveOpenAiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_OPENAI, key).apply()
    }

    fun getOpenAiKey(context: Context): String {
        return getPrefs(context).getString(KEY_OPENAI, "") ?: ""
    }

    fun getStoredOpenAiKey(context: Context): String {
        return getPrefs(context).getString(KEY_OPENAI, "") ?: ""
    }

    /**
     * Check if OpenAI provider is available
     */
    fun isPrimaryProviderAvailable(context: Context): Boolean {
        return getOpenAiKey(context).isNotBlank()
    }

    /**
     * Get status message focused on OpenAI configuration
     */
    fun getConfigurationStatus(context: Context): String {
        val openAiKey = getOpenAiKey(context)
        return if (openAiKey.isNotBlank()) {
            "✓ OpenAI konfiguriert"
        } else {
            "✗ OpenAI API-Schlüssel erforderlich. Bitte unter Einstellungen → API-Schlüssel eingeben."
        }
    }
}