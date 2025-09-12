package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.domain.entities.*

object AppAi {
    // Keep original core() method for legacy compatibility if needed
    private fun core(context: Context) = AiCore(context, AppDatabase.get(context).aiLogDao())

    suspend fun plan(
        context: Context,
        req: PlanRequest,
    ): Result<String> {
        return planWithOptimalProvider(context, req)
    }

    suspend fun recipes(
        context: Context,
        req: RecipeRequest,
    ) = AppAiClean.recipesWithOptimalProvider(context, req)

    suspend fun calories(
        context: Context,
        bitmap: Bitmap,
        note: String = "",
    ) = AppAiClean.caloriesWithOptimalProvider(context, bitmap, note)

    /**
     * Generate training plan using optimal provider routing
     */
    suspend fun planWithOptimalProvider(
        context: Context,
        req: PlanRequest,
    ): Result<String> {
        return AppAiClean.planWithOptimalProvider(context, req)
    }

    /**
     * Generate recipes using optimal provider routing
     */
    suspend fun recipesWithOptimalProvider(
        context: Context,
        req: RecipeRequest,
    ): Result<String> {
        return AppAiClean.recipesWithOptimalProvider(context, req)
    }

    /**
     * Estimate calories using optimal provider routing
     */
    suspend fun caloriesWithOptimalProvider(
        context: Context,
        bitmap: Bitmap,
        note: String = "",
    ): Result<CaloriesEstimate> {
        return AppAiClean.caloriesWithOptimalProvider(context, bitmap, note)
    }

    /**
     * Parse shopping list using optimal provider routing
     */
    suspend fun parseShoppingListWithOptimalProvider(
        context: Context,
        spokenText: String,
    ): Result<String> {
        return AppAiClean.parseShoppingListWithOptimalProvider(context, spokenText)
    }

    /**
     * Get simple AI calorie estimation for manual food entries
     */
    suspend fun estimateCaloriesForManualEntry(
        context: Context,
        foodDescription: String,
    ): Result<Int> {
        return AppAiClean.estimateCaloriesForManualEntry(context, foodDescription)
    }

    /**
     * Get AI provider status for debugging including usage statistics
     */
    fun getProviderStatus(context: Context): String {
        return AppAiClean.getProviderStatus(context)
    }

    /**
     * Generate daily workout steps using optimal provider routing
     */
    suspend fun generateDailyWorkoutSteps(
        context: Context,
        goal: String,
        minutes: Int,
        equipment: List<String>,
    ): Result<String> {
        return AppAiClean.generateDailyWorkoutSteps(context, goal, minutes, equipment)
    }

    /**
     * Get personalized recommendations using optimal provider routing
     */
    suspend fun getPersonalizedRecommendations(
        context: Context,
        userContext: UserContext,
    ): Result<AIPersonalTrainerResponse> {
        return AppAiClean.getPersonalizedRecommendations(context, userContext)
    }

    /**
     * Generate personalized workout using optimal provider routing
     */
    suspend fun generatePersonalizedWorkout(
        context: Context,
        request: AIPersonalTrainerRequest,
    ): Result<WorkoutPlan> {
        return AppAiClean.generatePersonalizedWorkout(context, request)
    }

    /**
     * Generate nutrition advice using optimal provider routing
     */
    suspend fun generateNutritionAdvice(
        context: Context,
        userProfile: UserProfile,
        goals: List<String>,
    ): Result<PersonalizedMealPlan> {
        return AppAiClean.generateNutritionAdvice(context, userProfile, goals)
    }

    /**
     * Analyze progress using optimal provider routing
     */
    suspend fun analyzeProgress(
        context: Context,
        progressData: List<WeightEntry>,
        userProfile: UserProfile,
    ): Result<ProgressAnalysis> {
        return AppAiClean.analyzeProgress(context, progressData, userProfile)
    }

    /**
     * Generate motivation using optimal provider routing
     */
    suspend fun generateMotivation(
        context: Context,
        userProfile: UserProfile,
        progressData: List<WeightEntry>,
    ): Result<MotivationalMessage> {
        return AppAiClean.generateMotivation(context, userProfile, progressData)
    }

    /**
     * Reset usage statistics (useful for monthly budget tracking)
     */
    fun resetUsageStats(context: Context) {
        AppAiClean.resetUsageStats(context)
    }
}
