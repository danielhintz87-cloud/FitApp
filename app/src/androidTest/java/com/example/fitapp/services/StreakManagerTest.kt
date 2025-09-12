package com.example.fitapp.services

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitapp.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class StreakManagerTest {
    private lateinit var database: AppDatabase
    private lateinit var streakManager: StreakManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database =
            Room.inMemoryDatabaseBuilder(
                context,
                AppDatabase::class.java,
            ).allowMainThreadQueries().build()

        streakManager = StreakManager(context, database)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testInitialMealLogging_CreatesStreakWith1Day() =
        runTest {
            val today = LocalDate.now()

            // First meal log should create streak with 1 day
            streakManager.onMealLogged(today)

            val streak = streakManager.getCurrentMealLoggingStreak()
            assertNotNull(streak)
            assertEquals(1, streak?.currentStreak)
            assertEquals(1, streak?.longestStreak)
            assertEquals(today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond(), streak?.lastActivityTimestamp)
        }

    @Test
    fun testConsecutiveDayMealLogging_IncrementsStreak() =
        runTest {
            val day1 = LocalDate.now().minusDays(2)
            val day2 = LocalDate.now().minusDays(1)
            val day3 = LocalDate.now()

            // Log meals for consecutive days
            streakManager.onMealLogged(day1)
            streakManager.onMealLogged(day2)
            streakManager.onMealLogged(day3)

            val streak = streakManager.getCurrentMealLoggingStreak()
            assertNotNull(streak)
            assertEquals(3, streak?.currentStreak)
            assertEquals(3, streak?.longestStreak)
        }

    @Test
    fun testMissedDay_ResetsStreak() =
        runTest {
            val day1 = LocalDate.now().minusDays(4)
            val day2 = LocalDate.now().minusDays(3)
            // Missing day3 (day.minusDays(2))
            val day4 = LocalDate.now().minusDays(1)

            // Log meals with a gap
            streakManager.onMealLogged(day1)
            streakManager.onMealLogged(day2)
            streakManager.onMealLogged(day4) // Gap breaks streak

            val streak = streakManager.getCurrentMealLoggingStreak()
            assertNotNull(streak)
            assertEquals(1, streak?.currentStreak) // Reset to 1
            assertEquals(2, streak?.longestStreak) // Preserves previous best
        }

    @Test
    fun testSameDayMultipleMealLogs_DoesNotIncrementStreak() =
        runTest {
            val today = LocalDate.now()

            // Log multiple meals on same day
            streakManager.onMealLogged(today)
            streakManager.onMealLogged(today)
            streakManager.onMealLogged(today)

            val streak = streakManager.getCurrentMealLoggingStreak()
            assertNotNull(streak)
            assertEquals(1, streak?.currentStreak) // Should remain 1
            assertEquals(1, streak?.longestStreak)
        }

    @Test
    fun testInitializeAchievements_CreatesExpectedAchievements() =
        runTest {
            streakManager.initializeMealLoggingAchievements()

            val achievements =
                database.personalAchievementDao()
                    .achievementsByCategoryFlow("nutrition")
                    .first()

            // Should create achievements for thresholds: 3, 7, 14, 30, 100
            assertTrue(achievements.size >= 5)

            val threeDayAchievement = achievements.find { it.title == "3 Day Meal Logging Streak" }
            assertNotNull(threeDayAchievement)
            assertEquals(3.0, threeDayAchievement?.targetValue)
            assertEquals("bronze", threeDayAchievement?.badgeType)
            assertEquals("days", threeDayAchievement?.unit)
            assertFalse(threeDayAchievement?.isCompleted ?: true)

            val hundredDayAchievement = achievements.find { it.title == "100 Day Meal Logging Streak" }
            assertNotNull(hundredDayAchievement)
            assertEquals(100.0, hundredDayAchievement?.targetValue)
            assertEquals("diamond", hundredDayAchievement?.badgeType)
            assertEquals("legendary", hundredDayAchievement?.rarity)
        }

    @Test
    fun testAchievementUnlocking_3DayStreak() =
        runTest {
            // Initialize achievements first
            streakManager.initializeMealLoggingAchievements()

            val day1 = LocalDate.now().minusDays(2)
            val day2 = LocalDate.now().minusDays(1)
            val day3 = LocalDate.now()

            // Build up to 3-day streak
            streakManager.onMealLogged(day1)
            streakManager.onMealLogged(day2)
            streakManager.onMealLogged(day3)

            // Check if 3-day achievement is unlocked
            val achievement =
                database.personalAchievementDao()
                    .getAchievementByTitle("3 Day Meal Logging Streak")

            assertNotNull(achievement)
            assertTrue(achievement?.isCompleted ?: false)
            assertNotNull(achievement?.completedAt)
            assertEquals(3.0, achievement?.currentValue)
        }

    @Test
    fun testAchievementUnlocking_SkipsLowerThresholds() =
        runTest {
            // Initialize achievements first
            streakManager.initializeMealLoggingAchievements()

            // Simulate logging meals for 7 consecutive days
            for (i in 6 downTo 0) {
                val date = LocalDate.now().minusDays(i.toLong())
                streakManager.onMealLogged(date)
            }

            // Both 3-day and 7-day achievements should be unlocked
            val threeDayAchievement =
                database.personalAchievementDao()
                    .getAchievementByTitle("3 Day Meal Logging Streak")
            val sevenDayAchievement =
                database.personalAchievementDao()
                    .getAchievementByTitle("7 Day Meal Logging Streak")

            assertTrue(threeDayAchievement?.isCompleted ?: false)
            assertTrue(sevenDayAchievement?.isCompleted ?: false)
            assertEquals(7.0, sevenDayAchievement?.currentValue)
        }

    @Test
    fun testPersistence_StreakSurvivesAppRestart() =
        runTest {
            val today = LocalDate.now()

            // Log a meal
            streakManager.onMealLogged(today)

            // Create new StreakManager instance to simulate app restart
            val newStreakManager = StreakManager(context, database)
            val streak = newStreakManager.getCurrentMealLoggingStreak()

            // Streak should still exist
            assertNotNull(streak)
            assertEquals(1, streak?.currentStreak)
            assertEquals(today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond(), streak?.lastActivityTimestamp)
        }

    @Test
    fun testPersistence_AchievementsSurviveAppRestart() =
        runTest {
            // Initialize achievements and unlock one
            streakManager.initializeMealLoggingAchievements()

            // Build 3-day streak
            for (i in 2 downTo 0) {
                val date = LocalDate.now().minusDays(i.toLong())
                streakManager.onMealLogged(date)
            }

            // Create new StreakManager to simulate restart
            val newStreakManager = StreakManager(context, database)

            // Achievement should still be unlocked
            val achievement =
                database.personalAchievementDao()
                    .getAchievementByTitle("3 Day Meal Logging Streak")

            assertTrue(achievement?.isCompleted ?: false)
            assertNotNull(achievement?.completedAt)
        }
}
