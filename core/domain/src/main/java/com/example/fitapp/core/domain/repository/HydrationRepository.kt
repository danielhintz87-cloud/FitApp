package com.example.fitapp.core.domain.repository

import com.example.fitapp.core.domain.model.hydration.HydrationGoal
import com.example.fitapp.core.domain.model.hydration.HydrationPreferences
import com.example.fitapp.core.domain.model.hydration.WaterIntake
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for hydration-related data operations
 */
interface HydrationRepository {
    /**
     * Get hydration goal for a specific date
     */
    suspend fun getHydrationGoal(date: LocalDate): HydrationGoal?
    
    /**
     * Get flow of hydration goal for a specific date
     */
    fun getHydrationGoalFlow(date: LocalDate): Flow<HydrationGoal?>
    
    /**
     * Update hydration goal for a specific date
     */
    suspend fun updateHydrationGoal(goal: HydrationGoal)
    
    /**
     * Add water intake entry
     */
    suspend fun addWaterIntake(intake: WaterIntake)
    
    /**
     * Get total water intake for a specific date
     */
    suspend fun getTotalWaterIntake(date: LocalDate): Int
    
    /**
     * Get flow of total water intake for a specific date
     */
    fun getTotalWaterIntakeFlow(date: LocalDate): Flow<Int>
    
    /**
     * Get all water intake entries for a specific date
     */
    suspend fun getWaterIntakeEntries(date: LocalDate): List<WaterIntake>
    
    /**
     * Delete water intake entry
     */
    suspend fun deleteWaterIntake(intakeId: Long)
    
    /**
     * Get hydration preferences
     */
    fun getHydrationPreferences(): Flow<HydrationPreferences>
    
    /**
     * Update hydration preferences
     */
    suspend fun updateHydrationPreferences(preferences: HydrationPreferences)
}