package com.example.fitapp.infrastructure.providers

import android.content.Context
import android.graphics.Bitmap
import com.example.fitapp.domain.entities.*
import com.example.fitapp.domain.repositories.AiProviderRepository

/**
 * Intelligenter AI-Router für funktionsbasierte Modellauswahl
 *
 * BUDGET TIER STRATEGIE ($10/Monat):
 * - Gemini Tier 1: $5/Monat (150-4,000 RPM, Premium-Qualität)
 * - Perplexity: $5/Monat (~1,000 Searches für aktuelle Informationen)
 *
 * = Professionelle AI-Fitness-App mit optimaler Quality/Cost Balance! �
 */
class IntelligentAiRouter(
    private val context: Context,
    private val geminiProvider: GeminiAiProvider,
    private val perplexityProvider: PerplexityAiProvider?,
    private val repository: AiProviderRepository,
    private val budgetOptimizer: BudgetTierOptimizer = BudgetTierOptimizer(context),
) {
    /**
     * Automatische Provider- und Modellauswahl für Text-Tasks
     * MIT BUDGET TIER OPTIMIERUNG ($10/Monat optimal nutzen)
     */
    suspend fun generateOptimalText(
        prompt: String,
        taskType: TaskType,
    ): Result<String> {
        val selection = budgetOptimizer.selectOptimalBudgetModel(taskType, false)

        android.util.Log.d("AiRouter", "Budget Tier: $taskType → ${selection.reason} (${selection.qualityLevel})")

        val result =
            when (selection.provider) {
                OptimalProvider.GEMINI -> {
                    geminiProvider.generateTextWithTaskType(prompt, taskType)
                }
                OptimalProvider.PERPLEXITY -> {
                    if (perplexityProvider?.isAvailable() == true && selection.withinBudget) {
                        perplexityProvider.generateText(prompt)
                    } else {
                        // Fallback zu Gemini bei Perplexity-Budget-Überschreitung
                        android.util.Log.w("AiRouter", "Perplexity Budget erreicht, Fallback zu Gemini")
                        geminiProvider.generateTextWithTaskType(prompt, TaskType.SIMPLE_TEXT_COACHING)
                    }
                }
            }

        // Track usage für Budget Management
        if (result.isSuccess) {
            budgetOptimizer.trackRequest(selection)
        }

        return result
    }

    /**
     * Automatische Provider- und Modellauswahl für Bildanalyse-Tasks
     * MIT BUDGET TIER OPTIMIERUNG (Premium-Qualität für kritische Vision-Tasks)
     */
    suspend fun analyzeOptimalImage(
        prompt: String,
        bitmap: Bitmap,
        taskType: TaskType,
    ): Result<CaloriesEstimate> {
        val selection = budgetOptimizer.selectOptimalBudgetModel(taskType, true)

        android.util.Log.d("AiRouter", "Budget Vision: $taskType → ${selection.reason} (${selection.qualityLevel})")

        val result = geminiProvider.analyzeImageWithTaskType(prompt, bitmap, taskType)

        // Track usage für Budget Management
        if (result.isSuccess) {
            budgetOptimizer.trackRequest(selection)
        }

        return result
    }

    /**
     * Fitness-spezifische Funktionen mit optimaler Modellauswahl
     */

    // 🖼️ FOOD RECOGNITION - Flash für beste Bildanalyse
    suspend fun analyzeFoodImage(
        bitmap: Bitmap,
        note: String = "",
    ): Result<CaloriesEstimate> {
        val prompt = buildFoodAnalysisPrompt(note)
        return analyzeOptimalImage(prompt, bitmap, TaskType.MEAL_PHOTO_ANALYSIS)
    }

    // 🏋️ FORM CHECK - Flash für Bewegungsanalyse
    suspend fun analyzeFormCheck(
        bitmap: Bitmap,
        exerciseType: String,
    ): Result<String> {
        val prompt = buildFormCheckPrompt(exerciseType)
        return generateOptimalText(prompt, TaskType.FORM_CHECK_ANALYSIS)
    }

    // 📸 PROGRESS PHOTOS - Flash für Body-Analyse
    suspend fun analyzeProgressPhoto(
        bitmap: Bitmap,
        previousNotes: String = "",
    ): Result<CaloriesEstimate> {
        val prompt = buildProgressAnalysisPrompt(previousNotes)
        return analyzeOptimalImage(prompt, bitmap, TaskType.PROGRESS_PHOTO_ANALYSIS)
    }

    // 🍳 RECIPE GENERATION mit Bildern - Flash für AI-Bildgenerierung
    suspend fun generateRecipeWithImages(
        preferences: String,
        count: Int = 3,
    ): Result<String> {
        val prompt = buildRecipePrompt(preferences, count, includeImages = true)
        return generateOptimalText(prompt, TaskType.RECIPE_WITH_IMAGE_GEN)
    }

    // 🏃 WORKOUT PLANS - Flash für komplexe Trainingspläne
    suspend fun generateAdaptiveWorkout(
        goal: String,
        equipment: List<String>,
        duration: Int,
    ): Result<String> {
        val prompt = buildWorkoutPrompt(goal, equipment, duration)
        return generateOptimalText(prompt, TaskType.TRAINING_PLAN)
    }

    // 💬 COACHING MESSAGES - Flash-Lite für einfache Motivation
    suspend fun generateMotivationalMessage(userContext: String): Result<String> {
        val prompt = buildCoachingPrompt(userContext)
        return generateOptimalText(prompt, TaskType.SIMPLE_TEXT_COACHING)
    }

    // 🔍 FITNESS TRENDS - Perplexity für aktuelle Informationen
    suspend fun researchFitnessTrends(topic: String): Result<String> {
        val prompt = buildTrendResearchPrompt(topic)
        return generateOptimalText(prompt, TaskType.RESEARCH_TRENDS)
    }

    // 💊 SUPPLEMENT RESEARCH - Perplexity für aktuelle Studien
    suspend fun researchSupplements(supplement: String): Result<String> {
        val prompt = buildSupplementResearchPrompt(supplement)
        return generateOptimalText(prompt, TaskType.SUPPLEMENT_RESEARCH)
    }

    /**
     * Kostenschätzung für einen Task - MIT BUDGET TIER BERÜCKSICHTIGUNG
     */
    fun estimateTaskCost(
        taskType: TaskType,
        hasImage: Boolean = false,
    ): TaskCost {
        val selection = budgetOptimizer.selectOptimalBudgetModel(taskType, hasImage)

        return TaskCost(
            total = selection.estimatedCost,
            breakdown =
                mapOf(
                    "model_cost" to selection.estimatedCost,
                    "quality_level" to
                        when (selection.qualityLevel) {
                            QualityLevel.PREMIUM -> 0.015
                            QualityLevel.BUDGET -> 0.004
                            QualityLevel.SPECIALIZED -> 0.005
                            QualityLevel.OVER_BUDGET -> 0.0
                        },
                ),
            model =
                when (selection.provider) {
                    OptimalProvider.GEMINI -> selection.geminiModel?.modelId ?: "gemini-flash"
                    OptimalProvider.PERPLEXITY -> selection.perplexityModel?.modelId ?: "sonar"
                },
        )
    }

    /**
     * Generiere BUDGET TIER Kostenübersicht
     */
    fun generateCostReport(): BudgetCostAnalysis {
        val status = budgetOptimizer.getBudgetStatus()
        val recommendations = budgetOptimizer.getBudgetRecommendations()

        return BudgetCostAnalysis(
            currentStatus = status,
            recommendations = recommendations,
            qualityDistribution =
                mapOf(
                    "Premium Flash" to status.flashUsed,
                    "Budget Flash-Lite" to status.flashLiteUsed,
                    "Specialized Perplexity" to status.perplexityUsed,
                ),
            budgetOptimization =
                """
                💰 BUDGET TIER OPTIMIERUNG ($10/Monat):
                
                📊 Aktueller Verbrauch:
                • Gemini: ${"%.2f".format(
                    status.geminiSpent,
                )}$ / $5.00 (${String.format("%.1f", (status.geminiSpent / 5.0) * 100)}%)
                • Perplexity: ${"%.2f".format(
                    status.perplexitySpent,
                )}$ / $5.00 (${String.format("%.1f", (status.perplexitySpent / 5.0) * 100)}%)
                • Total: ${"%.2f".format(status.totalSpent)}$ / $10.00
                
                🎯 Verfügbare Kapazität:
                • ${status.flashRemaining} Premium Flash-Requests (Multimodal + Complex)
                • ${status.flashLiteRemaining} Budget Flash-Lite-Requests (Routine Tasks)
                • ${status.perplexityRemaining} Perplexity-Searches (Current Info)
                
                ✅ Quality First Allocation funktioniert optimal!
                """.trimIndent(),
        )
    }

    // --- PRIVATE PROMPT BUILDERS ---

    private fun buildFoodAnalysisPrompt(note: String): String =
        """
        Analysiere das Bild und schätze präzise die Kalorien des gezeigten Essens.
        
        **Analyseschritte:**
        1. Identifiziere alle sichtbaren Lebensmittel/Getränke
        2. Schätze Portionsgrößen anhand von Referenzobjekten (Teller ≈ 25cm, Gabel ≈ 20cm, Hand ≈ 18cm)
        3. Berücksichtige Zubereitungsart (frittiert +30%, gedämpft -20%)
        4. Kalkuliere Gesamtkalorien mit USDA-Nährwertstandards
        
        **Zusätzliche Notiz:** $note
        
        **Antwortformat:**
        kcal: <Zahl>
        confidence: <0-100>
        Begründung: [Lebensmittel] ca. [Gramm]g = [kcal]kcal, [weitere Komponenten]
        Unsicherheitsfaktoren: [versteckte Fette, Portionsgröße, etc.]
        """.trimIndent()

    private fun buildFormCheckPrompt(exerciseType: String): String =
        """
        Analysiere die Trainingsform in diesem Bild für die Übung: $exerciseType
        
        **Analyse-Punkte:**
        1. Körperhaltung und Alignment
        2. Bewegungsausführung und Technik
        3. Potentielle Verletzungsrisiken
        4. Verbesserungsvorschläge
        
        **Antwortformat:**
        ✅ **Gut:** [positive Aspekte]
        ⚠️ **Verbesserung:** [konkrete Korrekturen]
        🎯 **Tipp:** [Coaching-Hinweise]
        """.trimIndent()

    private fun buildProgressAnalysisPrompt(previousNotes: String): String =
        """
        Analysiere dieses Progress-Foto für Body-Transformation tracking.
        
        **Vorherige Notizen:** $previousNotes
        
        **Analyse-Bereiche:**
        1. Sichtbare Veränderungen in Körperkomposition
        2. Muskeldefinition und -aufbau
        3. Gesamteindruck der Transformation
        4. Motivierende Erkenntnisse
        
        **Antwortformat:**
        📈 **Fortschritte:** [konkrete Veränderungen]
        💪 **Stärken:** [positive Entwicklungen]
        🎯 **Fokus:** [Bereiche für weitere Verbesserung]
        """.trimIndent()

    private fun buildRecipePrompt(
        preferences: String,
        count: Int,
        includeImages: Boolean,
    ): String =
        """
        Erstelle $count fitness-optimierte Rezepte basierend auf: $preferences
        
        ${if (includeImages) "**WICHTIG:** Generiere für jedes Rezept auch ein appetitliches Bild zur Visualisierung." else ""}
        
        **Anforderungen:**
        - Kalorie- und Makronährstoff-Angaben
        - Einfache Zubereitung (max. 30 Min)
        - Verfügbare Zutaten in Deutschland
        - Geschmackvoll und sättigend
        
        **Format pro Rezept:**
        ## [Rezeptname]
        **Kalorien:** [X] kcal | **Protein:** [X]g | **Kohlenhydrate:** [X]g | **Fett:** [X]g
        
        **Zutaten:**
        - [Liste der Zutaten mit Mengen]
        
        **Zubereitung:**
        1. [Schritt-für-Schritt Anleitung]
        
        ${if (includeImages) "**Bild:** [AI-generiertes appetitliches Foto des fertigen Gerichts]" else ""}
        """.trimIndent()

    private fun buildWorkoutPrompt(
        goal: String,
        equipment: List<String>,
        duration: Int,
    ): String =
        """
        Erstelle einen adaptiven $duration-Minuten Trainingsplan für: $goal
        
        **Verfügbare Ausrüstung:** ${equipment.joinToString(", ").ifEmpty { "Körpergewicht" }}
        
        **Trainingsaufbau:**
        1. Warm-up (5 Min)
        2. Hauptteil (${duration - 10} Min)
        3. Cool-down (5 Min)
        
        **Für jede Übung angeben:**
        - Übungsname und Beschreibung
        - Sets x Reps oder Zeit
        - Pausenzeiten
        - Ausführungshinweise
        - Progressionsmöglichkeiten
        
        **Adaptionshilfen:**
        - Vereinfachungen für Anfänger
        - Steigerungen für Fortgeschrittene
        - Alternative Übungen bei Beschwerden
        """.trimIndent()

    private fun buildCoachingPrompt(userContext: String): String =
        """
        Erstelle eine motivierende Coaching-Nachricht basierend auf: $userContext
        
        **Stil:**
        - Positiv und ermutigend
        - Konkret und umsetzbar
        - Persönlich und authentisch
        - Maximal 150 Wörter
        
        **Struktur:**
        💪 **Motivation:** [Ermutigende Worte]
        🎯 **Heute:** [Konkrete Aktion für heute]
        ⭐ **Reminder:** [Motivierender Gedanke]
        """.trimIndent()

    private fun buildTrendResearchPrompt(topic: String): String =
        """
        Recherchiere die aktuellsten Fitness-Trends und Entwicklungen zu: $topic
        
        **Fokus auf:**
        - Neueste wissenschaftliche Erkenntnisse (2024/2025)
        - Trending Workouts und Methoden
        - Innovative Equipment und Apps
        - Experten-Meinungen und Reviews
        
        **Quellen einbeziehen:**
        - Aktuelle Fitness-Magazine
        - Wissenschaftliche Studien
        - Social Media Trends
        - Equipment-Reviews und Tests
        
        **Format:**
        🔥 **Trends 2025:** [Neueste Entwicklungen]
        📊 **Studien:** [Wissenschaftliche Erkenntnisse]
        🛠️ **Equipment:** [Innovative Geräte/Apps]
        👥 **Community:** [Was die Fitness-Community bewegt]
        """.trimIndent()

    private fun buildSupplementResearchPrompt(supplement: String): String =
        """
        Recherchiere aktuelle wissenschaftliche Erkenntnisse zu: $supplement
        
        **Research-Bereiche:**
        - Neueste Studien und Meta-Analysen (2024/2025)
        - Wirksamkeit und Dosierung
        - Nebenwirkungen und Interaktionen
        - Preis-Leistungs-Verhältnis verschiedener Marken
        
        **Quellen:**
        - PubMed und wissenschaftliche Journals
        - Examine.com und ähnliche Evidenz-basierte Plattformen
        - Aktuelle Product-Reviews und Tests
        - FDA/BfR Warnungen und Empfehlungen
        
        **Format:**
        🧬 **Wissenschaft:** [Aktuelle Studienlage]
        💊 **Dosierung:** [Empfohlene Einnahme]
        ⚠️ **Sicherheit:** [Nebenwirkungen, Wechselwirkungen]
        💰 **Empfehlung:** [Beste Produkte und Bezugsquellen]
        """.trimIndent()
}

data class BudgetCostAnalysis(
    val currentStatus: BudgetStatus,
    val recommendations: BudgetRecommendation,
    val qualityDistribution: Map<String, Int>,
    val budgetOptimization: String,
)
