package com.example.fitapp.domain

import kotlin.math.pow

/**
 * BMI category classification with German names and color codes
 */
enum class BMICategory(val germanName: String, val colorCode: String, val range: ClosedFloatingPointRange<Float>) {
    UNDERWEIGHT("Untergewicht", "#3F51B5", 0f..18.4f),
    NORMAL("Normalgewicht", "#4CAF50", 18.5f..24.9f), 
    OVERWEIGHT("Übergewicht", "#FF9800", 25f..29.9f),
    OBESE("Adipositas", "#F44336", 30f..Float.MAX_VALUE);
    
    companion object {
        fun fromBMI(bmi: Float): BMICategory {
            return when {
                bmi < 18.5f -> UNDERWEIGHT
                bmi < 25f -> NORMAL
                bmi < 30f -> OVERWEIGHT
                else -> OBESE
            }
        }
    }
}

/**
 * BMI calculation result with recommendations
 */
data class BMIResult(
    val bmi: Float,
    val category: BMICategory,
    val idealWeightRange: ClosedFloatingPointRange<Float>,
    val recommendedWeightLoss: Float? = null // kg to lose for overweight/obese
)

/**
 * Activity level for weight loss calculations
 */
enum class ActivityLevel(val germanName: String, val multiplier: Float) {
    SEDENTARY("Sitzend", 1.2f),
    LIGHTLY_ACTIVE("Leicht aktiv", 1.375f),
    MODERATELY_ACTIVE("Mäßig aktiv", 1.55f),
    VERY_ACTIVE("Sehr aktiv", 1.725f),
    EXTRA_ACTIVE("Extrem aktiv", 1.9f)
}

/**
 * Weight loss program data
 */
data class WeightLossProgram(
    val dailyCalorieTarget: Int,
    val macroTargets: MacroTargets,
    val weeklyWeightLossGoal: Float,
    val recommendedExerciseMinutes: Int,
    val milestones: List<WeightLossMilestone>
)

/**
 * Macro nutrient targets
 */
data class MacroTargets(
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float
)

/**
 * Weight loss milestone
 */
data class WeightLossMilestone(
    val targetWeight: Float,
    val estimatedDate: String, // ISO date
    val description: String
)

/**
 * BMI Calculator utility class
 */
object BMICalculator {
    
    /**
     * Calculate BMI from height and weight
     */
    fun calculateBMI(heightCm: Float, weightKg: Float): Float {
        val heightM = heightCm / 100f
        return weightKg / (heightM.pow(2))
    }
    
    /**
     * Calculate BMI result with recommendations
     */
    fun calculateBMIResult(heightCm: Float, weightKg: Float): BMIResult {
        val bmi = calculateBMI(heightCm, weightKg)
        val category = BMICategory.fromBMI(bmi)
        val idealWeightRange = calculateIdealWeightRange(heightCm)
        val recommendedWeightLoss = if (category == BMICategory.OVERWEIGHT || category == BMICategory.OBESE) {
            weightKg - idealWeightRange.endInclusive
        } else null
        
        return BMIResult(
            bmi = bmi,
            category = category,
            idealWeightRange = idealWeightRange,
            recommendedWeightLoss = recommendedWeightLoss
        )
    }
    
    /**
     * Calculate ideal weight range (BMI 18.5-24.9)
     */
    private fun calculateIdealWeightRange(heightCm: Float): ClosedFloatingPointRange<Float> {
        val heightM = heightCm / 100f
        val minWeight = 18.5f * heightM.pow(2)
        val maxWeight = 24.9f * heightM.pow(2)
        return minWeight..maxWeight
    }
    
    /**
     * Calculate weight needed for target BMI
     */
    fun calculateWeightForTargetBMI(heightCm: Float, targetBMI: Float): Float {
        val heightM = heightCm / 100f
        return targetBMI * heightM.pow(2)
    }
    
    /**
     * Calculate BMR (Basal Metabolic Rate) using Mifflin-St Jeor equation
     */
    fun calculateBMR(weightKg: Float, heightCm: Float, ageYears: Int, isMale: Boolean): Float {
        return if (isMale) {
            10 * weightKg + 6.25f * heightCm - 5 * ageYears + 5
        } else {
            10 * weightKg + 6.25f * heightCm - 5 * ageYears - 161
        }
    }
    
    /**
     * Calculate daily calorie target for weight loss
     */
    fun calculateDailyCalorieTarget(
        bmr: Float,
        activityLevel: ActivityLevel,
        weeklyWeightLossGoal: Float
    ): Int {
        val tdee = bmr * activityLevel.multiplier
        val dailyDeficit = (weeklyWeightLossGoal * 7700) / 7 // 7700 kcal per kg
        return (tdee - dailyDeficit).toInt()
    }
    
    /**
     * Calculate macro targets based on calorie target
     */
    fun calculateMacroTargets(calorieTarget: Int): MacroTargets {
        // Standard macro distribution: 30% protein, 40% carbs, 30% fat
        val proteinCalories = calorieTarget * 0.3f
        val carbsCalories = calorieTarget * 0.4f
        val fatCalories = calorieTarget * 0.3f
        
        return MacroTargets(
            proteinGrams = proteinCalories / 4f, // 4 kcal per gram
            carbsGrams = carbsCalories / 4f,     // 4 kcal per gram
            fatGrams = fatCalories / 9f          // 9 kcal per gram
        )
    }
}