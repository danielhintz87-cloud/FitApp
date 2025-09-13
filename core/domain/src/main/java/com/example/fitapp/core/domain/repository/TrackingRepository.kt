package com.example.fitapp.core.domain.repository

import com.example.fitapp.core.domain.model.tracking.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for training/tracking-related data operations
 */
interface TrackingRepository {
    /**
     * Get workout by ID
     */
    suspend fun getWorkout(id: Long): Workout?
    
    /**
     * Get all workouts for a specific date
     */
    suspend fun getWorkoutsForDate(date: LocalDate): List<Workout>
    
    /**
     * Get flow of workouts for a specific date
     */
    fun getWorkoutsForDateFlow(date: LocalDate): Flow<List<Workout>>
    
    /**
     * Save or update workout
     */
    suspend fun saveWorkout(workout: Workout): Long
    
    /**
     * Delete workout
     */
    suspend fun deleteWorkout(id: Long)
    
    /**
     * Update workout status
     */
    suspend fun updateWorkoutStatus(id: Long, status: WorkoutStatus, completedAt: Long? = null)
    
    /**
     * Get HIIT workout by ID
     */
    suspend fun getHIITWorkout(id: Long): HIITWorkout?
    
    /**
     * Get all HIIT workouts
     */
    suspend fun getHIITWorkouts(): List<HIITWorkout>
    
    /**
     * Save HIIT workout
     */
    suspend fun saveHIITWorkout(workout: HIITWorkout): Long
    
    /**
     * Delete HIIT workout
     */
    suspend fun deleteHIITWorkout(id: Long)
    
    /**
     * Get training preferences
     */
    fun getTrainingPreferences(): Flow<TrainingPreferences>
    
    /**
     * Update training preferences
     */
    suspend fun updateTrainingPreferences(preferences: TrainingPreferences)
    
    /**
     * Get workout completion status for date range
     */
    suspend fun getWorkoutCompletionStats(startDate: LocalDate, endDate: LocalDate): List<Pair<LocalDate, Boolean>>
}