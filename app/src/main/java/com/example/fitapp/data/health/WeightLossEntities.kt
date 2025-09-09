package com.example.fitapp.data.health

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing BMI calculation history and progress tracking
 */
@Entity(tableName = "bmi_history")
data class BMIHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // ISO date format
    val heightCm: Float,
    val weightKg: Float,
    val bmi: Float,
    val category: String, // BMICategory name
    val notes: String? = null,
    val goalWeightKg: Float? = null,
    val timeframeWeeks: Int? = null
)

/**
 * Entity for tracking detailed weight loss progress
 */
@Entity(tableName = "weight_loss_progress")
data class WeightLossProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // ISO date
    val weight: Float,
    val bmi: Float,
    val waistCircumference: Float? = null,
    val bodyFatPercentage: Float? = null,
    val moodScore: Int? = null, // 1-10 scale
    val energyScore: Int? = null, // 1-10 scale
    val adherenceScore: Float? = null, // 0.0-1.0 based on daily targets met
    val progressPhotoPath: String? = null,
    val notes: String? = null,
    val sleepHours: Float? = null,
    val waterIntakeLiters: Float? = null,
    val exerciseMinutes: Int? = null
)

/**
 * Entity for tracking behavioral patterns during weight loss
 */
@Entity(tableName = "behavioral_checkins")
data class BehavioralCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val moodBefore: Int, // 1-10 scale
    val hungerLevel: Int, // 1-10 scale
    val stressLevel: Int, // 1-10 scale
    val sleepQuality: Int? = null, // previous night, 1-10
    val triggers: String, // Comma-separated list of EmotionalTrigger names
    val copingStrategy: String? = null,
    val mealContext: String? = null, // "breakfast", "lunch", "dinner", "snack"
    val socialContext: String? = null // "alone", "family", "friends", "work"
)

/**
 * Entity for weight loss challenges and goals
 */
@Entity(tableName = "weight_loss_challenges")
data class WeightLossChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val targetMetric: String, // ChallengeMetric name
    val targetValue: Float,
    val currentValue: Float = 0f,
    val isCompleted: Boolean = false,
    val difficulty: String, // ChallengeDifficulty name
    val rewardDescription: String? = null,
    val streakDays: Int = 0,
    val category: String // "nutrition", "exercise", "behavioral", "hydration"
)

/**
 * Emotional triggers for weight loss behavioral analysis
 */
enum class EmotionalTrigger(val germanName: String) {
    STRESS("Stress"),
    BOREDOM("Langeweile"),
    SADNESS("Traurigkeit"),
    CELEBRATION("Feier"),
    SOCIAL_PRESSURE("Sozialer Druck"),
    FATIGUE("Müdigkeit"),
    ANXIETY("Angst"),
    HAPPINESS("Freude")
}

/**
 * Challenge metrics for weight loss gamification
 */
enum class ChallengeMetric(val germanName: String, val unit: String) {
    CALORIE_DEFICIT("Kaloriendefizit", "kcal"),
    SUGAR_INTAKE("Zuckeraufnahme", "g"),
    STEPS("Schritte", "Anzahl"),
    WATER_INTAKE("Wasseraufnahme", "L"),
    VEGETABLE_SERVINGS("Gemüseportionen", "Portionen"),
    PROTEIN_TARGET("Proteinziel", "g"),
    EXERCISE_MINUTES("Trainingsminuten", "min"),
    SLEEP_HOURS("Schlafstunden", "h"),
    MINDFUL_MEALS("Achtsame Mahlzeiten", "Anzahl"),
    WEIGHT_LOSS("Gewichtsverlust", "kg")
}

/**
 * Challenge difficulty levels
 */
enum class ChallengeDifficulty(val germanName: String, val multiplier: Float) {
    EASY("Einfach", 1.0f),
    MEDIUM("Mittel", 1.5f),
    HARD("Schwer", 2.0f),
    EXTREME("Extrem", 3.0f)
}

/**
 * Weight loss program configuration
 */
data class WeightLossProgram(
    val id: String,
    val title: String,
    val dailyCalorieTarget: Int,
    val macroTargets: MacroTargets,
    val weeklyWeightLossGoal: Float,
    val recommendedExerciseMinutes: Int,
    val milestones: List<WeightLossMilestone>,
    val behavioralStrategies: List<String>,
    val duration: Int // weeks
)

/**
 * Macro nutrient targets for weight loss
 */
data class MacroTargets(
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val proteinPercentage: Float = 25f, // Higher for satiety
    val carbsPercentage: Float = 45f,
    val fatPercentage: Float = 30f
)

/**
 * Weight loss milestone for achievement tracking
 */
data class WeightLossMilestone(
    val id: String,
    val title: String,
    val description: String,
    val targetWeightLoss: Float, // kg lost
    val targetDate: String,
    val rewardTitle: String,
    val isCompleted: Boolean = false
)
