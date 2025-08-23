package com.example.fitapp.data.ai

import com.example.fitapp.data.*
import com.example.fitapp.logic.PlanGenerator
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.ChatModel
import com.openai.models.responses.Response
import com.openai.models.responses.ResponseCreateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenAIRepository(
    private val client: OpenAIClient
) : AICoach {

    companion object {
        /** Baut den Client mit API-Key und gibt Repository zurück. */
        fun fromApiKey(apiKey: String): OpenAIRepository {
            val client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build()
            return OpenAIRepository(client)
            }
    }

    override suspend fun generateBasePlan(
        goal: Goal,
        devices: List<Device>,
        minutes: Int,
        sessions: Int,
        level: String?
    ): Plan = withContext(Dispatchers.IO) {
        val prompt = buildString {
            appendLine("Erzeuge einen Wochen-Trainingsplan als Markdown.")
            appendLine("Ziel: ${goal.name}, Zeit: $minutes min, Einheiten/Woche: $sessions, Level: ${level ?: "auto"}.")
            appendLine("Verfügbare Geräte: ${devices.joinToString { it.name }}.")
            appendLine("Sprache: Deutsch. Knappe Übungen, klare Listen.")
        }

        val params = ResponseCreateParams.builder()
            .model(ChatModel.GPT_5)
            .input(prompt)
            .build()

        val res: Response = client.responses().create(params)
        val md = res.toString().ifBlank { "# Plan\n(keine Antwort)" }

        PlanGenerator.generateBasePlan(goal, devices, minutes, sessions).copy(markdown = md)
    }

    override suspend fun suggestAlternative(goal: Goal, deviceHint: String, minutes: Int): WorkoutDay =
        withContext(Dispatchers.IO) {
            PlanGenerator.alternativeForToday(goal, deviceHint, minutes)
        }

    override suspend fun suggestRecipes(prefs: RecipePrefs, count: Int): List<Recipe> =
        withContext(Dispatchers.IO) {
            // TODO: Implementiere echtes Prompting für Rezepte (Structured Output)
            emptyList()
        }

    override suspend fun estimateCaloriesFromPhoto(imageBytes: ByteArray): CalorieEstimate =
        withContext(Dispatchers.IO) {
            CalorieEstimate("Foto-Mahlzeit", 450, 0.4f, "Konservative MVP-Schätzung")
        }
}
