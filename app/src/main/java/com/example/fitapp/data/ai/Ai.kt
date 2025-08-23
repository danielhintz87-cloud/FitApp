package com.example.fitapp.data.ai

import com.example.fitapp.BuildConfig

object Ai {
    val repo: AICoach by lazy {
        val key = BuildConfig.OPENAI_API_KEY
        if (key.isNullOrBlank()) MockAiRepository() else OpenAIRepository.fromApiKey(key)
    }
}
