package com.example.fitapp.data.health

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * BMI Calculator with comprehensive health metrics and weight loss analysis
 * Based on WHO standards and fitness industry best practices
 */
object BMICalculator {
    /**
     * Calculate BMI (Body Mass Index) from height and weight
     * @param heightCm Height in centimeters
     * @param weightKg Weight in kilograms
     * @return BMI value rounded to 1 decimal place
     */
    fun calculateBMI(
        heightCm: Float,
        weightKg: Float,
    ): Float {
        val heightM = heightCm / 100f
        return (weightKg / (heightM.pow(2)) * 10).roundToInt() / 10f
    }

    /**
     * Calculate BMI from imperial units
     * @param heightFeet Height in feet
     * @param heightInches Additional inches
     * @param weightLbs Weight in pounds
     * @return BMI value
     */
    fun calculateBMIImperial(
        heightFeet: Int,
        heightInches: Int,
        weightLbs: Float,
    ): Float {
        val totalInches = heightFeet * 12 + heightInches
        val heightCm = totalInches * 2.54f
        val weightKg = weightLbs * 0.453592f
        return calculateBMI(heightCm, weightKg)
    }

    /**
     * Determine BMI category based on WHO classification
     */
    fun getBMICategory(bmi: Float): BMICategory {
        return when {
            bmi < 18.5f -> BMICategory.UNDERWEIGHT
            bmi < 25f -> BMICategory.NORMAL
            bmi < 30f -> BMICategory.OVERWEIGHT
            else -> BMICategory.OBESE
        }
    }

    /**
     * Calculate ideal weight range for given height
     * Based on BMI 18.5-24.9 range
     */
    fun getIdealWeightRange(heightCm: Float): ClosedFloatingPointRange<Float> {
        val heightM = heightCm / 100f
        val minWeight = 18.5f * (heightM.pow(2))
        val maxWeight = 24.9f * (heightM.pow(2))
        return minWeight..maxWeight
    }

    /**
     * Calculate recommended weight loss for overweight/obese individuals
     * Returns null if BMI is in normal range
     */
    fun getRecommendedWeightLoss(
        heightCm: Float,
        weightKg: Float,
    ): Float? {
        val bmi = calculateBMI(heightCm, weightKg)
        if (bmi <= 25f) return null

        val idealWeightRange = getIdealWeightRange(heightCm)
        val targetWeight = idealWeightRange.endInclusive
        return weightKg - targetWeight
    }

    /**
     * Generate weight loss timeline based on safe loss rate (0.5-1kg per week)
     */
    fun getWeightLossTimeline(
        currentWeight: Float,
        targetWeight: Float,
    ): WeightLossTimeline {
        val totalLoss = currentWeight - targetWeight
        if (totalLoss <= 0) {
            return WeightLossTimeline(0, 0, emptyList())
        }

        // Safe weight loss: 0.5-1kg per week
        val conservativeWeeks = (totalLoss / 0.5f).toInt()
        val aggressiveWeeks = (totalLoss / 1f).toInt()

        val milestones = generateMilestones(currentWeight, targetWeight, aggressiveWeeks)

        return WeightLossTimeline(
            conservativeWeeks = conservativeWeeks,
            aggressiveWeeks = aggressiveWeeks,
            milestones = milestones,
        )
    }

    /**
     * Calculate daily calorie deficit needed for weight loss goal
     * 1kg fat = approximately 7700 calories
     */
    fun calculateDailyCalorieDeficit(
        weightLossKg: Float,
        timeframeWeeks: Int,
    ): Int {
        val totalCalories = weightLossKg * 7700f
        val dailyDeficit = totalCalories / (timeframeWeeks * 7)
        return dailyDeficit.toInt()
    }

    private fun generateMilestones(
        startWeight: Float,
        targetWeight: Float,
        weeks: Int,
    ): List<WeightMilestone> {
        val milestones = mutableListOf<WeightMilestone>()
        val totalLoss = startWeight - targetWeight
        val weeklyLoss = totalLoss / weeks

        for (week in 1..weeks step 4) { // Every 4 weeks
            val weight = startWeight - (weeklyLoss * week)
            milestones.add(
                WeightMilestone(
                    week = week,
                    targetWeight = weight,
                    totalLoss = startWeight - weight,
                ),
            )
        }

        // Final milestone
        milestones.add(
            WeightMilestone(
                week = weeks,
                targetWeight = targetWeight,
                totalLoss = totalLoss,
            ),
        )

        return milestones
    }
}

/**
 * BMI Category classification with German names and UI colors
 */
enum class BMICategory(
    val germanName: String,
    val description: String,
    val colorHex: String,
    val healthRisk: String,
) {
    UNDERWEIGHT(
        "Untergewicht",
        "BMI unter 18,5",
        "#3F51B5", // Blue
        "Möglicherweise zu wenig Körpergewicht",
    ),
    NORMAL(
        "Normalgewicht",
        "BMI 18,5 - 24,9",
        "#4CAF50", // Green
        "Gesundes Körpergewicht",
    ),
    OVERWEIGHT(
        "Übergewicht",
        "BMI 25,0 - 29,9",
        "#FF9800", // Orange
        "Erhöhtes Risiko für gesundheitliche Probleme",
    ),
    OBESE(
        "Adipositas",
        "BMI 30,0 und höher",
        "#F44336", // Red
        "Deutlich erhöhtes Gesundheitsrisiko",
    ),
}

/**
 * Comprehensive BMI analysis result
 */
data class BMIResult(
    val bmi: Float,
    val category: BMICategory,
    val idealWeightRange: ClosedFloatingPointRange<Float>,
    val recommendedWeightLoss: Float? = null,
    val timeline: WeightLossTimeline? = null,
    val dailyCalorieDeficit: Int? = null,
)

/**
 * Weight loss timeline with milestones
 */
data class WeightLossTimeline(
    val conservativeWeeks: Int, // 0.5kg/week
    val aggressiveWeeks: Int, // 1kg/week
    val milestones: List<WeightMilestone>,
)

/**
 * Weight loss milestone for progress tracking
 */
data class WeightMilestone(
    val week: Int,
    val targetWeight: Float,
    val totalLoss: Float,
)

/**
 * Factory for creating complete BMI analysis
 */
object BMIAnalyzer {
    fun analyzeCompleteHealth(
        heightCm: Float,
        weightKg: Float,
        targetTimeframeWeeks: Int = 12,
    ): BMIResult {
        val bmi = BMICalculator.calculateBMI(heightCm, weightKg)
        val category = BMICalculator.getBMICategory(bmi)
        val idealWeightRange = BMICalculator.getIdealWeightRange(heightCm)
        val recommendedWeightLoss = BMICalculator.getRecommendedWeightLoss(heightCm, weightKg)

        val timeline =
            recommendedWeightLoss?.let { loss ->
                BMICalculator.getWeightLossTimeline(weightKg, weightKg - loss)
            }

        val dailyCalorieDeficit =
            recommendedWeightLoss?.let { loss ->
                BMICalculator.calculateDailyCalorieDeficit(loss, targetTimeframeWeeks)
            }

        return BMIResult(
            bmi = bmi,
            category = category,
            idealWeightRange = idealWeightRange,
            recommendedWeightLoss = recommendedWeightLoss,
            timeline = timeline,
            dailyCalorieDeficit = dailyCalorieDeficit,
        )
    }
}
