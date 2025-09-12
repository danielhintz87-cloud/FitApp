package com.example.fitapp.infrastructure.providers

import android.content.Context
import com.example.fitapp.domain.entities.TaskType
import java.time.LocalDate

/**
 * Budget Tier Optimizer für $10/Monat Budget
 *
 * Verfügbare Budgets:
 * - Gemini Tier 1: $5/Monat (150-4,000 RPM, unbegrenzte Token)
 * - Perplexity: $5/Monat (~1,000 Searches)
 *
 * Strategie: Quality First mit Smart Budget Allocation
 */
class BudgetTierOptimizer(private val context: Context) {
    companion object {
        // Monatliche Budgets
        const val MONTHLY_GEMINI_BUDGET = 5.00 // $5 Gemini Tier 1
        const val MONTHLY_PERPLEXITY_BUDGET = 5.00 // $5 Perplexity
        const val TOTAL_MONTHLY_BUDGET = 10.00 // $10 Total

        // Smart Budget Allocation für Gemini
        const val FLASH_BUDGET_RATIO = 0.70 // 70% für Premium Flash
        const val FLASH_LITE_BUDGET_RATIO = 0.30 // 30% für Budget Flash-Lite

        // Geschätzte Kosten pro Request (vereinfacht)
        const val FLASH_COST_PER_REQUEST = 0.015 // ~$0.015 pro Request
        const val FLASH_LITE_COST_PER_REQUEST = 0.004 // ~$0.004 pro Request
        const val PERPLEXITY_COST_PER_SEARCH = 0.005 // ~$0.005 pro Search

        // Daraus resultierende monatliche Kapazitäten
        val MONTHLY_FLASH_BUDGET = MONTHLY_GEMINI_BUDGET * FLASH_BUDGET_RATIO // $3.50
        val MONTHLY_FLASH_LITE_BUDGET = MONTHLY_GEMINI_BUDGET * FLASH_LITE_BUDGET_RATIO // $1.50
        val MONTHLY_FLASH_CAPACITY = (MONTHLY_FLASH_BUDGET / FLASH_COST_PER_REQUEST).toInt() // ~233 Requests
        val MONTHLY_FLASH_LITE_CAPACITY = (MONTHLY_FLASH_LITE_BUDGET / FLASH_LITE_COST_PER_REQUEST).toInt() // ~375 Requests
        val MONTHLY_PERPLEXITY_CAPACITY = (MONTHLY_PERPLEXITY_BUDGET / PERPLEXITY_COST_PER_SEARCH).toInt() // ~1000 Searches

        // Request Tracking Keys
        private const val PREFS_NAME = "budget_tier_tracking"
        private const val KEY_FLASH_USAGE = "flash_usage"
        private const val KEY_FLASH_LITE_USAGE = "flash_lite_usage"
        private const val KEY_PERPLEXITY_USAGE = "perplexity_usage"
        private const val KEY_CURRENT_MONTH = "current_month"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Intelligente Modellauswahl basierend auf Budget-Optimierung
     */
    fun selectOptimalBudgetModel(
        taskType: TaskType,
        hasImage: Boolean,
    ): BudgetModelSelection {
        resetMonthlyCountersIfNeeded()

        val currentFlashUsage = getCurrentFlashUsage()
        val currentFlashLiteUsage = getCurrentFlashLiteUsage()
        val currentPerplexityUsage = getCurrentPerplexityUsage()

        return when {
            // 🖼️ MULTIMODAL TASKS - Immer Flash (beste Vision)
            hasImage -> {
                if (currentFlashUsage < MONTHLY_FLASH_CAPACITY) {
                    BudgetModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH,
                        reason = "Premium Bildanalyse (${currentFlashUsage + 1}/${MONTHLY_FLASH_CAPACITY} Flash Budget)",
                        estimatedCost = FLASH_COST_PER_REQUEST,
                        withinBudget = true,
                        qualityLevel = QualityLevel.PREMIUM,
                    )
                } else {
                    // Fallback wenn Flash-Budget aufgebraucht
                    BudgetModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH_LITE,
                        reason = "Flash-Budget erreicht, Fallback zu Flash-Lite (eingeschränkte Vision)",
                        estimatedCost = FLASH_LITE_COST_PER_REQUEST,
                        withinBudget = currentFlashLiteUsage < MONTHLY_FLASH_LITE_CAPACITY,
                        qualityLevel = QualityLevel.BUDGET,
                    )
                }
            }

            // 🔍 RESEARCH & AKTUELLE INFOS - Perplexity bevorzugt
            taskType.requiresCurrentInfo() -> {
                if (currentPerplexityUsage < MONTHLY_PERPLEXITY_CAPACITY) {
                    BudgetModelSelection(
                        provider = OptimalProvider.PERPLEXITY,
                        perplexityModel = ModelOptimizer.PerplexityModel.SONAR_BASIC,
                        reason = "Aktuelle Web-Informationen (${currentPerplexityUsage + 1}/${MONTHLY_PERPLEXITY_CAPACITY} Searches)",
                        estimatedCost = PERPLEXITY_COST_PER_SEARCH,
                        withinBudget = true,
                        qualityLevel = QualityLevel.SPECIALIZED,
                    )
                } else {
                    // Fallback zu Gemini wenn Perplexity-Budget aufgebraucht
                    selectGeminiForComplexTask(
                        taskType,
                        currentFlashUsage,
                        currentFlashLiteUsage,
                        "Perplexity-Budget erreicht, Fallback zu Gemini (ohne aktuelle Web-Daten)",
                    )
                }
            }

            // 🏋️ KOMPLEXE TASKS - Flash bevorzugt für beste Qualität
            taskType.isComplex() -> {
                selectGeminiForComplexTask(
                    taskType,
                    currentFlashUsage,
                    currentFlashLiteUsage,
                    "Komplexe Logik benötigt Premium-Qualität",
                )
            }

            // 💬 HÄUFIGE EINFACHE TASKS - Flash-Lite für Kosteneffizienz
            else -> {
                if (currentFlashLiteUsage < MONTHLY_FLASH_LITE_CAPACITY) {
                    BudgetModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH_LITE,
                        reason = "Kosteneffizient für häufige Tasks (${currentFlashLiteUsage + 1}/${MONTHLY_FLASH_LITE_CAPACITY} Budget)",
                        estimatedCost = FLASH_LITE_COST_PER_REQUEST,
                        withinBudget = true,
                        qualityLevel = QualityLevel.BUDGET,
                    )
                } else if (currentFlashUsage < MONTHLY_FLASH_CAPACITY) {
                    BudgetModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH,
                        reason = "Flash-Lite Budget erreicht, Upgrade zu Flash",
                        estimatedCost = FLASH_COST_PER_REQUEST,
                        withinBudget = true,
                        qualityLevel = QualityLevel.PREMIUM,
                    )
                } else {
                    BudgetModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH_LITE,
                        reason = "Alle Budgets erreicht - warte auf nächsten Monat",
                        estimatedCost = FLASH_LITE_COST_PER_REQUEST,
                        withinBudget = false,
                        qualityLevel = QualityLevel.OVER_BUDGET,
                    )
                }
            }
        }
    }

    private fun selectGeminiForComplexTask(
        taskType: TaskType,
        currentFlashUsage: Int,
        currentFlashLiteUsage: Int,
        baseReason: String,
    ): BudgetModelSelection {
        return if (currentFlashUsage < MONTHLY_FLASH_CAPACITY) {
            BudgetModelSelection(
                provider = OptimalProvider.GEMINI,
                geminiModel = ModelOptimizer.GeminiModel.FLASH,
                reason = "$baseReason - Flash Premium (${currentFlashUsage + 1}/${MONTHLY_FLASH_CAPACITY})",
                estimatedCost = FLASH_COST_PER_REQUEST,
                withinBudget = true,
                qualityLevel = QualityLevel.PREMIUM,
            )
        } else if (currentFlashLiteUsage < MONTHLY_FLASH_LITE_CAPACITY) {
            BudgetModelSelection(
                provider = OptimalProvider.GEMINI,
                geminiModel = ModelOptimizer.GeminiModel.FLASH_LITE,
                reason = "Flash-Budget erreicht, Flash-Lite Fallback (${currentFlashLiteUsage + 1}/${MONTHLY_FLASH_LITE_CAPACITY})",
                estimatedCost = FLASH_LITE_COST_PER_REQUEST,
                withinBudget = true,
                qualityLevel = QualityLevel.BUDGET,
            )
        } else {
            BudgetModelSelection(
                provider = OptimalProvider.GEMINI,
                geminiModel = ModelOptimizer.GeminiModel.FLASH_LITE,
                reason = "Alle Gemini-Budgets erreicht",
                estimatedCost = FLASH_LITE_COST_PER_REQUEST,
                withinBudget = false,
                qualityLevel = QualityLevel.OVER_BUDGET,
            )
        }
    }

    /**
     * Verfolge Budget-Nutzung
     */
    fun trackRequest(selection: BudgetModelSelection) {
        if (!selection.withinBudget) return

        when (selection.provider) {
            OptimalProvider.GEMINI -> {
                when (selection.geminiModel) {
                    ModelOptimizer.GeminiModel.FLASH -> incrementFlashUsage()
                    ModelOptimizer.GeminiModel.FLASH_LITE -> incrementFlashLiteUsage()
                    null -> {} // Shouldn't happen
                }
            }
            OptimalProvider.PERPLEXITY -> incrementPerplexityUsage()
        }
    }

    /**
     * Erhalte aktuellen Budget-Status
     */
    fun getBudgetStatus(): BudgetStatus {
        resetMonthlyCountersIfNeeded()

        val flashUsage = getCurrentFlashUsage()
        val flashLiteUsage = getCurrentFlashLiteUsage()
        val perplexityUsage = getCurrentPerplexityUsage()

        val flashSpent = flashUsage * FLASH_COST_PER_REQUEST
        val flashLiteSpent = flashLiteUsage * FLASH_LITE_COST_PER_REQUEST
        val perplexitySpent = perplexityUsage * PERPLEXITY_COST_PER_SEARCH

        return BudgetStatus(
            // Gemini Usage
            flashUsed = flashUsage,
            flashRemaining = (MONTHLY_FLASH_CAPACITY - flashUsage).coerceAtLeast(0),
            flashLiteUsed = flashLiteUsage,
            flashLiteRemaining = (MONTHLY_FLASH_LITE_CAPACITY - flashLiteUsage).coerceAtLeast(0),
            // Perplexity Usage
            perplexityUsed = perplexityUsage,
            perplexityRemaining = (MONTHLY_PERPLEXITY_CAPACITY - perplexityUsage).coerceAtLeast(0),
            // Budget Tracking
            geminiSpent = flashSpent + flashLiteSpent,
            geminiRemaining = (MONTHLY_GEMINI_BUDGET - (flashSpent + flashLiteSpent)).coerceAtLeast(0.0),
            perplexitySpent = perplexitySpent,
            perplexityBudgetRemaining = (MONTHLY_PERPLEXITY_BUDGET - perplexitySpent).coerceAtLeast(0.0),
            totalSpent = flashSpent + flashLiteSpent + perplexitySpent,
            totalRemaining =
                (TOTAL_MONTHLY_BUDGET - (flashSpent + flashLiteSpent + perplexitySpent)).coerceAtLeast(
                    0.0,
                ),
        )
    }

    /**
     * Budget-optimierte Empfehlungen
     */
    fun getBudgetRecommendations(): BudgetRecommendation {
        val status = getBudgetStatus()

        return BudgetRecommendation(
            priorityActions =
                buildList {
                    if (status.flashRemaining > 50) {
                        add(
                            "🎯 Premium Features nutzen: ${status.flashRemaining} Flash-Requests für komplexe Trainingspläne",
                        )
                    }
                    if (status.perplexityRemaining > 100) {
                        add("🔍 Research maximieren: ${status.perplexityRemaining} Perplexity-Searches für Trends")
                    }
                    if (status.flashLiteRemaining > 100) {
                        add("💬 Coaching aktivieren: ${status.flashLiteRemaining} Flash-Lite für tägliche Motivation")
                    }
                },
            budgetWarnings =
                buildList {
                    if (status.geminiRemaining < 1.0) {
                        add(
                            "⚠️ Gemini-Budget fast aufgebraucht (${String.format(
                                "%.2f",
                                status.geminiRemaining,
                            )}$ verbleibend)",
                        )
                    }
                    // Verwende Budgetrest (Dollar) statt verbleibender Suchanzahl
                    if (status.perplexityBudgetRemaining < 1.0) {
                        add(
                            "⚠️ Perplexity-Budget fast aufgebraucht (${String.format(
                                "%.2f",
                                status.perplexityBudgetRemaining,
                            )}$ verbleibend)",
                        )
                    }
                    if (status.totalRemaining < 2.0) {
                        add("🚨 Monatsbudget von $10 fast erreicht!")
                    }
                },
            optimizationTips =
                listOf(
                    "💡 Flash für Multimodal-Tasks (Bildanalyse) bevorzugen",
                    "💡 Perplexity für zeitkritische Fitness-Trends nutzen",
                    "💡 Flash-Lite für häufige Coaching-Nachrichten",
                    "💡 Budget-Verteilung: 70% Flash, 30% Flash-Lite optimal",
                ),
            projectedMonthlySpend = status.totalSpent * (30.0 / java.time.LocalDate.now().dayOfMonth),
        )
    }

    // --- PRIVATE HELPER METHODS ---

    private fun resetMonthlyCountersIfNeeded() {
        val currentMonth = LocalDate.now().monthValue
        val lastTrackedMonth = prefs.getInt(KEY_CURRENT_MONTH, -1)

        if (lastTrackedMonth != currentMonth) {
            prefs.edit()
                .putInt(KEY_FLASH_USAGE, 0)
                .putInt(KEY_FLASH_LITE_USAGE, 0)
                .putInt(KEY_PERPLEXITY_USAGE, 0)
                .putInt(KEY_CURRENT_MONTH, currentMonth)
                .apply()
        }
    }

    private fun getCurrentFlashUsage() = prefs.getInt(KEY_FLASH_USAGE, 0)

    private fun getCurrentFlashLiteUsage() = prefs.getInt(KEY_FLASH_LITE_USAGE, 0)

    private fun getCurrentPerplexityUsage() = prefs.getInt(KEY_PERPLEXITY_USAGE, 0)

    private fun incrementFlashUsage() {
        prefs.edit().putInt(KEY_FLASH_USAGE, getCurrentFlashUsage() + 1).apply()
    }

    private fun incrementFlashLiteUsage() {
        prefs.edit().putInt(KEY_FLASH_LITE_USAGE, getCurrentFlashLiteUsage() + 1).apply()
    }

    private fun incrementPerplexityUsage() {
        prefs.edit().putInt(KEY_PERPLEXITY_USAGE, getCurrentPerplexityUsage() + 1).apply()
    }
}

