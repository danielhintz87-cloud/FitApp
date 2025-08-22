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
        delay(300) // simulierter Call
        val base = PlanGenerator.generateBasePlan(goal, devices, minutes, sessions)
        base.copy(markdown =
            """
            # Wochenplan – ${goal.name}
            Geräte: ${devices.joinToString { it.name }}
            
            ${base.week.joinToString("\n") { day ->
                "## ${day.title} – ${day.durationMin} min\n" +
                        day.exercises.joinToString("\n") { ex ->
                            val detail = when {
                                ex.sets != null && ex.reps != null -> "(${ex.sets}×${ex.reps})"
                                else -> ex.note ?: ""
                            }
                            "- ${ex.name} $detail".trim()
                        }
            }}
            """.trimIndent()
        )
    }

    override suspend fun suggestAlternative(goal: Goal, deviceHint: String, minutes: Int): WorkoutDay =
        withContext(Dispatchers.Default) {
            delay(150)
            PlanGenerator.alternativeForToday(goal, deviceHint, minutes)
        }

    override suspend fun suggestRecipes(prefs: RecipePrefs, count: Int): List<Recipe> =
        withContext(Dispatchers.Default) {
            delay(250)
            List(count) { idx ->
                Recipe(
                    id = UUID.randomUUID().toString(),
                    title = when {
                        prefs.highProtein -> "Protein-Bowl ${idx + 1}"
                        prefs.lowCarb -> "Low-Carb-Pfanne ${idx + 1}"
                        prefs.vegetarian -> "Veggie-Pasta ${idx + 1}"
                        else -> "Schnelle Bowl ${idx + 1}"
                    },
                    calories = prefs.targetCalories ?: 550,
                    tags = buildList {
                        if (prefs.vegetarian) add("Vegetarisch")
                        if (prefs.highProtein) add("High Protein")
                        if (prefs.lowCarb) add("Low Carb")
                    },
                    ingredients = listOf(
                        RecipeIngredient("Hähnchen/Tofu", "150 g"),
                        RecipeIngredient("Reis/Quinoa", "70 g (roh)"),
                        RecipeIngredient("Gemüse-Mix", "200 g")
                    ),
                    steps = listOf(
                        RecipeInstruction(1, "Zutaten vorbereiten, Gemüse schneiden."),
                        RecipeInstruction(2, "Proteinquelle scharf anbraten."),
                        RecipeInstruction(3, "Gemüse zugeben, garen, würzen."),
                        RecipeInstruction(4, "Mit Basis (Reis/Quinoa) servieren.")
                    ),
                    markdown = """
                        ### Tipps
                        - Mit Kräutern & Zitronensaft frisch abschmecken
                        - ${if (prefs.lowCarb) "Reis durch Blumenkohlreis ersetzen" else "Portion Reis für Sättigung"} 
                    """.trimIndent()
                )
            }
        }

    override suspend fun estimateCaloriesFromPhoto(imageBytes: ByteArray): CalorieEstimate =
        withContext(Dispatchers.Default) {
            delay(120)
            CalorieEstimate("Beispiel-Gericht", 480, 0.42f, "Demo-Schätzung (Mock)")
        }
}
