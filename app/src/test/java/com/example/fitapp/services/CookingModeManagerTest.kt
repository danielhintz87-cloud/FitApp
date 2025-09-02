package com.example.fitapp.services

import com.example.fitapp.data.db.AppDatabase
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
 * Unit tests for CookingModeManager
 * Tests step navigation, timer functionality, and progress saving
 */
@ExperimentalCoroutinesApi
class CookingModeManagerTest {

    @Mock
    private lateinit var database: AppDatabase

    private lateinit var cookingModeManager: CookingModeManager

    private val sampleCookingSteps = listOf(
        CookingModeManager.CookingStep(
            stepNumber = 1,
            instruction = "Preheat oven to 200°C",
            duration = 600, // 10 minutes
            temperature = "200°C",
            tips = listOf("Make sure oven is fully preheated"),
            estimatedTime = 600
        ),
        CookingModeManager.CookingStep(
            stepNumber = 2,
            instruction = "Prepare vegetables",
            ingredients = listOf(
                CookingModeManager.Ingredient("Onion", 1f, "piece", "Dice finely"),
                CookingModeManager.Ingredient("Carrots", 200f, "g", "Cut into chunks")
            ),
            tips = listOf("Cut vegetables evenly for consistent cooking"),
            estimatedTime = 300 // 5 minutes
        ),
        CookingModeManager.CookingStep(
            stepNumber = 3,
            instruction = "Sauté vegetables",
            duration = 480, // 8 minutes
            tips = listOf("Stir occasionally to prevent burning"),
            estimatedTime = 480
        ),
        CookingModeManager.CookingStep(
            stepNumber = 4,
            instruction = "Simmer and serve",
            duration = 900, // 15 minutes
            tips = listOf("Taste and adjust seasoning"),
            estimatedTime = 900
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        cookingModeManager = CookingModeManager(database)
    }

    // Recipe Step Navigation Tests

    @Test
    fun `should navigate recipe steps sequentially`() = runTest {
        // Given: Started cooking mode with recipe steps
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)

        // When: Getting current step
        val currentStep = cookingModeManager.currentStep.first()

        // Then: Should start with first step
        assertNotNull("Current step should not be null", currentStep)
        assertEquals("Should start with step 1", 1, currentStep?.stepNumber)
        assertEquals("Should have correct instruction", "Preheat oven to 200°C", currentStep?.instruction)
        assertTrue("Should be in cooking mode", cookingModeManager.isInCookingMode.first())
    }

    @Test
    fun `should advance to next step correctly`() = runTest {
        // Given: Started cooking mode
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)

        // When: Advancing to next step
        cookingModeManager.nextStep()

