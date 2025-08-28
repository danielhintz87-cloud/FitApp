package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.PersonalStreakEntity
import com.example.fitapp.data.repo.PersonalMotivationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val activeStreaks = repository.activeStreaksFlow().first()
        
        for (streak in activeStreaks) {
            when (streak.category) {
                CATEGORY_WORKOUT -> updateWorkoutStreak(streak, today)
                CATEGORY_NUTRITION -> updateNutritionStreak(streak, today)
                CATEGORY_HABIT -> updateHabitStreak(streak, today)
                CATEGORY_WEIGHT -> updateWeightTrackingStreak(streak, today)
            }
        }
    }
    
    /**
     * Track workout completion for streaks
     */
    suspend fun trackWorkoutCompletion(date: LocalDate = LocalDate.now()) {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val workoutStreaks = repository.streaksByCategoryFlow(CATEGORY_WORKOUT).first()
            .filter { it.isActive }
        
        for (streak in workoutStreaks) {
            incrementStreak(streak, dateStr)
        }
    }
    
    /**
     * Track nutrition logging for streaks
     */
    suspend fun trackNutritionLogging(date: LocalDate = LocalDate.now()) {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val nutritionStreaks = repository.streaksByCategoryFlow(CATEGORY_NUTRITION).first()
            .filter { it.isActive }
        
        for (streak in nutritionStreaks) {
            incrementStreak(streak, dateStr)
        }
    }
    
    /**
     * Track weight logging for streaks
     */
    suspend fun trackWeightLogging(date: LocalDate = LocalDate.now()) {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val weightStreaks = repository.streaksByCategoryFlow(CATEGORY_WEIGHT).first()
            .filter { it.isActive }
        
        for (streak in weightStreaks) {
            incrementStreak(streak, dateStr)
        }
    }
    
    /**
     * Check for broken streaks and send warnings
     */
    suspend fun checkStreakWarnings() {
        val activeStreaks = repository.activeStreaksFlow().first()
        val today = LocalDate.now()
        
        for (streak in activeStreaks) {
            if (streak.lastActivityDate != null) {
                val lastActivity = LocalDate.parse(streak.lastActivityDate)
                val daysSinceActivity = ChronoUnit.DAYS.between(lastActivity, today)
                
                // Send warning if streak is about to break
                if (daysSinceActivity >= 1 && streak.currentStreak > 0) {
                    SmartNotificationManager.showStreakWarning(context, streak)
                }
                
                // Break streak if more than grace period
                if (daysSinceActivity > 1) {
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
    
    private suspend fun updateWorkoutStreak(streak: PersonalStreakEntity, today: String) {
        val todayWorkout = repository.getTodayWorkout(today)
        
        if (todayWorkout?.status == "completed") {
            incrementStreak(streak, today)
        } else {
            checkForStreakBreak(streak, today)
        }
    }
    
    private suspend fun updateNutritionStreak(streak: PersonalStreakEntity, today: String) {
        val todayEpoch = LocalDate.parse(today).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val todayIntakeTotal = repository.getTotalIntakeForDay(todayEpoch)
        
        // Consider nutrition logged if any intake was recorded
        if (todayIntakeTotal > 0) {
            incrementStreak(streak, today)
        } else {
            checkForStreakBreak(streak, today)
        }
    }
    
    private suspend fun updateHabitStreak(streak: PersonalStreakEntity, today: String) {
        // For habit streaks, check multiple criteria
        val todayWorkout = repository.getTodayWorkout(today)
        val todayEpoch = LocalDate.parse(today).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val todayIntakeTotal = repository.getTotalIntakeForDay(todayEpoch)
        
        // Habit streak continues if either workout OR nutrition is tracked
        if (todayWorkout?.status == "completed" || todayIntakeTotal > 0) {
            incrementStreak(streak, today)
        } else {
            checkForStreakBreak(streak, today)
        }
    }
    
    private suspend fun updateWeightTrackingStreak(streak: PersonalStreakEntity, today: String) {
        // TODO: Implement weight tracking streak when weight tracking is added
        // For now, just maintain existing streak
        if (streak.lastActivityDate != null) {
            val lastActivity = LocalDate.parse(streak.lastActivityDate)
            val daysSinceActivity = ChronoUnit.DAYS.between(lastActivity, LocalDate.parse(today))
            
            if (daysSinceActivity > 1) {
                breakStreak(streak)
            }
        }
    }
    
    private suspend fun incrementStreak(streak: PersonalStreakEntity, date: String) {
        val lastActivity = streak.lastActivityDate?.let { LocalDate.parse(it) }
        val currentDate = LocalDate.parse(date)
        
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
            date
        )
        
        // Check for milestone achievements
        checkStreakMilestones(streak, newCurrentStreak)
    }
    
    private suspend fun checkForStreakBreak(streak: PersonalStreakEntity, today: String) {
        if (streak.lastActivityDate != null) {
            val lastActivity = LocalDate.parse(streak.lastActivityDate)
            val daysSinceActivity = ChronoUnit.DAYS.between(lastActivity, LocalDate.parse(today))
            
            if (daysSinceActivity > 1) {
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