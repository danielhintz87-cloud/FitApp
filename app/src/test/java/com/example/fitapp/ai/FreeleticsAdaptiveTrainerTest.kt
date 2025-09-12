package com.example.fitapp.ai

import com.example.fitapp.ui.screens.ExerciseStep
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit test for Freeletics-style Adaptive Training data models and algorithms
 *
 * Tests the core logic, data structures, and algorithms used in adaptive training
 * without requiring Android context or complex setup.
 */
class FreeleticsAdaptiveTrainerTest {
    @Test
    fun testAdaptiveExerciseModification_HasChanges_DetectsModifications() {
        // Test with changes
        val modificationWithChanges =
            AdaptiveExerciseModification(
                exerciseId = "push_ups",
                weightAdjustment = 0.1f,
                repAdjustment = 2,
                restTimeAdjustment = 1.2f,
                reason = "Excellent form - increase intensity",
            )

        assertTrue("Should detect weight adjustment", modificationWithChanges.hasChanges())

        // Test without changes
        val modificationNoChanges =
            AdaptiveExerciseModification(
                exerciseId = "squats",
                reason = "No change needed",
            )

        assertFalse(
            "Should not detect changes when all adjustments are default",
            modificationNoChanges.hasChanges(),
        )
    }

    @Test
    fun testAdaptiveExerciseModification_NoChange_CreatesCorrectInstance() {
        val noChangeModification = AdaptiveExerciseModification.noChange("bench_press")

        assertEquals("Exercise ID should match", "bench_press", noChangeModification.exerciseId)
        assertFalse("Should not have changes", noChangeModification.hasChanges())
        assertTrue(
            "Should have appropriate reason",
            noChangeModification.reason.contains("Keine Änderung", ignoreCase = true),
        )
    }

    @Test
    fun testWorkoutAdaptation_NoChange_CreatesCorrectInstance() {
        val noChangeAdaptation = WorkoutAdaptation.noChange()

        assertEquals("Should be NO_CHANGE type", AdaptationType.NO_CHANGE, noChangeAdaptation.type)
        assertTrue("Should have empty modifications", noChangeAdaptation.modifications.isEmpty())
        assertEquals("Should have full confidence", 1f, noChangeAdaptation.confidence, 0.01f)
        assertTrue(
            "Should have appropriate reasoning",
            noChangeAdaptation.reasoning.contains("Keine Anpassung", ignoreCase = true),
        )
    }

    @Test
    fun testRealTimePerformance_DataIntegrity() {
        val performance =
            RealTimePerformance(
                formQuality = 0.85f,
                rpe = 7,
                heartRate = 155,
                currentRep = 8,
                movementSpeed = 1.1f,
            )

        // Verify all data is stored correctly
        assertEquals("Form quality should match", 0.85f, performance.formQuality, 0.01f)
        assertEquals("RPE should match", 7, performance.rpe)
        assertEquals("Heart rate should match", 155, performance.heartRate)
        assertEquals("Current rep should match", 8, performance.currentRep)
        assertEquals("Movement speed should match", 1.1f, performance.movementSpeed, 0.01f)
        assertTrue(
            "Timestamp should be recent",
            performance.timestamp > System.currentTimeMillis() - 5000,
        )
    }

    @Test
    fun testAdaptationType_EnumValues() {
        val expectedTypes =
            setOf(
                AdaptationType.NO_CHANGE,
                AdaptationType.INCREASE_INTENSITY,
                AdaptationType.REDUCE_INTENSITY,
                AdaptationType.MODIFY_TECHNIQUE,
                AdaptationType.SUBSTITUTE_EXERCISE,
                AdaptationType.ADJUST_REST_TIME,
                AdaptationType.CHANGE_TEMPO,
            )

        val actualTypes = AdaptationType.values().toSet()

        assertEquals("Should have all expected adaptation types", expectedTypes, actualTypes)
        assertEquals("Should have 7 adaptation types", 7, AdaptationType.values().size)
    }

    @Test
    fun testSessionTrend_EnumValues() {
        val expectedTrends =
            setOf(
                SessionTrend.EXCELLENT,
                SessionTrend.STABLE,
                SessionTrend.DECLINING,
            )

        val actualTrends = SessionTrend.values().toSet()

        assertEquals("Should have all expected session trends", expectedTrends, actualTrends)
        assertEquals("Should have 3 session trends", 3, SessionTrend.values().size)
    }