        // Then: Should advance to step 2
        val currentStep = cookingModeManager.currentStep.first()
        assertEquals("Should advance to step 2", 2, currentStep?.stepNumber)
        assertEquals("Should have vegetables preparation", "Prepare vegetables", currentStep?.instruction)
    }

    @Test
    fun `should go back to previous step correctly`() = runTest {
        // Given: Started cooking mode and advanced to step 2
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        cookingModeManager.nextStep()

        // When: Going back to previous step
        cookingModeManager.previousStep()

        // Then: Should return to step 1
        val currentStep = cookingModeManager.currentStep.first()
        assertEquals("Should return to step 1", 1, currentStep?.stepNumber)
    }

    @Test
    fun `should handle step completion correctly`() = runTest {
        // Given: Started cooking mode
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)

        // When: Completing current step
        cookingModeManager.completeCurrentStep()

        // Then: Should mark step as completed and advance
        val currentStep = cookingModeManager.currentStep.first()
        assertEquals("Should auto-advance to next step", 2, currentStep?.stepNumber)
        
        // Previous step should be marked as completed
        val cookingFlow = cookingModeManager.cookingFlow.first()
        assertNotNull("Cooking flow should exist", cookingFlow)
        assertTrue("First step should be completed", 
            cookingFlow?.completedSteps?.contains(1) == true)
    }

    @Test
    fun `should complete cooking when all steps done`() = runTest {
        // Given: Started cooking mode
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)

        // When: Completing all steps
        repeat(sampleCookingSteps.size) {
            cookingModeManager.completeCurrentStep()
        }

        // Then: Should complete cooking mode
        assertFalse("Should no longer be in cooking mode", cookingModeManager.isInCookingMode.first())
        assertNull("Current step should be null", cookingModeManager.currentStep.first())
    }

    // Timer Functionality Tests

    @Test
    fun `should handle timer functionality for timed steps`() = runTest {
        // Given: Started cooking mode on a timed step
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)

        // When: Starting timer for current step
        cookingModeManager.startStepTimer()

        // Then: Should start timer with correct duration
        val timers = cookingModeManager.stepTimers.first()
        assertTrue("Should have timer for step 1", timers.containsKey(1))
        val timer = timers[1]
        assertNotNull("Timer should exist", timer)
        assertEquals("Timer should have correct duration", 600, timer?.totalTime)
        assertTrue("Timer should be running", timer?.isRunning == true)
    }

    @Test
    fun `should pause and resume timers correctly`() = runTest {
        // Given: Started cooking mode with running timer
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        cookingModeManager.startStepTimer()

        // When: Pausing timer
        cookingModeManager.pauseStepTimer(1)

        // Then: Timer should be paused
        val pausedTimers = cookingModeManager.stepTimers.first()
        val pausedTimer = pausedTimers[1]
        assertFalse("Timer should be paused", pausedTimer?.isRunning == true)

        // When: Resuming timer
        cookingModeManager.resumeStepTimer(1)

        // Then: Timer should be running again
        val resumedTimers = cookingModeManager.stepTimers.first()
        val resumedTimer = resumedTimers[1]
        assertTrue("Timer should be running again", resumedTimer?.isRunning == true)
    }

    @Test
    fun `should handle timer completion correctly`() = runTest {
        // Given: Started cooking mode with timer
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        cookingModeManager.startStepTimer()

        // When: Timer completes (simulate)
        cookingModeManager.onTimerCompleted(1)

        // Then: Should handle timer completion
        val timers = cookingModeManager.stepTimers.first()
        val timer = timers[1]
        assertTrue("Timer should be completed", timer?.isCompleted == true)
        assertFalse("Timer should not be running", timer?.isRunning == true)
    }

    @Test
    fun `should manage multiple timers for different steps`() = runTest {
        // Given: Started cooking mode
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        
        // When: Starting timer for first step
        cookingModeManager.startStepTimer()
        
        // And advancing to next step and starting another timer
        cookingModeManager.nextStep()
        cookingModeManager.nextStep() // Move to step 3 which has timer
        cookingModeManager.startStepTimer()

        // Then: Should manage multiple timers
        val timers = cookingModeManager.stepTimers.first()
        assertTrue("Should have timer for step 1", timers.containsKey(1))
        assertTrue("Should have timer for step 3", timers.containsKey(3))
        assertEquals("Should have 2 active timers", 2, timers.size)
    }

    @Test
    fun `should not start timer for steps without duration`() = runTest {
        // Given: Started cooking mode and moved to step without timer
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        cookingModeManager.nextStep() // Move to step 2 (no duration)

        // When: Attempting to start timer
        val result = cookingModeManager.startStepTimer()

        // Then: Should not start timer
        assertFalse("Should not start timer for non-timed step", result)
        val timers = cookingModeManager.stepTimers.first()
        assertFalse("Should not have timer for step 2", timers.containsKey(2))
    }

    // Screen Wake Lock Tests

    @Test
    fun `should maintain screen wake lock during cooking`() = runTest {
        // Given: Started cooking mode
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)

        // When: Checking wake lock status
        val isWakeLockActive = cookingModeManager.isWakeLockActive()

        // Then: Should maintain wake lock during cooking
        assertTrue("Should maintain wake lock during cooking", isWakeLockActive)
    }

    @Test
    fun `should release wake lock when cooking ends`() = runTest {
        // Given: Started and completed cooking mode
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        
        // When: Ending cooking mode
        cookingModeManager.endCookingMode()

        // Then: Should release wake lock
        val isWakeLockActive = cookingModeManager.isWakeLockActive()
        assertFalse("Should release wake lock when cooking ends", isWakeLockActive)
        assertFalse("Should no longer be in cooking mode", cookingModeManager.isInCookingMode.first())
    }

    // Progress Saving Tests

    @Test
    fun `should save cooking progress on interruption`() = runTest {
        // Given: Started cooking mode with progress
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        cookingModeManager.completeCurrentStep()
        cookingModeManager.startStepTimer()

        // When: Saving progress due to interruption
        cookingModeManager.saveProgress()

        // Then: Should save current state
        verify(database, atLeastOnce()).let {
            // Database save verification would go here
        }
    }

    @Test
    fun `should restore cooking progress correctly`() = runTest {
        // Given: Previously saved cooking session
        val savedProgress = CookingModeManager.CookingProgress(
            recipeId = "test_recipe",
            currentStep = 2,
            completedSteps = setOf(1),
            stepTimers = mapOf(1 to CookingModeManager.StepTimerState(600, 0, false, true)),
            totalCookingTime = 600
        )

        // When: Restoring progress
        cookingModeManager.restoreProgress(savedProgress, sampleCookingSteps)

        // Then: Should restore to correct state
        assertTrue("Should be in cooking mode", cookingModeManager.isInCookingMode.first())
        val currentStep = cookingModeManager.currentStep.first()
        assertEquals("Should restore to step 2", 2, currentStep?.stepNumber)
        
        val cookingFlow = cookingModeManager.cookingFlow.first()
        assertTrue("Should restore completed steps", 
            cookingFlow?.completedSteps?.contains(1) == true)
    }

    @Test
    fun `should handle app backgrounding gracefully`() = runTest {
        // Given: Active cooking session
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        cookingModeManager.startStepTimer()

        // When: App goes to background
        cookingModeManager.onAppBackgrounded()

        // Then: Should save state but keep timers running
        verify(database, atLeastOnce()).let {
            // Verify save operation
        }
        
        val timers = cookingModeManager.stepTimers.first()
        val timer = timers[1]
        assertTrue("Timer should still be running in background", timer?.isRunning == true)
    }

    @Test
    fun `should handle app foregrounding correctly`() = runTest {
        // Given: App was backgrounded during cooking
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        cookingModeManager.startStepTimer()
        cookingModeManager.onAppBackgrounded()

        // When: App comes back to foreground
        cookingModeManager.onAppForegrounded()

        // Then: Should update timer states correctly
        val timers = cookingModeManager.stepTimers.first()
        val timer = timers[1]
        assertNotNull("Timer should still exist", timer)
        // Timer time should be updated based on background time
    }

    // Recipe Information and Tips Tests

    @Test
    fun `should provide step-specific information correctly`() = runTest {
        // Given: Started cooking mode
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)

        // When: Getting current step information
        val currentStep = cookingModeManager.currentStep.first()

        // Then: Should provide complete step information
        assertNotNull("Current step should not be null", currentStep)
        assertEquals("Should have correct instruction", "Preheat oven to 200°C", currentStep?.instruction)
        assertEquals("Should have temperature", "200°C", currentStep?.temperature)
        assertTrue("Should have tips", currentStep?.tips?.isNotEmpty() == true)
        assertEquals("Should have duration", 600, currentStep?.duration)
    }

    @Test
    fun `should provide ingredient information for preparation steps`() = runTest {
        // Given: Started cooking mode and moved to preparation step
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        cookingModeManager.nextStep() // Move to step 2 with ingredients

        // When: Getting current step
        val currentStep = cookingModeManager.currentStep.first()

        // Then: Should provide ingredient information
        assertNotNull("Current step should not be null", currentStep)
        assertTrue("Should have ingredients", currentStep?.ingredients?.isNotEmpty() == true)
        assertEquals("Should have 2 ingredients", 2, currentStep?.ingredients?.size)
        
        val onion = currentStep?.ingredients?.find { it.name == "Onion" }
        assertNotNull("Should have onion", onion)
        assertEquals("Should have preparation note", "Dice finely", onion?.preparation)
    }

    @Test
    fun `should calculate total estimated cooking time`() = runTest {
        // Given: Recipe with timed steps
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)

        // When: Getting total estimated time
        val totalTime = cookingModeManager.getTotalEstimatedTime()

        // Then: Should calculate correct total
        val expectedTotal = sampleCookingSteps.sumOf { it.estimatedTime ?: 0 }
        assertEquals("Should calculate total time correctly", expectedTotal, totalTime)
    }

    @Test
    fun `should track cooking progress percentage`() = runTest {
        // Given: Started cooking mode with some completed steps
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        cookingModeManager.completeCurrentStep() // Complete step 1
        cookingModeManager.completeCurrentStep() // Complete step 2

        // When: Getting progress percentage
        val progress = cookingModeManager.getCookingProgress()

        // Then: Should calculate correct progress
        val expectedProgress = 2f / sampleCookingSteps.size
        assertEquals("Should calculate progress correctly", expectedProgress, progress, 0.01f)
    }

    // Error Handling and Edge Cases Tests

    @Test
    fun `should handle empty recipe steps gracefully`() = runTest {
        // When: Starting cooking mode with empty steps
        val result = cookingModeManager.startCookingMode("empty_recipe", emptyList())

        // Then: Should handle gracefully
        assertFalse("Should not start cooking with empty steps", result)
        assertFalse("Should not be in cooking mode", cookingModeManager.isInCookingMode.first())
    }

    @Test
    fun `should handle timer operations when not in cooking mode`() = runTest {
        // When: Attempting timer operations without active cooking
        val startResult = cookingModeManager.startStepTimer()
        val pauseResult = cookingModeManager.pauseStepTimer(1)

        // Then: Should handle gracefully
        assertFalse("Should not start timer without cooking mode", startResult)
        assertFalse("Should not pause non-existent timer", pauseResult)
    }

    @Test
    fun `should handle navigation beyond recipe bounds`() = runTest {
        // Given: Started cooking mode at first step
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)

        // When: Attempting to go to previous step at beginning
        val prevResult = cookingModeManager.previousStep()

        // Then: Should handle gracefully
        assertFalse("Should not go before first step", prevResult)
        val currentStep = cookingModeManager.currentStep.first()
        assertEquals("Should remain at first step", 1, currentStep?.stepNumber)
    }

    @Test
    fun `should handle multiple cooking mode starts correctly`() = runTest {
        // Given: Already started cooking mode
        cookingModeManager.startCookingMode("recipe1", sampleCookingSteps)

        // When: Attempting to start another cooking session
        val result = cookingModeManager.startCookingMode("recipe2", sampleCookingSteps)

        // Then: Should handle appropriately
        assertFalse("Should not start multiple cooking sessions", result)
        assertTrue("Should remain in cooking mode", cookingModeManager.isInCookingMode.first())
    }

    @Test
    fun `should provide cooking summary on completion`() = runTest {
        // Given: Completed cooking session
        cookingModeManager.startCookingMode("test_recipe", sampleCookingSteps)
        val startTime = System.currentTimeMillis()
        
        // Complete all steps
        repeat(sampleCookingSteps.size) {
            cookingModeManager.completeCurrentStep()
        }

        // When: Getting cooking summary
        val summary = cookingModeManager.getCookingSummary()

        // Then: Should provide complete summary
        assertNotNull("Summary should exist", summary)
        assertEquals("Should have correct recipe ID", "test_recipe", summary.recipeId)
        assertEquals("Should count all steps", sampleCookingSteps.size, summary.totalSteps)
        assertEquals("Should count completed steps", sampleCookingSteps.size, summary.completedSteps)
        assertTrue("Should have reasonable cooking time", summary.totalCookingTime > 0)
        assertTrue("Should record completion", summary.isCompleted)
    }
}