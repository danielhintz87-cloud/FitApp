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

    // Neue funktionsspezifische TaskTypes für optimale Modellauswahl
    FORM_CHECK_ANALYSIS, // Haltungskorrektur mit Trainingsfotos
    EQUIPMENT_RECOGNITION, // Gym-Geräte identifizieren
    PROGRESS_PHOTO_ANALYSIS, // Body-Transformation tracking
    LIVE_COACHING_FEEDBACK, // Echtzeit-Feedback basierend auf Pose-Daten
    RESEARCH_TRENDS, // Aktuelle Fitness-Trends via Perplexity
    SUPPLEMENT_RESEARCH, // Supplement-Studies und Reviews
    MEAL_PHOTO_ANALYSIS, // Detaillierte Food-Recognition
    RECIPE_WITH_IMAGE_GEN, // Rezepte mit AI-generierten Bildern
    SIMPLE_TEXT_COACHING, // Einfache Motivations-Texte
    COMPLEX_PLAN_ANALYSIS, // Komplexe Trainingsplan-Logik
}

data class PlanRequest(
    val goal: String,
    val weeks: Int = 12,
    val sessionsPerWeek: Int,
    val minutesPerSession: Int,
    val equipment: List<String> = emptyList(),
)

data class RecipeRequest(
    val preferences: String,
    val diet: String,
    val count: Int = 10,
)

data class CaloriesEstimate(
    val kcal: Int,
    val confidence: Int,
    val text: String,
)

data class UiRecipe(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val markdown: String,
    val calories: Int? = null,
    val imageUrl: String? = null,
)

data class AiRequest(
    val prompt: String,
    val provider: AiProvider,
    val taskType: TaskType,
    val hasImage: Boolean = false,
)

data class AiResponse(
    val content: String,
    val provider: AiProvider,
    val taskType: TaskType,
    val duration: Long,
    val estimatedTokens: Int,
)

// AI Personal Trainer Entities
data class UserProfile(
    val age: Int,
    val gender: String,
    val height: Float,
    val currentWeight: Float,
    val targetWeight: Float,
    val activityLevel: String,
    val fitnessGoals: List<String> = emptyList(),
)

data class FitnessLevel(
    val strength: String, // "beginner", "intermediate", "advanced"
    val cardio: String,
    val flexibility: String,
    val experience: String,
)

data class WorkoutPlan(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val exercises: List<Exercise>,
    val estimatedDuration: Int, // minutes
    val difficulty: String,
    val equipment: List<String>,
)

data class Exercise(
    val name: String,
    val sets: Int,
    val reps: String, // Can be "10-12" or "30 seconds"
    val restTime: String,
    val instructions: String,
)

data class PersonalizedMealPlan(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val dailyCalories: Int,
    val macroTargets: MacroTargets,
    val meals: List<MealPlan>,
)

data class MacroTargets(
    val protein: Int, // grams
    val carbs: Int, // grams
    val fat: Int, // grams
)

data class MealPlan(
    val type: String, // "breakfast", "lunch", "dinner", "snack"
    val name: String,
    val calories: Int,
    val description: String,
)

data class ProgressAnalysis(
    val weightTrend: String,
    val adherenceScore: Float,
    val insights: List<String>,
    val recommendations: List<String>,
)

data class GoalPrediction(
    val estimatedTimeToGoal: String,
    val probability: Float,
    val keyFactors: List<String>,
)

data class MotivationalMessage(
    val title: String,
    val message: String,
    val type: String, // "encouragement", "challenge", "tip"
    val actionSuggestion: String?,
)

data class AIRecommendation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val type: String, // "workout", "nutrition", "habit"
    val priority: String, // "high", "medium", "low"
    val actionRequired: Boolean = false,
)

data class AIPersonalTrainerRequest(
    val userProfile: UserProfile,
    val fitnessLevel: FitnessLevel,
    val availableTime: Int, // minutes
    val equipment: List<String>,
    val goals: List<String>,
)

data class UserContext(
    val profile: UserProfile,
    val fitnessLevel: FitnessLevel,
    val recentProgress: List<WeightEntry>,
    val currentGoals: List<String>,
    val availableEquipment: List<String>,
)

data class WeightEntry(
    val date: String,
    val weight: Float,
    val notes: String? = null,
)

data class AIPersonalTrainerResponse(
    val workoutPlan: WorkoutPlan?,
    val mealPlan: PersonalizedMealPlan?,
    val progressAnalysis: ProgressAnalysis?,
    val motivation: MotivationalMessage?,
    val recommendations: List<AIRecommendation>,
)

// Consolidated workout analysis entities to resolve duplicate declarations
data class PlateauDetectionResult(
    val exerciseId: String,
    val isPlateaued: Boolean,
    val plateauDuration: Int,
    val progressRate: Float,
    val plateauScore: Float,
    val confidence: Float,
    val recommendations: List<String>,
) {
    companion object {
        fun error(exerciseId: String) =
            PlateauDetectionResult(
                exerciseId = exerciseId,
                isPlateaued = false,
                plateauDuration = 0,
                progressRate = 0f,
                plateauScore = 0f,
                confidence = 0f,
                recommendations = listOf("Daten nicht verfügbar"),
            )
    }
}

