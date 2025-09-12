package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * Core Nutrition Manager for business logic
 * Handles daily macro calculations, meal logging, and calorie calculations
 */
class NutritionManager(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.get(context),
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
        goal: NutritionGoal,
    ): MacroCalculationResult =
        withContext(Dispatchers.IO) {
            try {
                require(bodyWeight > 0) { "Body weight must be positive" }
                require(height > 0) { "Height must be positive" }
                require(age > 0) { "Age must be positive" }

                // Calculate BMR using Mifflin-St Jeor equation
                val bmr = calculateBMR(bodyWeight, height, age, gender)

                // Calculate TDEE
                val tdee = bmr * activityLevel.multiplier

                // Adjust calories based on goal
                val targetCalories =
                    when (goal) {
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
                    goal = goal,
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.NUTRITION,
                    TAG,
                    "Failed to calculate daily macros",
                    exception = e,
                )
                MacroCalculationResult(
                    targetCalories = 2000,
                    proteinGrams = 120,
                    carbsGrams = 200,
                    fatGrams = 65,
                    bmr = 1600,
                    tdee = 2000,
                    goal = goal,
                )
            }
        }

    /**
     * Track meal logging with accuracy validation
     */
    suspend fun logMeal(
        mealType: MealType,
        foods: List<FoodItem>,
        timestamp: Long = System.currentTimeMillis(),
    ): MealLoggingResult =
        withContext(Dispatchers.IO) {
            try {
                require(foods.isNotEmpty()) { "Foods list cannot be empty" }

                val totalCalories = foods.sumOf { it.calories }
                val totalProtein = foods.sumOf { it.proteinGrams }
                val totalCarbs = foods.sumOf { it.carbsGrams }
                val totalFat = foods.sumOf { it.fatGrams }

                // Validate nutritional consistency
                val calculatedCalories = (totalProtein * 4) + (totalCarbs * 4) + (totalFat * 9)
                val calorieAccuracy =
                    if (totalCalories > 0) {
                        1 - kotlin.math.abs(totalCalories - calculatedCalories) / totalCalories
                    } else {
                        0.0
                    }

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
                    isValid = calorieAccuracy > 0.8,
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.NUTRITION,
                    TAG,
                    "Failed to log meal",
                    exception = e,
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
                    isValid = false,
                )
            }
        }

    /**
     * Suggest meal timing optimization
     */
    fun suggestMealTiming(
        workoutTime: Int, // Hour of day (0-23)
        workoutDuration: Int, // Minutes
        totalMeals: Int,
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
                reasoning = "Optimiert für Training um $workoutTime:00 Uhr",
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to suggest meal timing",
                exception = e,
            )
            MealTimingRecommendation(
                recommendedTimes = listOf(7, 12, 18),
                preWorkoutMealTime = null,
                postWorkoutMealTime = 19,
                reasoning = "Standard Mahlzeiten-Schema",
            )
        }
    }

    /**
     * Handle calorie surplus/deficit calculations
     */
    fun calculateCalorieBalance(
        targetCalories: Int,
        consumedCalories: Int,
        burnedCalories: Int = 0,
    ): CalorieBalanceResult {
        return try {
            val netCalories = consumedCalories - burnedCalories
            val balance = netCalories - targetCalories
            val balancePercentage = if (targetCalories > 0) balance.toFloat() / targetCalories else 0f

            val balanceType =
                when {
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
                recommendation = generateBalanceRecommendation(balanceType, balance),
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to calculate calorie balance",
                exception = e,
            )
            CalorieBalanceResult(
                targetCalories = targetCalories,
                consumedCalories = consumedCalories,
                burnedCalories = burnedCalories,
                netCalories = consumedCalories,
                balance = 0,
                balancePercentage = 0f,
                balanceType = BalanceType.BALANCED,
                recommendation = "Fehler bei Berechnung",
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
        goal: NutritionGoal,
    ): NutritionGoalValidation {
        return try {
            require(currentWeight > 0) { "Current weight must be positive" }
            require(targetWeight > 0) { "Target weight must be positive" }
            require(timeFrameWeeks > 0) { "Time frame must be positive" }

            val weightDifference = targetWeight - currentWeight
            val weeklyWeightChange = weightDifference / timeFrameWeeks

            val isRealistic =
                when (goal) {
                    NutritionGoal.WEIGHT_LOSS -> weeklyWeightChange >= -1.0f && weeklyWeightChange <= -0.2f
                    NutritionGoal.WEIGHT_GAIN -> weeklyWeightChange >= 0.2f && weeklyWeightChange <= 0.5f
                    NutritionGoal.MAINTENANCE -> kotlin.math.abs(weeklyWeightChange) <= 0.1f
                }

            val adjustedTimeFrame =
                if (!isRealistic) {
                    when (goal) {
                        NutritionGoal.WEIGHT_LOSS -> (kotlin.math.abs(weightDifference) / 0.7f).toInt()
                        NutritionGoal.WEIGHT_GAIN -> (weightDifference / 0.35f).toInt()
                        NutritionGoal.MAINTENANCE -> timeFrameWeeks
                    }
                } else {
                    timeFrameWeeks
                }

            NutritionGoalValidation(
                isRealistic = isRealistic,
                recommendedTimeFrameWeeks = adjustedTimeFrame,
                weeklyWeightChange = weeklyWeightChange,
                warnings = generateGoalWarnings(isRealistic, weeklyWeightChange, goal),
                recommendations = generateGoalRecommendations(weeklyWeightChange, goal),
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to validate nutrition goals",
                exception = e,
            )
            NutritionGoalValidation(
                isRealistic = false,
                recommendedTimeFrameWeeks = timeFrameWeeks,
                weeklyWeightChange = 0f,
                warnings = listOf("Fehler bei Validierung"),
                recommendations = emptyList(),
            )
        }
    }

    private fun calculateBMR(
        bodyWeight: Float,
        height: Float,
        age: Int,
        gender: Gender,
    ): Float {
        return when (gender) {
            Gender.MALE -> 10 * bodyWeight + 6.25f * height - 5 * age + 5
            Gender.FEMALE -> 10 * bodyWeight + 6.25f * height - 5 * age - 161
        }
    }

    private fun generateMealId(): String = "meal_${System.currentTimeMillis()}"

    private fun generateBalanceRecommendation(
        balanceType: BalanceType,
        balance: Int,
    ): String {
        return when (balanceType) {
            BalanceType.LARGE_SURPLUS -> "Kalorienüberschuss zu hoch ($balance kcal). Reduziere Aufnahme."
            BalanceType.SMALL_SURPLUS -> "Leichter Überschuss ($balance kcal). Gut für Muskelaufbau."
            BalanceType.BALANCED -> "Ausgeglichene Kalorienbilanz. Weiter so!"
            BalanceType.SMALL_DEFICIT -> "Leichtes Defizit ($balance kcal). Gut für Gewichtsverlust."
            BalanceType.LARGE_DEFICIT -> "Defizit zu hoch ($balance kcal). Erhöhe Aufnahme."
        }
    }

    private fun generateGoalWarnings(
        isRealistic: Boolean,
        weeklyChange: Float,
        goal: NutritionGoal,
    ): List<String> {
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

    private fun generateGoalRecommendations(
        weeklyChange: Float,
        goal: NutritionGoal,
    ): List<String> {
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

    /**
     * Generate workout-specific nutrition recommendations
     * Enhanced nutrition-training coupling feature
     */
    suspend fun generateWorkoutNutritionRecommendations(
        workoutType: WorkoutType,
        workoutDuration: Int,
        workoutIntensity: WorkoutIntensity,
        timeUntilWorkout: Int? = null,
    ): WorkoutNutritionRecommendation =
        withContext(Dispatchers.IO) {
            try {
                val preWorkoutMeal = generatePreWorkoutMeal(workoutType, workoutIntensity, timeUntilWorkout)
                val postWorkoutMeal = generatePostWorkoutMeal(workoutType, workoutDuration, workoutIntensity)
                val duringWorkoutHydration = calculateWorkoutHydration(workoutDuration, workoutIntensity)
                val macroAdjustments = calculateWorkoutMacroAdjustments(workoutType, workoutDuration, workoutIntensity)

                WorkoutNutritionRecommendation(
                    preWorkoutMeal = preWorkoutMeal,
                    postWorkoutMeal = postWorkoutMeal,
                    hydrationNeeds = duringWorkoutHydration,
                    macroAdjustments = macroAdjustments,
                    workoutType = workoutType,
                    estimatedCaloriesBurned = estimateCaloriesBurned(workoutType, workoutDuration, workoutIntensity),
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.NUTRITION,
                    TAG,
                    "Failed to generate workout nutrition recommendations",
                    exception = e,
                )
                WorkoutNutritionRecommendation.getDefault()
            }
        }

    private fun generatePreWorkoutMeal(
        workoutType: WorkoutType,
        intensity: WorkoutIntensity,
        timeUntilWorkout: Int?,
    ): MealRecommendation {
        val timeGap = timeUntilWorkout ?: 60 // Default 1 hour

        return when {
            timeGap < 30 ->
                MealRecommendation(
                    foods = listOf("Banane", "Datteln"),
                    calories = 100,
                    timing = "15-30 Min vor Training",
                    reasoning = "Schnell verfügbare Kohlenhydrate für sofortige Energie",
                )
            timeGap < 90 ->
                when (workoutType) {
                    WorkoutType.STRENGTH ->
                        MealRecommendation(
                            foods = listOf("Haferflocken mit Banane", "Kaffee"),
                            calories = 200,
                            timing = "60-90 Min vor Training",
                            reasoning = "Kohlenhydrate für Kraft + Koffein für Fokus",
                        )
                    WorkoutType.CARDIO, WorkoutType.HIIT ->
                        MealRecommendation(
                            foods = listOf("Toast mit Honig", "Beeren"),
                            calories = 150,
                            timing = "60-90 Min vor Training",
                            reasoning = "Leichte Kohlenhydrate für Ausdauerleistung",
                        )
                    WorkoutType.MIXED ->
                        MealRecommendation(
                            foods = listOf("Haferflocken mit Früchten"),
                            calories = 175,
                            timing = "60-90 Min vor Training",
                            reasoning = "Ausgewogene Energie für gemischtes Training",
                        )
                }
            else ->
                MealRecommendation(
                    foods = listOf("Haferflocken mit Protein", "Früchte"),
                    calories = 300,
                    timing = "2-3 Std vor Training",
                    reasoning = "Vollständige Mahlzeit für optimale Energieversorgung",
                )
        }
    }

    private fun generatePostWorkoutMeal(
        workoutType: WorkoutType,
        duration: Int,
        intensity: WorkoutIntensity,
    ): MealRecommendation {
        val proteinNeeds =
            when (workoutType) {
                WorkoutType.STRENGTH -> 25
                WorkoutType.CARDIO -> 15
                WorkoutType.HIIT -> 20
                WorkoutType.MIXED -> 20
            }

        val carbNeeds =
            when (intensity) {
                WorkoutIntensity.LOW -> 20
                WorkoutIntensity.MODERATE -> 30
                WorkoutIntensity.HIGH -> 40
            }

        val totalCalories = (proteinNeeds * 4) + (carbNeeds * 4)

        return when (workoutType) {
            WorkoutType.STRENGTH ->
                MealRecommendation(
                    foods = listOf("Magerquark mit Früchten", "Vollkornbrot"),
                    calories = totalCalories,
                    timing = "30-60 Min nach Training",
                    reasoning = "Protein für Muskelaufbau, Kohlenhydrate für Glykogen-Replenishment",
                )
            WorkoutType.CARDIO ->
                MealRecommendation(
                    foods = listOf("Smoothie mit Protein", "Banane"),
                    calories = totalCalories,
                    timing = "30-60 Min nach Training",
                    reasoning = "Schnelle Regeneration nach Ausdauertraining",
                )
            else ->
                MealRecommendation(
                    foods = listOf("Hühnerbrust mit Reis", "Gemüse"),
                    calories = totalCalories,
                    timing = "30-60 Min nach Training",
                    reasoning = "Ausgewogene Nährstoffe für komplette Regeneration",
                )
        }
    }

    private fun calculateWorkoutHydration(
        duration: Int,
        intensity: WorkoutIntensity,
    ): HydrationRecommendation {
        val baseFluid = 500 // ml per hour
        val intensityMultiplier =
            when (intensity) {
                WorkoutIntensity.LOW -> 1.0f
                WorkoutIntensity.MODERATE -> 1.3f
                WorkoutIntensity.HIGH -> 1.6f
            }

        val totalFluid = ((duration / 60f) * baseFluid * intensityMultiplier).toInt()

        return HydrationRecommendation(
            totalFluidNeeds = totalFluid,
            timing = "250ml alle 15-20 Min während Training",
            type = if (duration > 60) "Elektrolytgetränk" else "Wasser",
        )
    }

    private fun calculateWorkoutMacroAdjustments(
        workoutType: WorkoutType,
        duration: Int,
        intensity: WorkoutIntensity,
    ): WorkoutMacroAdjustments {
        val baseCaloriesBurned = estimateCaloriesBurned(workoutType, duration, intensity)

        return WorkoutMacroAdjustments(
            additionalProtein =
                when (workoutType) {
                    WorkoutType.STRENGTH -> 15
                    WorkoutType.CARDIO -> 5
                    WorkoutType.HIIT -> 10
                    WorkoutType.MIXED -> 10
                },
            additionalCarbs =
                when (intensity) {
                    WorkoutIntensity.LOW -> 20
                    WorkoutIntensity.MODERATE -> 35
                    WorkoutIntensity.HIGH -> 50
                },
            additionalCalories = (baseCaloriesBurned * 0.2).toInt(), // 20% of burned calories
        )
    }

    private fun estimateCaloriesBurned(
        workoutType: WorkoutType,
        duration: Int,
        intensity: WorkoutIntensity,
    ): Int {
        val baseRate =
            when (workoutType) {
                WorkoutType.STRENGTH -> 6 // calories per minute
                WorkoutType.CARDIO -> 8
                WorkoutType.HIIT -> 12
                WorkoutType.MIXED -> 7
            }

        val intensityMultiplier =
            when (intensity) {
                WorkoutIntensity.LOW -> 0.8f
                WorkoutIntensity.MODERATE -> 1.0f
                WorkoutIntensity.HIGH -> 1.3f
            }

        return (baseRate * duration * intensityMultiplier).toInt()
    }

    /**
     * Calculate optimal meal timing around workout schedule
     */
    suspend fun calculateOptimalMealTiming(
        workoutStartTime: Int, // Hour of day (24h format)
        workoutDuration: Int,
        totalDailyMeals: Int = 4,
    ): MealTimingRecommendation =
        withContext(Dispatchers.IO) {
            try {
                val workoutEndTime = workoutStartTime + (workoutDuration / 60f).toInt()
                val preWorkoutMealTime = maxOf(6, workoutStartTime - 2) // At least 2h before, not before 6 AM
                val postWorkoutMealTime = minOf(22, workoutEndTime + 1) // At most 1h after, not after 10 PM

                val otherMealTimes =
                    distributeRemainingMeals(
                        totalDailyMeals - 2, // Excluding pre/post workout meals
                        preWorkoutMealTime,
                        postWorkoutMealTime,
                    )

                val allMealTimes = (listOf(preWorkoutMealTime, postWorkoutMealTime) + otherMealTimes).sorted()

                MealTimingRecommendation(
                    recommendedTimes = allMealTimes,
                    preWorkoutMealTime = preWorkoutMealTime,
                    postWorkoutMealTime = postWorkoutMealTime,
                    reasoning = "Optimiert für Training um $workoutStartTime:00 Uhr",
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.NUTRITION,
                    TAG,
                    "Failed to calculate meal timing",
                    exception = e,
                )
                MealTimingRecommendation.getDefault()
            }
        }

    private fun distributeRemainingMeals(
        mealCount: Int,
        preWorkoutTime: Int,
        postWorkoutTime: Int,
    ): List<Int> {
        if (mealCount <= 0) return emptyList()

        val morningMeals = mealCount / 2
        val eveningMeals = mealCount - morningMeals

        val morningTimes =
            (0 until morningMeals).map { i ->
                val startTime = 7 // 7 AM
                val endTime = preWorkoutTime - 1
                startTime + (i * (endTime - startTime) / maxOf(1, morningMeals - 1))
            }

        val eveningTimes =
            (0 until eveningMeals).map { i ->
                val startTime = postWorkoutTime + 2
                val endTime = 21 // 9 PM
                startTime + (i * (endTime - startTime) / maxOf(1, eveningMeals - 1))
            }

        return morningTimes + eveningTimes
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
    val goal: NutritionGoal,
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
    val isValid: Boolean,
)

data class MealTimingRecommendation(
    val recommendedTimes: List<Int>,
    val preWorkoutMealTime: Int?,
    val postWorkoutMealTime: Int,
    val reasoning: String,
) {
    companion object {
        fun getDefault() =
            MealTimingRecommendation(
                recommendedTimes = listOf(7, 12, 18),
                preWorkoutMealTime = null,
                postWorkoutMealTime = 19,
                reasoning = "Standard Mahlzeitenverteilung",
            )
    }
}

data class CalorieBalanceResult(
    val targetCalories: Int,
    val consumedCalories: Int,
    val burnedCalories: Int,
    val netCalories: Int,
    val balance: Int,
    val balancePercentage: Float,
    val balanceType: BalanceType,
    val recommendation: String,
)

data class NutritionGoalValidation(
    val isRealistic: Boolean,
    val recommendedTimeFrameWeeks: Int,
    val weeklyWeightChange: Float,
    val warnings: List<String>,
    val recommendations: List<String>,
)

data class FoodItem(
    val name: String,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val quantity: Double,
    val unit: String,
)

enum class Gender {
    MALE,
    FEMALE,
}

enum class ActivityLevel(val multiplier: Float) {
    SEDENTARY(1.2f),
    LIGHTLY_ACTIVE(1.375f),
    MODERATELY_ACTIVE(1.55f),
    VERY_ACTIVE(1.725f),
    EXTREMELY_ACTIVE(1.9f),
}

enum class WorkoutType {
    STRENGTH,
    CARDIO,
    HIIT,
    MIXED,
}

enum class WorkoutIntensity {
    LOW,
    MODERATE,
    HIGH,
}

// Enhanced data classes for workout-nutrition coupling
data class WorkoutNutritionRecommendation(
    val preWorkoutMeal: MealRecommendation,
    val postWorkoutMeal: MealRecommendation,
    val hydrationNeeds: HydrationRecommendation,
    val macroAdjustments: WorkoutMacroAdjustments,
    val workoutType: WorkoutType,
    val estimatedCaloriesBurned: Int,
) {
    companion object {
        fun getDefault() =
            WorkoutNutritionRecommendation(
                preWorkoutMeal = MealRecommendation.getDefault(),
                postWorkoutMeal = MealRecommendation.getDefault(),
                hydrationNeeds = HydrationRecommendation.getDefault(),
                macroAdjustments = WorkoutMacroAdjustments.getDefault(),
                workoutType = WorkoutType.MIXED,
                estimatedCaloriesBurned = 0,
            )
    }
}

data class MealRecommendation(
    val foods: List<String>,
    val calories: Int,
    val timing: String,
    val reasoning: String,
) {
    companion object {
        fun getDefault() =
            MealRecommendation(
                foods = listOf("Banane"),
                calories = 100,
                timing = "Vor Training",
                reasoning = "Standard Empfehlung",
            )
    }
}

data class HydrationRecommendation(
    val totalFluidNeeds: Int, // ml
    val timing: String,
    val type: String,
) {
    companion object {
        fun getDefault() =
            HydrationRecommendation(
                totalFluidNeeds = 500,
                timing = "Während Training",
                type = "Wasser",
            )
    }
}

data class WorkoutMacroAdjustments(
    val additionalProtein: Int, // grams
    val additionalCarbs: Int, // grams
    val additionalCalories: Int,
) {
    companion object {
        fun getDefault() =
            WorkoutMacroAdjustments(
                additionalProtein = 0,
                additionalCarbs = 0,
                additionalCalories = 0,
            )
    }
}

enum class NutritionGoal {
    WEIGHT_LOSS,
    WEIGHT_GAIN,
    MAINTENANCE,
}

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
    PRE_WORKOUT,
    POST_WORKOUT,
}

enum class BalanceType {
    LARGE_DEFICIT,
    SMALL_DEFICIT,
    BALANCED,
    SMALL_SURPLUS,
    LARGE_SURPLUS,
}
