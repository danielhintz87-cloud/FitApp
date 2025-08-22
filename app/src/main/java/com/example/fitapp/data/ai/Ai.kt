package com.example.fitapp.data.ai

import com.example.fitapp.BuildConfig
import com.example.fitapp.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Zentraler Zugriffspunkt für AI-Funktionalität. Wählt je nach vorhandener
 * API-Schlüssel-Konfiguration entweder das OpenAI-Backend oder die Mock-Implementierung.
 */
object Ai {
    /** Fauler Initialisierer: Verwendet echten OpenAI-Client, falls API-Key vorhanden, sonst Mock. */
    val coach: AICoach by lazy {
        val key = BuildConfig.OPENAI_API_KEY ?: ""
        if (key.isNotBlank()) {
            OpenAIRepository.fromApiKey(key)
        } else {
            MockAiRepository()
        }
    }
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
