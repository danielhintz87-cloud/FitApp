package com.example.fitapp.domain.entities

/**
 * Domain entities for AI functionality
 * Pure business objects with no dependencies on Android or frameworks
 */

enum class AiProvider { Gemini, Perplexity }

enum class TaskType {
    TRAINING_PLAN,
    CALORIE_ESTIMATION,
    RECIPE_GENERATION,
    SHOPPING_LIST_PARSING
}

data class PlanRequest(
    val goal: String,
    val weeks: Int = 12,
    val sessionsPerWeek: Int,
    val minutesPerSession: Int,
    val equipment: List<String> = emptyList()
)

data class RecipeRequest(
    val preferences: String,
    val diet: String,
    val count: Int = 10
)

data class CaloriesEstimate(
    val kcal: Int, 
    val confidence: Int, 
    val text: String
)

data class UiRecipe(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val markdown: String,
    val calories: Int? = null,
    val imageUrl: String? = null
)

data class AiRequest(
    val prompt: String,
    val provider: AiProvider,
    val taskType: TaskType,
    val hasImage: Boolean = false
)

data class AiResponse(
    val content: String,
    val provider: AiProvider,
    val taskType: TaskType,
    val duration: Long,
    val estimatedTokens: Int
)