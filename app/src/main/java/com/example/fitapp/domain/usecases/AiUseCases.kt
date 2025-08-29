package com.example.fitapp.domain.usecases

import android.graphics.Bitmap
import com.example.fitapp.domain.entities.*

/**
 * Use case interfaces defining business operations
 * Pure domain logic without implementation details
 */

interface GenerateTrainingPlanUseCase {
    suspend fun execute(request: PlanRequest): Result<String>
}

interface GenerateRecipesUseCase {
    suspend fun execute(request: RecipeRequest): Result<String>
}

interface EstimateCaloriesUseCase {
    suspend fun execute(bitmap: Bitmap, note: String = ""): Result<CaloriesEstimate>
}

interface ParseShoppingListUseCase {
    suspend fun execute(spokenText: String): Result<String>
}

interface EstimateCaloriesForManualEntryUseCase {
    suspend fun execute(foodDescription: String): Result<Int>
}

interface GenerateDailyWorkoutStepsUseCase {
    suspend fun execute(goal: String, minutes: Int, equipment: List<String>): Result<String>
}