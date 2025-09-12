package com.example.fitapp.infrastructure.providers

import com.example.fitapp.domain.entities.TaskType
import java.time.LocalDate

/**
 * Free Tier Optimizer für maximale Nutzung der kostenlosen Gemini-Limits
 *
 * Gemini Free Tier Limits (2025):
 * - Flash-Lite: 15 RPM, 250K TPM, 1,000 RPD (Requests per Day)
 * - Flash: 10 RPM, 250K TPM, 250 RPD
 * - Bildanalyse: 100 Bilder pro Tag
 * - Perplexity Budget: $5/Monat (~1,000 Searches)
 */
class FreeTierOptimizer(private val context: android.content.Context) {
    companion object {
        // Gemini Free Tier Daily Limits
        const val FLASH_LITE_DAILY_LIMIT = 1000
        const val FLASH_DAILY_LIMIT = 250
        const val IMAGE_DAILY_LIMIT = 100

        // Rate Limits (Requests per Minute)
        const val FLASH_LITE_RPM = 15
        const val FLASH_RPM = 10

        // Perplexity Budget Management
        const val MONTHLY_SEARCH_BUDGET = 1000 // $5 Budget ≈ 1000 Searches
        const val DAILY_SEARCH_ALLOWANCE = 33 // 1000/30 days

        // Request Tracking Keys
        private const val PREFS_NAME = "free_tier_tracking"
        private const val KEY_FLASH_LITE_COUNT = "flash_lite_count"
        private const val KEY_FLASH_COUNT = "flash_count"
        private const val KEY_IMAGE_COUNT = "image_count"
        private const val KEY_PERPLEXITY_COUNT = "perplexity_count"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)

    /**
     * Intelligente Modellauswahl basierend auf Free Tier Limits und aktueller Nutzung
     */
    fun selectOptimalFreeModel(
        taskType: TaskType,
        hasImage: Boolean,
    ): FreeTierModelSelection {
        resetDailyCountersIfNeeded()

        val currentFlashLiteCount = getCurrentFlashLiteCount()
        val currentFlashCount = getCurrentFlashCount()
        val currentImageCount = getCurrentImageCount()
        val currentPerplexityCount = getCurrentPerplexityCount()

        return when {
            // 🖼️ BILDANALYSE - Flash erforderlich
            hasImage -> {
                if (currentImageCount < IMAGE_DAILY_LIMIT && currentFlashCount < FLASH_DAILY_LIMIT) {
                    FreeTierModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH,
                        reason = "Bildanalyse kostenlos (${currentImageCount + 1}/$IMAGE_DAILY_LIMIT Bilder heute)",
                        costEstimate = 0.0,
                        withinLimits = true,
                    )
                } else {
                    FreeTierModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH,
                        reason = "Bildanalyse-Limit erreicht ($currentImageCount/$IMAGE_DAILY_LIMIT) - Fallback zu Flash",
                        costEstimate = 0.15, // Estimated cost if upgrading
                        withinLimits = false,
                    )
                }
            }

