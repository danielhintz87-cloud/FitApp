package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.util.StructuredLogger
import kotlin.math.max
import kotlin.math.min

/**
 * MacroCalculator for advanced macro distribution and diet approaches
 * Handles protein requirements, macro adjustments, and various diet protocols
 */
class MacroCalculator(private val context: Context) {
    
    companion object {
        private const val TAG = "MacroCalculator"
        
        // Protein requirements per kg body weight
        private const val PROTEIN_SEDENTARY = 0.8f
        private const val PROTEIN_ACTIVE = 1.6f
        private const val PROTEIN_ATHLETE = 2.2f
        private const val PROTEIN_CUTTING = 2.4f
        
        // Fat requirements (percentage of calories)
        private const val FAT_MIN_PERCENTAGE = 0.15f
        private const val FAT_MAX_PERCENTAGE = 0.45f
        
        // Carb cycling ratios
        private const val HIGH_CARB_RATIO = 0.6f
        private const val MEDIUM_CARB_RATIO = 0.4f
        private const val LOW_CARB_RATIO = 0.2f
    }
    
    /**
     * Calculate protein requirements based on body weight and activity
     */
    fun calculateProteinRequirements(
        bodyWeight: Float,
        activityLevel: ActivityLevel,
        goal: NutritionGoal,
        trainingIntensity: TrainingIntensity
    ): ProteinRequirement {
        return try {
            require(bodyWeight > 0) { "Body weight must be positive" }
            
            val baseProteinPerKg = when {
                goal == NutritionGoal.WEIGHT_LOSS && trainingIntensity == TrainingIntensity.HIGH -> PROTEIN_CUTTING
                trainingIntensity == TrainingIntensity.HIGH -> PROTEIN_ATHLETE
                activityLevel >= ActivityLevel.MODERATELY_ACTIVE -> PROTEIN_ACTIVE
                else -> PROTEIN_SEDENTARY
            }
            
            val proteinGrams = bodyWeight * baseProteinPerKg
            val proteinCalories = proteinGrams * 4
            
            ProteinRequirement(
                gramsPerKg = baseProteinPerKg,
                totalGrams = proteinGrams,
                totalCalories = proteinCalories.toInt(),
                rationale = generateProteinRationale(baseProteinPerKg, goal, trainingIntensity)
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to calculate protein requirements",
                exception = e
            )
            ProteinRequirement(
                gramsPerKg = PROTEIN_ACTIVE,
                totalGrams = bodyWeight * PROTEIN_ACTIVE,
                totalCalories = (bodyWeight * PROTEIN_ACTIVE * 4).toInt(),
                rationale = "Standard Protein-Empfehlung"
            )
        }
    }
    
