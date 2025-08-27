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
        val availableProviders = getAvailableProviders(context)
        if (availableProviders.isEmpty()) {
            val statusInfo = getProviderStatus(context)
            return Result.failure(IllegalStateException("Keine AI-Provider konfiguriert.\n\n$statusInfo\n\nBitte unter Einstellungen → API-Schlüssel mindestens einen API-Schlüssel eingeben."))
        }
        val optimal = AiCore.selectOptimalProvider(TaskType.TRAINING_PLAN, availableProviders)
        
        return tryWithFallback(context, optimal, TaskType.TRAINING_PLAN) { provider ->
            core(context).generatePlan(provider, req)
        }
    }

    /**
     * Automatically selects the best provider for recipe generation with fallback
     */
    suspend fun recipesWithOptimalProvider(context: Context, req: RecipeRequest): Result<String> {
        val availableProviders = getAvailableProviders(context)
        if (availableProviders.isEmpty()) {
            val statusInfo = getProviderStatus(context)
            return Result.failure(IllegalStateException("Keine AI-Provider konfiguriert.\n\n$statusInfo\n\nBitte unter Einstellungen → API-Schlüssel mindestens einen API-Schlüssel eingeben."))
        }
        val optimal = AiCore.selectOptimalProvider(TaskType.RECIPE_GENERATION, availableProviders)
        
        return tryWithFallback(context, optimal, TaskType.RECIPE_GENERATION) { provider ->
            core(context).generateRecipes(provider, req)
        }
    }

    /**
     * Automatically selects the best provider for calorie estimation with fallback
     */
    suspend fun caloriesWithOptimalProvider(context: Context, bitmap: Bitmap, note: String = ""): Result<CaloriesEstimate> {
        val availableProviders = getAvailableProviders(context)
        if (availableProviders.isEmpty()) {
            return Result.failure(IllegalStateException("Keine AI-Provider konfiguriert. Bitte unter Einstellungen → API-Schlüssel mindestens einen API-Schlüssel eingeben."))
        }
        val optimal = AiCore.selectOptimalProvider(TaskType.CALORIE_ESTIMATION, availableProviders)
        
        return tryWithFallback(context, optimal, TaskType.CALORIE_ESTIMATION) { provider ->
            core(context).estimateCaloriesFromPhoto(provider, bitmap, note)
        }
    }

    /**
     * Automatically selects the best provider for shopping list parsing with fallback
     */
    suspend fun parseShoppingListWithOptimalProvider(context: Context, spokenText: String): Result<String> {
        val availableProviders = getAvailableProviders(context)
        if (availableProviders.isEmpty()) {
            return Result.failure(IllegalStateException("Keine AI-Provider konfiguriert. Bitte unter Einstellungen → API-Schlüssel mindestens einen API-Schlüssel eingeben."))
        }
        val optimal = AiCore.selectOptimalProvider(TaskType.SHOPPING_LIST_PARSING, availableProviders)
        
        return tryWithFallback(context, optimal, TaskType.SHOPPING_LIST_PARSING) { provider ->
            core(context).parseShoppingList(provider, spokenText)
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
            appendLine("- OpenAI: ${if (openAiKey.isNotBlank()) "✓ Configured (${openAiKey.take(10)}...)" else "✗ Not configured"}")
            appendLine("- Gemini: ${if (geminiKey.isNotBlank()) "✓ Configured (${geminiKey.take(10)}...)" else "✗ Not configured"}")
            appendLine("- DeepSeek: ${if (deepSeekKey.isNotBlank()) "✓ Configured (${deepSeekKey.take(10)}...)" else "✗ Not configured"}")
            appendLine("Available providers: ${getAvailableProviders(context).joinToString()}")
        }
    }
}