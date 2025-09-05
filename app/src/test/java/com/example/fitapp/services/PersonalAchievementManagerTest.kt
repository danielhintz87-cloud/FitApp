package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.PersonalAchievementEntity
import com.example.fitapp.data.repo.PersonalMotivationRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.time.LocalDate

/**
 * Unit tests for PersonalAchievementManager
 * Tests achievement tracking, categorization, and progress management
 */
class PersonalAchievementManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var repository: PersonalMotivationRepository

    private lateinit var personalAchievementManager: PersonalAchievementManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        personalAchievementManager = PersonalAchievementManager(context, repository)
    }

    @Test
    fun `should instantiate PersonalAchievementManager correctly`() {
        assertNotNull("PersonalAchievementManager should be instantiated", personalAchievementManager)
    }

    @Test
    fun `should have correct achievement categories`() {
        assertEquals("Fitness category constant", "fitness", PersonalAchievementManager.CATEGORY_FITNESS)
        assertEquals("Nutrition category constant", "nutrition", PersonalAchievementManager.CATEGORY_NUTRITION)
        assertEquals("Streak category constant", "streak", PersonalAchievementManager.CATEGORY_STREAK)
        assertEquals("Milestone category constant", "milestone", PersonalAchievementManager.CATEGORY_MILESTONE)
    }

    @Test
    fun `should have correct achievement types`() {
        assertEquals("Workout count type", "workout_count", PersonalAchievementManager.TYPE_WORKOUT_COUNT)
        assertEquals("Nutrition logging type", "nutrition_logging", PersonalAchievementManager.TYPE_NUTRITION_LOGGING)
        assertEquals("Streak achievement type", "streak_achievement", PersonalAchievementManager.TYPE_STREAK_ACHIEVEMENT)
        assertEquals("Weight tracking type", "weight_tracking", PersonalAchievementManager.TYPE_WEIGHT_TRACKING)
    }

    @Test
    fun `should get all achievements from repository`() = runTest {
        // Given: Mock achievements from repository
        val mockAchievements = listOf(
            createMockAchievement(1L, "First Workout", PersonalAchievementManager.CATEGORY_FITNESS),
            createMockAchievement(2L, "Week Streak", PersonalAchievementManager.CATEGORY_STREAK)
        )
        whenever(repository.allAchievementsFlow()).thenReturn(flowOf(mockAchievements))

        // When: Getting all achievements
        val achievements = personalAchievementManager.getAllAchievements().first()

        // Then: Should return all achievements from repository
        assertEquals("Should return all achievements", 2, achievements.size)
        assertEquals("First achievement should match", "First Workout", achievements[0].title)
        assertEquals("Second achievement should match", "Week Streak", achievements[1].title)
        verify(repository).allAchievementsFlow()
    }

    @Test
    fun `should get completed achievements from repository`() = runTest {
        // Given: Mock completed achievements
        val completedAchievements = listOf(
            createMockAchievement(1L, "Completed Achievement", PersonalAchievementManager.CATEGORY_FITNESS, true)
        )
        whenever(repository.achievementsByCompletionFlow(true)).thenReturn(flowOf(completedAchievements))

        // When: Getting completed achievements
        val achievements = personalAchievementManager.getCompletedAchievements().first()

        // Then: Should return completed achievements
        assertEquals("Should return completed achievements", 1, achievements.size)
        assertTrue("Achievement should be completed", achievements[0].isCompleted)
        verify(repository).achievementsByCompletionFlow(true)
    }

    @Test
    fun `should get pending achievements from repository`() = runTest {
        // Given: Mock pending achievements
        val pendingAchievements = listOf(
            createMockAchievement(1L, "Pending Achievement", PersonalAchievementManager.CATEGORY_FITNESS, false)
        )
        whenever(repository.achievementsByCompletionFlow(false)).thenReturn(flowOf(pendingAchievements))

        // When: Getting pending achievements
        val achievements = personalAchievementManager.getPendingAchievements().first()

        // Then: Should return pending achievements
        assertEquals("Should return pending achievements", 1, achievements.size)
        assertFalse("Achievement should not be completed", achievements[0].isCompleted)
        verify(repository).achievementsByCompletionFlow(false)
    }

    @Test
    fun `should get achievements by category from repository`() = runTest {
        // Given: Mock achievements for fitness category
        val fitnessAchievements = listOf(
            createMockAchievement(1L, "Fitness Achievement 1", PersonalAchievementManager.CATEGORY_FITNESS),
            createMockAchievement(2L, "Fitness Achievement 2", PersonalAchievementManager.CATEGORY_FITNESS)
        )
        whenever(repository.achievementsByCategoryFlow(PersonalAchievementManager.CATEGORY_FITNESS))
            .thenReturn(flowOf(fitnessAchievements))

        // When: Getting achievements by fitness category
        val achievements = personalAchievementManager.getAchievementsByCategory(
            PersonalAchievementManager.CATEGORY_FITNESS
        ).first()

        // Then: Should return fitness achievements
        assertEquals("Should return fitness achievements", 2, achievements.size)
        achievements.forEach { achievement ->
            assertEquals("All achievements should be fitness category", 
                PersonalAchievementManager.CATEGORY_FITNESS, achievement.category)
        }
        verify(repository).achievementsByCategoryFlow(PersonalAchievementManager.CATEGORY_FITNESS)
    }

    @Test
    fun `should handle different achievement categories`() = runTest {
        val categories = listOf(
            PersonalAchievementManager.CATEGORY_FITNESS,
            PersonalAchievementManager.CATEGORY_NUTRITION,
            PersonalAchievementManager.CATEGORY_STREAK,
            PersonalAchievementManager.CATEGORY_MILESTONE
        )

        for (category in categories) {
            // Given: Mock achievements for each category
            val categoryAchievements = listOf(
                createMockAchievement(1L, "Achievement for $category", category)
            )
            whenever(repository.achievementsByCategoryFlow(category))
                .thenReturn(flowOf(categoryAchievements))

            // When: Getting achievements for this category
            val achievements = personalAchievementManager.getAchievementsByCategory(category).first()

            // Then: Should return achievements for the correct category
            assertEquals("Should return 1 achievement for $category", 1, achievements.size)
            assertEquals("Achievement should have correct category", category, achievements[0].category)
        }
    }

    @Test
    fun `should handle empty achievement lists`() = runTest {
        // Given: Empty achievement lists from repository
        whenever(repository.allAchievementsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.achievementsByCompletionFlow(any())).thenReturn(flowOf(emptyList()))
        whenever(repository.achievementsByCategoryFlow(any())).thenReturn(flowOf(emptyList()))

        // When: Getting achievements
        val allAchievements = personalAchievementManager.getAllAchievements().first()
        val completedAchievements = personalAchievementManager.getCompletedAchievements().first()
        val pendingAchievements = personalAchievementManager.getPendingAchievements().first()
        val categoryAchievements = personalAchievementManager.getAchievementsByCategory(
            PersonalAchievementManager.CATEGORY_FITNESS
        ).first()

        // Then: Should handle empty lists gracefully
        assertTrue("All achievements should be empty", allAchievements.isEmpty())
        assertTrue("Completed achievements should be empty", completedAchievements.isEmpty())
        assertTrue("Pending achievements should be empty", pendingAchievements.isEmpty())
        assertTrue("Category achievements should be empty", categoryAchievements.isEmpty())
    }

    @Test
    fun `should handle achievement progress tracking`() {
        // Test different progress values
        val progressValues = listOf(0.0, 25.0, 50.0, 75.0, 100.0)

        for (progress in progressValues) {
            // Given: Achievement with specific progress
            val achievement = createMockAchievement(
                id = 1L,
                title = "Progress Test",
                category = PersonalAchievementManager.CATEGORY_FITNESS,
                isCompleted = progress >= 100.0,
                currentValue = progress
            )

            // Then: Progress should be handled correctly
            assertEquals("Progress should match", progress, achievement.currentValue, 0.01)
            assertEquals("Completion should match progress", progress >= 100.0, achievement.isCompleted)
        }
    }

    @Test
    fun `should handle achievement target values`() {
        // Test different target values
        val targetValues = listOf(1.0, 5.0, 10.0, 50.0, 100.0)

        for (target in targetValues) {
            // Given: Achievement with specific target
            val achievement = createMockAchievement(
                id = 1L,
                title = "Target Test",
                category = PersonalAchievementManager.CATEGORY_FITNESS,
                targetValue = target
            )

            // Then: Target should be set correctly
            assertEquals("Target value should match", target, achievement.targetValue)
        }
    }

    @Test
    fun `should handle achievement types correctly`() {
        val achievementTypes = listOf(
            PersonalAchievementManager.TYPE_WORKOUT_COUNT,
            PersonalAchievementManager.TYPE_NUTRITION_LOGGING,
            PersonalAchievementManager.TYPE_STREAK_ACHIEVEMENT,
            PersonalAchievementManager.TYPE_WEIGHT_TRACKING
        )

        for (type in achievementTypes) {
            // Given: Achievement with specific type (simulated in description)
            val achievement = createMockAchievement(
                id = 1L,
                title = "Type Test",
                category = PersonalAchievementManager.CATEGORY_FITNESS,
                description = "Achievement type: $type"
            )

            // Then: Type should be referenced correctly
            assertTrue("Description should contain type", achievement.description.contains(type))
        }
    }

    @Test
    fun `should handle achievement dates correctly`() {
        // Given: Achievement with timestamps
        val createdTime = System.currentTimeMillis() / 1000
        val completedTime = createdTime + 86400 // 1 day later

        val achievement = createMockAchievement(
            id = 1L,
            title = "Date Test",
            category = PersonalAchievementManager.CATEGORY_FITNESS,
            isCompleted = true,
            createdAt = createdTime,
            completedAt = completedTime
        )

        // Then: Timestamps should be set correctly
        assertEquals("Created timestamp should match", createdTime, achievement.createdAt)
        assertEquals("Completed timestamp should match", completedTime, achievement.completedAt)
    }

    @Test
    fun `should handle achievement descriptions and icons`() {
        // Given: Achievement with description and icon
        val description = "Complete your first workout session"
        val iconName = "fitness_center"

        val achievement = createMockAchievement(
            id = 1L,
            title = "First Workout",
            category = PersonalAchievementManager.CATEGORY_FITNESS,
            description = description,
            iconName = iconName
        )

        // Then: Description and icon should be set correctly
        assertEquals("Description should match", description, achievement.description)
        assertEquals("Icon name should match", iconName, achievement.iconName)
    }

    private fun createMockAchievement(
        id: Long,
        title: String,
        category: String,
        isCompleted: Boolean = false,
        currentValue: Double = 0.0,
        targetValue: Double? = 1.0,
        description: String = "Test description",
        iconName: String = "trophy",
        createdAt: Long = System.currentTimeMillis() / 1000,
        completedAt: Long? = if (isCompleted) System.currentTimeMillis() / 1000 else null
    ): PersonalAchievementEntity {
        return PersonalAchievementEntity(
            id = id,
            title = title,
            description = description,
            category = category,
            iconName = iconName,
            targetValue = targetValue,
            currentValue = currentValue,
            isCompleted = isCompleted,
            createdAt = createdAt,
            completedAt = completedAt
        )
    }
}