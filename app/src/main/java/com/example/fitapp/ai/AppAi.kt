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
        val optimal = AiCore.selectOptimalProvider(TaskType.CALORIE_ESTIMATION, availableProviders)
        
        return tryWithFallback(context, optimal, TaskType.CALORIE_ESTIMATION) { provider ->
            core(context).estimateCaloriesFromPhoto(provider, bitmap, note)
        }
    }

    private suspend fun <T> tryWithFallback(
        context: Context,
        primary: AiProvider,
        @Suppress("UNUSED_PARAMETER") taskType: TaskType,
        operation: suspend (AiProvider) -> Result<T>
    ): Result<T> {
        // Try primary provider
        val primaryResult = operation(primary)
        if (primaryResult.isSuccess) return primaryResult

        // Try fallback providers
        val fallbackChain = AiCore.getFallbackChain(primary)
        val availableProviders = getAvailableProviders(context)
        
        for (fallbackProvider in fallbackChain) {
            if (fallbackProvider in availableProviders) {
                val fallbackResult = operation(fallbackProvider)
                if (fallbackResult.isSuccess) return fallbackResult
            }
        }

        // If all failed, return the original error
        return primaryResult
    }

    private fun getAvailableProviders(context: Context): List<AiProvider> {
        val providers = mutableListOf<AiProvider>()
        
        if (ApiKeys.getOpenAiKey(context).isNotBlank()) {
            providers.add(AiProvider.OpenAI)
        }
        if (ApiKeys.getGeminiKey(context).isNotBlank()) {
            providers.add(AiProvider.Gemini)
        }
        if (ApiKeys.getDeepSeekKey(context).isNotBlank()) {
            providers.add(AiProvider.DeepSeek)
        }
        if (ApiKeys.getClaudeKey(context).isNotBlank()) {
            providers.add(AiProvider.Claude)
        }
        
        return providers
    }
}