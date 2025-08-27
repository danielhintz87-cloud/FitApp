package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.ApiKeys

object AppAi {
    private fun core(context: Context) = AiCore(context, AppDatabase.get(context).aiLogDao())

    suspend fun plan(context: Context, provider: AiProvider, req: PlanRequest) =
        core(context).generatePlan(provider, req)

    suspend fun recipes(context: Context, provider: AiProvider, req: RecipeRequest) =
        core(context).generateRecipes(provider, req)

    suspend fun calories(context: Context, provider: AiProvider, bitmap: Bitmap, note: String = "") =
        core(context).estimateCaloriesFromPhoto(provider, bitmap, note)

    /**
     * Automatically selects the best provider for training plan generation with fallback
     */
    suspend fun planWithOptimalProvider(context: Context, req: PlanRequest): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte unter Einstellungen → API-Schlüssel einen OpenAI API-Schlüssel eingeben."))
        }
        val availableProviders = getAvailableProviders(context)
        val optimal = AiCore.selectOptimalProvider(TaskType.TRAINING_PLAN, availableProviders)
        
        return tryWithFallback(context, optimal, TaskType.TRAINING_PLAN) { provider ->
            core(context).generatePlan(provider, req)
        }
    }

    /**
     * Automatically selects the best provider for recipe generation with fallback
     */
    suspend fun recipesWithOptimalProvider(context: Context, req: RecipeRequest): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte unter Einstellungen → API-Schlüssel einen OpenAI API-Schlüssel eingeben."))
        }
        val availableProviders = getAvailableProviders(context)
        val optimal = AiCore.selectOptimalProvider(TaskType.RECIPE_GENERATION, availableProviders)
        
        return tryWithFallback(context, optimal, TaskType.RECIPE_GENERATION) { provider ->
            core(context).generateRecipes(provider, req)
        }
    }

    /**
     * Automatically selects the best provider for calorie estimation with fallback
     */
    suspend fun caloriesWithOptimalProvider(context: Context, bitmap: Bitmap, note: String = ""): Result<CaloriesEstimate> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte unter Einstellungen → API-Schlüssel einen OpenAI API-Schlüssel eingeben."))
        }
        val availableProviders = getAvailableProviders(context)
        val optimal = AiCore.selectOptimalProvider(TaskType.CALORIE_ESTIMATION, availableProviders)
        
        return tryWithFallback(context, optimal, TaskType.CALORIE_ESTIMATION) { provider ->
            core(context).estimateCaloriesFromPhoto(provider, bitmap, note)
        }
    }

    /**
     * Automatically selects the best provider for shopping list parsing with fallback
     */
    suspend fun parseShoppingListWithOptimalProvider(context: Context, spokenText: String): Result<String> {
        if (!ApiKeys.isPrimaryProviderAvailable(context)) {
            val statusInfo = ApiKeys.getConfigurationStatus(context)
            return Result.failure(IllegalStateException("$statusInfo\n\nBitte unter Einstellungen → API-Schlüssel einen OpenAI API-Schlüssel eingeben."))
        }
        val availableProviders = getAvailableProviders(context)
        val optimal = AiCore.selectOptimalProvider(TaskType.SHOPPING_LIST_PARSING, availableProviders)
        
        return tryWithFallback(context, optimal, TaskType.SHOPPING_LIST_PARSING) { provider ->
            core(context).parseShoppingList(provider, spokenText)
        }
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

    private suspend fun <T> tryWithFallback(
        context: Context,
        primary: AiProvider,
        @Suppress("UNUSED_PARAMETER") taskType: TaskType,
        operation: suspend (AiProvider) -> Result<T>
    ): Result<T> {
        val errors = mutableListOf<String>()
        
        // Try primary provider
        val primaryResult = operation(primary)
        if (primaryResult.isSuccess) return primaryResult
        
        errors.add("$primary: ${primaryResult.exceptionOrNull()?.message}")

        // Try fallback providers
        val fallbackChain = AiCore.getFallbackChain(primary)
        val availableProviders = getAvailableProviders(context)
        
        for (fallbackProvider in fallbackChain) {
            if (fallbackProvider in availableProviders) {
                val fallbackResult = operation(fallbackProvider)
                if (fallbackResult.isSuccess) return fallbackResult
                errors.add("$fallbackProvider: ${fallbackResult.exceptionOrNull()?.message}")
            }
        }

        // If all failed, return a comprehensive error
        val allErrors = errors.joinToString("\n- ", prefix = "Alle AI-Provider fehlgeschlagen:\n- ")
        return Result.failure(Exception(allErrors))
    }

    private fun getAvailableProviders(context: Context): List<AiProvider> {
        val providers = mutableListOf<AiProvider>()
        
        val openAiKey = ApiKeys.getOpenAiKey(context)
        val geminiKey = ApiKeys.getGeminiKey(context)
        val deepSeekKey = ApiKeys.getDeepSeekKey(context)
        
        if (openAiKey.isNotBlank()) {
            providers.add(AiProvider.OpenAI)
        }
        if (geminiKey.isNotBlank()) {
            providers.add(AiProvider.Gemini)
        }
        if (deepSeekKey.isNotBlank()) {
            providers.add(AiProvider.DeepSeek)
        }
        
        return providers
    }
    
    /**
     * Get detailed provider status for debugging
     */
    fun getProviderStatus(context: Context): String {
        val openAiKey = ApiKeys.getOpenAiKey(context)
        val geminiKey = ApiKeys.getGeminiKey(context)
        val deepSeekKey = ApiKeys.getDeepSeekKey(context)
        
        return buildString {
            appendLine("AI Provider Status:")
            appendLine("- OpenAI (Primär): ${if (openAiKey.isNotBlank()) "✓ Configured (${openAiKey.take(10)}...)" else "✗ Not configured"}")
            appendLine("- Gemini (Backup): ${if (geminiKey.isNotBlank()) "✓ Configured (${geminiKey.take(10)}...)" else "✗ Not configured"}")
            appendLine("- DeepSeek (Backup): ${if (deepSeekKey.isNotBlank()) "✓ Configured (${deepSeekKey.take(10)}...)" else "✗ Not configured"}")
            appendLine("Verfügbare Provider: ${getAvailableProviders(context).joinToString()}")
            appendLine("\nEmpfehlung: Konfigurieren Sie mindestens OpenAI für optimale Funktionalität.")
        }
    }
}