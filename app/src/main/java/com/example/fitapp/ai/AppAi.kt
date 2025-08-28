package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.ApiKeys

object AppAi {
    private fun core(context: Context) = AiCore(context, AppDatabase.get(context).aiLogDao())

    suspend fun plan(context: Context, req: PlanRequest) =
        core(context).generatePlan(req)

    suspend fun recipes(context: Context, req: RecipeRequest) =
        core(context).generateRecipes(req)

    suspend fun calories(context: Context, bitmap: Bitmap, note: String = "") =
        core(context).estimateCaloriesFromPhoto(bitmap, note)

    /**
     * Generate training plan using optimal provider routing
     */
    suspend fun planWithOptimalProvider(context: Context, req: PlanRequest): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte beide API-Schlüssel unter Einstellungen → API-Schlüssel eingeben."))
        }
        
        return core(context).generatePlan(req)
    }

    /**
     * Generate recipes using optimal provider routing
     */
    suspend fun recipesWithOptimalProvider(context: Context, req: RecipeRequest): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte beide API-Schlüssel unter Einstellungen → API-Schlüssel eingeben."))
        }
        
        return core(context).generateRecipes(req)
    }

    /**
     * Estimate calories using optimal provider routing
     */
    suspend fun caloriesWithOptimalProvider(context: Context, bitmap: Bitmap, note: String = ""): Result<CaloriesEstimate> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte beide API-Schlüssel unter Einstellungen → API-Schlüssel eingeben."))
        }
        
        return core(context).estimateCaloriesFromPhoto(bitmap, note)
    }

    /**
     * Parse shopping list using optimal provider routing
     */
    suspend fun parseShoppingListWithOptimalProvider(context: Context, spokenText: String): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte beide API-Schlüssel unter Einstellungen → API-Schlüssel eingeben."))
        }
        
        return core(context).parseShoppingList(spokenText)
    }

    /**
     * Get simple AI calorie estimation for manual food entries
     */
    suspend fun estimateCaloriesForManualEntry(context: Context, foodDescription: String): Result<Int> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            return Result.failure(IllegalStateException("Beide API-Schlüssel erforderlich für Kalorienschätzung."))
        }
        
        return try {
            val prompt = "Schätze die Kalorien für: '$foodDescription'. Antworte nur mit einer Zahl (kcal) ohne zusätzlichen Text."
            // Use Perplexity for quick factual queries
            val result = core(context).callText(AiProvider.Perplexity, prompt)
            
            result.mapCatching { response ->
                // Extract number from response
                val kcalRegex = Regex("\\d+")
                val kcal = kcalRegex.find(response)?.value?.toIntOrNull() ?: 0
                if (kcal == 0) throw IllegalArgumentException("Keine gültige Kalorienzahl gefunden")
                kcal
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get AI provider status for debugging including usage statistics
     */
    fun getProviderStatus(context: Context): String {
        val geminiKey = ApiKeys.getGeminiKey(context)
        val perplexityKey = ApiKeys.getPerplexityKey(context)
        val totalUsage = UsageTracker.getTotalUsageStats(context)
        
        return buildString {
            appendLine("AI Provider Status:")
            appendLine("- Gemini: ${if (geminiKey.isNotBlank()) "✓ Configured (${geminiKey.take(10)}...)" else "✗ Not configured"}")
            appendLine("- Perplexity: ${if (perplexityKey.isNotBlank()) "✓ Configured (${perplexityKey.take(10)}...)" else "✗ Not configured"}")
            appendLine()
            appendLine("Task Routing:")
            appendLine("- Multimodale Aufgaben (Bilder) → Gemini")
            appendLine("- Strukturierte Trainingspläne → Gemini")
            appendLine("- Schnelle Q&A, Web-Suche → Perplexity")
            appendLine("- Shopping List Parsing → Perplexity")
            appendLine()
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
}