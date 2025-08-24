package com.example.fitapp.data.ai

import com.example.fitapp.data.*
import com.example.fitapp.logic.PlanGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lightweight placeholder implementation of [AICoach] that falls back to
 * locally generated results. This avoids compile-time issues when the OpenAI
 * SDK is unavailable or its API changes.
 */
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

    override suspend fun suggestBasePlan(
        goal: Goal,
        devices: List<Device>,
        minutes: Int,
        sessions: Int,
        level: String?
    ): Plan = generateBasePlan(goal, devices, minutes, sessions, level)

    override suspend fun suggestAlternative(goal: Goal, deviceHint: String, minutes: Int): WorkoutDay =
        withContext(Dispatchers.IO) {
            PlanGenerator.alternativeForToday(goal, deviceHint, minutes)
        }

    override suspend fun suggestRecipes(prefs: RecipePrefs, count: Int): List<Recipe> = emptyList()

    override suspend fun estimateCaloriesFromPhoto(imageBytes: ByteArray): CalorieEstimate =
        CalorieEstimate("Foto-Mahlzeit", 450, 0.4f, "Konservative Schätzung")
}
