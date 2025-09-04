package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.PersonalAchievementEntity
import com.example.fitapp.data.db.TodayWorkoutEntity
import com.example.fitapp.data.repo.PersonalMotivationRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Unit tests for PersonalAchievementManager
 * Tests achievement trigger logic and business rules
 */
class PersonalAchievementManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var repository: PersonalMotivationRepository

    private lateinit var achievementManager: PersonalAchievementManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        achievementManager = PersonalAchievementManager(context, repository)
    }

    // Achievement Trigger Tests

    @Test
    fun `first steps achievement triggers on first app use`() = runTest {
        // Given: A pending "Erste Schritte" achievement and completed workout
        val achievement = createTestAchievement(
            title = "Erste Schritte",
            category = PersonalAchievementManager.CATEGORY_FITNESS,
            isCompleted = false
        )
        
        val mockWorkout = createMockWorkout(status = "completed")
        
        whenever(repository.achievementsByCompletionFlow(false))
            .thenReturn(flowOf(listOf(achievement)))
        whenever(repository.getWorkoutsBetween(any(), any()))
            .thenReturn(listOf(mockWorkout))
        whenever(repository.getAchievement(achievement.id))
            .thenReturn(achievement)
        
        // When: Checking achievements
        achievementManager.checkAndUnlockAchievements()
        
        // Then: Achievement should be completed
        verify(repository).markAchievementCompleted(
            eq(achievement.id),
            eq(true),
            any()
        )
    }

    @Test
    fun `weekly warrior achievement triggers after 5 trainings`() = runTest {
        // Given: A weekly warrior achievement with 4 workouts (needs 5)
        val achievement = createTestAchievement(
            title = "Wöchentlicher Krieger",
            category = PersonalAchievementManager.CATEGORY_FITNESS,
            targetValue = 5.0,
            currentValue = 4.0,
            isCompleted = false
        )
        
        val completedWorkouts = (1..5).map { createMockWorkout(status = "completed") }
        
        whenever(repository.achievementsByCategoryFlow(PersonalAchievementManager.CATEGORY_FITNESS))
            .thenReturn(flowOf(listOf(achievement)))
        whenever(repository.getWorkoutsBetween(any(), any()))
            .thenReturn(completedWorkouts)
        whenever(repository.getAchievement(achievement.id))
            .thenReturn(achievement.copy(currentValue = 5.0))
        
        // When: Tracking workout completion
        achievementManager.trackWorkoutCompletion()
        
        // Then: Achievement progress should be updated to 5 and completed
        verify(repository).updateAchievementProgress(achievement.id, 5.0)
        verify(repository).markAchievementCompleted(
            eq(achievement.id),
            eq(true),
            any()
        )
    }

    @Test
    fun `nutrition tracker achievement triggers after 7 days logging`() = runTest {
        // Given: A nutrition tracker achievement at 6 days (needs 7)
        val achievement = createTestAchievement(
            title = "Nahrungs-Tracker",
            category = PersonalAchievementManager.CATEGORY_NUTRITION,
            targetValue = 7.0,
            currentValue = 6.0,
            isCompleted = false
        )
        
        whenever(repository.achievementsByCategoryFlow(PersonalAchievementManager.CATEGORY_NUTRITION))
            .thenReturn(flowOf(listOf(achievement)))
        whenever(repository.getAchievement(achievement.id))
            .thenReturn(achievement.copy(currentValue = 7.0))
        
        // When: Tracking nutrition logging
        achievementManager.trackNutritionLogging()
        
        // Then: Achievement should be completed
        verify(repository).updateAchievementProgress(achievement.id, 7.0)
        verify(repository).markAchievementCompleted(
            eq(achievement.id),
            eq(true),
            any()
        )
    }

    @Test
    fun `achievement progress updates correctly for numeric achievements`() = runTest {
        // Given: An achievement with progress tracking
        val achievement = createTestAchievement(
            targetValue = 10.0,
            currentValue = 5.0,
            isCompleted = false
        )
        
        whenever(repository.getAchievement(achievement.id))
            .thenReturn(achievement)
        
        // When: Updating progress
        achievementManager.updateAchievementProgress(achievement.id, 8.0)
        
        // Then: Progress should be updated
        verify(repository).updateAchievementProgress(achievement.id, 8.0)
        
        // But not completed since 8 < 10
        verify(repository, never()).markAchievementCompleted(
            eq(achievement.id),
            eq(true),
            any()
        )
    }

    @Test
    fun `achievement completes when target value reached`() = runTest {
        // Given: An achievement reaching its target
        val achievement = createTestAchievement(
            targetValue = 10.0,
            currentValue = 9.0,
            isCompleted = false
        )
        
        whenever(repository.getAchievement(achievement.id))
            .thenReturn(achievement)
        
        // When: Updating progress to meet target
        achievementManager.updateAchievementProgress(achievement.id, 10.0)
        
        // Then: Achievement should be marked as completed
        verify(repository).updateAchievementProgress(achievement.id, 10.0)
        verify(repository).markAchievementCompleted(
            eq(achievement.id),
            eq(true),
            any()
        )
    }

    @Test
    fun `initializeDefaultAchievements creates standard achievements`() = runTest {
        // Given: No existing achievements
        whenever(repository.allAchievementsFlow())
            .thenReturn(flowOf(emptyList()))
        
        // When: Initializing default achievements
        achievementManager.initializeDefaultAchievements()
        
        // Then: Default achievements should be created
        verify(repository, atLeastOnce()).insertAchievement(any())
    }

    @Test
    fun `initializeDefaultAchievements skips creation when achievements exist`() = runTest {
        // Given: Existing achievements
        val existingAchievement = createTestAchievement()
        whenever(repository.allAchievementsFlow())
            .thenReturn(flowOf(listOf(existingAchievement)))
        
        // When: Initializing default achievements
        achievementManager.initializeDefaultAchievements()
        
        // Then: No new achievements should be created
        verify(repository, never()).insertAchievement(any())
    }

    @Test
    fun `workout completion tracking updates fitness achievements`() = runTest {
        // Given: Fitness achievements that need updating
        val fitnessAchievement = createTestAchievement(
            title = "Wöchentlicher Krieger",
            category = PersonalAchievementManager.CATEGORY_FITNESS,
            isCompleted = false
        )
        
        val completedWorkouts = listOf(
            createMockWorkout(status = "completed"),
            createMockWorkout(status = "completed")
        )
        
        whenever(repository.achievementsByCategoryFlow(PersonalAchievementManager.CATEGORY_FITNESS))
            .thenReturn(flowOf(listOf(fitnessAchievement)))
        whenever(repository.getWorkoutsBetween(any(), any()))
            .thenReturn(completedWorkouts)
        
        // When: Tracking workout completion
        achievementManager.trackWorkoutCompletion()
        
        // Then: Fitness achievement progress should be updated
        verify(repository).updateAchievementProgress(fitnessAchievement.id, 2.0)
    }

    @Test
    fun `nutrition logging tracking updates nutrition achievements`() = runTest {
        // Given: Nutrition achievements that need updating
        val nutritionAchievement = createTestAchievement(
            title = "Nahrungs-Tracker",
            category = PersonalAchievementManager.CATEGORY_NUTRITION,
            currentValue = 3.0,
            isCompleted = false
        )
        
        whenever(repository.achievementsByCategoryFlow(PersonalAchievementManager.CATEGORY_NUTRITION))
            .thenReturn(flowOf(listOf(nutritionAchievement)))
        
        // When: Tracking nutrition logging
        achievementManager.trackNutritionLogging()
        
        // Then: Nutrition achievement progress should be incremented
        verify(repository).updateAchievementProgress(nutritionAchievement.id, 4.0)
    }

    @Test
    fun `completed achievements are not checked again`() = runTest {
        // Given: No pending achievements (completed ones are filtered out)
        whenever(repository.achievementsByCompletionFlow(false))
            .thenReturn(flowOf(emptyList())) // No pending achievements
        
        // When: Checking achievements
        achievementManager.checkAndUnlockAchievements()
        
        // Then: No achievement completion should be triggered
        verify(repository, never()).markAchievementCompleted(any(), any(), any())
    }

    @Test
    fun `achievement completion triggers notification`() = runTest {
        // Given: An achievement to complete
        val achievement = createTestAchievement()
        
        whenever(repository.getAchievement(achievement.id))
            .thenReturn(achievement)
        
        // When: Completing achievement manually
        achievementManager.completeAchievement(achievement.id)
        
        // Then: Achievement should be marked as completed
        verify(repository).markAchievementCompleted(
            eq(achievement.id),
            eq(true),
            any()
        )
        
        // And notification should be prepared (we verify the repository call)
        verify(repository).getAchievement(achievement.id)
    }

    // Helper Methods

    private fun createTestAchievement(
        id: Long = 1L,
        title: String = "Test Achievement",
        description: String = "Test Description",
        category: String = PersonalAchievementManager.CATEGORY_FITNESS,
        targetValue: Double? = null,
        currentValue: Double = 0.0,
        isCompleted: Boolean = false,
        iconName: String = "trophy"
    ): PersonalAchievementEntity {
        return PersonalAchievementEntity(
            id = id,
            title = title,
            description = description,
            iconName = iconName,
            category = category,
            targetValue = targetValue,
            currentValue = currentValue,
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() / 1000 else null,
            createdAt = System.currentTimeMillis() / 1000
        )
    }

    private fun createMockWorkout(status: String = "completed"): TodayWorkoutEntity {
        return TodayWorkoutEntity(
            dateIso = "2024-01-15",
            content = "Test workout content",
            status = status,
            createdAt = System.currentTimeMillis() / 1000,
            completedAt = if (status == "completed") System.currentTimeMillis() / 1000 else null,
            planId = null
        )
    }
}