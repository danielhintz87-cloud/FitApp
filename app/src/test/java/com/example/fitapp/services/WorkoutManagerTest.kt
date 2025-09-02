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
 * Unit tests for WorkoutManager
 * Tests critical workout management logic and business rules
 */
class WorkoutManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var database: AppDatabase

    private lateinit var workoutManager: WorkoutManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        workoutManager = WorkoutManager(context, database)
    }

    // Progressive Overload Calculation Tests

    @Test
    fun `should calculate progressive overload correctly when form is good and rpe low`() = runTest {
        // Given: Good form and manageable RPE
        val exerciseId = "bench_press"
        val currentWeight = 50f
        val currentReps = 8
        val currentSets = 3
        val formScore = 0.8f // Good form
        val rpeScore = 7f // Manageable RPE

        // When: Calculating progressive overload
        val result = workoutManager.calculateProgressiveOverload(
            exerciseId, currentWeight, currentReps, currentSets, formScore, rpeScore
        )

        // Then: Should recommend weight increase
        assertEquals(ProgressionType.WEIGHT_INCREASE, result.type)
        assertEquals(52.5f, result.newWeight, 0.1f)
        assertEquals(currentReps, result.newReps)
        assertEquals(currentSets, result.newSets)
        assertTrue("Reason should mention form", result.reason.contains("Form"))
    }

    @Test
    fun `should suggest rep increase when weight progression not suitable`() = runTest {
        // Given: Good form but weight near limit
        val exerciseId = "squat"
        val currentWeight = 95f // Near 100f limit
        val currentReps = 6
        val currentSets = 3
        val formScore = 0.7f
        val rpeScore = 6f // Lower RPE suitable for rep increase

        // When: Calculating progressive overload
        val result = workoutManager.calculateProgressiveOverload(
            exerciseId, currentWeight, currentReps, currentSets, formScore, rpeScore
        )

        // Then: Should recommend rep increase
        assertEquals(ProgressionType.REP_INCREASE, result.type)
        assertEquals(currentWeight, result.newWeight, 0.1f)
        assertEquals(7, result.newReps)
        assertEquals(currentSets, result.newSets)
    }

    @Test
    fun `should maintain when form is poor`() = runTest {
        // Given: Poor form score
        val exerciseId = "deadlift"
        val currentWeight = 80f
        val currentReps = 5
        val currentSets = 3
        val formScore = 0.4f // Poor form
        val rpeScore = 6f

        // When: Calculating progressive overload
        val result = workoutManager.calculateProgressiveOverload(
            exerciseId, currentWeight, currentReps, currentSets, formScore, rpeScore
        )

        // Then: Should maintain current parameters
        assertEquals(ProgressionType.MAINTAIN, result.type)
        assertEquals(currentWeight, result.newWeight, 0.1f)
        assertEquals(currentReps, result.newReps)
        assertEquals(currentSets, result.newSets)
        assertTrue("Should focus on form", result.reason.contains("Bewegungsqualität"))
    }

    @Test
    fun `should recommend deload when rpe too high`() = runTest {
        // Given: Very high RPE indicating overreaching
        val exerciseId = "overhead_press"
        val currentWeight = 40f
        val currentReps = 5
        val currentSets = 3
        val formScore = 0.7f
        val rpeScore = 9.5f // Very high RPE

        // When: Calculating progressive overload
        val result = workoutManager.calculateProgressiveOverload(
            exerciseId, currentWeight, currentReps, currentSets, formScore, rpeScore
        )

        // Then: Should recommend deload
        assertEquals(ProgressionType.DELOAD, result.type)
        assertEquals(36f, result.newWeight, 0.1f) // 90% of current
        assertEquals(currentReps, result.newReps)
        assertEquals(currentSets, result.newSets)
        assertTrue("Should mention deload", result.reason.contains("Deload"))
    }

    @Test
    fun `calculateProgressiveOverload should validate input parameters`() = runTest {
        // Test negative weight
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                workoutManager.calculateProgressiveOverload("test", -10f, 5, 3, 0.8f, 7f)
            }
        }

        // Test zero reps
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                workoutManager.calculateProgressiveOverload("test", 50f, 0, 3, 0.8f, 7f)
            }
        }

        // Test invalid form score
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                workoutManager.calculateProgressiveOverload("test", 50f, 5, 3, 1.5f, 7f)
            }
        }

        // Test invalid RPE score
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                workoutManager.calculateProgressiveOverload("test", 50f, 5, 3, 0.8f, 11f)
            }
        }
    }

    // Workout Set Logging Tests

    @Test
    fun `should log workout sets with proper validation`() = runTest {
        // Given: Valid workout set data
        val exerciseId = "bench_press"
        val weight = 70f
        val reps = 10
        val rpe = 8f
        val formScore = 0.85f

        // When: Logging workout set
        val result = workoutManager.logWorkoutSet(exerciseId, weight, reps, rpe, formScore)

        // Then: Should log successfully
        assertTrue("Should log set successfully", result)
    }

    @Test
    fun `logWorkoutSet should validate input parameters`() = runTest {
        // Test negative weight
        val result1 = workoutManager.logWorkoutSet("test", -10f, 5, 7f, 0.8f)
        assertFalse("Should fail with negative weight", result1)

        // Test zero reps
        val result2 = workoutManager.logWorkoutSet("test", 50f, 0, 7f, 0.8f)
        assertFalse("Should fail with zero reps", result2)

        // Test invalid RPE
        val result3 = workoutManager.logWorkoutSet("test", 50f, 5, 15f, 0.8f)
        assertFalse("Should fail with invalid RPE", result3)

        // Test invalid form score
        val result4 = workoutManager.logWorkoutSet("test", 50f, 5, 7f, 2f)
        assertFalse("Should fail with invalid form score", result4)
    }

    // Training Plateau Detection Tests

    @Test
    fun `should detect training plateau accurately`() = runTest {
        // Given: Exercise ID for plateau analysis
        val exerciseId = "squat"
        val weeksToAnalyze = 4

        // When: Detecting training plateau
        val result = workoutManager.detectTrainingPlateau(exerciseId, weeksToAnalyze)

        // Then: Should return valid plateau detection result
        assertNotNull("Result should not be null", result)
        assertTrue("Plateau percentage should be valid", result.plateauPercentage >= 0f)
        assertTrue("Weeks in plateau should be valid", result.weeksInPlateau >= 0)
        assertNotNull("Recommendation should not be null", result.recommendation)
    }

    // Rest Period Suggestion Tests

    @Test
    fun `should suggest rest periods based on intensity`() {
        // Test compound movement with high RPE
        val restRecommendation1 = workoutManager.suggestRestPeriod(9f, "legs", "compound")
        assertTrue("Should suggest longer rest for high RPE compound", restRecommendation1.recommendedSeconds >= 180)

        // Test isolation movement with moderate RPE
        val restRecommendation2 = workoutManager.suggestRestPeriod(6f, "biceps", "isolation")
        assertTrue("Should suggest shorter rest for isolation", restRecommendation2.recommendedSeconds <= 150)

        // Test cardio movement
        val restRecommendation3 = workoutManager.suggestRestPeriod(7f, "full_body", "cardio")
        assertTrue("Should suggest short rest for cardio", restRecommendation3.recommendedSeconds <= 90)
    }

    @Test
    fun `rest period suggestions should include valid ranges`() {
        val restRecommendation = workoutManager.suggestRestPeriod(8f, "chest", "compound")
        
        assertTrue("Min seconds should be less than recommended", 
            restRecommendation.minSeconds < restRecommendation.recommendedSeconds)
        assertTrue("Max seconds should be greater than recommended", 
            restRecommendation.maxSeconds > restRecommendation.recommendedSeconds)
        assertNotNull("Should include reason", restRecommendation.reason)
    }

    // Workout Completion Flow Tests

    @Test
    fun `should handle workout completion flow`() = runTest {
        // Given: Valid workout completion data
        val workoutId = "workout_123"
        val totalSets = 15
        val totalVolume = 2500f
        val averageRpe = 7.5f
        val duration = 3600000L // 1 hour

        // When: Completing workout
        val result = workoutManager.completeWorkout(workoutId, totalSets, totalVolume, averageRpe, duration)

        // Then: Should return valid completion result
        assertEquals(workoutId, result.workoutId)
        assertEquals(totalSets, result.totalSets)
        assertEquals(totalVolume, result.totalVolume, 0.1f)
        assertEquals(averageRpe, result.averageRpe, 0.1f)
        assertEquals(duration, result.duration)
        assertTrue("Completion time should be recent", result.completedAt > 0)
        assertTrue("Workout quality should be valid", result.workoutQuality in 0f..1f)
        assertNotNull("Should have next workout recommendation", result.nextWorkoutRecommendation)
    }

    @Test
    fun `completeWorkout should validate input parameters`() = runTest {
        // Test negative total sets
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                workoutManager.completeWorkout("test", -5, 1000f, 7f, 3600000L)
            }
        }

        // Test negative volume
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                workoutManager.completeWorkout("test", 10, -1000f, 7f, 3600000L)
            }
        }

        // Test invalid RPE
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                workoutManager.completeWorkout("test", 10, 1000f, 15f, 3600000L)
            }
        }

        // Test negative duration
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                workoutManager.completeWorkout("test", 10, 1000f, 7f, -1000L)
            }
        }
    }

    // Exercise Form Scoring Tests

    @Test
    fun `should validate exercise form scoring`() {
        // Given: Valid movement pattern and measurements
        val movementPattern = MovementPattern(
            name = "squat",
            validAngleRange = 90f..180f,
            optimalTempo = 2f..4f
        )
        val validJointAngles = listOf(120f, 150f, 95f)
        val optimalTempo = 3f

        // When: Validating exercise form
        val result = workoutManager.validateExerciseForm(movementPattern, validJointAngles, optimalTempo)

        // Then: Should return positive validation
        assertTrue("Form should be valid", result.isValid)
        assertTrue("Overall score should be high", result.overallScore >= 0.8f)
        assertNotNull("Should provide feedback", result.feedback)
        assertTrue("Should have minimal corrections", result.corrections.isEmpty())
    }

    @Test
    fun `should detect invalid exercise form`() {
        // Given: Invalid movement pattern
        val movementPattern = MovementPattern(
            name = "deadlift",
            validAngleRange = 90f..180f,
            optimalTempo = 2f..4f
        )
        val invalidJointAngles = listOf(45f, 200f, 70f) // Outside valid range
        val poorTempo = 6f // Too slow

        // When: Validating exercise form
        val result = workoutManager.validateExerciseForm(movementPattern, invalidJointAngles, poorTempo)

        // Then: Should detect form issues
        assertFalse("Form should be invalid", result.isValid)
        assertTrue("Overall score should be lower", result.overallScore < 0.8f)
        assertNotNull("Should provide feedback", result.feedback)
    }

    @Test
    fun `workout quality calculation should be reasonable`() = runTest {
        // Test high quality workout
        val highQualityResult = workoutManager.completeWorkout("test1", 20, 3000f, 8f, 3600000L)
        assertTrue("High quality workout should score well", highQualityResult.workoutQuality > 0.6f)

        // Test low quality workout
        val lowQualityResult = workoutManager.completeWorkout("test2", 5, 500f, 3f, 900000L)
        assertTrue("Low quality workout should score lower", lowQualityResult.workoutQuality < 0.8f)
    }

    @Test
    fun `next workout recommendations should be appropriate`() = runTest {
        // Test excellent workout
        val excellentResult = workoutManager.completeWorkout("test1", 25, 4000f, 9f, 4500000L)
        assertTrue("Should suggest progression for excellent workout", 
            excellentResult.nextWorkoutRecommendation.contains("Steigerung") ||
            excellentResult.nextWorkoutRecommendation.contains("Ausgezeichnet"))

        // Test easy workout
        val easyResult = workoutManager.completeWorkout("test2", 10, 1000f, 4f, 2700000L)
        assertTrue("Should suggest intensity increase for easy workout",
            easyResult.nextWorkoutRecommendation.contains("Intensität") ||
            easyResult.nextWorkoutRecommendation.contains("leicht"))
    }
}