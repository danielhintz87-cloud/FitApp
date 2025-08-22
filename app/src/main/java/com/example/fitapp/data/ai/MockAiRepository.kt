package com.example.fitapp.data.ai

import com.example.fitapp.data.*
import com.example.fitapp.logic.PlanGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MockAiRepository : AiCoach {

    override suspend fun generateBasePlan(
        goal: Goal,
        devices: List<Device>,
        minutes: Int,
        sessions: Int,
        level: String?
    ): Plan = withContext(Dispatchers.Default) {
        delay(150) // kleine Verzögerung für "AI-Feeling"
        // Struktur lokal generieren; Markdown bleibt leer (oder kurz)
        PlanGenerator.generateBasePlan(goal, devices, minutes, sessions)
    }

    override suspend fun suggestAlternative(
        goal: Goal,
        deviceHint: String,
        minutes: Int
    ): WorkoutDay = withContext(Dispatchers.Default) {
        delay(80)
        PlanGenerator.alternativeForToday(goal, deviceHint, minutes)
    }

    override suspend fun suggestRecipes(prefs: RecipePrefs, count: Int): List<Recipe> =
        withContext(Dispatchers.Default) {
            delay(120)
            // Bis die echte OpenAI-Antwort sauber gemappt ist, geben wir leer zurück.
            emptyList()
        }

    override suspend fun estimateCaloriesFromPhoto(imageBytes: ByteArray): CalorieEstimate =
        withContext(Dispatchers.Default) {
            delay(80)
            CalorieEstimate(
                title = "Foto-Mahlzeit",
                calories = 450,
                confidence = 0.4f,
                note = "MVP-Schätzung – KI folgt"
            )
        }
}
