package com.example.fitapp.application.usecases

import android.graphics.Bitmap
import com.example.fitapp.domain.entities.*
import com.example.fitapp.domain.repositories.AiProviderRepository
import com.example.fitapp.domain.usecases.*

/**
 * Implementation of training plan generation use case
 */
class GenerateTrainingPlanUseCaseImpl(
    private val repository: AiProviderRepository,
) : GenerateTrainingPlanUseCase {
    override suspend fun execute(request: PlanRequest): Result<String> {
        val provider = repository.selectOptimalProvider(TaskType.TRAINING_PLAN)

        val prompt =
            buildString {
                append(
                    "Erstelle einen wissenschaftlich fundierten **${request.weeks}-Wochen-Trainingsplan** in Markdown. ",
                )
                append(
                    "Ziel: ${request.goal}. Trainingsfrequenz: ${request.sessionsPerWeek} Einheiten/Woche, ${request.minutesPerSession} Min/Einheit. ",
                )
                append("Verfügbare Geräte: ${request.equipment.joinToString()}. ")
                append("\n\n**Medizinische Anforderungen:**\n")
                append("- Progressive Überlastung mit 5-10% Steigerung alle 2 Wochen\n")
                append("- Deload-Wochen alle 4 Wochen (50-60% Intensität)\n")
                append("- RPE-Skala (6-20) für Intensitätskontrolle\n")
                append("- Mindestens 48h Pause zwischen gleichen Muskelgruppen\n")
                append("- Aufwärm- und Cool-Down-Protokolle\n")
                append("\n**Struktur:** Jede Woche mit H2-Überschrift, dann Trainingstage mit:\n")
                append("- Aufwärmung (5-10 Min)\n")
                append("- Hauptübungen: Übung | Sätze x Wiederholungen | Tempo (1-2-1-0) | RPE | Pausenzeit\n")
                append("- Cool-Down & Mobility (5-10 Min)\n")
                append("- Progressionshinweise für nächste Woche\n")
                append("- Anpassungen bei Beschwerden oder Stagnation")
            }

        val aiRequest =
            AiRequest(
                prompt = prompt,
                provider = provider,
                taskType = TaskType.TRAINING_PLAN,
            )

        return repository.generateText(aiRequest).recoverCatching { error ->
            // Try fallback provider
            val fallbackProvider = repository.getFallbackProvider(provider)
            if (fallbackProvider != null && repository.isProviderAvailable(fallbackProvider)) {
                val fallbackRequest =
                    aiRequest.copy(
                        provider = fallbackProvider,
                        prompt = "Fallback zu ${fallbackProvider.name}: $prompt",
                    )
                repository.generateText(fallbackRequest).getOrThrow()
            } else {
                throw error
            }
        }
    }
}

/**
 * Implementation of recipe generation use case
 */
class GenerateRecipesUseCaseImpl(
    private val repository: AiProviderRepository,
) : GenerateRecipesUseCase {
    override suspend fun execute(request: RecipeRequest): Result<String> {
        val provider = repository.selectOptimalProvider(TaskType.RECIPE_GENERATION)

        val prompt =
            buildString {
                append(
                    "Erstelle ${request.count} **einzelne, klar getrennte Rezepte** als strukturierte Markdown-Liste. ",
                )
                append("Präferenzen: ${request.preferences}. Diätform: ${request.diet}. ")
                append(
                    "\n\n**WICHTIG: Jedes Rezept MUSS mit '## ' beginnen und durch eine Leerzeile getrennt sein!**\n\n",
                )
                append("**Format pro Rezept:**\n")
                append("## [Rezeptname]\n")
                append("**Kalorien:** [Anzahl] kcal pro Portion\n")
                append("**Portionen:** [Anzahl]\n")
                append("**Zubereitungszeit:** [Zeit] Minuten\n")
                append("**Schwierigkeit:** Leicht/Mittel/Schwer\n\n")
                append("**Zutaten:**\n")
                append("- [Zutat] ([exakte Gramm-Angabe])\n")
                append("- [Weitere Zutaten mit präzisen Mengen]\n\n")
                append("**Zubereitung:**\n")
                append("1. [Erster Schritt der Zubereitung]\n")
                append("2. [Zweiter Schritt der Zubereitung]\n")
                append("[Weitere nummerierte Schritte]\n\n")
                append("**Nährwerte pro Portion:**\n")
                append("- Protein: [X]g\n")
                append("- Kohlenhydrate: [X]g\n")
                append("- Fett: [X]g\n")
                append("- Ballaststoffe: [X]g\n\n")
                append("**Mikronährstoff-Highlights:** [Vitamin C, Eisen, etc.]\n\n")
                append("---\n\n")
                append("\n**Kalkulationsbasis:** Verwende USDA-Nährwertdatenbank-Standards für genaue Berechnungen. ")
                append("Achte auf realistische Portionsgrößen und präzise Makronährstoff-Verteilung. ")
                append("JEDES REZEPT MUSS VOLLSTÄNDIG GETRENNT UND MIT ## ÜBERSCHRIFT BEGINNEN!")
            }

        val aiRequest =
            AiRequest(
                prompt = prompt,
                provider = provider,
                taskType = TaskType.RECIPE_GENERATION,
            )

        return repository.generateText(aiRequest).recoverCatching { error ->
            // Try fallback provider
            val fallbackProvider = repository.getFallbackProvider(provider)
            if (fallbackProvider != null && repository.isProviderAvailable(fallbackProvider)) {
                val fallbackRequest =
                    aiRequest.copy(
                        provider = fallbackProvider,
                        prompt = "Erstelle ${request.count} **einzelne, klar getrennte Rezepte** mit ## Überschriften...",
                    )
                repository.generateText(fallbackRequest).getOrThrow()
            } else {
                throw error
            }
        }
    }
}

