package com.example.fitapp.infrastructure.providers

import com.example.fitapp.domain.entities.TaskType

/**
 * Intelligente Modellauswahl für optimale Funktionalität und Kosteneffizienz
 * 
 * Strategien basierend auf Nutzer-Empfehlungen:
 * - Gemini 2.5 Flash: Für Multimodal-Tasks (Bilder) und komplexe Reasoning
 * - Gemini 2.5 Flash-Lite: Für einfache Textgenerierung 
 * - Perplexity Sonar: Für aktuelle Informationen und Research
 */
object ModelOptimizer {

    /**
     * Gemini-Modellauswahl basierend auf Task-Anforderungen
     */
    enum class GeminiModel(
        val modelId: String,
        val costInput: Double,    // $ per million tokens
        val costOutput: Double,   // $ per million tokens
        val costImage: Double,    // $ per image
        val capabilities: Set<ModelCapability>
    ) {
        FLASH(
            modelId = "gemini-2.5-flash-latest",
            costInput = 0.30,
            costOutput = 2.50,
            costImage = 0.039,
            capabilities = setOf(
                ModelCapability.MULTIMODAL,
                ModelCapability.IMAGE_GENERATION,
                ModelCapability.COMPLEX_REASONING,
                ModelCapability.HIGH_QUALITY_TEXT
            )
        ),
        FLASH_LITE(
            modelId = "gemini-2.5-flash-8b",
            costInput = 0.10,
            costOutput = 0.40,
            costImage = 0.0,  // Keine Bildverarbeitung
            capabilities = setOf(
                ModelCapability.BASIC_TEXT,
                ModelCapability.FAST_INFERENCE
            )
        )
    }

    /**
     * Perplexity-Modellauswahl
     */
    enum class PerplexityModel(
        val modelId: String,
        val costInput: Double,
        val costOutput: Double,
        val costSearch: Double,  // $ per 1000 searches
        val capabilities: Set<ModelCapability>
    ) {
        SONAR_BASIC(
            modelId = "sonar",
            costInput = 1.0,
            costOutput = 1.0,
            costSearch = 5.0,
            capabilities = setOf(
                ModelCapability.CURRENT_INFO,
                ModelCapability.WEB_SEARCH,
                ModelCapability.RESEARCH
            )
        )
    }

    enum class ModelCapability {
        MULTIMODAL,           // Bild + Text Verarbeitung
        IMAGE_GENERATION,     // Bilder generieren
        COMPLEX_REASONING,    // Komplexe Logik
        HIGH_QUALITY_TEXT,    // Hochwertige Textausgabe
        BASIC_TEXT,          // Einfache Textgenerierung
        FAST_INFERENCE,      // Schnelle Antworten
        CURRENT_INFO,        // Aktuelle Informationen
        WEB_SEARCH,          // Internet-Suche
        RESEARCH             // Wissenschaftliche Quellen
    }

    /**
     * Optimale Modellauswahl für spezifische Fitness-App Funktionen
     */
    fun selectOptimalModel(taskType: TaskType, hasImage: Boolean = false): ModelSelection {
        return when {
            // 🖼️ MULTIMODAL TASKS - Benötigen Gemini Flash
            hasImage || taskType == TaskType.CALORIE_ESTIMATION -> {
                ModelSelection(
                    provider = OptimalProvider.GEMINI,
                    geminiModel = GeminiModel.FLASH,
                    reason = "Bildanalyse erfordert Multimodal-Fähigkeiten: Food Recognition, Form Check, Progress Photos"
                )
            }

            // 🍳 REZEPT-GENERATION mit Bildern - Gemini Flash für Bild-Output
            taskType == TaskType.RECIPE_GENERATION -> {
                ModelSelection(
                    provider = OptimalProvider.GEMINI,
                    geminiModel = GeminiModel.FLASH,
                    reason = "Rezept-Visualisierung mit AI-generierten Bildern ($0.039/Bild)"
                )
            }

            // 🏋️ TRAININGSPLAN-GENERATION - Komplexe Logik benötigt Flash
            taskType == TaskType.TRAINING_PLAN -> {
                ModelSelection(
                    provider = OptimalProvider.GEMINI,
                    geminiModel = GeminiModel.FLASH,
                    reason = "Adaptive Trainingspläne benötigen komplexe Reasoning-Fähigkeiten"
                )
            }

            // 🔍 AKTUELLE FITNESS-TRENDS - Perplexity für Web-Research
            taskType == TaskType.RESEARCH_TRENDS -> {
                ModelSelection(
                    provider = OptimalProvider.PERPLEXITY,
                    perplexityModel = PerplexityModel.SONAR_BASIC,
                    reason = "Neueste Fitness-Trends, Supplement-Research, Equipment-Reviews"
                )
            }

            // 💬 EINFACHE COACHING-NACHRICHTEN - Flash-Lite für Kosteneffizienz
            taskType == TaskType.MOTIVATIONAL_COACHING -> {
                ModelSelection(
                    provider = OptimalProvider.GEMINI,
                    geminiModel = GeminiModel.FLASH_LITE,
                    reason = "Einfache Motivation und Coaching-Texte - 4x günstiger"
                )
            }

            // 📝 EINKAUFSLISTEN - Flash-Lite ausreichend
            taskType == TaskType.SHOPPING_LIST_PARSING -> {
                ModelSelection(
                    provider = OptimalProvider.GEMINI,
                    geminiModel = GeminiModel.FLASH_LITE,
                    reason = "Einfache Textverarbeitung - kostengünstig"
                )
            }

            // 🥗 NUTRITION ADVICE ohne Bilder - Flash-Lite
            taskType == TaskType.NUTRITION_ADVICE -> {
                ModelSelection(
                    provider = OptimalProvider.GEMINI,
                    geminiModel = GeminiModel.FLASH_LITE,
                    reason = "Grundlegende Ernährungsberatung ohne Bildanalyse"
                )
            }

            // 📊 PROGRESS ANALYSIS - Flash für bessere Analyse
            taskType == TaskType.PROGRESS_ANALYSIS -> {
                ModelSelection(
                    provider = OptimalProvider.GEMINI,
                    geminiModel = GeminiModel.FLASH,
                    reason = "Komplexe Datenanalyse und Trend-Erkennung"
                )
            }

            // DEFAULT: Flash-Lite für unbekannte Tasks
            else -> {
                ModelSelection(
                    provider = OptimalProvider.GEMINI,
                    geminiModel = GeminiModel.FLASH_LITE,
                    reason = "Standard-Fallback - kosteneffizient"
                )
            }
        }
    }