    /**
     * Adjust macros for training days vs rest days
     */
    fun adjustMacrosForTrainingDays(
        baseMacros: MacroCalculationResult,
        isTrainingDay: Boolean,
        trainingType: TrainingType
    ): MacroAdjustment {
        return try {
            val adjustment = if (isTrainingDay) {
                when (trainingType) {
                    TrainingType.STRENGTH -> {
                        // Increase carbs for strength training
                        val additionalCarbs = (baseMacros.carbsGrams * 0.2f).toInt()
                        MacroAdjustment(
                            carbsAdjustment = additionalCarbs,
                            proteinAdjustment = 0,
                            fatAdjustment = 0,
                            caloriesAdjustment = additionalCarbs * 4,
                            reason = "Krafttraining: Mehr Kohlenhydrate für Energie"
                        )
                    }
                    TrainingType.CARDIO -> {
                        // Slight increase in carbs and protein for cardio
                        val additionalCarbs = (baseMacros.carbsGrams * 0.15f).toInt()
                        val additionalProtein = (baseMacros.proteinGrams * 0.1f).toInt()
                        MacroAdjustment(
                            carbsAdjustment = additionalCarbs,
                            proteinAdjustment = additionalProtein,
                            fatAdjustment = 0,
                            caloriesAdjustment = (additionalCarbs * 4) + (additionalProtein * 4),
                            reason = "Ausdauertraining: Mehr Kohlenhydrate und Protein"
                        )
                    }
                    TrainingType.MIXED -> {
                        // Balanced increase for mixed training
                        val additionalCarbs = (baseMacros.carbsGrams * 0.15f).toInt()
                        val additionalProtein = (baseMacros.proteinGrams * 0.05f).toInt()
                        MacroAdjustment(
                            carbsAdjustment = additionalCarbs,
                            proteinAdjustment = additionalProtein,
                            fatAdjustment = 0,
                            caloriesAdjustment = (additionalCarbs * 4) + (additionalProtein * 4),
                            reason = "Kombiniertes Training: Ausgewogene Erhöhung"
                        )
                    }
                }
            } else {
                // Rest day - slightly reduce carbs
                val carbReduction = (baseMacros.carbsGrams * 0.1f).toInt()
                MacroAdjustment(
                    carbsAdjustment = -carbReduction,
                    proteinAdjustment = 0,
                    fatAdjustment = 0,
                    caloriesAdjustment = -carbReduction * 4,
                    reason = "Ruhetag: Reduzierte Kohlenhydrate"
                )
            }
            
            adjustment
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to adjust macros for training day",
                exception = e
            )
            MacroAdjustment(0, 0, 0, 0, "Fehler bei Anpassung")
        }
    }
    
    /**
     * Handle different diet approaches (keto, low-carb, balanced, etc.)
     */
    fun calculateMacrosForDietApproach(
        targetCalories: Int,
        bodyWeight: Float,
        dietApproach: DietApproach
    ): DietSpecificMacros {
        return try {
            require(targetCalories > 0) { "Target calories must be positive" }
            require(bodyWeight > 0) { "Body weight must be positive" }
            
            when (dietApproach) {
                DietApproach.KETOGENIC -> {
                    val protein = bodyWeight * 1.5f
                    val fat = (targetCalories * 0.7f) / 9f // 70% fat
                    val carbs = (targetCalories * 0.05f) / 4f // 5% carbs
                    
                    DietSpecificMacros(
                        proteinGrams = protein.toInt(),
                        carbsGrams = carbs.toInt(),
                        fatGrams = fat.toInt(),
                        proteinPercentage = (protein * 4 / targetCalories * 100).toInt(),
                        carbsPercentage = 5,
                        fatPercentage = 70,
                        approach = dietApproach,
                        guidelines = listOf(
                            "Kohlenhydrate unter 25g täglich",
                            "Fokus auf gesunde Fette",
                            "Moderate Proteinzufuhr"
                        )
                    )
                }
                DietApproach.LOW_CARB -> {
                    val protein = bodyWeight * 1.8f
                    val carbs = (targetCalories * 0.15f) / 4f // 15% carbs
                    val fat = (targetCalories - (protein * 4) - (carbs * 4)) / 9f
                    
                    DietSpecificMacros(
                        proteinGrams = protein.toInt(),
                        carbsGrams = carbs.toInt(),
                        fatGrams = fat.toInt(),
                        proteinPercentage = (protein * 4 / targetCalories * 100).toInt(),
                        carbsPercentage = 15,
                        fatPercentage = (fat * 9 / targetCalories * 100).toInt(),
                        approach = dietApproach,
                        guidelines = listOf(
                            "Kohlenhydrate hauptsächlich aus Gemüse",
                            "Hohe Proteinzufuhr",
                            "Gesunde Fette bevorzugen"
                        )
                    )
                }
                DietApproach.BALANCED -> {
                    val protein = bodyWeight * 1.6f
                    val fat = (targetCalories * 0.25f) / 9f // 25% fat
                    val carbs = (targetCalories - (protein * 4) - (fat * 9)) / 4f
                    
                    DietSpecificMacros(
                        proteinGrams = protein.toInt(),
                        carbsGrams = carbs.toInt(),
                        fatGrams = fat.toInt(),
                        proteinPercentage = (protein * 4 / targetCalories * 100).toInt(),
                        carbsPercentage = (carbs * 4 / targetCalories * 100).toInt(),
                        fatPercentage = 25,
                        approach = dietApproach,
                        guidelines = listOf(
                            "Ausgewogene Makronährstoffverteilung",
                            "Komplexe Kohlenhydrate bevorzugen",
                            "Vielfältige Proteinquellen"
                        )
                    )
                }
                DietApproach.HIGH_CARB -> {
                    val protein = bodyWeight * 1.4f
                    val fat = (targetCalories * 0.2f) / 9f // 20% fat
                    val carbs = (targetCalories - (protein * 4) - (fat * 9)) / 4f
                    
                    DietSpecificMacros(
                        proteinGrams = protein.toInt(),
                        carbsGrams = carbs.toInt(),
                        fatGrams = fat.toInt(),
                        proteinPercentage = (protein * 4 / targetCalories * 100).toInt(),
                        carbsPercentage = (carbs * 4 / targetCalories * 100).toInt(),
                        fatPercentage = 20,
                        approach = dietApproach,
                        guidelines = listOf(
                            "Hoher Kohlenhydratanteil für Ausdauersport",
                            "Timing der Kohlenhydrate wichtig",
                            "Niedrigerer Fettanteil"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to calculate diet-specific macros",
                exception = e
            )
            DietSpecificMacros(
                proteinGrams = (bodyWeight * 1.6f).toInt(),
                carbsGrams = (targetCalories * 0.45f / 4f).toInt(),
                fatGrams = (targetCalories * 0.25f / 9f).toInt(),
                proteinPercentage = 20,
                carbsPercentage = 45,
                fatPercentage = 25,
                approach = DietApproach.BALANCED,
                guidelines = listOf("Standard-Empfehlung")
            )
        }
    }
    
    /**
     * Validate macro distribution ratios
     */
    fun validateMacroDistribution(
        proteinGrams: Int,
        carbsGrams: Int,
        fatGrams: Int,
        totalCalories: Int
    ): MacroValidationResult {
        return try {
            require(proteinGrams >= 0) { "Protein cannot be negative" }
            require(carbsGrams >= 0) { "Carbs cannot be negative" }
            require(fatGrams >= 0) { "Fat cannot be negative" }
            require(totalCalories > 0) { "Total calories must be positive" }
            
            val proteinCalories = proteinGrams * 4
            val carbsCalories = carbsGrams * 4
            val fatCalories = fatGrams * 9
            val macroCalories = proteinCalories + carbsCalories + fatCalories
            
            val proteinPercentage = proteinCalories.toFloat() / totalCalories
            val carbsPercentage = carbsCalories.toFloat() / totalCalories
            val fatPercentage = fatCalories.toFloat() / totalCalories
            
            val warnings = mutableListOf<String>()
            val recommendations = mutableListOf<String>()
            
            // Validate protein
            if (proteinPercentage < 0.10f) {
                warnings.add("Protein zu niedrig (${(proteinPercentage * 100).toInt()}%)")
                recommendations.add("Proteinzufuhr auf mindestens 15% erhöhen")
            } else if (proteinPercentage > 0.35f) {
                warnings.add("Protein sehr hoch (${(proteinPercentage * 100).toInt()}%)")
                recommendations.add("Proteinzufuhr möglicherweise reduzieren")
            }
            
            // Validate fat
            if (fatPercentage < FAT_MIN_PERCENTAGE) {
                warnings.add("Fett zu niedrig (${(fatPercentage * 100).toInt()}%)")
                recommendations.add("Fettzufuhr auf mindestens 20% erhöhen")
            } else if (fatPercentage > FAT_MAX_PERCENTAGE) {
                warnings.add("Fett sehr hoch (${(fatPercentage * 100).toInt()}%)")
                recommendations.add("Fettanteil überprüfen")
            }
            
            // Validate total
            val totalPercentage = proteinPercentage + carbsPercentage + fatPercentage
            val calorieAccuracy = macroCalories.toFloat() / totalCalories
            
            if (calorieAccuracy < 0.9f || calorieAccuracy > 1.1f) {
                warnings.add("Makros stimmen nicht mit Gesamtkalorien überein")
                recommendations.add("Makronährstoff-Berechnung überprüfen")
            }
            
            MacroValidationResult(
                isValid = warnings.isEmpty(),
                proteinPercentage = proteinPercentage,
                carbsPercentage = carbsPercentage,
                fatPercentage = fatPercentage,
                calorieAccuracy = calorieAccuracy,
                warnings = warnings,
                recommendations = recommendations
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to validate macro distribution",
                exception = e
            )
            MacroValidationResult(
                isValid = false,
                proteinPercentage = 0f,
                carbsPercentage = 0f,
                fatPercentage = 0f,
                calorieAccuracy = 0f,
                warnings = listOf("Fehler bei Validierung"),
                recommendations = emptyList()
            )
        }
    }
    
    /**
     * Generate carb cycling recommendations
     */
    fun generateCarbCyclingPlan(
        baseMacros: MacroCalculationResult,
        trainingDaysPerWeek: Int
    ): CarbCyclingPlan {
        return try {
            require(trainingDaysPerWeek in 1..7) { "Training days must be between 1 and 7" }
            
            val highCarbCarbs = (baseMacros.carbsGrams * (1 + HIGH_CARB_RATIO)).toInt()
            val mediumCarbCarbs = baseMacros.carbsGrams
            val lowCarbCarbs = (baseMacros.carbsGrams * LOW_CARB_RATIO).toInt()
            
            val weekPlan = mutableMapOf<DayType, DayMacros>()
            
            // Assign high carb days to intense training days
            val highCarbDays = minOf(trainingDaysPerWeek, 2)
            val mediumCarbDays = trainingDaysPerWeek - highCarbDays
            val lowCarbDays = 7 - trainingDaysPerWeek
            
            weekPlan[DayType.HIGH_CARB] = DayMacros(
                carbs = highCarbCarbs,
                protein = baseMacros.proteinGrams,
                fat = baseMacros.fatGrams,
                calories = calculateCaloriesFromMacros(highCarbCarbs, baseMacros.proteinGrams, baseMacros.fatGrams),
                daysPerWeek = highCarbDays
            )
            
            weekPlan[DayType.MEDIUM_CARB] = DayMacros(
                carbs = mediumCarbCarbs,
                protein = baseMacros.proteinGrams,
                fat = baseMacros.fatGrams,
                calories = baseMacros.targetCalories,
                daysPerWeek = mediumCarbDays
            )
            
            weekPlan[DayType.LOW_CARB] = DayMacros(
                carbs = lowCarbCarbs,
                protein = baseMacros.proteinGrams,
                fat = baseMacros.fatGrams + ((baseMacros.carbsGrams - lowCarbCarbs) * 4 / 9).toInt(),
                calories = baseMacros.targetCalories,
                daysPerWeek = lowCarbDays
            )
            
            CarbCyclingPlan(
                weekPlan = weekPlan,
                averageWeeklyCalories = calculateAverageWeeklyCalories(weekPlan),
                guidelines = listOf(
                    "High-Carb Tage: Intensive Trainingstage",
                    "Medium-Carb Tage: Moderate Trainingstage",
                    "Low-Carb Tage: Ruhetage",
                    "Protein konstant halten"
                )
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to generate carb cycling plan",
                exception = e
            )
            CarbCyclingPlan(
                weekPlan = emptyMap(),
                averageWeeklyCalories = baseMacros.targetCalories,
                guidelines = listOf("Fehler bei Carb Cycling Plan")
            )
        }
    }
    
    private fun generateProteinRationale(
        proteinPerKg: Float,
        goal: NutritionGoal,
        intensity: TrainingIntensity
    ): String {
        return when {
            proteinPerKg >= PROTEIN_CUTTING -> "Hohe Proteinzufuhr für Muskelerhalt in Diät"
            proteinPerKg >= PROTEIN_ATHLETE -> "Athleten-Level für intensives Training"
            proteinPerKg >= PROTEIN_ACTIVE -> "Aktive Person mit regelmäßigem Training"
            else -> "Grundbedarf für wenig aktive Person"
        }
    }
    
    private fun calculateCaloriesFromMacros(carbs: Int, protein: Int, fat: Int): Int {
        return (carbs * 4) + (protein * 4) + (fat * 9)
    }
    
    private fun calculateAverageWeeklyCalories(weekPlan: Map<DayType, DayMacros>): Int {
        val totalCalories = weekPlan.values.sumOf { it.calories * it.daysPerWeek }
        return totalCalories / 7
    }
}

// Data classes for macro calculation
data class ProteinRequirement(
    val gramsPerKg: Float,
    val totalGrams: Float,
    val totalCalories: Int,
    val rationale: String
)

data class MacroAdjustment(
    val carbsAdjustment: Int,
    val proteinAdjustment: Int,
    val fatAdjustment: Int,
    val caloriesAdjustment: Int,
    val reason: String
)

data class DietSpecificMacros(
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val proteinPercentage: Int,
    val carbsPercentage: Int,
    val fatPercentage: Int,
    val approach: DietApproach,
    val guidelines: List<String>
)

data class MacroValidationResult(
    val isValid: Boolean,
    val proteinPercentage: Float,
    val carbsPercentage: Float,
    val fatPercentage: Float,
    val calorieAccuracy: Float,
    val warnings: List<String>,
    val recommendations: List<String>
)

data class CarbCyclingPlan(
    val weekPlan: Map<DayType, DayMacros>,
    val averageWeeklyCalories: Int,
    val guidelines: List<String>
)

data class DayMacros(
    val carbs: Int,
    val protein: Int,
    val fat: Int,
    val calories: Int,
    val daysPerWeek: Int
)

enum class TrainingIntensity {
    LOW,
    MODERATE,
    HIGH
}

enum class TrainingType {
    STRENGTH,
    CARDIO,
    MIXED
}

enum class DietApproach {
    KETOGENIC,
    LOW_CARB,
    BALANCED,
    HIGH_CARB
}

enum class DayType {
    HIGH_CARB,
    MEDIUM_CARB,
    LOW_CARB
}