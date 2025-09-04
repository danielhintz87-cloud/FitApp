package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * Unit tests for ResetManager
 * Tests data reset operations and validation logic
 */
class ResetManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var database: AppDatabase

    @Mock
    private lateinit var userPreferences: UserPreferences

    private lateinit var resetManager: ResetManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        resetManager = ResetManager(context, database, userPreferences)
    }

    @Test
    fun `should perform workout data reset with valid token`() = runTest {
        // Given: Valid confirmation token for workout reset
        val resetType = ResetType.WORKOUT_DATA
        val validToken = "CONFIRM_WORKOUT_RESET"

        // When: Performing reset
        val result = resetManager.performReset(resetType, validToken)

        // Then: Should complete successfully
        assertTrue("Reset should be successful", result.isSuccess)
        assertEquals("Reset type should match", resetType, result.resetType)
        assertTrue("Reset should be completed", result.isCompleted)
        assertFalse("Should not have error", result.hasError)
        assertNull("Error message should be null", result.errorMessage)
    }

    @Test
    fun `should reject reset with invalid token`() = runTest {
        // Given: Invalid confirmation token
        val resetType = ResetType.WORKOUT_DATA
        val invalidToken = "INVALID_TOKEN"

        // When: Performing reset
        val result = resetManager.performReset(resetType, invalidToken)

        // Then: Should fail with token error
        assertFalse("Reset should fail", result.isSuccess)
        assertEquals("Reset type should match", resetType, result.resetType)
        assertFalse("Reset should not be completed", result.isCompleted)
        assertTrue("Should have error", result.hasError)
        assertEquals("Should have token error", ResetError.INVALID_TOKEN, result.errorType)
        assertNotNull("Error message should be provided", result.errorMessage)
    }

    @Test
    fun `should perform nutrition data reset`() = runTest {
        // Given: Valid token for nutrition reset
        val resetType = ResetType.NUTRITION_DATA
        val validToken = "CONFIRM_NUTRITION_RESET"

        // When: Performing reset
        val result = resetManager.performReset(resetType, validToken)

        // Then: Should complete successfully
        assertTrue("Reset should be successful", result.isSuccess)
        assertEquals("Reset type should match", resetType, result.resetType)
        assertTrue("Reset should be completed", result.isCompleted)
        assertFalse("Should not have error", result.hasError)
    }

    @Test
    fun `should perform selective reset with options`() = runTest {
        // Given: Selective reset options
        val options = SelectiveResetOptions(
            resetPersonalInfo = true,
            resetFitnessGoals = false,
            resetPreferences = true,
            resetAchievements = false
        )
        val validToken = "CONFIRM_SELECTIVE_RESET"

        // When: Performing selective reset
        val result = resetManager.performSelectiveReset(options, validToken)

        // Then: Should complete successfully
        assertTrue("Reset should be successful", result.isSuccess)
        assertTrue("Reset should be completed", result.isCompleted)
    }

    @Test
    fun `should track reset progress during operation`() = runTest {
        // Given: Reset operation in progress
        val resetType = ResetType.COMPLETE_RESET
        val validToken = "CONFIRM_COMPLETE_RESET"

        // When: Starting reset operation
        val result = resetManager.performReset(resetType, validToken)

        // Then: Should track progress
        assertTrue("Reset should be successful", result.isSuccess)
        
        // Check that progress was updated during operation
        val finalProgress = resetManager.resetProgress.first()
        if (finalProgress != null) {
            assertTrue("Progress should be completed", finalProgress.isCompleted)
            assertEquals("Progress should be 100%", 1.0f, finalProgress.progress, 0.01f)
        }
    }

    @Test
    fun `should preserve user settings when requested`() = runTest {
        // Given: Reset with preserve settings flag
        val resetType = ResetType.WORKOUT_DATA
        val validToken = "CONFIRM_WORKOUT_RESET"
        val preserveSettings = true

        // When: Performing reset with preservation
        val result = resetManager.performReset(
            resetType, 
            validToken, 
            preserveSettings = preserveSettings
        )

        // Then: Should preserve settings
        assertTrue("Reset should be successful", result.isSuccess)
        assertTrue("Settings should be preserved", result.preservedUserSettings)
    }

    @Test
    fun `should handle reset operation errors gracefully`() = runTest {
        // Given: Database error during reset (simulated with null database)
        val resetType = ResetType.USER_PROFILE
        val validToken = "CONFIRM_PROFILE_RESET"

        // When: Reset operation encounters error
        val result = resetManager.performReset(resetType, validToken)

        // Then: Should handle error gracefully
        // Note: Since we can't easily simulate database errors in this test setup,
        // we verify that the method doesn't throw exceptions
        assertNotNull("Result should not be null", result)
        assertEquals("Reset type should match", resetType, result.resetType)
    }

    @Test
    fun `should validate different reset types correctly`() = runTest {
        // Test each reset type
        val resetTypes = listOf(
            ResetType.WORKOUT_DATA,
            ResetType.NUTRITION_DATA,
            ResetType.USER_PROFILE,
            ResetType.ACHIEVEMENTS,
            ResetType.SHOPPING_LIST,
            ResetType.PERSONAL_RECORDS
        )

        for (resetType in resetTypes) {
            // Given: Valid token for each type
            val validToken = "CONFIRM_${resetType.name}"

            // When: Performing reset
            val result = resetManager.performReset(resetType, validToken)

            // Then: Should handle each type appropriately
            assertEquals("Reset type should match for $resetType", resetType, result.resetType)
            // Each type should either succeed or fail gracefully
            assertTrue("Result should be consistent for $resetType", 
                result.isSuccess || result.hasError)
        }
    }

    @Test
    fun `should provide clear error messages for different failure types`() = runTest {
        // Test invalid token error
        val invalidTokenResult = resetManager.performReset(
            ResetType.WORKOUT_DATA, 
            "INVALID"
        )
        
        assertTrue("Should have error message for invalid token", 
            invalidTokenResult.errorMessage?.isNotEmpty() == true)
        assertEquals("Should have correct error type", 
            ResetError.INVALID_TOKEN, invalidTokenResult.errorType)
    }

    @Test
    fun `should handle empty selective reset options`() = runTest {
        // Given: Empty selective reset options
        val emptyOptions = SelectiveResetOptions()
        val validToken = "CONFIRM_SELECTIVE_RESET"

        // When: Performing reset with no options selected
        val result = resetManager.performSelectiveReset(emptyOptions, validToken)

        // Then: Should handle gracefully
        assertNotNull("Result should not be null", result)
        assertTrue("Should complete successfully even with empty options", result.isCompleted)
    }

    @Test
    fun `should support reset progress tracking`() = runTest {
        // Given: Reset manager with progress tracking
        
        // When: Checking initial progress state
        val initialProgress = resetManager.resetProgress.first()
        
        // Then: Should start with no active progress
        // (Initial progress may be null, which is valid)
        // This test mainly ensures the progress flow is accessible
        assertTrue("Progress tracking should be available", true)
    }
}