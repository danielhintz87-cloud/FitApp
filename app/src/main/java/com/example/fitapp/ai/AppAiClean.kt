package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import com.example.fitapp.application.di.AiDiContainer
import com.example.fitapp.data.prefs.ApiKeys
import com.example.fitapp.domain.entities.PlanRequest as DomainPlanRequest
import com.example.fitapp.domain.entities.RecipeRequest as DomainRecipeRequest
import com.example.fitapp.domain.entities.CaloriesEstimate as DomainCaloriesEstimate

/**
 * Clean Architecture version of AppAi
 * Maintains backward compatibility while using new architecture
 */
object AppAiClean {
    
    /**
     * Generate training plan using Clean Architecture
     */
    suspend fun planWithOptimalProvider(context: Context, req: PlanRequest): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte beide API-Schlüssel unter Einstellungen → API-Schlüssel eingeben."))
        }
        
        val container = AiDiContainer.getInstance(context)
        val domainRequest = DomainPlanRequest(
            goal = req.goal,
            weeks = req.weeks,
            sessionsPerWeek = req.sessionsPerWeek,
            minutesPerSession = req.minutesPerSession,
            equipment = req.equipment
        )
        return container.generateTrainingPlanUseCase.execute(domainRequest)
    }
    
    /**
     * Generate recipes using Clean Architecture
     */
    suspend fun recipesWithOptimalProvider(context: Context, req: RecipeRequest): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte beide API-Schlüssel unter Einstellungen → API-Schlüssel eingeben."))
        }
        
        val container = AiDiContainer.getInstance(context)
        val domainRequest = DomainRecipeRequest(
            preferences = req.preferences,
            diet = req.diet,
            count = req.count
        )
        return container.generateRecipesUseCase.execute(domainRequest)
    }
    
    /**
     * Estimate calories using Clean Architecture
     */
    suspend fun caloriesWithOptimalProvider(context: Context, bitmap: Bitmap, note: String = ""): Result<CaloriesEstimate> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte beide API-Schlüssel unter Einstellungen → API-Schlüssel eingeben."))
        }
        
        val container = AiDiContainer.getInstance(context)
        return container.estimateCaloriesUseCase.execute(bitmap, note).mapCatching { domainEstimate ->
            CaloriesEstimate(
                kcal = domainEstimate.kcal,
                confidence = domainEstimate.confidence,
                text = domainEstimate.text
            )
        }
    }
    
    /**
     * Parse shopping list using Clean Architecture
     */
    suspend fun parseShoppingListWithOptimalProvider(context: Context, spokenText: String): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte beide API-Schlüssel unter Einstellungen → API-Schlüssel eingeben."))
        }
        
        val container = AiDiContainer.getInstance(context)
        return container.parseShoppingListUseCase.execute(spokenText)
    }
    
    /**
     * Get simple AI calorie estimation for manual food entries using Clean Architecture
     */
    suspend fun estimateCaloriesForManualEntry(context: Context, foodDescription: String): Result<Int> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            return Result.failure(IllegalStateException("Beide API-Schlüssel erforderlich für Kalorienschätzung."))
        }
        
        val container = AiDiContainer.getInstance(context)
        return container.estimateCaloriesForManualEntryUseCase.execute(foodDescription)
    }
    
    /**
     * Generate daily workout steps using Clean Architecture
     */
    suspend fun generateDailyWorkoutSteps(context: Context, goal: String, minutes: Int, equipment: List<String>): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte beide API-Schlüssel unter Einstellungen → API-Schlüssel eingeben."))
        }
        
        val container = AiDiContainer.getInstance(context)
        return container.generateDailyWorkoutStepsUseCase.execute(goal, minutes, equipment)
    }
    
    /**
     * Get AI provider status for debugging including usage statistics
     */
    fun getProviderStatus(context: Context): String {
        val geminiKey = ApiKeys.getGeminiKey(context)
        val perplexityKey = ApiKeys.getPerplexityKey(context)
        val totalUsage = UsageTracker.getTotalUsageStats(context)
        
        return buildString {
            appendLine("AI Provider Status (Clean Architecture):")
            appendLine("- Gemini: ${if (geminiKey.isNotBlank()) "✓ Configured (${geminiKey.take(10)}...)" else "✗ Not configured"}")
            appendLine("- Perplexity: ${if (perplexityKey.isNotBlank()) "✓ Configured (${perplexityKey.take(10)}...)" else "✗ Not configured"}")
            appendLine()
            appendLine("Task Routing:")
            appendLine("- Multimodale Aufgaben (Bilder) → Gemini")
            appendLine("- Strukturierte Trainingspläne → Gemini")
            appendLine("- Schnelle Q&A, Web-Suche → Perplexity")
            appendLine("- Shopping List Parsing → Perplexity")
            appendLine()
            appendLine("Architecture: Clean Architecture with Use Cases & DI")
            appendLine("Usage Statistics:")
            appendLine("- Total Requests: ${totalUsage.totalRequests}")
            appendLine("- Total Tokens: ${totalUsage.totalTokens}")
            appendLine("- Estimated Cost: $${String.format("%.3f", totalUsage.totalEstimatedCost)}")
            appendLine("  - Gemini: ${totalUsage.geminiStats.requests} requests, ${totalUsage.geminiStats.tokens} tokens, $${String.format("%.3f", totalUsage.geminiStats.estimatedCost)}")
            appendLine("  - Perplexity: ${totalUsage.perplexityStats.requests} requests, ${totalUsage.perplexityStats.tokens} tokens, $${String.format("%.3f", totalUsage.perplexityStats.estimatedCost)}")
        }
    }
    
    /**
     * Reset usage statistics (useful for monthly budget tracking)
     */
    fun resetUsageStats(context: Context) {
        UsageTracker.resetUsageStats(context)
    }
    
    // Legacy API compatibility methods
    suspend fun plan(context: Context, req: PlanRequest) = planWithOptimalProvider(context, req)
    suspend fun recipes(context: Context, req: RecipeRequest) = recipesWithOptimalProvider(context, req)
    suspend fun calories(context: Context, bitmap: Bitmap, note: String = "") = caloriesWithOptimalProvider(context, bitmap, note)
}