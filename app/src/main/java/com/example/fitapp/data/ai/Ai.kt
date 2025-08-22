package com.example.fitapp.data.ai

import com.example.fitapp.BuildConfig

object Ai {
    val coach: AiCoach by lazy {
        val key = BuildConfig.OPENAI_API_KEY ?: ""
        if (key.isBlank()) MockAiRepository() else OpenAIRepository()
    }
}