// Helper Extensions
private fun TaskType.isComplex(): Boolean =
    when (this) {
        TaskType.TRAINING_PLAN,
        TaskType.COMPLEX_PLAN_ANALYSIS,
        TaskType.PROGRESS_ANALYSIS,
        TaskType.RECIPE_WITH_IMAGE_GEN,
        -> true
        else -> false
    }

private fun TaskType.requiresCurrentInfo(): Boolean =
    when (this) {
        TaskType.RESEARCH_TRENDS,
        TaskType.SUPPLEMENT_RESEARCH,
        -> true
        else -> false
    }

enum class QualityLevel {
    PREMIUM, // Flash - beste Qualität
    BUDGET, // Flash-Lite - kosteneffizient
    SPECIALIZED, // Perplexity - aktuelle Infos
    OVER_BUDGET, // Budget überschritten
}

data class BudgetModelSelection(
    val provider: OptimalProvider,
    val geminiModel: ModelOptimizer.GeminiModel? = null,
    val perplexityModel: ModelOptimizer.PerplexityModel? = null,
    val reason: String,
    val estimatedCost: Double,
    val withinBudget: Boolean,
    val qualityLevel: QualityLevel,
)

data class BudgetStatus(
    val flashUsed: Int,
    val flashRemaining: Int,
    val flashLiteUsed: Int,
    val flashLiteRemaining: Int,
    val perplexityUsed: Int,
    val perplexityRemaining: Int,
    val geminiSpent: Double,
    val geminiRemaining: Double,
    val perplexitySpent: Double,
    val perplexityBudgetRemaining: Double,
    val totalSpent: Double,
    val totalRemaining: Double,
)

data class BudgetRecommendation(
    val priorityActions: List<String>,
    val budgetWarnings: List<String>,
    val optimizationTips: List<String>,
    val projectedMonthlySpend: Double,
)