/**
 * Implementation of calorie estimation use case
 */
class EstimateCaloriesUseCaseImpl(
    private val repository: AiProviderRepository,
) : EstimateCaloriesUseCase {
    override suspend fun execute(
        bitmap: Bitmap,
        note: String,
    ): Result<CaloriesEstimate> {
        val prompt =
            buildString {
                append("Analysiere das Bild und schätze präzise die Kalorien des gezeigten Essens.\n\n")
                append("**Analyseschritte:**\n")
                append("1. Identifiziere alle sichtbaren Lebensmittel/Getränke\n")
                append(
                    "2. Schätze Portionsgrößen anhand von Referenzobjekten (Teller ≈ 25cm, Gabel ≈ 20cm, Hand ≈ 18cm)\n",
                )
                append("3. Berücksictige Zubereitungsart (frittiert +30%, gedämpft -20%)\n")
                append("4. Kalkuliere Gesamtkalorien mit USDA-Nährwertstandards\n\n")
                append("**Antwortformat:**\n")
                append("kcal: <Zahl>\n")
                append("confidence: <0-100>\n")
                append("Begründung: [Lebensmittel] ca. [Gramm]g = [kcal]kcal, [weitere Komponenten]\n")
                append("Unsicherheitsfaktoren: [versteckte Fette, Portionsgröße, etc.]")
                if (note.isNotBlank()) {
                    append("\n\nZusätzliche Notiz: $note")
                }
            }

        return repository.analyzeImage(prompt, bitmap, AiProvider.Gemini)
    }
}

/**
 * Implementation of shopping list parsing use case
 */
class ParseShoppingListUseCaseImpl(
    private val repository: AiProviderRepository,
) : ParseShoppingListUseCase {
    override suspend fun execute(spokenText: String): Result<String> {
        val provider = repository.selectOptimalProvider(TaskType.SHOPPING_LIST_PARSING)

        val prompt =
            buildString {
                append(
                    "Analysiere folgenden gesprochenen Text und extrahiere einzelne Einkaufsliste-Items: '$spokenText'\n\n",
                )
                append("**Aufgabe:** Zerlege den Text in einzelne Lebensmittel mit optional genannten Mengen.\n\n")
                append("**Ausgabeformat:** Eine Zeile pro Item im Format: 'Produktname|Menge'\n")
                append("**Beispiele:**\n")
                append("- 'Äpfel|2kg'\n")
                append("- 'Milch|1L'\n")
                append("- 'Brot|1 Stück'\n")
                append("- 'Bananen|' (wenn keine Menge genannt)\n\n")
                append("**Regeln:**\n")
                append("- Erkenne Trennwörter wie 'und', 'sowie', ',', '&'\n")
                append("- Normalisiere Produktnamen (z.B. 'Tomaten' statt 'Tomate')\n")
                append("- Wenn keine Menge explizit genannt, lasse Mengenfeld leer\n")
                append("- Ignoriere Füllwörter wie 'ich brauche', 'kaufen', etc.\n")
                append("- Ein Item pro Zeile, keine zusätzlichen Erklärungen")
            }

        val aiRequest =
            AiRequest(
                prompt = prompt,
                provider = provider,
                taskType = TaskType.SHOPPING_LIST_PARSING,
            )

        return repository.generateText(aiRequest).recoverCatching { error ->
            // Try fallback provider
            val fallbackProvider = repository.getFallbackProvider(provider)
            if (fallbackProvider != null && repository.isProviderAvailable(fallbackProvider)) {
                val fallbackRequest =
                    aiRequest.copy(
                        provider = fallbackProvider,
                        prompt = "Analysiere folgenden gesprochenen Text und extrahiere einzelne Einkaufsliste-Items: '$spokenText'...",
                    )
                repository.generateText(fallbackRequest).getOrThrow()
            } else {
                throw error
            }
        }
    }
}
