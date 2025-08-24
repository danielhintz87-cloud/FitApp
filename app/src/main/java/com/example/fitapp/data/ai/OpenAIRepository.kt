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
import java.util.UUID

class OpenAIRepository(
    private val client: OpenAIClient
) : AICoach {

    companion object {
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
        val response: Response = client.responses().create(params)
        val markdown = response.toString().ifBlank { "# Plan\n(keine Antwort)" }
        // KI-generierten Markdown-Plan in Plan-Objekt übernehmen
        PlanGenerator.generateBasePlan(goal, devices, minutes, sessions).copy(markdown = markdown)
    }

    override suspend fun suggestBasePlan(
        goal: Goal,
        devices: List<Device>,
        minutes: Int,
        sessions: Int,
        level: String?
    ): Plan {
        // Nicht verwendet (generateBasePlan wird genutzt)
        return generateBasePlan(goal, devices, minutes, sessions, level)
    }

    override suspend fun suggestAlternative(goal: Goal, deviceHint: String, minutes: Int): WorkoutDay =
        withContext(Dispatchers.IO) {
            // Alternative Übungen lokal generieren (KI optional)
            PlanGenerator.alternativeForToday(goal, deviceHint, minutes)
        }

    override suspend fun suggestRecipes(prefs: RecipePrefs, count: Int): List<Recipe> =
        withContext(Dispatchers.IO) {
            val prompt = buildString {
                appendLine("Gib mir $count Rezeptvorschläge auf Deutsch im Markdown-Format.")
                appendLine("Kriterien:")
                if (prefs.vegetarian) appendLine("- Vegetarisch")
                if (prefs.highProtein) appendLine("- High Protein")
                if (prefs.lowCarb) appendLine("- Low Carb")
                if (prefs.targetCalories != null) appendLine("- Ziel: ~${prefs.targetCalories} kcal")
                appendLine("Format: Beginne jedes Rezept mit '## ' gefolgt vom Rezeptnamen.")
                appendLine("Nenne dann Kaloriengehalt, Zutaten (• Liste) und kurze Zubereitungsschritte.")
            }
            val params = ResponseCreateParams.builder()
                .model(ChatModel.GPT_5)
                .input(prompt)
                .build()
            return@withContext try {
                val response: Response = client.responses().create(params)
                val fullMarkdown = response.toString()
                // Markdown-Antwort in einzelne Rezepte aufteilen
                val parts = fullMarkdown.split(Regex("^## ", RegexOption.MULTILINE))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                parts.map { part ->
                    val lines = part.lines()
                    val title = lines.first().replace("#", "").trim()
                    // Kalorienzahl extrahieren, falls vorhanden
                    val kcalRegex = Regex("(\\d+)\\s?kcal")
                    val calories = kcalRegex.find(part)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    // Tags aus Präferenzen ableiten
                    val tags = mutableListOf<String>().apply {
                        if (prefs.vegetarian) add("Vegetarisch")
                        if (prefs.highProtein) add("High Protein")
                        if (prefs.lowCarb) add("Low Carb")
                    }
                    // Zutaten aus Aufzählungslinien extrahieren
                    val ingredients = lines.filter { it.trimStart().startsWith("- ") || it.trimStart().startsWith("• ") }
                        .map { line ->
                            val content = line.trimStart().drop(2).trim()
                            val split = content.split("—", " - ", "–").map { it.trim() }
                            val name = split.firstOrNull().orEmpty()
                            val amount = split.getOrNull(1) ?: ""
                            RecipeIngredient(name, amount)
                        }
                    // Zubereitungsschritte aus nummerierten Listen extrahieren
                    val instructions = lines.filter { it.trimStart().matches(Regex("^\\d+\\.\\s")) }
                        .mapIndexed { idx, line ->
                            val stepText = line.substringAfter(".").trim()
                            RecipeInstruction(idx + 1, stepText)
                        }
                    Recipe(
                        id = UUID.randomUUID().toString(),
                        title = title.ifBlank { "Rezept" },
                        calories = calories,
                        tags = tags,
                        ingredients = ingredients,
                        steps = instructions,
                        markdown = "## $title\n" + lines.drop(1).joinToString("\n")
                    )
                }
            } catch (e: Exception) {
                // Bei Fehler: Leere Liste zurückgeben (UI nutzt dann Mock-Daten):contentReference[oaicite:3]{index=3}
                emptyList<Recipe>()
            }
        }

    override suspend fun estimateCaloriesFromPhoto(imageBytes: ByteArray): CalorieEstimate =
        withContext(Dispatchers.IO) {
            // Platzhalter für Bildanalyse (z.B. Google Vision oder OpenAI Vision nutzen)
            CalorieEstimate("Foto-Mahlzeit", 450, 0.4f, "Konservative Schätzung")
        }
}
