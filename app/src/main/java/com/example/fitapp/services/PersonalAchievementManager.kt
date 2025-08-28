package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.PersonalAchievementEntity
import com.example.fitapp.data.repo.PersonalMotivationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PersonalAchievementManager(
    private val context: Context,
    private val repository: PersonalMotivationRepository
) {
    
    companion object {
        // Achievement categories
        const val CATEGORY_FITNESS = "fitness"
        const val CATEGORY_NUTRITION = "nutrition"
        const val CATEGORY_STREAK = "streak"
        const val CATEGORY_MILESTONE = "milestone"
        
        // Achievement types for tracking
        const val TYPE_WORKOUT_COUNT = "workout_count"
        const val TYPE_NUTRITION_LOGGING = "nutrition_logging"
        const val TYPE_STREAK_ACHIEVEMENT = "streak_achievement"
        const val TYPE_WEIGHT_TRACKING = "weight_tracking"
    }
    
    fun getAllAchievements(): Flow<List<PersonalAchievementEntity>> =
        repository.allAchievementsFlow()
    
    fun getCompletedAchievements(): Flow<List<PersonalAchievementEntity>> =
        repository.achievementsByCompletionFlow(true)
    
    fun getPendingAchievements(): Flow<List<PersonalAchievementEntity>> =
        repository.achievementsByCompletionFlow(false)
    
    fun getAchievementsByCategory(category: String): Flow<List<PersonalAchievementEntity>> =
        repository.achievementsByCategoryFlow(category)
    
    /**
     * Initialize default achievements for new users
     */
    suspend fun initializeDefaultAchievements() {
        val existingAchievements = repository.allAchievementsFlow().first()
        if (existingAchievements.isNotEmpty()) return
        
        val defaultAchievements = createDefaultAchievements()
        defaultAchievements.forEach { achievement ->
            repository.insertAchievement(achievement)
        }
    }
    
    /**
     * Auto-unlock achievements based on app usage patterns
     */
    suspend fun checkAndUnlockAchievements() {
        val pendingAchievements = repository.achievementsByCompletionFlow(false).first()
        
        for (achievement in pendingAchievements) {
            when (achievement.title) {
                "Erste Schritte" -> checkFirstStepsAchievement(achievement)
                "Wöchentlicher Krieger" -> checkWeeklyWarriorAchievement(achievement)
                "Nahrungs-Tracker" -> checkNutritionTrackerAchievement(achievement)
                "Ausdauer-Champion" -> checkEnduranceChampionAchievement(achievement)
                "Streak-Meister" -> checkStreakMasterAchievement(achievement)
            }
        }
    }
    
    /**
     * Update progress for numeric achievements
     */
    suspend fun updateAchievementProgress(achievementId: Long, newValue: Double) {
        val achievement = repository.getAchievement(achievementId) ?: return
        
        repository.updateAchievementProgress(achievementId, newValue)
        
        // Check if achievement is now completed
        if (achievement.targetValue != null && newValue >= achievement.targetValue) {
            completeAchievement(achievementId)
        }
    }
    
    /**
     * Mark an achievement as completed
     */
    suspend fun completeAchievement(achievementId: Long) {
        val currentTime = System.currentTimeMillis() / 1000
        repository.markAchievementCompleted(achievementId, true, currentTime)
        
        // Trigger notification
        val achievement = repository.getAchievement(achievementId)
        if (achievement != null) {
            SmartNotificationManager.showAchievementUnlocked(context, achievement)
        }
    }
    
    /**
     * Track workout completion for achievements
     */
    suspend fun trackWorkoutCompletion() {
        // Update workout-related achievements
        updateWorkoutCountAchievements()
    }
    
    /**
     * Track nutrition logging for achievements
     */
    suspend fun trackNutritionLogging() {
        // Update nutrition-related achievements
        updateNutritionAchievements()
    }
    
    private suspend fun updateWorkoutCountAchievements() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val oneWeekAgo = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        val recentWorkouts = repository.getWorkoutsBetween(oneWeekAgo, today)
            .filter { it.status == "completed" }
        
        // Update weekly workout achievement
        val weeklyAchievements = repository.achievementsByCategoryFlow(CATEGORY_FITNESS).first()
            .filter { it.title == "Wöchentlicher Krieger" && !it.isCompleted }
        
        weeklyAchievements.forEach { achievement ->
            updateAchievementProgress(achievement.id, recentWorkouts.size.toDouble())
        }
    }
    
    private suspend fun updateNutritionAchievements() {
        val nutritionAchievements = repository.achievementsByCategoryFlow(CATEGORY_NUTRITION).first()
            .filter { !it.isCompleted }
        
        nutritionAchievements.forEach { achievement ->
            when (achievement.title) {
                "Nahrungs-Tracker" -> {
                    // Count nutrition logging days - simplified version
                    updateAchievementProgress(achievement.id, achievement.currentValue + 1.0)
                }
            }
        }
    }
    
    private suspend fun checkFirstStepsAchievement(achievement: PersonalAchievementEntity) {
        // Complete first workout
        val hasAnyWorkout = repository.getWorkoutsBetween(
            LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE),
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        ).any { it.status == "completed" }
        
        if (hasAnyWorkout && !achievement.isCompleted) {
            completeAchievement(achievement.id)
        }
    }
    
    private suspend fun checkWeeklyWarriorAchievement(achievement: PersonalAchievementEntity) {
        // Already handled in updateWorkoutCountAchievements
    }
    
    private suspend fun checkNutritionTrackerAchievement(achievement: PersonalAchievementEntity) {
        // Already handled in updateNutritionAchievements
    }
    
    private suspend fun checkEnduranceChampionAchievement(achievement: PersonalAchievementEntity) {
        val oneMonthAgo = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        val monthlyWorkouts = repository.getWorkoutsBetween(oneMonthAgo, today)
            .filter { it.status == "completed" }
        
        updateAchievementProgress(achievement.id, monthlyWorkouts.size.toDouble())
    }
    
    private suspend fun checkStreakMasterAchievement(achievement: PersonalAchievementEntity) {
        val activeStreaks = repository.activeStreaksFlow().first()
        val maxStreak = activeStreaks.maxOfOrNull { it.currentStreak } ?: 0
        
        updateAchievementProgress(achievement.id, maxStreak.toDouble())
    }
    
    private fun createDefaultAchievements(): List<PersonalAchievementEntity> {
        return listOf(
            PersonalAchievementEntity(
                title = "Erste Schritte",
                description = "Absolviere dein erstes Training",
                category = CATEGORY_FITNESS,
                iconName = "fitness_center",
                targetValue = 1.0,
                unit = "Training"
            ),
            PersonalAchievementEntity(
                title = "Wöchentlicher Krieger",
                description = "Trainiere 5 Mal in einer Woche",
                category = CATEGORY_FITNESS,
                iconName = "emoji_events",
                targetValue = 5.0,
                unit = "Trainings"
            ),
            PersonalAchievementEntity(
                title = "Nahrungs-Tracker",
                description = "Tracke deine Ernährung für 7 Tage",
                category = CATEGORY_NUTRITION,
                iconName = "restaurant",
                targetValue = 7.0,
                unit = "Tage"
            ),
            PersonalAchievementEntity(
                title = "Ausdauer-Champion", 
                description = "Absolviere 20 Trainings",
                category = CATEGORY_FITNESS,
                iconName = "military_tech",
                targetValue = 20.0,
                unit = "Trainings"
            ),
            PersonalAchievementEntity(
                title = "Streak-Meister",
                description = "Erreiche eine 10-Tage Streak",
                category = CATEGORY_STREAK,
                iconName = "local_fire_department",
                targetValue = 10.0,
                unit = "Tage"
            ),
            PersonalAchievementEntity(
                title = "Gewohnheits-Guru",
                description = "Führe eine Aktivität 30 Tage am Stück durch",
                category = CATEGORY_STREAK,
                iconName = "self_improvement",
                targetValue = 30.0,
                unit = "Tage"
            ),
            PersonalAchievementEntity(
                title = "Fitness-Enthusiast",
                description = "Trainiere 50 Mal insgesamt",
                category = CATEGORY_FITNESS,
                iconName = "sports_gymnastics",
                targetValue = 50.0,
                unit = "Trainings"
            ),
            PersonalAchievementEntity(
                title = "Ernährungs-Experte",
                description = "Protokolliere Mahlzeiten für 30 Tage",
                category = CATEGORY_NUTRITION,
                iconName = "book",
                targetValue = 30.0,
                unit = "Tage"
            )
        )
    }
}