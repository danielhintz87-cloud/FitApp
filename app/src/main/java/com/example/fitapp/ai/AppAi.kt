package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.ApiKeys

object AppAi {
    private fun core(context: Context) = AiCore(context, AppDatabase.get(context).aiLogDao())

    suspend fun plan(context: Context, req: PlanRequest) =
        core(context).generatePlan(AiProvider.OpenAI, req)

    suspend fun recipes(context: Context, req: RecipeRequest) =
        core(context).generateRecipes(AiProvider.OpenAI, req)

    suspend fun calories(context: Context, bitmap: Bitmap, note: String = "") =
        core(context).estimateCaloriesFromPhoto(AiProvider.OpenAI, bitmap, note)

    /**
     * Generate training plan using OpenAI
     */
    suspend fun planWithOptimalProvider(context: Context, req: PlanRequest): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte unter Einstellungen → API-Schlüssel einen OpenAI API-Schlüssel eingeben."))
        }
        
        return core(context).generatePlan(AiProvider.OpenAI, req)
    }

    /**
     * Generate recipes using OpenAI
     */
    suspend fun recipesWithOptimalProvider(context: Context, req: RecipeRequest): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte unter Einstellungen → API-Schlüssel einen OpenAI API-Schlüssel eingeben."))
        }
        
        return core(context).generateRecipes(AiProvider.OpenAI, req)
    }

    /**
     * Estimate calories using OpenAI
     */
    suspend fun caloriesWithOptimalProvider(context: Context, bitmap: Bitmap, note: String = ""): Result<CaloriesEstimate> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte unter Einstellungen → API-Schlüssel einen OpenAI API-Schlüssel eingeben."))
        }
        
        return core(context).estimateCaloriesFromPhoto(AiProvider.OpenAI, bitmap, note)
    }

    /**
     * Parse shopping list using OpenAI
     */
    suspend fun parseShoppingListWithOptimalProvider(context: Context, spokenText: String): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte unter Einstellungen → API-Schlüssel einen OpenAI API-Schlüssel eingeben."))
        }
        
        return core(context).parseShoppingList(AiProvider.OpenAI, spokenText)
    }

    /**
     * Get simple AI calorie estimation for manual food entries
     */
    suspend fun estimateCaloriesForManualEntry(context: Context, foodDescription: String): Result<Int> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            return Result.failure(IllegalStateException("OpenAI API-Schlüssel erforderlich für Kalorienschätzung."))
        }
        
        return try {
            val prompt = "Schätze die Kalorien für: '$foodDescription'. Antworte nur mit einer Zahl (kcal) ohne zusätzlichen Text."
            val result = core(context).callText(AiProvider.OpenAI, prompt)
            
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
     * Get OpenAI provider status for debugging
     */
    fun getProviderStatus(context: Context): String {
        val openAiKey = ApiKeys.getOpenAiKey(context)
        
        return buildString {
            appendLine("AI Provider Status:")
            appendLine("- OpenAI: ${if (openAiKey.isNotBlank()) "✓ Configured (${openAiKey.take(10)}...)" else "✗ Not configured"}")
            appendLine("\nNur OpenAI wird als AI-Provider unterstützt.")
        }
    }
}