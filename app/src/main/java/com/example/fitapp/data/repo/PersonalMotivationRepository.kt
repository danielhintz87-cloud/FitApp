package com.example.fitapp.data.repo

import com.example.fitapp.data.db.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class PersonalMotivationRepository(private val db: AppDatabase) {
    
    // Achievement methods
    fun allAchievementsFlow(): Flow<List<PersonalAchievementEntity>> = 
        db.personalAchievementDao().allAchievementsFlow()
    
    fun achievementsByCompletionFlow(completed: Boolean): Flow<List<PersonalAchievementEntity>> = 
        db.personalAchievementDao().achievementsByCompletionFlow(completed)
    
    fun achievementsByCategoryFlow(category: String): Flow<List<PersonalAchievementEntity>> = 
        db.personalAchievementDao().achievementsByCategoryFlow(category)
    
    suspend fun insertAchievement(achievement: PersonalAchievementEntity): Long = 
        db.personalAchievementDao().insert(achievement)
    
    suspend fun updateAchievement(achievement: PersonalAchievementEntity) = 
        db.personalAchievementDao().update(achievement)
    
    suspend fun markAchievementCompleted(id: Long, completed: Boolean, completedAt: Long?) = 
        db.personalAchievementDao().markAsCompleted(id, completed, completedAt)
    
    suspend fun updateAchievementProgress(id: Long, value: Double) = 
        db.personalAchievementDao().updateProgress(id, value)
    
    suspend fun getAchievement(id: Long): PersonalAchievementEntity? = 
        db.personalAchievementDao().getAchievement(id)
    
    // Streak methods
    fun allStreaksFlow(): Flow<List<PersonalStreakEntity>> = 
        db.personalStreakDao().allStreaksFlow()
    
    fun activeStreaksFlow(): Flow<List<PersonalStreakEntity>> = 
        db.personalStreakDao().activeStreaksFlow()
    
    fun streaksByCategoryFlow(category: String): Flow<List<PersonalStreakEntity>> = 
        db.personalStreakDao().streaksByCategoryFlow(category)
    
    suspend fun insertStreak(streak: PersonalStreakEntity): Long = 
        db.personalStreakDao().insert(streak)
    
    suspend fun updateStreak(streak: PersonalStreakEntity) = 
        db.personalStreakDao().update(streak)
    
    suspend fun updateStreakCounts(id: Long, currentStreak: Int, longestStreak: Int, lastActivityTimestamp: Long?) = 
        db.personalStreakDao().updateStreak(id, currentStreak, longestStreak, lastActivityTimestamp)
    
    suspend fun setStreakActive(id: Long, active: Boolean) = 
        db.personalStreakDao().setActive(id, active)
    
    suspend fun getStreak(id: Long): PersonalStreakEntity? = 
        db.personalStreakDao().getStreak(id)
    
    // Personal records methods
    fun allRecordsFlow(): Flow<List<PersonalRecordEntity>> = 
        db.personalRecordDao().allRecordsFlow()
    
    fun recordsByExerciseFlow(exerciseName: String): Flow<List<PersonalRecordEntity>> = 
        db.personalRecordDao().recordsByExerciseFlow(exerciseName)
    
    suspend fun insertRecord(record: PersonalRecordEntity): Long = 
        db.personalRecordDao().insert(record)
    
    suspend fun getBestRecord(exerciseName: String, recordType: String): PersonalRecordEntity? = 
        db.personalRecordDao().getBestRecord(exerciseName, recordType)
    
    suspend fun getExerciseNames(): List<String> = 
        db.personalRecordDao().getExerciseNames()
    
    // Progress milestones methods
    fun allMilestonesFlow(): Flow<List<ProgressMilestoneEntity>> = 
        db.progressMilestoneDao().allMilestonesFlow()
    
    fun milestonesByCategoryFlow(category: String): Flow<List<ProgressMilestoneEntity>> = 
        db.progressMilestoneDao().milestonesByCategoryFlow(category)
    
    fun milestonesByCompletionFlow(completed: Boolean): Flow<List<ProgressMilestoneEntity>> = 
        db.progressMilestoneDao().milestonesByCompletionFlow(completed)
    
    suspend fun insertMilestone(milestone: ProgressMilestoneEntity): Long = 
        db.progressMilestoneDao().insert(milestone)
    
    suspend fun updateMilestone(milestone: ProgressMilestoneEntity) = 
        db.progressMilestoneDao().update(milestone)
    
    suspend fun updateMilestoneProgress(id: Long, value: Double, progress: Double) = 
        db.progressMilestoneDao().updateProgress(id, value, progress)
    
    suspend fun markMilestoneCompleted(id: Long, completed: Boolean, completedAt: Long?) = 
        db.progressMilestoneDao().markAsCompleted(id, completed, completedAt)
    
    suspend fun getMilestone(id: Long): ProgressMilestoneEntity? = 
        db.progressMilestoneDao().getMilestone(id)
    
    // Today workout methods for streak tracking
    suspend fun getTodayWorkout(dateIso: String): TodayWorkoutEntity? = 
        db.todayWorkoutDao().getByDate(dateIso)
    
    suspend fun getWorkoutsBetween(fromIso: String, toIso: String): List<TodayWorkoutEntity> = 
        db.todayWorkoutDao().getBetween(fromIso, toIso)
    
    // Intake entries for nutrition streak tracking
    suspend fun getTotalIntakeForDay(epochSec: Long): Int = 
        db.intakeDao().totalForDay(epochSec)
}