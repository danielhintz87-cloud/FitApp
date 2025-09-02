package com.example.fitapp.domain.entities

import java.util.UUID

/**
 * Domain entities for AI functionality
 * Pure business objects with no dependencies on Android or frameworks
 */

enum class AiProvider { Gemini, Perplexity }

enum class TaskType {
    TRAINING_PLAN,
    CALORIE_ESTIMATION,
    RECIPE_GENERATION,
    SHOPPING_LIST_PARSING,
    AI_PERSONAL_TRAINER,
    WORKOUT_GENERATION,
    NUTRITION_ADVICE,
    PROGRESS_ANALYSIS,
    MOTIVATIONAL_COACHING,
    COOKING_ASSISTANCE
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

// AI Personal Trainer Entities
data class UserProfile(
    val age: Int,
    val gender: String,
    val height: Float,
    val currentWeight: Float,
    val targetWeight: Float,
    val activityLevel: String,
    val fitnessGoals: List<String> = emptyList()
)

data class FitnessLevel(
    val strength: String, // "beginner", "intermediate", "advanced"
    val cardio: String,
    val flexibility: String,
    val experience: String
)

data class WorkoutPlan(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val exercises: List<Exercise>,
    val estimatedDuration: Int, // minutes
    val difficulty: String,
    val equipment: List<String>
)

data class Exercise(
    val name: String,
    val sets: Int,
    val reps: String, // Can be "10-12" or "30 seconds"
    val restTime: String,
    val instructions: String
)

data class PersonalizedMealPlan(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val dailyCalories: Int,
    val macroTargets: MacroTargets,
    val meals: List<MealPlan>
)

data class MacroTargets(
    val protein: Int, // grams
    val carbs: Int,   // grams
    val fat: Int      // grams
)

data class MealPlan(
    val type: String, // "breakfast", "lunch", "dinner", "snack"
    val name: String,
    val calories: Int,
    val description: String
)

data class ProgressAnalysis(
    val weightTrend: String,
    val adherenceScore: Float,
    val insights: List<String>,
    val recommendations: List<String>
)

data class GoalPrediction(
    val estimatedTimeToGoal: String,
    val probability: Float,
    val keyFactors: List<String>
)

data class MotivationalMessage(
    val title: String,
    val message: String,
    val type: String, // "encouragement", "challenge", "tip"
    val actionSuggestion: String?
)

data class AIRecommendation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val type: String, // "workout", "nutrition", "habit"
    val priority: String, // "high", "medium", "low"
    val actionRequired: Boolean = false
)

data class AIPersonalTrainerRequest(
    val userProfile: UserProfile,
    val fitnessLevel: FitnessLevel,
    val availableTime: Int, // minutes
    val equipment: List<String>,
    val goals: List<String>
)

data class UserContext(
    val profile: UserProfile,
    val fitnessLevel: FitnessLevel,
    val recentProgress: List<WeightEntry>,
    val currentGoals: List<String>,
    val availableEquipment: List<String>
)

data class WeightEntry(
    val date: String,
    val weight: Float,
    val notes: String? = null
)

data class AIPersonalTrainerResponse(
    val workoutPlan: WorkoutPlan?,
    val mealPlan: PersonalizedMealPlan?,
    val progressAnalysis: ProgressAnalysis?,
    val motivation: MotivationalMessage?,
    val recommendations: List<AIRecommendation>
)