    @Test
    fun testIssueType_EnumValues() {
        val expectedIssues =
            setOf(
                IssueType.FORM_DEGRADATION,
                IssueType.EXCESSIVE_FATIGUE,
                IssueType.INSUFFICIENT_INTENSITY,
                IssueType.MOVEMENT_ASYMMETRY,
                IssueType.SAFETY_CONCERN,
            )

        val actualIssues = IssueType.values().toSet()

        assertEquals("Should have all expected issue types", expectedIssues, actualIssues)
        assertEquals("Should have 5 issue types", 5, IssueType.values().size)
    }

    @Test
    fun testDifficultyAdjustment_CalculatesAdjustmentCorrectly() {
        val adjustment =
            DifficultyAdjustment(
                originalDifficulty = 0.7f,
                newDifficulty = 0.85f,
                adjustmentFactor = 0.85f / 0.7f,
                reasons = listOf("Excellent form", "Low fatigue"),
                recommendedChanges = listOf("Increase weight by 10%", "Add 2 reps"),
            )

        assertEquals("Original difficulty should match", 0.7f, adjustment.originalDifficulty, 0.01f)
        assertEquals("New difficulty should match", 0.85f, adjustment.newDifficulty, 0.01f)
        assertTrue(
            "Adjustment factor should be calculated correctly",
            kotlin.math.abs(adjustment.adjustmentFactor - (0.85f / 0.7f)) < 0.01f,
        )
        assertEquals("Should have 2 reasons", 2, adjustment.reasons.size)
        assertEquals("Should have 2 recommended changes", 2, adjustment.recommendedChanges.size)
    }

    @Test
    fun testAdaptiveRestCalculation_LogicAndReasoning() {
        val restCalc =
            AdaptiveRestCalculation(
                baseRestTime = 90f,
                adjustedRestTime = 120f,
                fatigueAdjustment = 1.2f,
                performanceAdjustment = 1.1f,
                intensityAdjustment = 1.0f,
                reasoning = "High fatigue detected - extended rest recommended",
            )

        assertEquals("Base rest time should match", 90f, restCalc.baseRestTime, 0.01f)
        assertEquals("Adjusted rest time should match", 120f, restCalc.adjustedRestTime, 0.01f)
        assertTrue("Should increase rest time", restCalc.adjustedRestTime > restCalc.baseRestTime)
        assertTrue("Fatigue adjustment should be > 1", restCalc.fatigueAdjustment > 1f)
        assertTrue("Reasoning should be descriptive", restCalc.reasoning.isNotEmpty())

        // Test calculation logic approximation (allowing for rounding)
        val expectedAdjusted =
            restCalc.baseRestTime * restCalc.fatigueAdjustment *
                restCalc.performanceAdjustment * restCalc.intensityAdjustment
        assertEquals(
            "Adjusted time should match calculation",
            expectedAdjusted,
            restCalc.adjustedRestTime,
            2f,
        )
    }

    @Test
    fun testWorkoutSessionContext_ConfigurationData() {
        val sessionContext =
            WorkoutSessionContext(
                sessionId = "session_456",
                sessionDuration = 45L,
                totalExercises = 6,
                completedExercises = 3,
                overallIntensity = 0.75f,
                userEnergyLevel = 0.6f,
            )

        assertEquals("Session ID should match", "session_456", sessionContext.sessionId)
        assertEquals("Duration should match", 45L, sessionContext.sessionDuration)
        assertEquals(
            "Should be 50% complete",
            0.5f,
            sessionContext.completedExercises.toFloat() / sessionContext.totalExercises,
            0.01f,
        )
        assertTrue(
            "Intensity should be in valid range",
            sessionContext.overallIntensity >= 0f && sessionContext.overallIntensity <= 1f,
        )
        assertTrue(
            "Energy level should be in valid range",
            sessionContext.userEnergyLevel >= 0f && sessionContext.userEnergyLevel <= 1f,
        )
    }

    @Test
    fun testDataConsistency_RelatedModels() {
        val exercise =
            ExerciseStep(
                name = "Squats",
                type = "reps",
                value = "3x12",
                description = "Bodyweight squats",
                restTime = 60,
            )

        val modification =
            AdaptiveExerciseModification(
                exerciseId = exercise.name,
                weightAdjustment = 0.1f,
                reason = "Excellent form",
            )

        val workoutAdaptation =
            WorkoutAdaptation(
                type = AdaptationType.INCREASE_INTENSITY,
                modifications = listOf(modification),
                reasoning = "Performance indicates readiness for progression",
                confidence = 0.85f,
            )

        // Test consistency
        assertEquals(
            "Exercise names should match",
            exercise.name,
            modification.exerciseId,
        )
        assertTrue(
            "Modification should be included in adaptation",
            workoutAdaptation.modifications.contains(modification),
        )
        assertEquals(
            "Adaptation type should match modification intent",
            AdaptationType.INCREASE_INTENSITY,
            workoutAdaptation.type,
        )
    }
}
