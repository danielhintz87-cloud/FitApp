package com.example.fitapp.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.fitapp.BuildConfig

/**
 * Helper for storing and retrieving API keys from SharedPreferences
 * with fallback to BuildConfig values
 */
object ApiKeys {
    private const val PREFS_NAME = "api_keys"
    private const val KEY_OPENAI = "openai_api_key"
    private const val KEY_GEMINI = "gemini_api_key"
    private const val KEY_DEEPSEEK = "deepseek_api_key"
    private const val KEY_CLAUDE = "claude_api_key"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveOpenAiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_OPENAI, key).apply()
    }

    fun saveGeminiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_GEMINI, key).apply()
    }

    fun saveDeepSeekKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_DEEPSEEK, key).apply()
    }

    fun saveClaudeKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_CLAUDE, key).apply()
    }

    fun getOpenAiKey(context: Context): String {
        val stored = getPrefs(context).getString(KEY_OPENAI, "")
        return if (stored.isNullOrBlank()) BuildConfig.OPENAI_API_KEY else stored
    }

    fun getGeminiKey(context: Context): String {
        val stored = getPrefs(context).getString(KEY_GEMINI, "")
        return if (stored.isNullOrBlank()) BuildConfig.GEMINI_API_KEY else stored
    }

    fun getDeepSeekKey(context: Context): String {
        val stored = getPrefs(context).getString(KEY_DEEPSEEK, "")
        return if (stored.isNullOrBlank()) BuildConfig.DEEPSEEK_API_KEY else stored
    }

    fun getClaudeKey(context: Context): String {
        val stored = getPrefs(context).getString(KEY_CLAUDE, "")
        return if (stored.isNullOrBlank()) BuildConfig.CLAUDE_API_KEY else stored
    }

    fun getStoredOpenAiKey(context: Context): String {
        return getPrefs(context).getString(KEY_OPENAI, "") ?: ""
    }

    fun getStoredGeminiKey(context: Context): String {
        return getPrefs(context).getString(KEY_GEMINI, "") ?: ""
    }

    fun getStoredDeepSeekKey(context: Context): String {
        return getPrefs(context).getString(KEY_DEEPSEEK, "") ?: ""
    }

    fun getStoredClaudeKey(context: Context): String {
        return getPrefs(context).getString(KEY_CLAUDE, "") ?: ""
    }
}