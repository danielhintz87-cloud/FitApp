package com.example.fitapp.data.ai

import com.example.fitapp.BuildConfig
import com.example.fitapp.data.*
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Ai {
    val repo: AiCoach by lazy {
        val key = BuildConfig.OPENAI_API_KEY ?: ""
        if (key.isNotBlank()) {
            // OpenAI erzwingen – Client mit API-Key initialisieren
            OpenAIRepository.fromApiKey(key)
        } else {
            MockAiRepository()
        }
    }
}

    val coach: AICoach get() = coachImpl
}

/* ---------- Bequeme Delegations (optional) ---------- */

suspend fun AiGenerateBasePlan(
    goal: Goal,
    devices: List<Device>,
    minutes: Int,
    sessions: Int,
    level: String? = null
): Plan = withContext(Dispatchers.IO) {
    Ai.coach.generateBasePlan(goal, devices, minutes, sessions, level)
}

suspend fun AiSuggestAlternative(
    goal: Goal,
    deviceHint: String,
    minutes: Int
): WorkoutDay = withContext(Dispatchers.IO) {
    Ai.coach.suggestAlternative(goal, deviceHint, minutes)
}

suspend fun AiSuggestRecipes(
    prefs: RecipePrefs,
    count: Int = 3
): List<Recipe> = withContext(Dispatchers.IO) {
    Ai.coach.suggestRecipes(prefs, count)
}