            // 🏋️ KOMPLEXE TRAININGSPLÄNE - Flash bevorzugt wenn Budget da
            taskType == TaskType.TRAINING_PLAN || taskType == TaskType.COMPLEX_PLAN_ANALYSIS -> {
                if (currentFlashCount < FLASH_DAILY_LIMIT) {
                    FreeTierModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH,
                        reason = "Komplexer Plan kostenlos mit Flash (${currentFlashCount + 1}/$FLASH_DAILY_LIMIT heute)",
                        costEstimate = 0.0,
                        withinLimits = true,
                    )
                } else if (currentFlashLiteCount < FLASH_LITE_DAILY_LIMIT) {
                    FreeTierModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH_LITE,
                        reason = "Flash-Limit erreicht, Fallback zu Flash-Lite (${currentFlashLiteCount + 1}/$FLASH_LITE_DAILY_LIMIT)",
                        costEstimate = 0.0,
                        withinLimits = true,
                    )
                } else {
                    FreeTierModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH_LITE,
                        reason = "Alle Gemini-Limits erreicht - Upgrade empfohlen",
                        costEstimate = 0.05,
                        withinLimits = false,
                    )
                }
            }

            // 🔍 RESEARCH & TRENDS - Perplexity wenn Budget da
            taskType == TaskType.RESEARCH_TRENDS || taskType == TaskType.SUPPLEMENT_RESEARCH -> {
                if (currentPerplexityCount < DAILY_SEARCH_ALLOWANCE) {
                    FreeTierModelSelection(
                        provider = OptimalProvider.PERPLEXITY,
                        perplexityModel = ModelOptimizer.PerplexityModel.SONAR_BASIC,
                        reason = "Research im $5 Budget (${currentPerplexityCount + 1}/$DAILY_SEARCH_ALLOWANCE heute)",
                        costEstimate = 0.17, // ~$5/30 days
                        withinLimits = true,
                    )
                } else if (currentFlashLiteCount < FLASH_LITE_DAILY_LIMIT) {
                    FreeTierModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH_LITE,
                        reason = "Perplexity-Budget erreicht, Fallback zu Flash-Lite (kostenlos)",
                        costEstimate = 0.0,
                        withinLimits = true,
                    )
                } else {
                    FreeTierModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH_LITE,
                        reason = "Alle Budgets erreicht - Limits abwarten",
                        costEstimate = 0.0,
                        withinLimits = false,
                    )
                }
            }

            // 💬 STANDARD TEXT-TASKS - Flash-Lite optimal
            else -> {
                if (currentFlashLiteCount < FLASH_LITE_DAILY_LIMIT) {
                    FreeTierModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH_LITE,
                        reason = "Standard-Text kostenlos (${currentFlashLiteCount + 1}/$FLASH_LITE_DAILY_LIMIT heute)",
                        costEstimate = 0.0,
                        withinLimits = true,
                    )
                } else if (currentFlashCount < FLASH_DAILY_LIMIT) {
                    FreeTierModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH,
                        reason = "Flash-Lite-Limit erreicht, Fallback zu Flash (kostenlos)",
                        costEstimate = 0.0,
                        withinLimits = true,
                    )
                } else {
                    FreeTierModelSelection(
                        provider = OptimalProvider.GEMINI,
                        geminiModel = ModelOptimizer.GeminiModel.FLASH_LITE,
                        reason = "Alle kostenlosen Limits erreicht",
                        costEstimate = 0.02,
                        withinLimits = false,
                    )
                }
            }
        }
    }

    /**
     * Verfolge Request-Nutzung für Free Tier Management
     */
    fun trackRequest(selection: FreeTierModelSelection) {
        when (selection.provider) {
            OptimalProvider.GEMINI -> {
                when (selection.geminiModel) {
                    ModelOptimizer.GeminiModel.FLASH_LITE -> incrementFlashLiteCount()
                    ModelOptimizer.GeminiModel.FLASH -> incrementFlashCount()
                    null -> {} // Shouldn't happen
                }
            }
            OptimalProvider.PERPLEXITY -> incrementPerplexityCount()
        }
    }

    /**
     * Verfolge Bildanalyse separat
     */
    fun trackImageRequest() {
        incrementImageCount()
    }

    /**
     * Erhalte aktuellen Free Tier Status
     */
    fun getFreeTierStatus(): FreeTierStatus {
        resetDailyCountersIfNeeded()

        val flashLiteCount = getCurrentFlashLiteCount()
        val flashCount = getCurrentFlashCount()
        val imageCount = getCurrentImageCount()
        val perplexityCount = getCurrentPerplexityCount()

        return FreeTierStatus(
            flashLiteUsed = flashLiteCount,
            flashLiteRemaining = (FLASH_LITE_DAILY_LIMIT - flashLiteCount).coerceAtLeast(0),
            flashUsed = flashCount,
            flashRemaining = (FLASH_DAILY_LIMIT - flashCount).coerceAtLeast(0),
            imageUsed = imageCount,
            imageRemaining = (IMAGE_DAILY_LIMIT - imageCount).coerceAtLeast(0),
            perplexityUsed = perplexityCount,
            perplexityRemaining = (DAILY_SEARCH_ALLOWANCE - perplexityCount).coerceAtLeast(0),
            estimatedMonthlyCost = calculateEstimatedMonthlyCost(),
        )
    }

    /**
     * Smart Request Distribution für optimale Free Tier Nutzung
     */
    fun getRecommendedDistribution(): FreeTierRecommendation {
        val status = getFreeTierStatus()

        return FreeTierRecommendation(
            priorityTasks =
                buildList {
                    if (status.imageRemaining > 0) {
                        add("📸 Bildanalyse: ${status.imageRemaining} kostenlose Bilder verfügbar")
                    }
                    if (status.flashRemaining > 0) {
                        add("🏋️ Komplexe Pläne: ${status.flashRemaining} Flash-Requests verfügbar")
                    }
                    if (status.flashLiteRemaining > 0) {
                        add("💬 Coaching: ${status.flashLiteRemaining} Flash-Lite-Requests verfügbar")
                    }
                    if (status.perplexityRemaining > 0) {
                        add("🔍 Research: ${status.perplexityRemaining} Perplexity-Searches im Budget")
                    }
                },
            warningsIfAny =
                buildList {
                    if (status.flashLiteRemaining < 100) {
                        add("⚠️ Flash-Lite bald erschöpft (${status.flashLiteRemaining} verbleibend)")
                    }
                    if (status.imageRemaining < 10) {
                        add("⚠️ Bildanalyse-Limit fast erreicht (${status.imageRemaining} verbleibend)")
                    }
                    if (status.perplexityRemaining < 5) {
                        add("⚠️ Perplexity-Budget fast aufgebraucht (${status.perplexityRemaining} verbleibend)")
                    }
                },
            optimizationTips =
                listOf(
                    "💡 Nutze Flash-Lite für einfache Coaching-Texte (1000/Tag kostenlos)",
                    "💡 Spare Flash für komplexe Trainingspläne (250/Tag kostenlos)",
                    "💡 Bildanalyse strategisch einsetzen (100/Tag kostenlos)",
                    "💡 Perplexity nur für High-Value Research ($5 Budget)",
                ),
        )
    }

    // --- PRIVATE HELPER METHODS ---

    private fun resetDailyCountersIfNeeded() {
        val today = LocalDate.now().toString()
        val lastReset = prefs.getString(KEY_LAST_RESET_DATE, "")

        if (lastReset != today) {
            prefs.edit()
                .putInt(KEY_FLASH_LITE_COUNT, 0)
                .putInt(KEY_FLASH_COUNT, 0)
                .putInt(KEY_IMAGE_COUNT, 0)
                .putString(KEY_LAST_RESET_DATE, today)
                .apply()
        }
    }

    private fun getCurrentFlashLiteCount() = prefs.getInt(KEY_FLASH_LITE_COUNT, 0)

    private fun getCurrentFlashCount() = prefs.getInt(KEY_FLASH_COUNT, 0)

    private fun getCurrentImageCount() = prefs.getInt(KEY_IMAGE_COUNT, 0)

    private fun getCurrentPerplexityCount() = prefs.getInt(KEY_PERPLEXITY_COUNT, 0)

    private fun incrementFlashLiteCount() {
        prefs.edit().putInt(KEY_FLASH_LITE_COUNT, getCurrentFlashLiteCount() + 1).apply()
    }

    private fun incrementFlashCount() {
        prefs.edit().putInt(KEY_FLASH_COUNT, getCurrentFlashCount() + 1).apply()
    }

    private fun incrementImageCount() {
        prefs.edit().putInt(KEY_IMAGE_COUNT, getCurrentImageCount() + 1).apply()
    }

    private fun incrementPerplexityCount() {
        prefs.edit().putInt(KEY_PERPLEXITY_COUNT, getCurrentPerplexityCount() + 1).apply()
    }

    private fun calculateEstimatedMonthlyCost(): Double {
        // Perplexity: $5/Monat Budget
        return 5.0
    }
}

data class FreeTierModelSelection(
    val provider: OptimalProvider,
    val geminiModel: ModelOptimizer.GeminiModel? = null,
    val perplexityModel: ModelOptimizer.PerplexityModel? = null,
    val reason: String,
    val costEstimate: Double,
    val withinLimits: Boolean,
)

data class FreeTierStatus(
    val flashLiteUsed: Int,
    val flashLiteRemaining: Int,
    val flashUsed: Int,
    val flashRemaining: Int,
    val imageUsed: Int,
    val imageRemaining: Int,
    val perplexityUsed: Int,
    val perplexityRemaining: Int,
    val estimatedMonthlyCost: Double,
)

data class FreeTierRecommendation(
    val priorityTasks: List<String>,
    val warningsIfAny: List<String>,
    val optimizationTips: List<String>,
)
