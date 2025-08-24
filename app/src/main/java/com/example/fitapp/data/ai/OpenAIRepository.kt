package com.example.fitapp.data.ai

import com.example.fitapp.data.*
import com.example.fitapp.logic.PlanGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenAIRepository private constructor() : AICoach {

    companion object {
        fun fromApiKey(@Suppress("UNUSED_PARAMETER") apiKey: String): OpenAIRepository = OpenAIRepository()
    }

    override suspend fun generateBasePlan(
        goal: Goal,
        devices: List<Device>,
        minutes: Int,
        sessions: Int,
        level: String?
    ): Plan = withContext(Dispatchers.IO) {
        PlanGenerator.generateBasePlan(goal, devices, minutes, sessions)
    }

    override suspend fun suggestAlternative(goal: Goal, deviceHint: String, minutes: Int): WorkoutDay =
        withContext(Dispatchers.IO) {
            PlanGenerator.alternativeForToday(goal, deviceHint, minutes)
        }

    override suspend fun suggestRecipes(prefs: RecipePrefs, count: Int): List<Recipe> = withContext(Dispatchers.IO) {
        // TODO: Echte OpenAI-Anbindung implementieren; vorerst lokale Mock-Daten nutzen
        MockAiRepository().suggestRecipes(prefs, count)
    }

    override suspend fun estimateCaloriesFromPhoto(imageBytes: ByteArray): CalorieEstimate =
        CalorieEstimate("Foto-Mahlzeit", 450, 0.4f, "Konservative Schätzung")
}
