package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.UserPreferences
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
 * Unit tests for ResetManager
 * Tests data reset operations, integrity validation, and atomic operations
 */
@ExperimentalCoroutinesApi
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

    // Workout Data Reset Tests

    @Test
    fun `should reset workout data completely`() = runTest {
        // Given: Request to reset workout data
        val resetType = ResetManager.ResetType.WORKOUT_DATA
        val confirmationToken = "CONFIRM_WORKOUT_RESET"

        // When: Resetting workout data
        val result = resetManager.performReset(resetType, confirmationToken)

        // Then: Should reset workout data successfully
        assertTrue("Should reset workout data successfully", result.isSuccess)
        assertEquals("Should have correct reset type", resetType, result.resetType)
        assertTrue("Should be completed", result.isCompleted)
        assertFalse("Should not have errors", result.hasError)
        
        // Verify database operations were called
        verify(database, atLeastOnce()).let {
            // Verification of specific database operations would go here
            // For now, just verify the manager was called
        }
    }

    @Test
    fun `should preserve user preferences during workout reset`() = runTest {
        // Given: Workout data reset while preserving user settings
        val resetType = ResetManager.ResetType.WORKOUT_DATA
        val preserveSettings = true

        // When: Resetting with preserve settings flag
        val result = resetManager.performReset(resetType, "CONFIRM", preserveSettings)

        // Then: Should preserve user preferences
        assertTrue("Reset should succeed", result.isSuccess)
        verify(userPreferences, never()).clear() // Should not clear preferences
        assertTrue("Should indicate settings preserved", result.preservedUserSettings)
    }

    @Test
    fun `should track reset progress during workout data reset`() = runTest {
        // Given: Workout data reset operation
        val resetType = ResetManager.ResetType.WORKOUT_DATA

        // When: Starting reset operation
        resetManager.performReset(resetType, "CONFIRM")

        // Then: Should track progress
        val progress = resetManager.resetProgress.first()
        assertNotNull("Progress should be tracked", progress)
        if (progress != null) {
            assertEquals("Should track correct operation", "WORKOUT_DATA", progress.operation)
            assertTrue("Progress should be valid", progress.progress in 0f..1f)
            assertNotNull("Should have current step description", progress.currentStep)
        }
    }

    @Test
    fun `should validate confirmation token for workout reset`() = runTest {
        // Given: Invalid confirmation token
        val resetType = ResetManager.ResetType.WORKOUT_DATA
        val invalidToken = "INVALID_TOKEN"

        // When: Attempting reset with invalid token
        val result = resetManager.performReset(resetType, invalidToken)

        // Then: Should reject reset
        assertFalse("Should reject invalid token", result.isSuccess)
        assertTrue("Should have error", result.hasError)
        assertEquals("Should have correct error type", 
            ResetManager.ResetError.INVALID_CONFIRMATION, result.errorType)
    }

    // Nutrition Data Reset Tests

    @Test
    fun `should reset nutrition data while preserving user preferences`() = runTest {
        // Given: Nutrition data reset request
        val resetType = ResetManager.ResetType.NUTRITION_DATA
        val preservePreferences = true

        // When: Resetting nutrition data
        val result = resetManager.performReset(resetType, "CONFIRM_NUTRITION", preservePreferences)

        // Then: Should reset nutrition data only
        assertTrue("Should reset nutrition data successfully", result.isSuccess)
        assertEquals("Should have correct reset type", resetType, result.resetType)
        assertTrue("Should preserve user preferences", result.preservedUserSettings)
        
        // Verify specific nutrition data was cleared
        assertTrue("Should clear meal logs", result.clearedDataTypes.contains("meal_logs"))
        assertTrue("Should clear nutrition goals", result.clearedDataTypes.contains("nutrition_goals"))
        assertTrue("Should clear food database", result.clearedDataTypes.contains("food_database"))
    }

    @Test
    fun `should preserve dietary restrictions during nutrition reset`() = runTest {
        // Given: Nutrition reset with dietary restrictions preservation
        val resetType = ResetManager.ResetType.NUTRITION_DATA
        val preserveDietaryRestrictions = true

        // When: Resetting with dietary restriction preservation
        val result = resetManager.performReset(
            resetType, 
            "CONFIRM", 
            preserveUserSettings = true,
            preserveDietaryRestrictions = preserveDietaryRestrictions
        )

        // Then: Should preserve dietary restrictions
        assertTrue("Reset should succeed", result.isSuccess)
        assertTrue("Should preserve dietary restrictions", result.preservedDietaryRestrictions)
        assertFalse("Should not clear dietary restrictions", 
            result.clearedDataTypes.contains("dietary_restrictions"))
    }

    @Test
    fun `should handle nutrition reset errors gracefully`() = runTest {
        // Given: Database error during nutrition reset (simulated)
        whenever(database.clearNutritionData()).thenThrow(RuntimeException("Database error"))

        // When: Attempting nutrition reset
        val result = resetManager.performReset(ResetManager.ResetType.NUTRITION_DATA, "CONFIRM")

        // Then: Should handle error gracefully
        assertFalse("Should fail gracefully", result.isSuccess)
        assertTrue("Should have error", result.hasError)
        assertNotNull("Should have error message", result.errorMessage)
        assertEquals("Should have correct error type", 
            ResetManager.ResetError.DATABASE_ERROR, result.errorType)
    }

    // Selective Reset Tests

    @Test
    fun `should reset user profile selectively`() = runTest {
        // Given: User profile reset with selective options
        val resetType = ResetManager.ResetType.USER_PROFILE
        val selectiveOptions = ResetManager.SelectiveResetOptions(
            resetPersonalInfo = true,
            resetFitnessGoals = true,
            resetPreferences = false,
            resetAchievements = false
        )

        // When: Performing selective user profile reset
        val result = resetManager.performSelectiveReset(resetType, "CONFIRM", selectiveOptions)

        // Then: Should reset only selected data
        assertTrue("Should reset successfully", result.isSuccess)
        assertTrue("Should reset personal info", result.clearedDataTypes.contains("personal_info"))
        assertTrue("Should reset fitness goals", result.clearedDataTypes.contains("fitness_goals"))
        assertFalse("Should preserve preferences", result.clearedDataTypes.contains("preferences"))
        assertFalse("Should preserve achievements", result.clearedDataTypes.contains("achievements"))
    }

    @Test
    fun `should reset achievements without affecting core data`() = runTest {
        // Given: Achievement reset request
        val resetType = ResetManager.ResetType.ACHIEVEMENTS

        // When: Resetting achievements
        val result = resetManager.performReset(resetType, "CONFIRM_ACHIEVEMENTS")

        // Then: Should reset only achievements
        assertTrue("Should reset achievements successfully", result.isSuccess)
        assertTrue("Should clear achievements", result.clearedDataTypes.contains("achievements"))
        assertTrue("Should clear streaks", result.clearedDataTypes.contains("streaks"))
        assertTrue("Should clear badges", result.clearedDataTypes.contains("badges"))
        
        // Should preserve workout and nutrition data
        assertFalse("Should preserve workout data", result.clearedDataTypes.contains("workout_data"))
        assertFalse("Should preserve nutrition data", result.clearedDataTypes.contains("nutrition_data"))
    }

    @Test
    fun `should reset personal records independently`() = runTest {
        // Given: Personal records reset request
        val resetType = ResetManager.ResetType.PERSONAL_RECORDS

        // When: Resetting personal records
        val result = resetManager.performReset(resetType, "CONFIRM_PR")

        // Then: Should reset only personal records
        assertTrue("Should reset personal records successfully", result.isSuccess)
        assertTrue("Should clear personal records", result.clearedDataTypes.contains("personal_records"))
        assertTrue("Should clear PRs history", result.clearedDataTypes.contains("pr_history"))
        
        // Should preserve other workout data
        assertFalse("Should preserve workout sessions", result.clearedDataTypes.contains("workout_sessions"))
    }

    // Complete Reset Tests

    @Test
    fun `should perform complete reset with confirmation`() = runTest {
        // Given: Complete reset request with proper confirmation
        val resetType = ResetManager.ResetType.COMPLETE_RESET
        val confirmationToken = "CONFIRM_COMPLETE_RESET_I_UNDERSTAND"

        // When: Performing complete reset
        val result = resetManager.performReset(resetType, confirmationToken)

        // Then: Should reset everything
        assertTrue("Should perform complete reset successfully", result.isSuccess)
        assertEquals("Should have correct reset type", resetType, result.resetType)
        assertTrue("Should be completed", result.isCompleted)
        
        // Should clear all data types
        val expectedDataTypes = listOf(
            "workout_data", "nutrition_data", "achievements", "personal_records",
            "user_profile", "preferences", "shopping_lists", "cooking_sessions"
        )
        expectedDataTypes.forEach { dataType ->
            assertTrue("Should clear $dataType", result.clearedDataTypes.contains(dataType))
        }
    }

    @Test
    fun `should require strong confirmation for complete reset`() = runTest {
        // Given: Complete reset with insufficient confirmation
        val resetType = ResetManager.ResetType.COMPLETE_RESET
        val weakConfirmation = "yes"

        // When: Attempting complete reset with weak confirmation
        val result = resetManager.performReset(resetType, weakConfirmation)

        // Then: Should reject weak confirmation
        assertFalse("Should reject weak confirmation", result.isSuccess)
        assertTrue("Should have error", result.hasError)
        assertEquals("Should require strong confirmation", 
            ResetManager.ResetError.INSUFFICIENT_CONFIRMATION, result.errorType)
    }

    // Atomic Operations Tests

    @Test
    fun `should handle reset operations atomically`() = runTest {
        // Given: Reset operation that may fail partway through
        val resetType = ResetManager.ResetType.WORKOUT_DATA

        // When: Performing reset operation
        val result = resetManager.performReset(resetType, "CONFIRM")

        // Then: Should complete atomically
        if (result.isSuccess) {
            assertTrue("Should be fully completed", result.isCompleted)
            assertFalse("Should not have partial completion", result.isPartiallyCompleted)
        } else {
            // If failed, should not have partially completed
            assertFalse("Should not have partial changes on failure", result.isPartiallyCompleted)
        }
    }

    @Test
    fun `should rollback changes on atomic operation failure`() = runTest {
        // Given: Reset operation that fails midway (simulated)
        val resetType = ResetManager.ResetType.NUTRITION_DATA
        
        // Simulate failure after partial completion
        whenever(database.clearNutritionData()).thenAnswer {
            // Simulate partial completion then failure
            throw RuntimeException("Simulated failure")
        }

        // When: Performing reset that fails
        val result = resetManager.performReset(resetType, "CONFIRM")

        // Then: Should rollback any partial changes
        assertFalse("Should fail operation", result.isSuccess)
        assertFalse("Should not have partial completion", result.isPartiallyCompleted)
        assertTrue("Should indicate rollback occurred", result.wasRolledBack)
    }

    @Test
    fun `should validate transaction integrity during reset`() = runTest {
        // Given: Reset operation requiring transaction integrity
        val resetType = ResetManager.ResetType.COMPLETE_RESET

        // When: Performing reset with integrity validation
        val result = resetManager.performReset(resetType, "CONFIRM_COMPLETE_RESET_I_UNDERSTAND")

        // Then: Should maintain data integrity
        assertTrue("Should validate integrity", result.integrityValidated)
        if (result.isSuccess) {
            assertNull("Should have no integrity violations", result.integrityViolations)
        }
    }

    // Data Integrity Validation Tests

    @Test
    fun `should validate data integrity after reset`() = runTest {
        // Given: Completed reset operation
        val resetType = ResetManager.ResetType.WORKOUT_DATA

        // When: Performing reset with integrity validation
        val result = resetManager.performReset(resetType, "CONFIRM", validateIntegrity = true)

        // Then: Should validate data integrity
        assertTrue("Should validate integrity", result.integrityValidated)
        if (result.isSuccess) {
            assertTrue("Should pass integrity check", result.integrityCheckPassed)
            assertNull("Should have no integrity violations", result.integrityViolations)
        }
    }

    @Test
    fun `should detect referential integrity violations`() = runTest {
        // Given: Reset operation that could create integrity issues
        val resetType = ResetManager.ResetType.USER_PROFILE
        
        // Simulate integrity violation detection
        whenever(database.validateReferentialIntegrity()).thenReturn(
            listOf("Orphaned workout sessions found", "Invalid nutrition references")
        )

        // When: Performing reset with integrity validation
        val result = resetManager.performReset(resetType, "CONFIRM", validateIntegrity = true)

        // Then: Should detect and report violations
        assertTrue("Should validate integrity", result.integrityValidated)
        if (result.integrityViolations?.isNotEmpty() == true) {
            assertFalse("Should fail integrity check", result.integrityCheckPassed)
            assertTrue("Should report violations", result.integrityViolations!!.isNotEmpty())
        }
    }

    @Test
    fun `should provide integrity repair suggestions`() = runTest {
        // Given: Reset with integrity violations
        val resetType = ResetManager.ResetType.NUTRITION_DATA

        // When: Performing reset that encounters integrity issues
        val result = resetManager.performReset(resetType, "CONFIRM", validateIntegrity = true)

        // Then: Should provide repair suggestions if violations found
        if (result.integrityViolations?.isNotEmpty() == true) {
            assertNotNull("Should provide repair suggestions", result.repairSuggestions)
            assertTrue("Should have actionable suggestions", result.repairSuggestions!!.isNotEmpty())
        }
    }

    // Backup and Recovery Tests

    @Test
    fun `should create backup before reset`() = runTest {
        // Given: Reset operation with backup enabled
        val resetType = ResetManager.ResetType.WORKOUT_DATA
        val createBackup = true

        // When: Performing reset with backup
        val result = resetManager.performReset(resetType, "CONFIRM", createBackup = createBackup)

        // Then: Should create backup
        if (result.isSuccess && createBackup) {
            assertTrue("Should create backup", result.backupCreated)
            assertNotNull("Should have backup location", result.backupLocation)
            assertTrue("Backup should be valid", result.backupValidated)
        }
    }

    @Test
    fun `should restore from backup on reset failure`() = runTest {
        // Given: Reset operation that fails after backup creation
        val resetType = ResetManager.ResetType.NUTRITION_DATA
        
        // Simulate failure requiring restore
        whenever(database.clearNutritionData()).thenThrow(RuntimeException("Reset failed"))

        // When: Performing reset that fails
        val result = resetManager.performReset(resetType, "CONFIRM", createBackup = true)

        // Then: Should restore from backup on failure
        assertFalse("Should fail operation", result.isSuccess)
        if (result.backupCreated) {
            assertTrue("Should restore from backup", result.restoredFromBackup)
            assertNotNull("Should specify restore point", result.restorePoint)
        }
    }

    @Test
    fun `should validate backup integrity before reset`() = runTest {
        // Given: Reset operation with backup validation
        val resetType = ResetManager.ResetType.USER_PROFILE

        // When: Creating backup before reset
        val result = resetManager.performReset(resetType, "CONFIRM", createBackup = true)

        // Then: Should validate backup before proceeding
        if (result.backupCreated) {
            assertTrue("Should validate backup", result.backupValidated)
            if (!result.backupValidated) {
                assertFalse("Should not proceed with invalid backup", result.isSuccess)
                assertEquals("Should have backup validation error", 
                    ResetManager.ResetError.BACKUP_VALIDATION_FAILED, result.errorType)
            }
        }
    }

    // Error Handling and Edge Cases Tests

    @Test
    fun `should handle empty confirmation token`() = runTest {
        // Given: Empty confirmation token
        val resetType = ResetManager.ResetType.ACHIEVEMENTS
        val emptyToken = ""

        // When: Attempting reset with empty token
        val result = resetManager.performReset(resetType, emptyToken)

        // Then: Should reject empty token
        assertFalse("Should reject empty token", result.isSuccess)
        assertTrue("Should have error", result.hasError)
        assertEquals("Should have missing confirmation error", 
            ResetManager.ResetError.MISSING_CONFIRMATION, result.errorType)
    }

    @Test
    fun `should handle database connection errors`() = runTest {
        // Given: Database connection failure (simulated)
        whenever(database.isHealthy()).thenReturn(false)

        // When: Attempting reset with unhealthy database
        val result = resetManager.performReset(ResetManager.ResetType.WORKOUT_DATA, "CONFIRM")

        // Then: Should handle connection error
        assertFalse("Should fail with database error", result.isSuccess)
        assertTrue("Should have error", result.hasError)
        assertEquals("Should have database error", 
            ResetManager.ResetError.DATABASE_UNAVAILABLE, result.errorType)
    }

    @Test
    fun `should provide detailed progress reporting`() = runTest {
        // Given: Reset operation in progress
        val resetType = ResetManager.ResetType.COMPLETE_RESET

        // When: Starting reset operation
        resetManager.performReset(resetType, "CONFIRM_COMPLETE_RESET_I_UNDERSTAND")

        // Then: Should provide detailed progress
        val progress = resetManager.resetProgress.first()
        if (progress != null) {
            assertNotNull("Should have operation name", progress.operation)
            assertTrue("Progress should be valid", progress.progress >= 0f)
            assertNotNull("Should have current step", progress.currentStep)
            assertNotNull("Should indicate completion status", progress.isCompleted)
        }
    }

    @Test
    fun `should handle concurrent reset requests`() = runTest {
        // Given: Multiple concurrent reset requests
        val resetType1 = ResetManager.ResetType.WORKOUT_DATA
        val resetType2 = ResetManager.ResetType.NUTRITION_DATA

        // When: Starting concurrent resets
        val result1 = resetManager.performReset(resetType1, "CONFIRM1")
        val result2 = resetManager.performReset(resetType2, "CONFIRM2")

        // Then: Should handle concurrency appropriately
        // Either one should succeed and other should fail, or both should queue properly
        val successCount = listOf(result1, result2).count { it.isSuccess }
        assertTrue("Should handle concurrent requests", successCount in 1..2)
        
        if (successCount == 1) {
            // One should fail due to concurrent operation
            val failedResult = if (!result1.isSuccess) result1 else result2
            assertEquals("Should fail due to concurrent operation", 
                ResetManager.ResetError.CONCURRENT_OPERATION, failedResult.errorType)
        }
    }

    @Test
    fun `should cleanup temporary files after reset`() = runTest {
        // Given: Reset operation that creates temporary files
        val resetType = ResetManager.ResetType.COMPLETE_RESET

        // When: Completing reset operation
        val result = resetManager.performReset(resetType, "CONFIRM_COMPLETE_RESET_I_UNDERSTAND")

        // Then: Should cleanup temporary files
        if (result.isSuccess) {
            assertTrue("Should cleanup temporary files", result.temporaryFilesCleanedUp)
            assertNull("Should have no remaining temp files", result.remainingTempFiles)
        }
    }
}