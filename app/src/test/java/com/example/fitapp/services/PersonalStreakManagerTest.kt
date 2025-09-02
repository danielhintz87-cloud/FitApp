package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.PersonalStreakEntity
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
import java.time.ZoneId

/**
 * Unit tests for PersonalStreakManager
 * Tests critical streak calculation logic and business rules
 */
class PersonalStreakManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var repository: PersonalMotivationRepository

    private lateinit var streakManager: PersonalStreakManager

    private val testDate = LocalDate.of(2024, 1, 15)
    private val testTimestamp = testDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        streakManager = PersonalStreakManager(context, repository)
    }

    // Core Streak Calculation Tests

    @Test
    fun `updateWeightTrackingStreak increases current streak when weight logged today`() = runTest {
        // Given: A weight tracking streak and weight entry for today
        val streak = createTestStreak(
            category = PersonalStreakManager.CATEGORY_WEIGHT,
            currentStreak = 5,
            longestStreak = 10
        )
        
        whenever(repository.hasWeightEntryForDate(any())).thenReturn(1)
        
        whenever(repository.streaksByCategoryFlow(PersonalStreakManager.CATEGORY_WEIGHT))
            .thenReturn(flowOf(listOf(streak)))
        
        // When: Updating weight tracking streak
        streakManager.trackWeightLogging(testDate)
        
        // Then: Current streak should increment
        verify(repository).updateStreakCounts(
            streak.id,
            6, // currentStreak + 1
            10, // longestStreak remains
            testTimestamp
        )
    }

    @Test
    fun `updateTrainingStreak resets to 0 when training skipped for 2 days`() = runTest {
        // Given: A workout streak with last activity 2 days ago
        val twoDaysAgo = testDate.minusDays(2)
        val twoDaysAgoTimestamp = twoDaysAgo.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        
        val streak = createTestStreak(
            category = PersonalStreakManager.CATEGORY_WORKOUT,
            currentStreak = 5,
            longestStreak = 10,
            lastActivityTimestamp = twoDaysAgoTimestamp
        )
        
        whenever(repository.streaksByCategoryFlow(PersonalStreakManager.CATEGORY_WORKOUT))
            .thenReturn(flowOf(listOf(streak)))
        whenever(repository.getTodayWorkout(any())).thenReturn(null)
        
        // When: Updating workout streak
        streakManager.trackWorkoutCompletion(testDate)
        
        // Then: Streak should be broken (reset to 0)
        verify(repository).updateStreakCounts(
            streak.id,
            0, // currentStreak reset
            10, // longestStreak preserved
            null // lastActivityTimestamp cleared
        )
    }

    @Test
    fun `calculateLongestStreak returns correct maximum streak period`() = runTest {
        // Given: Multiple streaks with different longest streaks
        val streaks = listOf(
            createTestStreak(longestStreak = 15),
            createTestStreak(longestStreak = 7),
            createTestStreak(longestStreak = 25),
            createTestStreak(longestStreak = 3)
        )
        
        whenever(repository.allStreaksFlow()).thenReturn(flowOf(streaks))
        whenever(repository.activeStreaksFlow()).thenReturn(flowOf(streaks.filter { it.isActive }))
        
        // When: Getting streak statistics
        val stats = streakManager.getStreakStatistics()
        
        // Then: Longest streak should be the maximum
        assertEquals(25, stats.longestEverStreak)
    }

    @Test
    fun `initializeDefaultStreaks creates standard streak categories`() = runTest {
        // Given: No existing streaks
        whenever(repository.allStreaksFlow()).thenReturn(flowOf(emptyList()))
        
        // When: Initializing default streaks
        streakManager.initializeDefaultStreaks()
        
        // Then: Should create default streaks for all categories
        verify(repository, times(4)).insertStreak(any())
        
        // Verify specific categories are created
        val capturedStreaks = argumentCaptor<PersonalStreakEntity>()
        verify(repository, times(4)).insertStreak(capturedStreaks.capture())
        
        val categories = capturedStreaks.allValues.map { it.category }
        assertTrue(categories.contains(PersonalStreakManager.CATEGORY_WORKOUT))
        assertTrue(categories.contains(PersonalStreakManager.CATEGORY_NUTRITION))
        assertTrue(categories.contains(PersonalStreakManager.CATEGORY_HABIT))
        assertTrue(categories.contains(PersonalStreakManager.CATEGORY_WEIGHT))
    }

    @Test
    fun `trackWeightLogging triggers streak update when weight logged`() = runTest {
        // Given: A weight tracking streak
        val streak = createTestStreak(
            category = PersonalStreakManager.CATEGORY_WEIGHT,
            currentStreak = 2
        )
        
        whenever(repository.streaksByCategoryFlow(PersonalStreakManager.CATEGORY_WEIGHT))
            .thenReturn(flowOf(listOf(streak)))
        whenever(repository.hasWeightEntryForDate(any())).thenReturn(1)
        
        // When: Tracking weight logging
        streakManager.trackWeightLogging(testDate)
        
        // Then: Streak should be incremented
        verify(repository).updateStreakCounts(
            streak.id,
            3, // currentStreak + 1
            eq(streak.longestStreak),
            testTimestamp
        )
    }

    // Helper Methods

    private fun createTestStreak(
        id: Long = 1L,
        category: String = PersonalStreakManager.CATEGORY_WORKOUT,
        currentStreak: Int = 0,
        longestStreak: Int = 0,
        lastActivityTimestamp: Long? = null,
        isActive: Boolean = true
    ): PersonalStreakEntity {
        return PersonalStreakEntity(
            id = id,
            name = "Test Streak",
            description = "Test Description",
            category = category,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            targetDays = 30,
            isActive = isActive,
            lastActivityTimestamp = lastActivityTimestamp,
            createdAt = testTimestamp
        )
    }
}