package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

/**
 * Core Nutrition Manager for business logic
 * Handles daily macro calculations, meal logging, and calorie calculations
 */
class NutritionManager(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.get(context)
) {
    
    companion object {
        private const val TAG = "NutritionManager"
        private const val PROTEIN_PER_KG_MIN = 1.6f
        private const val PROTEIN_PER_KG_MAX = 2.2f
        private const val FAT_PERCENTAGE_MIN = 0.2f
        private const val FAT_PERCENTAGE_MAX = 0.35f
        private const val SURPLUS_PERCENTAGE = 0.1f
        private const val DEFICIT_PERCENTAGE = 0.2f
    }
    
    /**
     * Calculate daily macros based on user profile and goals
     */
    suspend fun calculateDailyMacros(
        bodyWeight: Float,
        height: Float,
        age: Int,
        gender: Gender,
        activityLevel: ActivityLevel,
        goal: NutritionGoal
    ): MacroCalculationResult = withContext(Dispatchers.IO) {
        try {
            require(bodyWeight > 0) { "Body weight must be positive" }
            require(height > 0) { "Height must be positive" }
            require(age > 0) { "Age must be positive" }
            
            // Calculate BMR using Mifflin-St Jeor equation
            val bmr = calculateBMR(bodyWeight, height, age, gender)
            
            // Calculate TDEE
            val tdee = bmr * activityLevel.multiplier
            
            // Adjust calories based on goal
            val targetCalories = when (goal) {
                NutritionGoal.WEIGHT_LOSS -> tdee * (1 - DEFICIT_PERCENTAGE)
                NutritionGoal.WEIGHT_GAIN -> tdee * (1 + SURPLUS_PERCENTAGE)
                NutritionGoal.MAINTENANCE -> tdee
            }
            
            // Calculate macros
            val protein = bodyWeight * PROTEIN_PER_KG_MIN
            val fat = targetCalories * FAT_PERCENTAGE_MIN / 9 // 9 cal per gram fat
            val carbs = (targetCalories - (protein * 4) - (fat * 9)) / 4 // 4 cal per gram
            
            MacroCalculationResult(
                targetCalories = targetCalories.toInt(),
                proteinGrams = protein.toInt(),
                carbsGrams = max(0, carbs.toInt()),
                fatGrams = fat.toInt(),
                bmr = bmr.toInt(),
                tdee = tdee.toInt(),
                goal = goal
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to calculate daily macros",
                exception = e
            )
            MacroCalculationResult(
                targetCalories = 2000,
                proteinGrams = 120,
                carbsGrams = 200,
                fatGrams = 65,
                bmr = 1600,
                tdee = 2000,
                goal = goal
            )
        }
    }
    
    /**
     * Track meal logging with accuracy validation
     */
    suspend fun logMeal(
        mealType: MealType,
        foods: List<FoodItem>,
        timestamp: Long = System.currentTimeMillis()
    ): MealLoggingResult = withContext(Dispatchers.IO) {
        try {
            require(foods.isNotEmpty()) { "Foods list cannot be empty" }
            
            val totalCalories = foods.sumOf { it.calories }
            val totalProtein = foods.sumOf { it.proteinGrams }
            val totalCarbs = foods.sumOf { it.carbsGrams }
            val totalFat = foods.sumOf { it.fatGrams }
            
            // Validate nutritional consistency
            val calculatedCalories = (totalProtein * 4) + (totalCarbs * 4) + (totalFat * 9)
            val calorieAccuracy = if (totalCalories > 0) {
                1 - kotlin.math.abs(totalCalories - calculatedCalories) / totalCalories
            } else 0.0
            
            MealLoggingResult(
                mealId = generateMealId(),
                mealType = mealType,
                timestamp = timestamp,
                totalCalories = totalCalories.toInt(),
                totalProtein = totalProtein.toInt(),
                totalCarbs = totalCarbs.toInt(),
                totalFat = totalFat.toInt(),
                foods = foods,
                accuracy = calorieAccuracy.toFloat(),
                isValid = calorieAccuracy > 0.8
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to log meal",
                exception = e
            )
            MealLoggingResult(
                mealId = "",
                mealType = mealType,
                timestamp = timestamp,
                totalCalories = 0,
                totalProtein = 0,
                totalCarbs = 0,
                totalFat = 0,
                foods = emptyList(),
                accuracy = 0f,
                isValid = false
            )
        }
    }
    
    /**
     * Suggest meal timing optimization
     */
    fun suggestMealTiming(
        workoutTime: Int, // Hour of day (0-23)
        workoutDuration: Int, // Minutes
        totalMeals: Int
    ): MealTimingRecommendation {
        return try {
            require(workoutTime in 0..23) { "Workout time must be between 0 and 23" }
            require(workoutDuration > 0) { "Workout duration must be positive" }
            require(totalMeals in 3..6) { "Total meals should be between 3 and 6" }
            
            val preWorkoutTime = workoutTime - 2 // 2 hours before
            val postWorkoutTime = (workoutTime + (workoutDuration / 60) + 1) % 24 // 1 hour after
            
            val mealTimes = mutableListOf<Int>()
            
            // Add breakfast
            mealTimes.add(7)
            
            // Add pre-workout meal if needed
            if (preWorkoutTime >= 6 && preWorkoutTime <= 20) {
                mealTimes.add(preWorkoutTime)
            }
            
            // Add post-workout meal
            mealTimes.add(postWorkoutTime)
            
            // Fill remaining meals
            val remainingMeals = totalMeals - mealTimes.size
            for (i in 0 until remainingMeals) {
                val time = 12 + (i * 4) // Space out remaining meals
                if (time <= 22 && !mealTimes.contains(time)) {
                    mealTimes.add(time)
                }
            }
            
            MealTimingRecommendation(
                recommendedTimes = mealTimes.sorted(),
                preWorkoutMealTime = if (preWorkoutTime in mealTimes) preWorkoutTime else null,
                postWorkoutMealTime = postWorkoutTime,
                reasoning = "Optimiert für Training um ${workoutTime}:00 Uhr"
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to suggest meal timing",
                exception = e
            )
            MealTimingRecommendation(
                recommendedTimes = listOf(7, 12, 18),
                preWorkoutMealTime = null,
                postWorkoutMealTime = 19,
                reasoning = "Standard Mahlzeiten-Schema"
            )
        }
    }
    
    /**
     * Handle calorie surplus/deficit calculations
     */
    fun calculateCalorieBalance(
        targetCalories: Int,
        consumedCalories: Int,
        burnedCalories: Int = 0
    ): CalorieBalanceResult {
        return try {
            val netCalories = consumedCalories - burnedCalories
            val balance = netCalories - targetCalories
            val balancePercentage = if (targetCalories > 0) balance.toFloat() / targetCalories else 0f
            
            val balanceType = when {
                balance > targetCalories * SURPLUS_PERCENTAGE -> BalanceType.LARGE_SURPLUS
                balance > 0 -> BalanceType.SMALL_SURPLUS
                balance > -targetCalories * DEFICIT_PERCENTAGE -> BalanceType.SMALL_DEFICIT
                else -> BalanceType.LARGE_DEFICIT
            }
            
            CalorieBalanceResult(
                targetCalories = targetCalories,
                consumedCalories = consumedCalories,
                burnedCalories = burnedCalories,
                netCalories = netCalories,
                balance = balance,
                balancePercentage = balancePercentage,
                balanceType = balanceType,
                recommendation = generateBalanceRecommendation(balanceType, balance)
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to calculate calorie balance",
                exception = e
            )
            CalorieBalanceResult(
                targetCalories = targetCalories,
                consumedCalories = consumedCalories,
                burnedCalories = burnedCalories,
                netCalories = consumedCalories,
                balance = 0,
                balancePercentage = 0f,
                balanceType = BalanceType.BALANCED,
                recommendation = "Fehler bei Berechnung"
            )
        }
    }
    
    /**
     * Validate nutrition goals against user profile
     */
    fun validateNutritionGoals(
        currentWeight: Float,
        targetWeight: Float,
        timeFrameWeeks: Int,
        goal: NutritionGoal
    ): NutritionGoalValidation {
        return try {
            require(currentWeight > 0) { "Current weight must be positive" }
            require(targetWeight > 0) { "Target weight must be positive" }
            require(timeFrameWeeks > 0) { "Time frame must be positive" }
            
            val weightDifference = targetWeight - currentWeight
            val weeklyWeightChange = weightDifference / timeFrameWeeks
            
            val isRealistic = when (goal) {
                NutritionGoal.WEIGHT_LOSS -> weeklyWeightChange >= -1.0f && weeklyWeightChange <= -0.2f
                NutritionGoal.WEIGHT_GAIN -> weeklyWeightChange >= 0.2f && weeklyWeightChange <= 0.5f
                NutritionGoal.MAINTENANCE -> kotlin.math.abs(weeklyWeightChange) <= 0.1f
            }
            
            val adjustedTimeFrame = if (!isRealistic) {
                when (goal) {
                    NutritionGoal.WEIGHT_LOSS -> (kotlin.math.abs(weightDifference) / 0.7f).toInt()
                    NutritionGoal.WEIGHT_GAIN -> (weightDifference / 0.35f).toInt()
                    NutritionGoal.MAINTENANCE -> timeFrameWeeks
                }
            } else timeFrameWeeks
            
            NutritionGoalValidation(
                isRealistic = isRealistic,
                recommendedTimeFrameWeeks = adjustedTimeFrame,
                weeklyWeightChange = weeklyWeightChange,
                warnings = generateGoalWarnings(isRealistic, weeklyWeightChange, goal),
                recommendations = generateGoalRecommendations(weeklyWeightChange, goal)
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to validate nutrition goals",
                exception = e
            )
            NutritionGoalValidation(
                isRealistic = false,
                recommendedTimeFrameWeeks = timeFrameWeeks,
                weeklyWeightChange = 0f,
                warnings = listOf("Fehler bei Validierung"),
                recommendations = emptyList()
            )
        }
    }
    
    private fun calculateBMR(bodyWeight: Float, height: Float, age: Int, gender: Gender): Float {
        return when (gender) {
            Gender.MALE -> 10 * bodyWeight + 6.25f * height - 5 * age + 5
            Gender.FEMALE -> 10 * bodyWeight + 6.25f * height - 5 * age - 161
        }
    }
    
    private fun generateMealId(): String = "meal_${System.currentTimeMillis()}"
    
    private fun generateBalanceRecommendation(balanceType: BalanceType, balance: Int): String {
        return when (balanceType) {
            BalanceType.LARGE_SURPLUS -> "Kalorienüberschuss zu hoch ($balance kcal). Reduziere Aufnahme."
            BalanceType.SMALL_SURPLUS -> "Leichter Überschuss ($balance kcal). Gut für Muskelaufbau."
            BalanceType.BALANCED -> "Ausgeglichene Kalorienbilanz. Weiter so!"
            BalanceType.SMALL_DEFICIT -> "Leichtes Defizit ($balance kcal). Gut für Gewichtsverlust."
            BalanceType.LARGE_DEFICIT -> "Defizit zu hoch ($balance kcal). Erhöhe Aufnahme."
        }
    }
    
    private fun generateGoalWarnings(isRealistic: Boolean, weeklyChange: Float, goal: NutritionGoal): List<String> {
        val warnings = mutableListOf<String>()
        
        if (!isRealistic) {
            warnings.add("Ziel ist unrealistisch für den gewählten Zeitrahmen")
        }
        
        when (goal) {
            NutritionGoal.WEIGHT_LOSS -> {
                if (weeklyChange < -1.0f) warnings.add("Gewichtsverlust zu schnell (>1kg/Woche)")
            }
            NutritionGoal.WEIGHT_GAIN -> {
                if (weeklyChange > 0.5f) warnings.add("Gewichtszunahme zu schnell (>0.5kg/Woche)")
            }
            NutritionGoal.MAINTENANCE -> {
                // No specific warnings for maintenance
            }
        }
        
        return warnings
    }
    
    private fun generateGoalRecommendations(weeklyChange: Float, goal: NutritionGoal): List<String> {
        val recommendations = mutableListOf<String>()
        
        when (goal) {
            NutritionGoal.WEIGHT_LOSS -> {
                recommendations.add("Fokus auf Proteinaufnahme für Muskelerhalt")
                recommendations.add("Krafttraining beibehalten")
                if (weeklyChange < -0.7f) recommendations.add("Refeed Days einplanen")
            }
            NutritionGoal.WEIGHT_GAIN -> {
                recommendations.add("Regelmäßige Mahlzeiten für Kalorienüberschuss")
                recommendations.add("Progressive Overload im Training")
                recommendations.add("Ausreichend Schlaf für Regeneration")
            }
            NutritionGoal.MAINTENANCE -> {
                recommendations.add("Konsistente Routine beibehalten")
                recommendations.add("Periodische Gewichtskontrolle")
            }
        }
        
        return recommendations
    }
}

