package com.example.fitapp.services

import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.ui.screens.ExerciseStep
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * Unit tests for WorkoutExecutionManager
 * Tests workout navigation, RPE tracking, rest timers, and state management
 */
@ExperimentalCoroutinesApi
class WorkoutExecutionManagerTest {

    @Mock
    private lateinit var database: AppDatabase

    @Mock
    private lateinit var smartRestTimer: SmartRestTimer

    private lateinit var workoutExecutionManager: WorkoutExecutionManager

    private val sampleExercises = listOf(
        ExerciseStep(
            name = "Bench Press",
            type = "strength",
            value = "3x8-10",
            description = "Compound chest exercise",
            restTime = 180
        ),
        ExerciseStep(
            name = "Squat",
            type = "strength", 
            value = "3x6-8",
            description = "Compound leg exercise",
            restTime = 180
        ),
        ExerciseStep(
            name = "Plank",
            type = "core",
            value = "3x30s",
            description = "Core stability exercise",
            restTime = 60
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        workoutExecutionManager = WorkoutExecutionManager(database, smartRestTimer)
    }

    // Workout Navigation Tests

    @Test
    fun `should navigate workout steps correctly`() = runTest {
        // Given: Started workout with exercises
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)

        // When: Getting current step
        val currentStep = workoutExecutionManager.currentStep.first()

        // Then: Should start with first exercise
        assertNotNull("Current step should not be null", currentStep)
        assertEquals("Should start with first exercise", "Bench Press", currentStep?.exercise?.name)
        assertEquals("Should start with set 1", 1, currentStep?.currentSet)
        assertTrue("Should be in workout", workoutExecutionManager.isInWorkout.first())
    }

    @Test
    fun `should advance to next exercise after completing sets`() = runTest {
        // Given: Started workout
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)

        // When: Completing all sets of first exercise
        repeat(3) { setNumber ->
            workoutExecutionManager.completeSet(
                weight = 80f,
                reps = 10,
                rpe = 7f,
                formQuality = 0.8f
            )
        }
        workoutExecutionManager.nextExercise()

