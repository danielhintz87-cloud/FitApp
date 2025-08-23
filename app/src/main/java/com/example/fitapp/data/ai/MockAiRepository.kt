package com.example.fitapp.data.ai

import com.example.fitapp.data.*
import com.example.fitapp.logic.PlanGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

class MockAiRepository : AICoach {

    override suspend fun generateBasePlan(
        goal: Goal,
        devices: List<Device>,
        minutes: Int,
        sessions: Int,
        level: String?
    ): Plan = withContext(Dispatchers.Default) {
        delay(300)
        PlanGenerator.generateBasePlan(goal, devices, minutes, sessions).copy(
            markdown = "# Demo-Wochenplan\n(Mock: AI nicht aktiv)"
        )
    }

    override suspend fun suggestAlternative(goal: Goal, deviceHint: String, minutes: Int): WorkoutDay =
        withContext(Dispatchers.Default) {
            delay(100)
            PlanGenerator.alternativeForToday(goal, deviceHint, minutes)
        }

    override suspend fun suggestRecipes(prefs: RecipePrefs, count: Int): List<Recipe> =
        withContext(Dispatchers.Default) {
            delay(200)
            List(count) { idx ->
                Recipe(
                    id = UUID.randomUUID().toString(),
                    title = when {
                        prefs.highProtein -> "Protein-Bowl $idx"
                        prefs.lowCarb -> "Low-Carb-Pfanne $idx"
                        prefs.vegetarian -> "Veggie-Pasta $idx"
                        else -> "Schnelle Bowl $idx"
                    },
                    calories = prefs.targetCalories ?: 500,
                    tags = buildList {
                        if (prefs.vegetarian) add("Vegetarisch")
                        if (prefs.highProtein) add("High Protein")
                        if (prefs.lowCarb) add("Low Carb")
                    },
                    ingredients = listOf(
                        RecipeIngredient("Protein", "150 g"),
                        RecipeIngredient("Gemüse", "200 g"),
                        RecipeIngredient("Beilage", "80 g")
                    ),
                    steps = listOf(
                        RecipeInstruction(1, "Zutaten vorbereiten"),
                        RecipeInstruction(2, "Protein anbraten"),
                        RecipeInstruction(3, "Gemüse garen"),
                        RecipeInstruction(4, "Anrichten")
                    ),
                    markdown = null
                )
            }
        }

    override suspend fun estimateCaloriesFromPhoto(imageBytes: ByteArray): CalorieEstimate =
        withContext(Dispatchers.Default) {
            delay(100)
            CalorieEstimate("Demo-Gericht", 450, 0.4f, "Demo-Schätzung (Mock)")
        }
}
