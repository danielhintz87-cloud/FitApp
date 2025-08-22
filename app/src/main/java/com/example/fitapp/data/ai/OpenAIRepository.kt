package com.example.fitapp.data.ai

import com.example.fitapp.data.*
import com.example.fitapp.logic.PlanGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Platzhalter-Implementierung eines AI-Backends. Die tatsächliche Kommunikation
 * mit der OpenAI-API ist hier noch nicht implementiert, damit das Projekt dennoch
 * kompilierbar bleibt. Die Methoden liefern daher lokal generierte Ergebnisse.
 */
class OpenAIRepository private constructor() : AICoach {

    companion object {
        fun fromApiKey(apiKey: String): OpenAIRepository = OpenAIRepository()
    }

    override suspend fun generateBasePlan(
        goal: Goal,
        devices: List<Device>,
        minutes: Int,
        sessions: Int,
        level: String?
    ): Plan = withContext(Dispatchers.IO) {
        // In einer echten Implementierung würde hier ein Request an OpenAI erfolgen.
        PlanGenerator.generateBasePlan(goal, devices, minutes, sessions)
    }

    override suspend fun suggestAlternative(goal: Goal, deviceHint: String, minutes: Int): WorkoutDay =
        withContext(Dispatchers.IO) {
            PlanGenerator.alternativeForToday(goal, deviceHint, minutes)
        }

    override suspend fun suggestRecipes(prefs: RecipePrefs, count: Int): List<Recipe> =
        withContext(Dispatchers.IO) { emptyList() }

    override suspend fun estimateCaloriesFromPhoto(imageBytes: ByteArray): CalorieEstimate =
        withContext(Dispatchers.IO) { CalorieEstimate("Foto", 0, 0f, "Nicht implementiert") }
}