enum class TrendDirection {
    UP,
    DOWN,
    STABLE,
}

data class ProgressionRecommendation(
    val type: ProgressionType,
    val weightIncrease: Float?,
    val repIncrease: Int?,
    val description: String,
    val confidence: Float,
    val nextEvaluationWeeks: Int,
) {
    companion object {
        fun weightIncrease(
            weight: Float,
            reps: Int,
            sets: Int,
            description: String,
        ): ProgressionRecommendation {
            return ProgressionRecommendation(
                type = ProgressionType.WEIGHT_INCREASE,
                weightIncrease = weight,
                repIncrease = null,
                description = description,
                confidence = 0.8f,
                nextEvaluationWeeks = 1,
            )
        }

        fun repIncrease(
            weight: Float,
            reps: Int,
            sets: Int,
            description: String,
        ): ProgressionRecommendation {
            return ProgressionRecommendation(
                type = ProgressionType.REP_INCREASE,
                weightIncrease = null,
                repIncrease = reps,
                description = description,
                confidence = 0.8f,
                nextEvaluationWeeks = 1,
            )
        }

        fun maintain(
            weight: Float,
            reps: Int,
            sets: Int,
            description: String,
        ): ProgressionRecommendation {
            return ProgressionRecommendation(
                type = ProgressionType.MAINTAIN,
                weightIncrease = null,
                repIncrease = null,
                description = description,
                confidence = 0.9f,
                nextEvaluationWeeks = 2,
            )
        }

        fun deload(
            weight: Float,
            reps: Int,
            sets: Int,
            description: String,
        ): ProgressionRecommendation {
            return ProgressionRecommendation(
                type = ProgressionType.DELOAD,
                weightIncrease = weight,
                repIncrease = null,
                description = description,
                confidence = 0.7f,
                nextEvaluationWeeks = 1,
            )
        }
    }
}

enum class ProgressionType {
    WEIGHT_INCREASE,
    REP_INCREASE,
    DELOAD,
    MAINTAIN,
    TECHNIQUE_FOCUS,

    // Bodyweight progression types
    TIME_INCREASE,
    DIFFICULTY_INCREASE,
    INTERVAL_DECREASE,
    HIIT_INTENSITY_INCREASE,
}

// Bodyweight Exercise and HIIT specific entities
data class BodyweightExercise(
    val name: String,
    val category: BodyweightCategory,
    val difficultyLevel: Int = 1, // 1-5 scale
    val baseReps: Int? = null,
    val baseTime: Int? = null, // seconds
    val description: String,
    val instructions: List<String> = emptyList(),
    val progressionOptions: List<BodyweightProgression> = emptyList(),
)

enum class BodyweightCategory {
    PUSH, // Push-ups, Pike push-ups, etc.
    PULL, // Pull-ups, Bodyweight rows, etc.
    SQUAT, // Squats, Pistol squats, etc.
    CORE, // Planks, Mountain climbers, etc.
    CARDIO, // Burpees, Jumping jacks, etc.
    FULL_BODY, // Combination movements
}

data class BodyweightProgression(
    val type: ProgressionType,
    val targetIncrease: String, // "5 reps", "10 seconds", "Next difficulty"
    val description: String,
    val difficultyIncrease: Int = 0,
)

data class HIITWorkout(
    val name: String,
    val rounds: Int,
    val workInterval: Int, // seconds
    val restInterval: Int, // seconds
    val exercises: List<HIITExercise>,
    val totalDuration: Int, // calculated total duration in seconds
    val difficulty: HIITDifficulty = HIITDifficulty.BEGINNER,
)

data class HIITExercise(
    val bodyweightExercise: BodyweightExercise,
    val targetReps: Int? = null,
    val isTimeBased: Boolean = false, // true if exercise is performed for time, false for reps
    val order: Int,
)

enum class HIITDifficulty {
    BEGINNER, // 20s work, 40s rest
    INTERMEDIATE, // 30s work, 30s rest
    ADVANCED, // 45s work, 15s rest
    EXPERT, // 60s work, 10s rest
}

data class HIITBuilder(
    val selectedExercises: List<BodyweightExercise> = emptyList(),
    val workInterval: Int = 30,
    val restInterval: Int = 30,
    val rounds: Int = 4,
    val difficulty: HIITDifficulty = HIITDifficulty.BEGINNER,
) {
    fun generateWorkout(name: String): HIITWorkout {
        val hiitExercises =
            selectedExercises.mapIndexed { index, exercise ->
                HIITExercise(
                    bodyweightExercise = exercise,
                    targetReps = exercise.baseReps,
                    isTimeBased = exercise.baseTime != null,
                    order = index,
                )
            }

        val exerciseTime = selectedExercises.size * workInterval
        val restTime = selectedExercises.size * restInterval
        val totalDuration = rounds * (exerciseTime + restTime)

        return HIITWorkout(
            name = name,
            rounds = rounds,
            workInterval = workInterval,
            restInterval = restInterval,
            exercises = hiitExercises,
            totalDuration = totalDuration,
            difficulty = difficulty,
        )
    }
}
