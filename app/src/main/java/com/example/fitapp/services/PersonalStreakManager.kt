package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.PersonalStreakEntity
import com.example.fitapp.data.repo.PersonalMotivationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class PersonalStreakManager(
    private val context: Context,
    private val repository: PersonalMotivationRepository
) {
    
    companion object {
        // Streak categories
        const val CATEGORY_WORKOUT = "workout"
        const val CATEGORY_NUTRITION = "nutrition"
        const val CATEGORY_HABIT = "habit"
        const val CATEGORY_WEIGHT = "weight"
        
        // Grace period for streaks (hours)
        const val GRACE_PERIOD_HOURS = 6
        
        // Helper functions to convert between LocalDate and timestamp
        private fun LocalDate.toEpochSecond(): Long = 
            this.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        
        private fun Long.toLocalDate(): LocalDate =
            Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    
    fun getAllStreaks(): Flow<List<PersonalStreakEntity>> =
        repository.allStreaksFlow()
    
    fun getActiveStreaks(): Flow<List<PersonalStreakEntity>> =
        repository.activeStreaksFlow()
    
    fun getStreaksByCategory(category: String): Flow<List<PersonalStreakEntity>> =
        repository.streaksByCategoryFlow(category)
    
    /**
     * Initialize default streaks for new users
     */
    suspend fun initializeDefaultStreaks() {
        val existingStreaks = repository.allStreaksFlow().first()
        if (existingStreaks.isNotEmpty()) return
        
        val defaultStreaks = createDefaultStreaks()
        defaultStreaks.forEach { streak ->
            repository.insertStreak(streak)
        }
    }
    
    /**
     * Update all streaks based on daily activities
     */
    suspend fun updateDailyStreaks() {
        val todayTimestamp = LocalDate.now().toEpochSecond()
        val activeStreaks = repository.activeStreaksFlow().first()
        
        for (streak in activeStreaks) {
            when (streak.category) {
                CATEGORY_WORKOUT -> updateWorkoutStreak(streak, todayTimestamp)
                CATEGORY_NUTRITION -> updateNutritionStreak(streak, todayTimestamp)
                CATEGORY_HABIT -> updateHabitStreak(streak, todayTimestamp)
                CATEGORY_WEIGHT -> updateWeightTrackingStreak(streak, todayTimestamp)
            }
        }
    }
    
    /**
     * Track workout completion for streaks
     */
    suspend fun trackWorkoutCompletion(date: LocalDate = LocalDate.now()) {
        val dateTimestamp = date.toEpochSecond()
        val workoutStreaks = repository.streaksByCategoryFlow(CATEGORY_WORKOUT).first()
            .filter { it.isActive }
        
        for (streak in workoutStreaks) {
            incrementStreak(streak, dateTimestamp)
        }
    }
    
    /**
     * Track nutrition logging for streaks
     */
    suspend fun trackNutritionLogging(date: LocalDate = LocalDate.now()) {
        val dateTimestamp = date.toEpochSecond()
        val nutritionStreaks = repository.streaksByCategoryFlow(CATEGORY_NUTRITION).first()
            .filter { it.isActive }
        
        for (streak in nutritionStreaks) {
            incrementStreak(streak, dateTimestamp)
        }
    }
    
    /**
     * Track weight logging for streaks
     */
    suspend fun trackWeightLogging(date: LocalDate = LocalDate.now()) {
        val dateTimestamp = date.toEpochSecond()
        val weightStreaks = repository.streaksByCategoryFlow(CATEGORY_WEIGHT).first()
            .filter { it.isActive }
        
        for (streak in weightStreaks) {
            incrementStreak(streak, dateTimestamp)
        }
    }
    
    /**
     * Check for broken streaks and send warnings
     */
    suspend fun checkStreakWarnings() {
        val activeStreaks = repository.activeStreaksFlow().first()
        val today = LocalDate.now()
        
        for (streak in activeStreaks) {
            if (streak.lastActivityTimestamp != null) {
                // Send warning if streak is about to break
                val hoursSinceActivity = ChronoUnit.HOURS.between(
                    Instant.ofEpochSecond(streak.lastActivityTimestamp),
                    Instant.now()
                )

                if (hoursSinceActivity >= 24 && streak.currentStreak > 0) {
                    SmartNotificationManager.showStreakWarning(context, streak)
                }

                // Break streak if more than grace period
                if (hoursSinceActivity > 24 + GRACE_PERIOD_HOURS) {
                    breakStreak(streak)
                }
            }
        }
    }
    
    /**
     * Get streak statistics for display
     */
    suspend fun getStreakStatistics(): StreakStatistics {
        val allStreaks = repository.allStreaksFlow().first()
        val activeStreaks = allStreaks.filter { it.isActive }
        
        return StreakStatistics(
            totalActiveStreaks = activeStreaks.size,
            longestCurrentStreak = activeStreaks.maxOfOrNull { it.currentStreak } ?: 0,
            longestEverStreak = allStreaks.maxOfOrNull { it.longestStreak } ?: 0,
            totalStreakDays = allStreaks.sumOf { it.longestStreak }
        )
    }
    
    private suspend fun updateWorkoutStreak(streak: PersonalStreakEntity, todayTimestamp: Long) {
        val todayIso = todayTimestamp.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayWorkout = repository.getTodayWorkout(todayIso)
        
        if (todayWorkout?.status == "completed") {
            incrementStreak(streak, todayTimestamp)
        } else {
            checkForStreakBreak(streak, todayTimestamp)
        }
    }
    
    private suspend fun updateNutritionStreak(streak: PersonalStreakEntity, todayTimestamp: Long) {
        val todayIntakeTotal = repository.getTotalIntakeForDay(todayTimestamp)
        
        // Consider nutrition logged if any intake was recorded
        if (todayIntakeTotal > 0) {
            incrementStreak(streak, todayTimestamp)
        } else {
            checkForStreakBreak(streak, todayTimestamp)
        }
    }
    
    private suspend fun updateHabitStreak(streak: PersonalStreakEntity, todayTimestamp: Long) {
        // For habit streaks, check multiple criteria
        val todayIso = todayTimestamp.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayWorkout = repository.getTodayWorkout(todayIso)
        val todayIntakeTotal = repository.getTotalIntakeForDay(todayTimestamp)
        
        // Habit streak continues if either workout OR nutrition is tracked
        if (todayWorkout?.status == "completed" || todayIntakeTotal > 0) {
            incrementStreak(streak, todayTimestamp)
        } else {
            checkForStreakBreak(streak, todayTimestamp)
        }
    }
    
    private suspend fun updateWeightTrackingStreak(streak: PersonalStreakEntity, todayTimestamp: Long) {
        // TODO: Implement weight tracking streak when weight tracking is added
        // For now, just maintain existing streak
        if (streak.lastActivityTimestamp != null) {
            val hoursSinceActivity = ChronoUnit.HOURS.between(
                Instant.ofEpochSecond(streak.lastActivityTimestamp),
                Instant.ofEpochSecond(todayTimestamp)
            )

            if (hoursSinceActivity > 24 + GRACE_PERIOD_HOURS) {
                breakStreak(streak)
            }
        }
    }
    
    private suspend fun incrementStreak(streak: PersonalStreakEntity, timestamp: Long) {
        val lastActivity = streak.lastActivityTimestamp?.toLocalDate()
        val currentDate = timestamp.toLocalDate()
        
        val newCurrentStreak = if (lastActivity == null || lastActivity.isBefore(currentDate)) {
            // Check if this is consecutive
            val daysBetween = lastActivity?.let { ChronoUnit.DAYS.between(it, currentDate) } ?: 1
            
            if (daysBetween == 1L || lastActivity == null) {
                // Consecutive day
                streak.currentStreak + 1
            } else if (daysBetween == 0L) {
                // Same day, don't increment
                streak.currentStreak
            } else {
                // Gap in streak, restart
                1
            }
        } else {
            streak.currentStreak
        }
        
        val newLongestStreak = maxOf(streak.longestStreak, newCurrentStreak)
        
        repository.updateStreakCounts(
            streak.id,
            newCurrentStreak,
            newLongestStreak,
            timestamp
        )
        
        // Check for milestone achievements
        checkStreakMilestones(streak, newCurrentStreak)
    }
    
    private suspend fun checkForStreakBreak(streak: PersonalStreakEntity, todayTimestamp: Long) {
        if (streak.lastActivityTimestamp != null) {
            val hoursSinceActivity = ChronoUnit.HOURS.between(
                Instant.ofEpochSecond(streak.lastActivityTimestamp),
                Instant.ofEpochSecond(todayTimestamp)
            )

            if (hoursSinceActivity > 24 + GRACE_PERIOD_HOURS) {
                breakStreak(streak)
            }
        }
    }
    
    private suspend fun breakStreak(streak: PersonalStreakEntity) {
        repository.updateStreakCounts(
            streak.id,
            0, // Reset current streak
            streak.longestStreak, // Keep longest streak
            null // Clear last activity date
        )
    }
    
    private suspend fun checkStreakMilestones(streak: PersonalStreakEntity, currentStreak: Int) {
        val milestones = listOf(7, 14, 30, 50, 100)
        
        if (milestones.contains(currentStreak)) {
            SmartNotificationManager.showStreakMilestone(context, streak, currentStreak)
        }
    }
    
    private fun createDefaultStreaks(): List<PersonalStreakEntity> {
        return listOf(
            PersonalStreakEntity(
                name = "Tägliches Training",
                description = "Trainiere jeden Tag",
                category = CATEGORY_WORKOUT,
                targetDays = 30
            ),
            PersonalStreakEntity(
                name = "Ernährungs-Tracking",
                description = "Protokolliere täglich deine Mahlzeiten",
                category = CATEGORY_NUTRITION,
                targetDays = 30
            ),
            PersonalStreakEntity(
                name = "Fitness-Gewohnheit",
                description = "Nutze die App täglich für Training oder Ernährung",
                category = CATEGORY_HABIT,
                targetDays = 30
            ),
            PersonalStreakEntity(
                name = "Gewichtskontrolle",
                description = "Wiege dich regelmäßig",
                category = CATEGORY_WEIGHT,
                targetDays = 7
            )
        )
    }
    
    data class StreakStatistics(
        val totalActiveStreaks: Int,
        val longestCurrentStreak: Int,
        val longestEverStreak: Int,
        val totalStreakDays: Int
    )
}