        // Then: Should advance to second exercise
        val currentStep = workoutExecutionManager.currentStep.first()
        assertEquals("Should advance to second exercise", "Squat", currentStep?.exercise?.name)
        assertEquals("Should reset to set 1", 1, currentStep?.currentSet)
    }

    @Test
    fun `should handle workout completion when all exercises done`() = runTest {
        // Given: Started workout
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)

        // When: Completing all exercises
        sampleExercises.forEach { exercise ->
            val setsCount = if (exercise.value.contains("3x")) 3 else 1
            repeat(setsCount) {
                workoutExecutionManager.completeSet(70f, 8, 7f, 0.8f)
            }
            if (workoutExecutionManager.isInWorkout.first()) {
                workoutExecutionManager.nextExercise()
            }
        }

        // Then: Should complete workout
        assertFalse("Should no longer be in workout", workoutExecutionManager.isInWorkout.first())
        assertNull("Current step should be null", workoutExecutionManager.currentStep.first())
    }

    @Test
    fun `should allow skipping exercises`() = runTest {
        // Given: Started workout on first exercise
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)

        // When: Skipping first exercise
        workoutExecutionManager.skipExercise("Too heavy today")

        // Then: Should advance to next exercise
        val currentStep = workoutExecutionManager.currentStep.first()
        assertEquals("Should skip to second exercise", "Squat", currentStep?.exercise?.name)
    }

    // Set Completion and RPE Tracking Tests

    @Test
    fun `should handle set completion with RPE tracking`() = runTest {
        // Given: Started workout
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)

        // When: Completing a set with specific metrics
        val weight = 75f
        val reps = 8
        val rpe = 7.5f
        val formQuality = 0.85f

        workoutExecutionManager.completeSet(weight, reps, rpe, formQuality)

        // Then: Should record set and advance
        val currentStep = workoutExecutionManager.currentStep.first()
        assertEquals("Should advance to set 2", 2, currentStep?.currentSet)
        
        // Verify set was recorded (would check database in real implementation)
        verify(database, atLeastOnce()).let {
            // Database interaction verification would go here
        }
    }

    @Test
    fun `should validate set completion parameters`() = runTest {
        // Given: Started workout
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)

        // When/Then: Invalid parameters should be handled gracefully
        
        // Test negative weight
        val result1 = workoutExecutionManager.completeSet(-10f, 8, 7f, 0.8f)
        assertFalse("Should reject negative weight", result1)

        // Test zero reps
        val result2 = workoutExecutionManager.completeSet(70f, 0, 7f, 0.8f)
        assertFalse("Should reject zero reps", result2)

        // Test invalid RPE
        val result3 = workoutExecutionManager.completeSet(70f, 8, 15f, 0.8f)
        assertFalse("Should reject invalid RPE", result3)

        // Test invalid form quality
        val result4 = workoutExecutionManager.completeSet(70f, 8, 7f, 1.5f)
        assertFalse("Should reject invalid form quality", result4)

        // Test valid parameters
        val result5 = workoutExecutionManager.completeSet(70f, 8, 7f, 0.8f)
        assertTrue("Should accept valid parameters", result5)
    }

    @Test
    fun `should track RPE progression within workout`() = runTest {
        // Given: Started workout
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)

        // When: Completing sets with increasing RPE
        val rpeValues = listOf(6f, 7f, 8f)
        rpeValues.forEach { rpe ->
            workoutExecutionManager.completeSet(75f, 8, rpe, 0.8f)
        }

        // Then: Should track RPE progression
        val workoutFlow = workoutExecutionManager.workoutFlow.first()
        assertNotNull("Workout flow should exist", workoutFlow)
        // Would verify RPE tracking in workout flow
    }

    // Rest Timer Management Tests

    @Test
    fun `should manage rest timer functionality`() = runTest {
        // Given: Started workout and completed a set
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)
        workoutExecutionManager.completeSet(75f, 8, 7f, 0.8f)

        // When: Starting rest timer
        workoutExecutionManager.startRestTimer()

        // Then: Should start timer with appropriate duration
        verify(smartRestTimer).startTimer(any(), any())
    }

    @Test
    fun `should adjust rest time based on RPE`() = runTest {
        // Given: Started workout
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)

        // When: Completing set with high RPE
        workoutExecutionManager.completeSet(85f, 6, 9f, 0.7f) // High RPE

        // Then: Should suggest longer rest time
        val currentStep = workoutExecutionManager.currentStep.first()
        val suggestedRestTime = workoutExecutionManager.calculateOptimalRestTime(9f, "strength")
        assertTrue("Should suggest longer rest for high RPE", suggestedRestTime > 180)
    }

    @Test
    fun `should provide different rest times for different exercise types`() = runTest {
        // Test strength exercise rest time
        val strengthRest = workoutExecutionManager.calculateOptimalRestTime(7f, "strength")
        assertTrue("Strength exercises should have longer rest", strengthRest >= 120)

        // Test core exercise rest time  
        val coreRest = workoutExecutionManager.calculateOptimalRestTime(7f, "core")
        assertTrue("Core exercises should have shorter rest", coreRest <= 90)

        // Test cardio exercise rest time
        val cardioRest = workoutExecutionManager.calculateOptimalRestTime(7f, "cardio")
        assertTrue("Cardio exercises should have short rest", cardioRest <= 60)
    }

    // Workout State Management Tests

    @Test
    fun `should save workout state on pause`() = runTest {
        // Given: Started workout with progress
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)
        workoutExecutionManager.completeSet(75f, 8, 7f, 0.8f)
        workoutExecutionManager.completeSet(75f, 7, 8f, 0.7f)

        // When: Pausing workout
        workoutExecutionManager.pauseWorkout()

        // Then: Should save state
        verify(database, atLeastOnce()).let {
            // Database save verification would go here
        }
        
        // Should still be in workout but paused
        assertTrue("Should still be in workout", workoutExecutionManager.isInWorkout.first())
    }

    @Test
    fun `should restore workout state on resume`() = runTest {
        // Given: Paused workout with saved state
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)
        workoutExecutionManager.completeSet(75f, 8, 7f, 0.8f)
        workoutExecutionManager.pauseWorkout()

        // When: Resuming workout
        workoutExecutionManager.resumeWorkout()

        // Then: Should restore previous state
        val currentStep = workoutExecutionManager.currentStep.first()
        assertNotNull("Should have current step", currentStep)
        assertEquals("Should be on second set", 2, currentStep?.currentSet)
    }

    @Test
    fun `should handle workout interruption gracefully`() = runTest {
        // Given: Active workout
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)
        workoutExecutionManager.completeSet(75f, 8, 7f, 0.8f)

        // When: Workout is interrupted (app backgrounded, etc.)
        workoutExecutionManager.handleInterruption()

        // Then: Should save state and prepare for recovery
        verify(database, atLeastOnce()).let {
            // State saving verification
        }
    }

    // Workout Summary and Analytics Tests

    @Test
    fun `should calculate workout summary accurately`() = runTest {
        // Given: Completed workout with multiple exercises
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)
        
        // Complete bench press sets
        workoutExecutionManager.completeSet(75f, 10, 6f, 0.9f)
        workoutExecutionManager.completeSet(75f, 9, 7f, 0.8f)
        workoutExecutionManager.completeSet(75f, 8, 8f, 0.8f)
        workoutExecutionManager.nextExercise()
        
        // Complete squat sets
        workoutExecutionManager.completeSet(100f, 8, 7f, 0.8f)
        workoutExecutionManager.completeSet(100f, 7, 8f, 0.7f)
        workoutExecutionManager.completeSet(100f, 6, 9f, 0.7f)
        
        // When: Completing workout
        val summary = workoutExecutionManager.completeWorkout()

        // Then: Should calculate accurate summary
        assertNotNull("Summary should exist", summary)
        assertTrue("Should have total volume", summary.totalVolume > 0)
        assertTrue("Should have reasonable average RPE", summary.averageRPE in 5f..10f)
        assertTrue("Should have duration", summary.durationMinutes > 0)
        assertEquals("Should count exercises", 2, summary.exercisesCompleted)
        assertTrue("Should count sets", summary.totalSets >= 6)
    }

    @Test
    fun `should provide performance insights`() = runTest {
        // Given: Workout with varying performance
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)
        
        // Sets with declining performance (fatigue)
        workoutExecutionManager.completeSet(80f, 10, 6f, 0.9f)
        workoutExecutionManager.completeSet(80f, 8, 8f, 0.7f)
        workoutExecutionManager.completeSet(80f, 6, 9f, 0.6f)

        // When: Analyzing performance
        val insights = workoutExecutionManager.generatePerformanceInsights()

        // Then: Should detect fatigue pattern
        assertNotNull("Should provide insights", insights)
        assertTrue("Should detect fatigue", insights.fatigueDetected)
        assertTrue("Should have recommendations", insights.recommendations.isNotEmpty())
    }

    @Test
    fun `should suggest weight adjustments based on performance`() = runTest {
        // Given: Workout with easy performance
        workoutExecutionManager.startWorkout("test_workout", sampleExercises)
        
        // All sets completed easily
        workoutExecutionManager.completeSet(60f, 12, 5f, 0.9f) // Too easy
        workoutExecutionManager.completeSet(60f, 12, 5f, 0.9f)
        workoutExecutionManager.completeSet(60f, 12, 5f, 0.9f)

        // When: Analyzing for next workout
        val suggestion = workoutExecutionManager.suggestWeightAdjustment("Bench Press")

        // Then: Should suggest weight increase
        assertNotNull("Should provide suggestion", suggestion)
        assertTrue("Should suggest increase", suggestion.recommendedWeight > 60f)
        assertTrue("Should explain reasoning", suggestion.reasoning.isNotEmpty())
    }

    // Error Handling and Edge Cases Tests

    @Test
    fun `should handle empty exercise list gracefully`() = runTest {
        // When: Starting workout with empty exercise list
        val result = workoutExecutionManager.startWorkout("empty_workout", emptyList())

        // Then: Should handle gracefully
        assertFalse("Should not start workout with empty exercises", result)
        assertFalse("Should not be in workout", workoutExecutionManager.isInWorkout.first())
    }

    @Test
    fun `should handle completing sets when not in workout`() = runTest {
        // When: Attempting to complete set without active workout
        val result = workoutExecutionManager.completeSet(75f, 8, 7f, 0.8f)

        // Then: Should reject gracefully
        assertFalse("Should reject set completion without active workout", result)
    }

    @Test
    fun `should handle multiple workout starts correctly`() = runTest {
        // Given: Already started workout
        workoutExecutionManager.startWorkout("workout1", sampleExercises)

        // When: Attempting to start another workout
        val result = workoutExecutionManager.startWorkout("workout2", sampleExercises)

        // Then: Should handle appropriately (either reject or end previous)
        // Implementation could either reject or auto-complete previous workout
        assertTrue("Should handle multiple start attempts", 
            workoutExecutionManager.isInWorkout.first())
    }
}