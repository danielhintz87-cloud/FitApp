package com.example.fitapp.services

import android.content.Context
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit tests for MacroCalculator
 * Tests protein calculations, macro adjustments, and diet approaches
 */
class MacroCalculatorTest {

    @Mock
    private lateinit var context: Context

    private lateinit var macroCalculator: MacroCalculator

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        macroCalculator = MacroCalculator(context)
    }

    // Protein Requirements Tests

    @Test
    fun `should calculate protein requirements by body weight for sedentary person`() {
        // Given: Sedentary person
        val bodyWeight = 70f
        val activityLevel = ActivityLevel.SEDENTARY
        val goal = NutritionGoal.MAINTENANCE
        val trainingIntensity = TrainingIntensity.LOW

        // When: Calculating protein requirements
        val result = macroCalculator.calculateProteinRequirements(
            bodyWeight, activityLevel, goal, trainingIntensity
        )

        // Then: Should use sedentary protein ratio
        assertEquals(0.8f, result.gramsPerKg, 0.1f)
        assertEquals(56f, result.totalGrams, 5f)
        assertEquals(224, result.totalCalories)
        assertNotNull("Should provide rationale", result.rationale)
    }

    @Test
    fun `should calculate protein requirements by body weight for active person`() {
        // Given: Active person
        val bodyWeight = 75f
        val activityLevel = ActivityLevel.MODERATELY_ACTIVE
        val goal = NutritionGoal.MAINTENANCE
        val trainingIntensity = TrainingIntensity.MODERATE

        // When: Calculating protein requirements
        val result = macroCalculator.calculateProteinRequirements(
            bodyWeight, activityLevel, goal, trainingIntensity
        )

        // Then: Should use active protein ratio
        assertEquals(1.6f, result.gramsPerKg, 0.1f)
        assertEquals(120f, result.totalGrams, 5f)
        assertEquals(480, result.totalCalories)
        assertTrue("Should mention active person", result.rationale.contains("aktive") || result.rationale.contains("Training"))
    }

    @Test
    fun `should calculate protein requirements by body weight for athlete`() {
        // Given: High-intensity athlete
        val bodyWeight = 80f
        val activityLevel = ActivityLevel.VERY_ACTIVE
        val goal = NutritionGoal.WEIGHT_GAIN
        val trainingIntensity = TrainingIntensity.HIGH

        // When: Calculating protein requirements
        val result = macroCalculator.calculateProteinRequirements(
            bodyWeight, activityLevel, goal, trainingIntensity
        )

        // Then: Should use athlete protein ratio
        assertEquals(2.2f, result.gramsPerKg, 0.1f)
        assertEquals(176f, result.totalGrams, 5f)
        assertEquals(704, result.totalCalories)
        assertTrue("Should mention athlete level", result.rationale.contains("Athleten") || result.rationale.contains("intensiv"))
    }

    @Test
    fun `should calculate protein requirements by body weight for cutting`() {
        // Given: Person cutting weight with high training intensity
        val bodyWeight = 85f
        val activityLevel = ActivityLevel.VERY_ACTIVE
        val goal = NutritionGoal.WEIGHT_LOSS
        val trainingIntensity = TrainingIntensity.HIGH

        // When: Calculating protein requirements
        val result = macroCalculator.calculateProteinRequirements(
            bodyWeight, activityLevel, goal, trainingIntensity
        )

        // Then: Should use highest protein ratio for muscle retention
        assertEquals(2.4f, result.gramsPerKg, 0.1f)
        assertEquals(204f, result.totalGrams, 5f)
        assertEquals(816, result.totalCalories)
        assertTrue("Should mention muscle retention", result.rationale.contains("Muskelerhalt") || result.rationale.contains("Diät"))
    }

    @Test
    fun `calculateProteinRequirements should validate input parameters`() {
        // Test negative body weight
        assertThrows(IllegalArgumentException::class.java) {
            macroCalculator.calculateProteinRequirements(
                -70f, ActivityLevel.MODERATELY_ACTIVE, NutritionGoal.MAINTENANCE, TrainingIntensity.MODERATE
            )
        }
    }

    // Macro Adjustments for Training Days Tests

    @Test
    fun `should adjust macros for training days with strength training`() {
        // Given: Base macros and strength training day
        val baseMacros = MacroCalculationResult(
            targetCalories = 2000,
            proteinGrams = 120,
            carbsGrams = 200,
            fatGrams = 65,
            bmr = 1600,
            tdee = 2000,
            goal = NutritionGoal.MAINTENANCE
        )
        val isTrainingDay = true
        val trainingType = TrainingType.STRENGTH

        // When: Adjusting macros for training day
        val result = macroCalculator.adjustMacrosForTrainingDays(baseMacros, isTrainingDay, trainingType)

        // Then: Should increase carbs for strength training
        assertTrue("Should increase carbs", result.carbsAdjustment > 0)
        assertEquals("Protein should remain same", 0, result.proteinAdjustment)
        assertEquals("Fat should remain same", 0, result.fatAdjustment)
        assertTrue("Should increase calories", result.caloriesAdjustment > 0)
        assertTrue("Should mention strength training", result.reason.contains("Kraft"))
    }

    @Test
    fun `should adjust macros for training days with cardio training`() {
        // Given: Base macros and cardio training day
        val baseMacros = MacroCalculationResult(
            targetCalories = 2000,
            proteinGrams = 120,
            carbsGrams = 200,
            fatGrams = 65,
            bmr = 1600,
            tdee = 2000,
            goal = NutritionGoal.MAINTENANCE
        )
        val isTrainingDay = true
        val trainingType = TrainingType.CARDIO

        // When: Adjusting macros for cardio day
        val result = macroCalculator.adjustMacrosForTrainingDays(baseMacros, isTrainingDay, trainingType)

        // Then: Should increase both carbs and protein
        assertTrue("Should increase carbs", result.carbsAdjustment > 0)
        assertTrue("Should increase protein", result.proteinAdjustment > 0)
        assertEquals("Fat should remain same", 0, result.fatAdjustment)
        assertTrue("Should increase calories", result.caloriesAdjustment > 0)
        assertTrue("Should mention cardio", result.reason.contains("Ausdauer"))
    }

    @Test
    fun `should adjust macros for rest days`() {
        // Given: Base macros and rest day
        val baseMacros = MacroCalculationResult(
            targetCalories = 2000,
            proteinGrams = 120,
            carbsGrams = 200,
            fatGrams = 65,
            bmr = 1600,
            tdee = 2000,
            goal = NutritionGoal.MAINTENANCE
        )
        val isTrainingDay = false
        val trainingType = TrainingType.STRENGTH // Irrelevant for rest day

        // When: Adjusting macros for rest day
        val result = macroCalculator.adjustMacrosForTrainingDays(baseMacros, isTrainingDay, trainingType)

        // Then: Should reduce carbs
        assertTrue("Should reduce carbs", result.carbsAdjustment < 0)
        assertEquals("Protein should remain same", 0, result.proteinAdjustment)
        assertEquals("Fat should remain same", 0, result.fatAdjustment)
        assertTrue("Should reduce calories", result.caloriesAdjustment < 0)
        assertTrue("Should mention rest day", result.reason.contains("Ruhetag"))
    }

    // Diet Approaches Tests

    @Test
    fun `should handle different diet approaches - ketogenic`() {
        // Given: Ketogenic diet parameters
        val targetCalories = 2000
        val bodyWeight = 70f
        val dietApproach = DietApproach.KETOGENIC

        // When: Calculating keto macros
        val result = macroCalculator.calculateMacrosForDietApproach(targetCalories, bodyWeight, dietApproach)

        // Then: Should follow keto ratios
        assertTrue("Protein should be moderate", result.proteinGrams >= 100)
        assertTrue("Carbs should be very low", result.carbsGrams <= 30)
        assertTrue("Fat should be high", result.fatGrams >= 140)
        assertEquals("Carbs should be ~5%", 5, result.carbsPercentage)
        assertEquals("Fat should be ~70%", 70, result.fatPercentage)
        assertEquals("Diet approach should match", dietApproach, result.approach)
        assertTrue("Should have keto guidelines", result.guidelines.isNotEmpty())
    }

    @Test
    fun `should handle different diet approaches - low carb`() {
        // Given: Low-carb diet parameters
        val targetCalories = 2000
        val bodyWeight = 70f
        val dietApproach = DietApproach.LOW_CARB

        // When: Calculating low-carb macros
        val result = macroCalculator.calculateMacrosForDietApproach(targetCalories, bodyWeight, dietApproach)

        // Then: Should follow low-carb ratios
        assertTrue("Protein should be high", result.proteinGrams >= 120)
        assertEquals("Carbs should be ~15%", 15, result.carbsPercentage)
        assertTrue("Fat should be moderate to high", result.fatGrams >= 50)
        assertEquals("Diet approach should match", dietApproach, result.approach)
        assertTrue("Should have low-carb guidelines", result.guidelines.isNotEmpty())
    }

    @Test
    fun `should handle different diet approaches - balanced`() {
        // Given: Balanced diet parameters
        val targetCalories = 2000
        val bodyWeight = 70f
        val dietApproach = DietApproach.BALANCED

        // When: Calculating balanced macros
        val result = macroCalculator.calculateMacrosForDietApproach(targetCalories, bodyWeight, dietApproach)

        // Then: Should follow balanced ratios
        assertTrue("Protein should be adequate", result.proteinGrams >= 100)
        assertTrue("Carbs should be moderate", result.carbsGrams >= 150)
        assertEquals("Fat should be ~25%", 25, result.fatPercentage)
        assertEquals("Diet approach should match", dietApproach, result.approach)
        assertTrue("Should have balanced guidelines", result.guidelines.isNotEmpty())
    }

    @Test
    fun `should handle different diet approaches - high carb`() {
        // Given: High-carb diet parameters
        val targetCalories = 2000
        val bodyWeight = 70f
        val dietApproach = DietApproach.HIGH_CARB

        // When: Calculating high-carb macros
        val result = macroCalculator.calculateMacrosForDietApproach(targetCalories, bodyWeight, dietApproach)

        // Then: Should follow high-carb ratios
        assertTrue("Protein should be adequate", result.proteinGrams >= 90)
        assertTrue("Carbs should be high", result.carbsGrams >= 250)
        assertEquals("Fat should be ~20%", 20, result.fatPercentage)
        assertEquals("Diet approach should match", dietApproach, result.approach)
        assertTrue("Should have high-carb guidelines", result.guidelines.isNotEmpty())
    }

    @Test
    fun `calculateMacrosForDietApproach should validate input parameters`() {
        // Test negative calories
        assertThrows(IllegalArgumentException::class.java) {
            macroCalculator.calculateMacrosForDietApproach(-2000, 70f, DietApproach.BALANCED)
        }

        // Test negative body weight
        assertThrows(IllegalArgumentException::class.java) {
            macroCalculator.calculateMacrosForDietApproach(2000, -70f, DietApproach.BALANCED)
        }
    }

    // Macro Distribution Validation Tests

    @Test
    fun `should validate macro distribution ratios with valid macros`() {
        // Given: Valid macro distribution
        val proteinGrams = 120 // 480 cal = 24%
        val carbsGrams = 200 // 800 cal = 40%
        val fatGrams = 80 // 720 cal = 36%
        val totalCalories = 2000

        // When: Validating distribution
        val result = macroCalculator.validateMacroDistribution(proteinGrams, carbsGrams, fatGrams, totalCalories)

        // Then: Should be valid
        assertTrue("Should be valid distribution", result.isValid)
        assertEquals("Protein percentage should be calculated", 0.24f, result.proteinPercentage, 0.01f)
        assertEquals("Carbs percentage should be calculated", 0.40f, result.carbsPercentage, 0.01f)
        assertEquals("Fat percentage should be calculated", 0.36f, result.fatPercentage, 0.01f)
        assertTrue("Calorie accuracy should be good", result.calorieAccuracy > 0.95f)
        assertTrue("Should have no warnings", result.warnings.isEmpty())
    }

    @Test
    fun `should validate macro distribution ratios with low protein`() {
        // Given: Too low protein
        val proteinGrams = 40 // 160 cal = 8% (too low)
        val carbsGrams = 250 // 1000 cal = 50%
        val fatGrams = 93 // 840 cal = 42%
        val totalCalories = 2000

        // When: Validating distribution
        val result = macroCalculator.validateMacroDistribution(proteinGrams, carbsGrams, fatGrams, totalCalories)

        // Then: Should detect low protein
        assertFalse("Should not be valid", result.isValid)
        assertTrue("Should warn about low protein", 
            result.warnings.any { it.contains("Protein zu niedrig") })
        assertTrue("Should recommend increasing protein", 
            result.recommendations.any { it.contains("Protein") })
    }

    @Test
    fun `should validate macro distribution ratios with high fat`() {
        // Given: Too high fat
        val proteinGrams = 100 // 400 cal = 20%
        val carbsGrams = 100 // 400 cal = 20%
        val fatGrams = 133 // 1200 cal = 60% (too high)
        val totalCalories = 2000

        // When: Validating distribution
        val result = macroCalculator.validateMacroDistribution(proteinGrams, carbsGrams, fatGrams, totalCalories)

        // Then: Should detect high fat
        assertFalse("Should not be valid", result.isValid)
        assertTrue("Should warn about high fat", 
            result.warnings.any { it.contains("Fett sehr hoch") })
    }

    @Test
    fun `should validate macro distribution ratios with calorie mismatch`() {
        // Given: Macros that don't match total calories
        val proteinGrams = 200 // 800 cal
        val carbsGrams = 300 // 1200 cal
        val fatGrams = 200 // 1800 cal = Total 3800 cal from macros
        val totalCalories = 2000 // But claiming only 2000 total

        // When: Validating distribution
        val result = macroCalculator.validateMacroDistribution(proteinGrams, carbsGrams, fatGrams, totalCalories)

        // Then: Should detect calorie mismatch
        assertFalse("Should not be valid", result.isValid)
        assertTrue("Calorie accuracy should be poor", result.calorieAccuracy < 0.9f || result.calorieAccuracy > 1.1f)
        assertTrue("Should warn about calorie mismatch", 
            result.warnings.any { it.contains("stimmen nicht") })
    }

    @Test
    fun `validateMacroDistribution should validate input parameters`() {
        // Test negative protein
        assertThrows(IllegalArgumentException::class.java) {
            macroCalculator.validateMacroDistribution(-100, 200, 80, 2000)
        }

        // Test negative carbs
        assertThrows(IllegalArgumentException::class.java) {
            macroCalculator.validateMacroDistribution(120, -200, 80, 2000)
        }

        // Test negative fat
        assertThrows(IllegalArgumentException::class.java) {
            macroCalculator.validateMacroDistribution(120, 200, -80, 2000)
        }

        // Test zero or negative total calories
        assertThrows(IllegalArgumentException::class.java) {
            macroCalculator.validateMacroDistribution(120, 200, 80, 0)
        }
    }

    // Carb Cycling Tests

    @Test
    fun `should generate carb cycling plan`() {
        // Given: Base macros and training frequency
        val baseMacros = MacroCalculationResult(
            targetCalories = 2000,
            proteinGrams = 150,
            carbsGrams = 200,
            fatGrams = 70,
            bmr = 1600,
            tdee = 2000,
            goal = NutritionGoal.MAINTENANCE
        )
        val trainingDaysPerWeek = 4

        // When: Generating carb cycling plan
        val result = macroCalculator.generateCarbCyclingPlan(baseMacros, trainingDaysPerWeek)

        // Then: Should create appropriate plan
        assertEquals("Should have 3 day types", 3, result.weekPlan.size)
        assertTrue("Should have high carb days", result.weekPlan.containsKey(DayType.HIGH_CARB))
        assertTrue("Should have medium carb days", result.weekPlan.containsKey(DayType.MEDIUM_CARB))
        assertTrue("Should have low carb days", result.weekPlan.containsKey(DayType.LOW_CARB))
        
        val highCarbDay = result.weekPlan[DayType.HIGH_CARB]!!
        val mediumCarbDay = result.weekPlan[DayType.MEDIUM_CARB]!!
        val lowCarbDay = result.weekPlan[DayType.LOW_CARB]!!
        
        assertTrue("High carb should have more carbs", highCarbDay.carbs > mediumCarbDay.carbs)
        assertTrue("Medium carb should have more carbs than low", mediumCarbDay.carbs > lowCarbDay.carbs)
        
        // Check that all days add up to 7
        val totalDays = highCarbDay.daysPerWeek + mediumCarbDay.daysPerWeek + lowCarbDay.daysPerWeek
        assertEquals("Should total 7 days", 7, totalDays)
        
        assertTrue("Should have guidelines", result.guidelines.isNotEmpty())
        assertTrue("Average calories should be reasonable", result.averageWeeklyCalories > 1500)
    }

    @Test
    fun `carb cycling should maintain protein across days`() {
        // Given: Base macros
        val baseMacros = MacroCalculationResult(
            targetCalories = 2200,
            proteinGrams = 160,
            carbsGrams = 220,
            fatGrams = 75,
            bmr = 1700,
            tdee = 2200,
            goal = NutritionGoal.WEIGHT_GAIN
        )

        // When: Generating carb cycling plan
        val result = macroCalculator.generateCarbCyclingPlan(baseMacros, 5)

        // Then: Protein should be consistent across all days
        val highCarbDay = result.weekPlan[DayType.HIGH_CARB]!!
        val mediumCarbDay = result.weekPlan[DayType.MEDIUM_CARB]!!
        val lowCarbDay = result.weekPlan[DayType.LOW_CARB]!!
        
        assertEquals("High carb protein should match base", baseMacros.proteinGrams, highCarbDay.protein)
        assertEquals("Medium carb protein should match base", baseMacros.proteinGrams, mediumCarbDay.protein)
        assertEquals("Low carb protein should match base", baseMacros.proteinGrams, lowCarbDay.protein)
    }

    @Test
    fun `generateCarbCyclingPlan should validate input parameters`() {
        val baseMacros = MacroCalculationResult(
            targetCalories = 2000,
            proteinGrams = 150,
            carbsGrams = 200,
            fatGrams = 70,
            bmr = 1600,
            tdee = 2000,
            goal = NutritionGoal.MAINTENANCE
        )

        // Test invalid training days
        assertThrows(IllegalArgumentException::class.java) {
            macroCalculator.generateCarbCyclingPlan(baseMacros, 0)
        }

        assertThrows(IllegalArgumentException::class.java) {
            macroCalculator.generateCarbCyclingPlan(baseMacros, 8)
        }
    }
}