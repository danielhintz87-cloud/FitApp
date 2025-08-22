package com.example.fitapp.data.ai

import com.example.fitapp.data.*

interface AiCoach {
    suspend fun generateBasePlan(
        goal: Goal,
        devices: List<Device>,
        minutes: Int,
        sessions: Int,
        level: String? = null
    ): Plan

    suspend fun suggestAlternative(goal: Goal, deviceHint: String, minutes: Int): WorkoutDay

    suspend fun suggestRecipes(prefs: RecipePrefs, count: Int = 5): List<Recipe>

    suspend fun estimateCaloriesFromPhoto(imageBytes: ByteArray): CalorieEstimate
}
