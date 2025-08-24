package com.example.fitapp.ai

import com.example.fitapp.BuildConfig

/**
 * Central configuration for AI providers and API keys.
 */

enum class AiProvider { OpenAI, Gemini, DeepSeek }

object AiConfig {
    /** Returns API key for the given provider. */
    fun apiKey(provider: AiProvider): String = when (provider) {
        AiProvider.OpenAI -> BuildConfig.OPENAI_API_KEY
        AiProvider.Gemini -> BuildConfig.GEMINI_API_KEY
        AiProvider.DeepSeek -> BuildConfig.DEEPSEEK_API_KEY
    }

    /** Returns base URL for the given provider. */
    fun baseUrl(provider: AiProvider): String = when (provider) {
        AiProvider.OpenAI -> BuildConfig.OPENAI_BASE_URL
        AiProvider.Gemini -> "https://generativelanguage.googleapis.com"
        AiProvider.DeepSeek -> BuildConfig.DEEPSEEK_BASE_URL
    }
}