// Data classes for nutrition management
data class MacroCalculationResult(
    val targetCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val bmr: Int,
    val tdee: Int,
    val goal: NutritionGoal
)

data class MealLoggingResult(
    val mealId: String,
    val mealType: MealType,
    val timestamp: Long,
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFat: Int,
    val foods: List<FoodItem>,
    val accuracy: Float,
    val isValid: Boolean
)

data class MealTimingRecommendation(
    val recommendedTimes: List<Int>,
    val preWorkoutMealTime: Int?,
    val postWorkoutMealTime: Int,
    val reasoning: String
)

data class CalorieBalanceResult(
    val targetCalories: Int,
    val consumedCalories: Int,
    val burnedCalories: Int,
    val netCalories: Int,
    val balance: Int,
    val balancePercentage: Float,
    val balanceType: BalanceType,
    val recommendation: String
)

data class NutritionGoalValidation(
    val isRealistic: Boolean,
    val recommendedTimeFrameWeeks: Int,
    val weeklyWeightChange: Float,
    val warnings: List<String>,
    val recommendations: List<String>
)

data class FoodItem(
    val name: String,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val quantity: Double,
    val unit: String
)

enum class Gender {
    MALE,
    FEMALE
}

enum class ActivityLevel(val multiplier: Float) {
    SEDENTARY(1.2f),
    LIGHTLY_ACTIVE(1.375f),
    MODERATELY_ACTIVE(1.55f),
    VERY_ACTIVE(1.725f),
    EXTREMELY_ACTIVE(1.9f)
}

enum class NutritionGoal {
    WEIGHT_LOSS,
    WEIGHT_GAIN,
    MAINTENANCE
}

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
    PRE_WORKOUT,
    POST_WORKOUT
}

enum class BalanceType {
    LARGE_DEFICIT,
    SMALL_DEFICIT,
    BALANCED,
    SMALL_SURPLUS,
    LARGE_SURPLUS
}