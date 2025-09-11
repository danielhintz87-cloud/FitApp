package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.PersonalStreakEntity
import com.example.fitapp.data.db.PersonalAchievementEntity
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

/**
 * StreakManager handles meal logging streaks and related achievements.
 * Tracks consecutive days of meal logging and unlocks achievements based on milestones.
 */
class StreakManager(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.get(context)
) {
    
    companion object {
        private const val TAG = "StreakManager"
        private const val MEAL_LOGGING_STREAK_NAME = "meal_logging"
        private const val MEAL_LOGGING_CATEGORY = "nutrition"
        
        // Achievement milestone thresholds (days)
        private val ACHIEVEMENT_THRESHOLDS = listOf(3, 7, 14, 30, 100)
    }
    
    /**
     * Called when a meal is logged. Updates streak and checks for achievements.
     */
    suspend fun onMealLogged(date: LocalDate = LocalDate.now()) = withContext(Dispatchers.IO) {
        try {
            val streak = getOrCreateMealLoggingStreak()
            val updatedStreak = updateStreakForDate(streak, date)
            
            // Save updated streak
            database.personalStreakDao().update(updatedStreak)
            
            // Check for achievement unlocks
            checkAndUnlockAchievements(updatedStreak)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Meal logging streak updated",
                mapOf(
                    "date" to date.toString(),
                    "currentStreak" to updatedStreak.currentStreak,
                    "longestStreak" to updatedStreak.longestStreak
                )
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to update meal logging streak",
                exception = e
            )
        }
    }
    
    /**
     * Get current meal logging streak info
     */
    suspend fun getCurrentMealLoggingStreak(): PersonalStreakEntity? = withContext(Dispatchers.IO) {
        try {
            database.personalStreakDao().getStreakByNameAndCategory(
                MEAL_LOGGING_STREAK_NAME,
                MEAL_LOGGING_CATEGORY
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to get current meal logging streak",
                exception = e
            )
            null
        }
    }
    
    /**
     * Initialize default meal logging achievements if they don't exist
     */
    suspend fun initializeMealLoggingAchievements() = withContext(Dispatchers.IO) {
        try {
            ACHIEVEMENT_THRESHOLDS.forEach { threshold ->
                val achievementId = "meal_logging_${threshold}_days"
                
                // Check if achievement already exists
                val existing = database.personalAchievementDao()
                    .getAchievementByTitle("$threshold Day Meal Logging Streak")
                
                if (existing == null) {
                    val achievement = PersonalAchievementEntity(
                        title = "$threshold Day Meal Logging Streak",
                        description = "Log meals for $threshold consecutive days",
                        category = MEAL_LOGGING_CATEGORY,
                        iconName = when (threshold) {
                            3 -> "emoji_food_beverage"
                            7 -> "local_fire_department"
                            14 -> "military_tech"
                            30 -> "workspace_premium"
                            100 -> "diamond"
                            else -> "star"
                        },
                        targetValue = threshold.toDouble(),
                        unit = "days",
                        badgeType = when (threshold) {
                            3 -> "bronze"
                            7 -> "silver"
                            14 -> "gold"
                            30 -> "platinum"
                            100 -> "diamond"
                            else -> "bronze"
                        },
                        rarity = when (threshold) {
                            3, 7 -> "common"
                            14 -> "rare"
                            30 -> "epic"
                            100 -> "legendary"
                            else -> "common"
                        },
                        pointsValue = threshold * 10 // 10 points per day
                    )
                    
                    database.personalAchievementDao().insert(achievement)
                    
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.NUTRITION,
                        TAG,
                        "Created meal logging achievement",
                        mapOf("threshold" to threshold, "title" to achievement.title)
                    )
                }
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to initialize meal logging achievements",
                exception = e
            )
        }
    }
    
    /**
     * Get or create the meal logging streak entity
     */
    private suspend fun getOrCreateMealLoggingStreak(): PersonalStreakEntity {
        val existing = database.personalStreakDao().getStreakByNameAndCategory(
            MEAL_LOGGING_STREAK_NAME,
            MEAL_LOGGING_CATEGORY
        )
        
        return existing ?: run {
            val newStreak = PersonalStreakEntity(
                name = MEAL_LOGGING_STREAK_NAME,
                description = "Consecutive days of meal logging",
                category = MEAL_LOGGING_CATEGORY,
                currentStreak = 0,
                longestStreak = 0,
                lastActivityTimestamp = null,
                isActive = true
            )
            
            val id = database.personalStreakDao().insert(newStreak)
            newStreak.copy(id = id)
        }
    }
    
    /**
     * Update streak based on the logging date
     */
    private fun updateStreakForDate(streak: PersonalStreakEntity, date: LocalDate): PersonalStreakEntity {
        val dateTimestamp = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val yesterday = date.minusDays(1)
        val yesterdayTimestamp = yesterday.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        
        return when {
            // First time logging or same day as last activity
            streak.lastActivityTimestamp == null || streak.lastActivityTimestamp == dateTimestamp -> {
                streak.copy(
                    currentStreak = maxOf(1, streak.currentStreak),
                    longestStreak = maxOf(streak.longestStreak, maxOf(1, streak.currentStreak)),
                    lastActivityTimestamp = dateTimestamp
                )
            }
            
            // Consecutive day (logged yesterday)
            streak.lastActivityTimestamp == yesterdayTimestamp -> {
                val newCurrentStreak = streak.currentStreak + 1
                streak.copy(
                    currentStreak = newCurrentStreak,
                    longestStreak = maxOf(streak.longestStreak, newCurrentStreak),
                    lastActivityTimestamp = dateTimestamp
                )
            }
            
            // Gap in logging - reset streak
            else -> {
                streak.copy(
                    currentStreak = 1,
                    longestStreak = maxOf(streak.longestStreak, streak.currentStreak),
                    lastActivityTimestamp = dateTimestamp
                )
            }
        }
    }
    
    /**
     * Check and unlock achievements based on current streak
     */
    private suspend fun checkAndUnlockAchievements(streak: PersonalStreakEntity) {
        try {
            ACHIEVEMENT_THRESHOLDS.forEach { threshold ->
                if (streak.currentStreak >= threshold) {
                    val achievement = database.personalAchievementDao()
                        .getAchievementByTitle("$threshold Day Meal Logging Streak")
                    
                    if (achievement != null && !achievement.isCompleted) {
                        val completed = achievement.copy(
                            isCompleted = true,
                            completedAt = System.currentTimeMillis() / 1000,
                            currentValue = streak.currentStreak.toDouble()
                        )
                        
                        database.personalAchievementDao().update(completed)
                        
                        StructuredLogger.info(
                            StructuredLogger.LogCategory.NUTRITION,
                            TAG,
                            "Achievement unlocked",
                            mapOf(
                                "achievement" to achievement.title,
                                "streak" to streak.currentStreak
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to check achievements",
                exception = e
            )
        }
    }
}