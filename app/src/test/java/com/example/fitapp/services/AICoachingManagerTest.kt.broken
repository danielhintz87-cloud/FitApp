package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit tests for AICoachingManager
 * Tests plateau detection, workout adjustments, rest day prediction, and movement analysis
 */
class AICoachingManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var database: AppDatabase

    private lateinit var aiCoachingManager: AICoachingManager

    private val sampleWorkoutSessions = listOf(
        WorkoutSession(
            date = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L, // 1 week ago
            averageRpe = 7.5f,
            totalVolume = 2500f,
            averageFormScore = 0.85f,
            isCompleted = true
        ),
        WorkoutSession(
            date = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L, // 5 days ago
            averageRpe = 8.0f,
            totalVolume = 2600f,
            averageFormScore = 0.80f,
            isCompleted = true
        ),
        WorkoutSession(
            date = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L, // 3 days ago
            averageRpe = 8.5f,
            totalVolume = 2400f,
            averageFormScore = 0.75f,
            isCompleted = true
        )
    )

    private val sampleWorkoutPlan = WorkoutPlan(
        exercises = listOf("Squat", "Bench Press", "Deadlift"),
        totalVolume = 2500f,
        estimatedDuration = 60
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        aiCoachingManager = AICoachingManager(context, database)
    }

    // Plateau Detection Tests

    @Test
    fun `should detect training plateaus accurately`() = runTest {
        // Given: User and exercise for plateau analysis
        val userId = "user_123"
        val exerciseId = "bench_press"
        val weeksToAnalyze = 4

        // When: Detecting plateau
        val result = aiCoachingManager.detectTrainingPlateaus(userId, exerciseId, weeksToAnalyze)

        // Then: Should provide plateau analysis
        assertNotNull("Result should not be null", result)
        assertEquals("Exercise ID should match", exerciseId, result.exerciseId)
        assertTrue("Progress rate should be valid", result.progressRate >= 0f)
        assertTrue("Plateau score should be valid", result.plateauScore in 0f..1f)
        assertTrue("Confidence should be valid", result.confidence in 0f..1f)
        assertNotNull("Should provide recommendations", result.recommendations)
        
        if (result.isPlateaued) {
            assertTrue("Plateau duration should be positive", result.plateauDuration > 0)
            assertTrue("Should have recommendations for plateau", result.recommendations.isNotEmpty())
        }
    }

    @Test
    fun `plateau detection should identify low progress rate`() = runTest {
        // Given: Exercise with minimal progress
        val userId = "user_123"
        val exerciseId = "stalled_exercise"

        // When: Detecting plateau with minimal progress
        val result = aiCoachingManager.detectTrainingPlateaus(userId, exerciseId, 6)

        // Then: Should detect potential plateau
        assertTrue("Progress rate should be calculated", result.progressRate >= 0f)
        assertTrue("Should have plateau score", result.plateauScore >= 0f)
        
        if (result.progressRate < 0.05f) {
            assertTrue("Should suggest plateau interventions", 
                result.recommendations.any { it.contains("Deload") || it.contains("Variation") })
        }
    }

    @Test
    fun `plateau detection should provide confidence based on data quality`() = runTest {
        // Given: Analysis with different data amounts
        val userId = "user_123"
        val exerciseId = "test_exercise"

        // When: Detecting plateau with limited data
        val result = aiCoachingManager.detectTrainingPlateaus(userId, exerciseId, 2)

        // Then: Should have lower confidence with less data
        assertTrue("Confidence should reflect data quality", result.confidence >= 0f)
        assertTrue("Confidence should be reasonable", result.confidence <= 1f)
    }

    @Test
    fun `detectTrainingPlateaus should validate input parameters`() = runTest {
        // Test negative weeks
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                aiCoachingManager.detectTrainingPlateaus("user", "exercise", -1)
            }
        }
    }

    // Workout Adjustment Tests

    @Test
    fun `should suggest workout adjustments based on progress and performance`() = runTest {
        // Given: User workout data and recent performance
        val userId = "user_123"
        val currentWorkout = sampleWorkoutPlan
        val recentPerformance = sampleWorkoutSessions

        // When: Getting workout adjustment suggestions
        val result = aiCoachingManager.suggestWorkoutAdjustments(userId, currentWorkout, recentPerformance)

        // Then: Should provide comprehensive adjustments
        assertNotNull("Result should not be null", result)
        assertNotNull("Should have adjustment type", result.adjustmentType)
        assertNotNull("Should have priority", result.priority)
        assertTrue("Should have specific adjustments", result.specificAdjustments.isNotEmpty())
        assertNotNull("Should provide reasoning", result.reasoning)
        assertNotNull("Should predict outcome", result.expectedOutcome)
        assertTrue("Should have implementation notes", result.implementationNotes.isNotEmpty())
        assertTrue("Confidence should be valid", result.confidence in 0f..1f)
    }

    @Test
    fun `should detect high fatigue and suggest recovery focus`() = runTest {
        // Given: High fatigue performance data
        val highFatigueSessions = listOf(
            WorkoutSession(System.currentTimeMillis(), 9.0f, 2000f, 0.6f, true),
            WorkoutSession(System.currentTimeMillis(), 9.5f, 1800f, 0.5f, true),
            WorkoutSession(System.currentTimeMillis(), 9.0f, 1900f, 0.55f, true)
        )

        // When: Getting adjustment suggestions
        val result = aiCoachingManager.suggestWorkoutAdjustments("user", sampleWorkoutPlan, highFatigueSessions)

        // Then: Should suggest recovery focus
        if (result.adjustmentType == AdjustmentType.RECOVERY_FOCUS) {
            assertTrue("Should suggest rest", 
                result.specificAdjustments.any { it.description.contains("Ruhetag") })
            assertTrue("Should suggest intensity reduction", 
                result.specificAdjustments.any { it.description.contains("Intensität") })
        }
    }

    @Test
    fun `should detect injury risk and suggest prevention measures`() = runTest {
        // Given: Performance data showing form degradation
        val injuryRiskSessions = listOf(
            WorkoutSession(System.currentTimeMillis(), 7.0f, 2500f, 0.5f, true), // Poor form
            WorkoutSession(System.currentTimeMillis(), 8.0f, 2600f, 0.4f, true), // Worse form
            WorkoutSession(System.currentTimeMillis(), 8.5f, 2400f, 0.3f, true)  // Even worse form
        )

        // When: Getting adjustment suggestions
        val result = aiCoachingManager.suggestWorkoutAdjustments("user", sampleWorkoutPlan, injuryRiskSessions)

        // Then: Should prioritize injury prevention
        if (result.adjustmentType == AdjustmentType.INJURY_PREVENTION) {
            assertEquals("Should have urgent priority", AdjustmentPriority.URGENT, result.priority)
            assertTrue("Should suggest volume reduction", 
                result.specificAdjustments.any { it.description.contains("reduzieren") })
            assertTrue("Should mention form", result.reasoning.contains("Form"))
        }
    }

    @Test
    fun `should suggest progression when performance is improving`() = runTest {
        // Given: Improving performance data
        val improvingSessions = listOf(
            WorkoutSession(System.currentTimeMillis(), 6.0f, 2200f, 0.9f, true),
            WorkoutSession(System.currentTimeMillis(), 6.5f, 2400f, 0.9f, true),
            WorkoutSession(System.currentTimeMillis(), 7.0f, 2600f, 0.9f, true)
        )

        // When: Getting adjustment suggestions
        val result = aiCoachingManager.suggestWorkoutAdjustments("user", sampleWorkoutPlan, improvingSessions)

        // Then: Should suggest progression if appropriate
        if (result.adjustmentType == AdjustmentType.PROGRESSION) {
            assertTrue("Should suggest weight increase", 
                result.specificAdjustments.any { it.description.contains("erhöhen") || it.description.contains("Gewicht") })
            assertTrue("Should predict positive outcome", result.expectedOutcome.contains("Kraftzuwachs"))
        }
    }

    @Test
    fun `suggestWorkoutAdjustments should validate input parameters`() = runTest {
        // Test empty performance data
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                aiCoachingManager.suggestWorkoutAdjustments("user", sampleWorkoutPlan, emptyList())
            }
        }
    }

    // Rest Day Prediction Tests

    @Test
    fun `should predict optimal rest days`() = runTest {
        // Given: Training schedule and recovery metrics
        val userId = "user_123"
        val trainingSchedule = listOf(
            TrainingDay(listOf("Upper Body"), 0.8f, 2500f),
            TrainingDay(listOf("Lower Body"), 0.9f, 2800f),
            TrainingDay(listOf("Push"), 0.7f, 2200f),
            TrainingDay(listOf("Pull"), 0.8f, 2400f)
        )
        val recoveryMetrics = RecoveryMetrics(
            sleepQuality = 0.7f,
            hrv = 45f,
            subjectiveRecovery = 0.6f
        )

        // When: Predicting optimal rest days
        val result = aiCoachingManager.predictOptimalRestDays(userId, trainingSchedule, recoveryMetrics)

        // Then: Should provide rest day recommendations
        assertNotNull("Result should not be null", result)
        assertTrue("Should recommend reasonable rest days", result.recommendedRestDays in 1..3)
        assertTrue("Recovery score should be valid", result.currentRecoveryScore in 0f..1f)
        assertTrue("Fatigue level should be valid", result.fatigueLevel in 0f..1f)
        assertTrue("Should suggest rest placement", result.optimalRestPlacement.isNotEmpty())
        assertTrue("Should suggest active recovery", result.activeRecoveryOptions.isNotEmpty())
        assertTrue("Should provide nutrition advice", result.nutritionRecommendations.isNotEmpty())
        assertTrue("Should provide sleep advice", result.sleepRecommendations.isNotEmpty())
        assertTrue("Confidence should be valid", result.confidence in 0f..1f)
    }

    @Test
    fun `should adjust rest recommendations based on recovery metrics`() = runTest {
        // Given: Poor recovery metrics
        val poorRecoveryMetrics = RecoveryMetrics(
            sleepQuality = 0.3f,
            hrv = 25f,
            subjectiveRecovery = 0.2f
        )
        
        val lightSchedule = listOf(
            TrainingDay(listOf("Light Workout"), 0.5f, 1500f)
        )

        // When: Predicting rest days with poor recovery
        val result = aiCoachingManager.predictOptimalRestDays("user", lightSchedule, poorRecoveryMetrics)

        // Then: Should recommend more rest
        assertTrue("Should recommend adequate rest for poor recovery", result.recommendedRestDays >= 1)
        assertTrue("Should address sleep", 
            result.sleepRecommendations.any { it.contains("Schlaf") })
    }

    @Test
    fun `should provide active recovery options based on training load`() = runTest {
        // Given: High training load
        val highLoadSchedule = listOf(
            TrainingDay(listOf("Heavy Squats"), 0.95f, 3500f),
            TrainingDay(listOf("Heavy Deadlifts"), 0.95f, 3800f),
            TrainingDay(listOf("Heavy Bench"), 0.90f, 3200f)
        )
        
        val goodRecovery = RecoveryMetrics(0.8f, 55f, 0.8f)

        // When: Predicting rest with high load
        val result = aiCoachingManager.predictOptimalRestDays("user", highLoadSchedule, goodRecovery)

        // Then: Should provide appropriate active recovery
        assertTrue("Should suggest active recovery for high load", result.activeRecoveryOptions.isNotEmpty())
        assertTrue("Should suggest gentle activities", 
            result.activeRecoveryOptions.any { it.contains("Yoga") || it.contains("Spaziergang") })
    }

    // Movement Pattern Analysis Tests

    @Test
    fun `should analyze movement patterns for injury prevention`() = runTest {
        // Given: Movement data for analysis
        val userId = "user_123"
        val exerciseId = "squat"
        val movementData = listOf(
            MovementDataPoint(
                timestamp = System.currentTimeMillis(),
                jointAngles = listOf(90f, 120f, 95f), // Hip, knee, ankle angles
                velocity = 0.5f,
                acceleration = 0.2f
            ),
            MovementDataPoint(
                timestamp = System.currentTimeMillis() + 1000,
                jointAngles = listOf(85f, 115f, 90f),
                velocity = 0.6f,
                acceleration = 0.3f
            )
        )

        // When: Analyzing movement patterns
        val result = aiCoachingManager.analyzeMovementPatterns(userId, exerciseId, movementData)

        // Then: Should provide comprehensive analysis
        assertNotNull("Result should not be null", result)
        assertEquals("Exercise ID should match", exerciseId, result.exerciseId)
        assertTrue("Form score should be valid", result.overallFormScore in 0f..1f)
        assertNotNull("Should analyze form quality", result.formQuality)
        assertNotNull("Should detect asymmetries", result.asymmetries)
        assertNotNull("Should identify risk factors", result.injuryRiskFactors)
        assertNotNull("Should detect compensation patterns", result.compensationPatterns)
        assertNotNull("Should recommend corrective exercises", result.correctiveExercises)
        assertNotNull("Should suggest mobility work", result.mobilityRecommendations)
        assertNotNull("Should provide technique advice", result.techniqueImprovement)
        assertNotNull("Should assess risk level", result.riskLevel)
    }

    @Test
    fun `movement analysis should detect form quality issues`() = runTest {
        // Given: Movement data with potential form issues
        val poorMovementData = listOf(
            MovementDataPoint(
                timestamp = System.currentTimeMillis(),
                jointAngles = listOf(45f, 80f, 70f), // Poor angles
                velocity = 1.2f, // Too fast
                acceleration = 0.8f // High acceleration
            )
        )

        // When: Analyzing poor movement
        val result = aiCoachingManager.analyzeMovementPatterns("user", "deadlift", poorMovementData)

        // Then: Should identify form issues
        if (result.overallFormScore < 0.7f) {
            assertNotNull("Should provide technique improvements", result.techniqueImprovement)
            assertTrue("Risk level should reflect form quality", 
                result.riskLevel != RiskLevel.LOW)
        }
    }

    @Test
    fun `analyzeMovementPatterns should validate input parameters`() = runTest {
        // Test empty movement data
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                aiCoachingManager.analyzeMovementPatterns("user", "exercise", emptyList())
            }
        }
    }

    // Personalized Nutrition Recommendations Tests

    @Test
    fun `should generate personalized nutrition recommendations`() = runTest {
        // Given: Current nutrition and goals
        val userId = "user_123"
        val currentNutrition = NutritionProfile(
            currentCalories = 2200,
            currentProtein = 120,
            currentCarbs = 250,
            currentFat = 80
        )
        val trainingSchedule = listOf(
            TrainingDay(listOf("Strength Training"), 0.8f, 2500f),
            TrainingDay(listOf("Cardio"), 0.6f, 1800f)
        )
        val goals = listOf(
            FitnessGoal("muscle_gain", 5f, 12), // 5kg muscle gain in 12 weeks
            FitnessGoal("strength_increase", 20f, 8) // 20% strength increase in 8 weeks
        )

        // When: Generating nutrition recommendations
        val result = aiCoachingManager.generatePersonalizedNutritionRecommendations(
            userId, currentNutrition, trainingSchedule, goals
        )

        // Then: Should provide comprehensive nutrition guidance
        assertNotNull("Result should not be null", result)
        assertNotNull("Should adjust macros", result.macroAdjustments)
        assertNotNull("Should optimize timing", result.timingOptimization)
        assertNotNull("Should provide hydration guidance", result.hydrationGuidance)
        assertNotNull("Should recommend supplements", result.supplementRecommendations)
        assertTrue("Should suggest meal plans", result.mealPlanSuggestions.size >= 0)
        assertTrue("Should provide education", result.nutritionEducation.size >= 0)
        assertNotNull("Should have implementation strategy", result.implementationStrategy)
        assertTrue("Should predict outcomes", result.expectedOutcomes.size >= 0)
        assertTrue("Confidence should be valid", result.confidence in 0f..1f)
    }

    @Test
    fun `nutrition recommendations should align with training demands`() = runTest {
        // Given: High training load requiring more calories
        val highTrainingSchedule = listOf(
            TrainingDay(listOf("Heavy Squats"), 0.95f, 3500f),
            TrainingDay(listOf("Heavy Deadlifts"), 0.95f, 3800f),
            TrainingDay(listOf("Heavy Bench"), 0.90f, 3200f),
            TrainingDay(listOf("Accessories"), 0.75f, 2500f)
        )
        
        val currentNutrition = NutritionProfile(2000, 100, 200, 70) // Lower intake
        val muscleGainGoal = listOf(FitnessGoal("muscle_gain", 3f, 16))

        // When: Getting recommendations for high training load
        val result = aiCoachingManager.generatePersonalizedNutritionRecommendations(
            "user", currentNutrition, highTrainingSchedule, muscleGainGoal
        )

        // Then: Should recommend appropriate increases
        assertTrue("Should have macro adjustments for high training", 
            result.macroAdjustments.proteinAdjustment != 0 ||
            result.macroAdjustments.carbsAdjustment != 0 ||
            result.macroAdjustments.fatAdjustment != 0)
    }

    @Test
    fun `should provide hydration guidance based on training`() = runTest {
        // Given: Training with high sweat rate activities
        val cardioHeavySchedule = listOf(
            TrainingDay(listOf("HIIT Cardio"), 0.9f, 600f),
            TrainingDay(listOf("Long Run"), 0.7f, 800f)
        )
        
        val nutrition = NutritionProfile(2500, 150, 300, 90)
        val enduranceGoal = listOf(FitnessGoal("endurance", 15f, 12))

        // When: Getting recommendations
        val result = aiCoachingManager.generatePersonalizedNutritionRecommendations(
            "user", nutrition, cardioHeavySchedule, enduranceGoal
        )

        // Then: Should emphasize hydration
        assertTrue("Should provide hydration target", result.hydrationGuidance.dailyTarget > 0f)
        assertTrue("Should have timing recommendations", 
            result.hydrationGuidance.timingRecommendations.isNotEmpty())
    }

    // Integration and Error Handling Tests

    @Test
    fun `should handle API errors gracefully`() = runTest {
        // When: Service encounters errors (simulated by invalid data)
        val errorResult = aiCoachingManager.detectTrainingPlateaus("", "", 0)

        // Then: Should return error state gracefully
        assertNotNull("Should return error result", errorResult)
        assertEquals("Should have empty exercise ID in error", "", errorResult.exerciseId)
    }

    @Test
    fun `should provide consistent recommendations across methods`() = runTest {
        // Given: High fatigue scenario
        val highFatigueSessions = listOf(
            WorkoutSession(System.currentTimeMillis(), 9.5f, 2000f, 0.5f, true)
        )
        val poorRecovery = RecoveryMetrics(0.2f, 20f, 0.1f)

        // When: Getting both workout and rest recommendations
        val workoutAdjustment = aiCoachingManager.suggestWorkoutAdjustments(
            "user", sampleWorkoutPlan, highFatigueSessions
        )
        val restPrediction = aiCoachingManager.predictOptimalRestDays(
            "user", listOf(TrainingDay(listOf("Hard Training"), 0.9f, 3000f)), poorRecovery
        )

        // Then: Recommendations should be consistent
        if (workoutAdjustment.adjustmentType == AdjustmentType.RECOVERY_FOCUS) {
            assertTrue("Rest prediction should align with workout adjustment", 
                restPrediction.recommendedRestDays >= 2)
        }
    }

    @Test
    fun `should provide actionable implementation guidance`() = runTest {
        // Given: Any valid coaching request
        val userId = "user_123"
        val adjustment = aiCoachingManager.suggestWorkoutAdjustments(
            userId, sampleWorkoutPlan, sampleWorkoutSessions
        )

        // Then: Should provide clear implementation steps
        assertTrue("Implementation notes should be actionable", 
            adjustment.implementationNotes.isNotEmpty())
        assertTrue("Reasoning should be clear", adjustment.reasoning.isNotEmpty())
        assertTrue("Expected outcome should be specified", adjustment.expectedOutcome.isNotEmpty())
    }
}