    /**
     * Kostenschätzung für einen Task
     */
    fun estimateCost(taskType: TaskType, hasImage: Boolean = false, inputTokens: Int = 1000, outputTokens: Int = 500): TaskCost {
        val selection = selectOptimalModel(taskType, hasImage)
        
        return when (selection.provider) {
            OptimalProvider.GEMINI -> {
                val model = selection.geminiModel!!
                val inputCost = (inputTokens / 1_000_000.0) * model.costInput
                val outputCost = (outputTokens / 1_000_000.0) * model.costOutput
                val imageCost = if (hasImage) model.costImage else 0.0
                
                TaskCost(
                    total = inputCost + outputCost + imageCost,
                    breakdown = mapOf(
                        "input" to inputCost,
                        "output" to outputCost,
                        "image" to imageCost
                    ),
                    model = model.modelId
                )
            }
            OptimalProvider.PERPLEXITY -> {
                val model = selection.perplexityModel!!
                val inputCost = (inputTokens / 1_000_000.0) * model.costInput
                val outputCost = (outputTokens / 1_000_000.0) * model.costOutput
                val searchCost = 0.005 // Annahme: 1 Search per Request
                
                TaskCost(
                    total = inputCost + outputCost + searchCost,
                    breakdown = mapOf(
                        "input" to inputCost,
                        "output" to outputCost,
                        "search" to searchCost
                    ),
                    model = model.modelId
                )
            }
        }
    }

    /**
     * Generiere Kostenübersicht für typische App-Nutzung
     */
    fun generateCostAnalysis(): CostAnalysis {
        val typicalUsage = mapOf(
            TaskType.CALORIE_ESTIMATION to UsagePattern(10, true, 500, 300),      // 10x täglich mit Bildern
            TaskType.RECIPE_GENERATION to UsagePattern(3, false, 800, 1500),      // 3x täglich, ausführlich
            TaskType.TRAINING_PLAN to UsagePattern(1, false, 1200, 2000),         // 1x täglich, komplex
            TaskType.MOTIVATIONAL_COACHING to UsagePattern(5, false, 300, 200),   // 5x täglich, kurz
            TaskType.NUTRITION_ADVICE to UsagePattern(3, false, 400, 500),        // 3x täglich
            TaskType.SHOPPING_LIST_PARSING to UsagePattern(1, false, 200, 100)    // 1x täglich
        )

        val dailyCosts = typicalUsage.map { (task, usage) ->
            val singleCost = estimateCost(task, usage.hasImage, usage.inputTokens, usage.outputTokens)
            task to singleCost.total * usage.frequency
        }.toMap()

        val dailyTotal = dailyCosts.values.sum()
        val monthlyTotal = dailyTotal * 30

        return CostAnalysis(
            dailyCosts = dailyCosts,
            dailyTotal = dailyTotal,
            monthlyTotal = monthlyTotal,
            comparison = mapOf(
                "Aktuelle Implementierung" to monthlyTotal,
                "Nur Flash (ohne Optimierung)" to monthlyTotal * 2.5,
                "Nur Flash-Lite (eingeschränkt)" to monthlyTotal * 0.4
            )
        )
    }
}

enum class OptimalProvider { GEMINI, PERPLEXITY }

data class ModelSelection(
    val provider: OptimalProvider,
    val geminiModel: ModelOptimizer.GeminiModel? = null,
    val perplexityModel: ModelOptimizer.PerplexityModel? = null,
    val reason: String
)

data class TaskCost(
    val total: Double,
    val breakdown: Map<String, Double>,
    val model: String
)

data class UsagePattern(
    val frequency: Int,        // Anzahl pro Tag
    val hasImage: Boolean,
    val inputTokens: Int,
    val outputTokens: Int
)

data class CostAnalysis(
    val dailyCosts: Map<TaskType, Double>,
    val dailyTotal: Double,
    val monthlyTotal: Double,
    val comparison: Map<String, Double>
)

// Neue TaskTypes für erweiterte Funktionalität
enum class ExtendedTaskType {
    FORM_CHECK_ANALYSIS,      // Haltungskorrektur mit Bildern
    EQUIPMENT_RECOGNITION,    // Gym-Geräte identifizieren
    PROGRESS_PHOTO_ANALYSIS,  // Body-Transformation tracking
    LIVE_COACHING_FEEDBACK,   // Echtzeit-Feedback
    TREND_RESEARCH,          // Aktuelle Fitness-Trends
    SUPPLEMENT_RESEARCH,     // Supplement-Studies
    MEAL_PHOTO_ANALYSIS      // Detaillierte Mahlzeit-Analyse
}
