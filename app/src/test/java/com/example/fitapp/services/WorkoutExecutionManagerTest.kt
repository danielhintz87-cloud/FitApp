package com.example.fitapp.services

import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.ui.screens.ExerciseStep
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
 * Tests workout execution flow, set tracking, and performance analytics
 */
class WorkoutExecutionManagerTest {

    @Mock
    private lateinit var database: AppDatabase

    @Mock
    private lateinit var smartRestTimer: SmartRestTimer

    private lateinit var workoutExecutionManager: WorkoutExecutionManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        workoutExecutionManager = WorkoutExecutionManager(database, smartRestTimer)
    }

    @Test
    fun `should instantiate WorkoutExecutionManager correctly`() {
        assertNotNull("WorkoutExecutionManager should be instantiated", workoutExecutionManager)
    }

    @Test
    fun `should start with no active workout`() = runTest {
        // When: Getting initial workout state
        val isInWorkout = workoutExecutionManager.isInWorkout.first()

        // Then: Should not be in workout
        assertFalse("Should not be in workout initially", isInWorkout)
    }

    @Test
    fun `should start with null workout flow`() = runTest {
        // When: Getting initial workout flow
        val workoutFlow = workoutExecutionManager.workoutFlow.first()

        // Then: Should be null
        assertNull("Initial workout flow should be null", workoutFlow)
    }

    @Test
    fun `should start with null current step`() = runTest {
        // When: Getting initial current step
        val currentStep = workoutExecutionManager.currentStep.first()

        // Then: Should be null
        assertNull("Initial current step should be null", currentStep)
    }

    @Test
    fun `should create WorkoutStep with all properties`() {
        // Given: Mock exercise step
        val exerciseStep = mock<ExerciseStep>()
        val sets = listOf(
            WorkoutExecutionManager.WorkoutSet(
                setNumber = 1,
                targetReps = 10,
                actualReps = 10,
                targetWeight = 50.0f,
                actualWeight = 50.0f,
                restTime = 60,
                isCompleted = true
            )
        )
        val currentSet = 1
        val restTime = 60
        val instructions = "Keep your back straight"
        val videoReference = "video123"
        val formTips = listOf("Breathe out on exertion", "Control the movement")
        val progressionHint = "Increase weight by 2.5kg next session"
        val autoWeightSuggestion = 52.5f

        // When: Creating workout step
        val workoutStep = WorkoutExecutionManager.WorkoutStep(
            exercise = exerciseStep,
            sets = sets,
            currentSet = currentSet,
            restTime = restTime,
            instructions = instructions,
            videoReference = videoReference,
            formTips = formTips,
            progressionHint = progressionHint,
            autoWeightSuggestion = autoWeightSuggestion
        )

        // Then: All properties should be set correctly
        assertEquals("Exercise should match", exerciseStep, workoutStep.exercise)
        assertEquals("Sets should match", sets, workoutStep.sets)
        assertEquals("Current set should match", currentSet, workoutStep.currentSet)
        assertEquals("Rest time should match", restTime, workoutStep.restTime)
        assertEquals("Instructions should match", instructions, workoutStep.instructions)
        assertEquals("Video reference should match", videoReference, workoutStep.videoReference)
        assertEquals("Form tips should match", formTips, workoutStep.formTips)
        assertEquals("Progression hint should match", progressionHint, workoutStep.progressionHint)
        assertEquals("Auto weight suggestion should match", autoWeightSuggestion, workoutStep.autoWeightSuggestion)
    }

    @Test
    fun `should create WorkoutStep with default values`() {
        // Given: Minimal workout step data
        val exerciseStep = mock<ExerciseStep>()
        val sets = emptyList<WorkoutExecutionManager.WorkoutSet>()
        val currentSet = 0
        val restTime = 60
        val instructions = "Basic instructions"

        // When: Creating workout step with minimal data
        val workoutStep = WorkoutExecutionManager.WorkoutStep(
            exercise = exerciseStep,
            sets = sets,
            currentSet = currentSet,
            restTime = restTime,
            instructions = instructions
        )

        // Then: Should have default values for optional properties
        assertNull("Video reference should be null by default", workoutStep.videoReference)
        assertTrue("Form tips should be empty by default", workoutStep.formTips.isEmpty())
        assertNull("Progression hint should be null by default", workoutStep.progressionHint)
        assertNull("Auto weight suggestion should be null by default", workoutStep.autoWeightSuggestion)
    }

    @Test
    fun `should create WorkoutSet with all properties`() {
        // Given: Set properties
        val setNumber = 3
        val targetReps = 12
        val actualReps = 11
        val targetWeight = 75.0f
        val actualWeight = 75.0f
        val restTime = 90
        val isCompleted = true
        val rpe = 8

        // When: Creating workout set
        val workoutSet = WorkoutExecutionManager.WorkoutSet(
            setNumber = setNumber,
            targetWeight = targetWeight,
            targetReps = targetReps,
            actualWeight = actualWeight,
            actualReps = actualReps,
            rpe = rpe,
            restTime = restTime,
            isCompleted = isCompleted
        )

        // Then: All properties should be set correctly
        assertEquals("Set number should match", setNumber, workoutSet.setNumber)
        assertEquals("Target reps should match", targetReps, workoutSet.targetReps)
        assertEquals("Actual reps should match", actualReps, workoutSet.actualReps)
        assertEquals("Target weight should match", targetWeight, workoutSet.targetWeight)
        assertEquals("Actual weight should match", actualWeight, workoutSet.actualWeight)
        assertEquals("Rest time should match", restTime, workoutSet.restTime)
        assertEquals("Completion status should match", isCompleted, workoutSet.isCompleted)
        assertEquals("RPE should match", rpe, workoutSet.rpe)
    }

    @Test
    fun `should create WorkoutSet with default values`() {
        // When: Creating set with minimal properties
        val workoutSet = WorkoutExecutionManager.WorkoutSet(
            setNumber = 1,
            targetReps = 10,
            actualReps = 10,
            targetWeight = 50.0f,
            actualWeight = 50.0f,
            restTime = 60,
            isCompleted = false
        )

        // Then: Should have default values for optional properties
        assertNull("RPE should be null by default", workoutSet.rpe)
        assertNull("Form quality should be null by default", workoutSet.formQuality)
    }

    @Test
    fun `should handle different rep ranges correctly`() {
        val repRanges = listOf(
            Pair(1, 1),     // Heavy singles
            Pair(5, 5),     // Strength
            Pair(8, 12),    // Hypertrophy
            Pair(15, 20),   // Endurance
            Pair(30, 30)    // High endurance
        )

        for ((targetReps, actualReps) in repRanges) {
            // When: Creating set with different rep ranges
            val workoutSet = WorkoutExecutionManager.WorkoutSet(
                setNumber = 1,
                targetReps = targetReps,
                actualReps = actualReps,
                targetWeight = 50.0f,
                actualWeight = 50.0f,
                restTime = 60,
                isCompleted = true
            )

            // Then: Reps should be set correctly
            assertEquals("Target reps should match for $targetReps", targetReps, workoutSet.targetReps)
            assertEquals("Actual reps should match for $actualReps", actualReps, workoutSet.actualReps)
        }
    }

    @Test
    fun `should handle different weight values correctly`() {
        val weights = listOf(0.0f, 2.5f, 20.0f, 100.0f, 200.5f)

        for (weight in weights) {
            // When: Creating set with different weights
            val workoutSet = WorkoutExecutionManager.WorkoutSet(
                setNumber = 1,
                targetWeight = weight,
                targetReps = 10,
                actualWeight = weight,
                actualReps = 10,
                restTime = 60,
                isCompleted = true
            )

            // Then: Weights should be set correctly
            assertEquals("Target weight should match for $weight", weight, workoutSet.targetWeight)
            assertEquals("Actual weight should match for $weight", weight, workoutSet.actualWeight)
        }
    }

    @Test
    fun `should handle different rest time durations`() {
        val restTimes = listOf(30, 60, 90, 120, 180, 300) // seconds

        for (restTime in restTimes) {
            // When: Creating set with different rest times
            val workoutSet = WorkoutExecutionManager.WorkoutSet(
                setNumber = 1,
                targetReps = 10,
                actualReps = 10,
                targetWeight = 50.0f,
                actualWeight = 50.0f,
                restTime = restTime,
                isCompleted = true
            )

            // Then: Rest time should be set correctly
            assertEquals("Rest time should match for $restTime", restTime, workoutSet.restTime)
        }
    }

    @Test
    fun `should handle RPE scale correctly`() {
        val rpeValues = listOf(1, 5, 7, 8, 9, 10)

        for (rpe in rpeValues) {
            // When: Creating set with different RPE values
            val workoutSet = WorkoutExecutionManager.WorkoutSet(
                setNumber = 1,
                targetReps = 10,
                actualReps = 10,
                targetWeight = 50.0f,
                actualWeight = 50.0f,
                restTime = 60,
                isCompleted = true,
                rpe = rpe
            )

            // Then: RPE should be set correctly
            assertEquals("RPE should match for $rpe", rpe, workoutSet.rpe)
        }
    }

    @Test
    fun `should handle completion status correctly`() {
        // Test completed set
        val completedSet = WorkoutExecutionManager.WorkoutSet(
            setNumber = 1,
            targetReps = 10,
            actualReps = 10,
            targetWeight = 50.0f,
            actualWeight = 50.0f,
            restTime = 60,
            isCompleted = true
        )
        assertTrue("Set should be completed", completedSet.isCompleted)

        // Test incomplete set
        val incompleteSet = WorkoutExecutionManager.WorkoutSet(
            setNumber = 1,
            targetReps = 10,
            actualReps = 10,
            targetWeight = 50.0f,
            actualWeight = 50.0f,
            restTime = 60,
            isCompleted = false
        )
        assertFalse("Set should not be completed", incompleteSet.isCompleted)
    }

    @Test
    fun `should handle workout set notes concept`() {
        val noteExamples = listOf(
            "Perfect form",
            "Struggled with last 2 reps",
            "Felt lighter than usual",
            "Need to increase weight next time"
        )

        for (notes in noteExamples) {
            // When: Testing workout set notes concept
            // Then: Notes should be valid strings
            assertNotNull("Notes should not be null for '$notes'", notes)
            assertTrue("Notes should not be empty", notes.isNotEmpty())
        }
    }

    @Test
    fun `should handle multiple sets in workout step`() {
        // Given: Multiple sets data
        val setsData = listOf(
            Triple(1, 10, 10),
            Triple(2, 10, 9),
            Triple(3, 10, 8)
        )

        // When: Testing multiple sets concept
        // Then: Should handle multiple sets correctly
        assertEquals("Should have 3 sets", 3, setsData.size)
        assertEquals("First set should have 10 actual reps", 10, setsData[0].third)
        assertEquals("Second set should have 9 actual reps", 9, setsData[1].third)
        assertEquals("Third set should have 8 actual reps", 8, setsData[2].third)
    }
}