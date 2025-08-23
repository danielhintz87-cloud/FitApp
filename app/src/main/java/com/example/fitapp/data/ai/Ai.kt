package com.example.fitapp.data.ai

import com.example.fitapp.BuildConfig
import com.example.fitapp.data.*

/**
 * Singleton, das automatisch das passende AI-Repository wählt.
 * Bei vorhandenem OPENAI_API_KEY → OpenAI; sonst → Mock.
 */
object Ai {
    val repo: AICoach by lazy {
        val key = BuildConfig.OPENAI_API_KEY
        if (key.isNullOrBlank()) {
            MockAiRepository()
        } else {
            OpenAIRepository.fromApiKey(key)
        }
    }
}
