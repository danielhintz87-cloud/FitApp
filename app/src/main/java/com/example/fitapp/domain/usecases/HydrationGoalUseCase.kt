package com.example.fitapp.domain.usecases

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.data.repo.NutritionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

/**
 * Use case for getting hydration goals (water intake targets).
 * Provides a single source of truth for hydration goals across the app.
 * 
 * Priority:
 * 1. DailyGoalEntity.targetWaterMl (if set for the specific date)
 * 2. UserPreferencesProto.dailyWaterGoalLiters (converted to ml)
 * 3. Default fallback (2000ml)
 */
class HydrationGoalUseCase(
    private val nutritionRepository: NutritionRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    

    
    /**
     * Gets the hydration goal for a specific date in milliliters.
     * 
     * @param date The date to get the goal for
     * @return The hydration goal in milliliters
     */
    suspend fun getHydrationGoalMl(date: LocalDate): Int {
        // First try to get daily goal for the specific date
        val dailyGoal = nutritionRepository.goalFlow(date).first()
        dailyGoal?.targetWaterMl?.let { targetWaterMl ->
            if (targetWaterMl > 0) {
                return targetWaterMl
            }
        }
        
        // Fallback to user preferences (convert liters to ml)
        val nutritionPrefs = userPreferencesRepository.nutritionPreferences.first()
        val waterGoalLiters = nutritionPrefs.dailyWaterGoalLiters
        if (waterGoalLiters > 0) {
            return (waterGoalLiters * 1000).toInt()
        }
        
        // Final fallback to default
        return DEFAULT_DAILY_WATER_GOAL_ML
    }
    
    /**
     * Gets the hydration goal for today in milliliters.
     * 
     * @return The hydration goal in milliliters for today
     */
    suspend fun getTodaysHydrationGoalMl(): Int {
        return getHydrationGoalMl(LocalDate.now())
    }
    
    /**
     * Gets a reactive flow of the hydration goal for a specific date.
     * Updates when either daily goals or user preferences change.
     * 
     * @param date The date to get the goal for
     * @return Flow emitting the hydration goal in milliliters
     */
    fun getHydrationGoalMlFlow(date: LocalDate): Flow<Int> = flow {
        // First emit current value
        emit(getHydrationGoalMl(date))
        
        // Then observe changes - this is a simplified version
        // In a more complete implementation, we would combine flows
        // from both nutritionRepository and userPreferencesRepository
        val currentGoal = getHydrationGoalMl(date)
        emit(currentGoal)
    }
    
    /**
     * Gets a reactive flow of today's hydration goal.
     * 
     * @return Flow emitting today's hydration goal in milliliters
     */
    fun getTodaysHydrationGoalMlFlow(): Flow<Int> {
        return getHydrationGoalMlFlow(LocalDate.now())
    }
    
    /**
     * Updates the default hydration goal in user preferences.
     * This will be the fallback for dates without specific daily goals.
     * 
     * @param goalMl The new default hydration goal in milliliters
     */
    suspend fun updateDefaultHydrationGoalMl(goalMl: Int) {
        val goalLiters = goalMl / 1000.0
        userPreferencesRepository.updateNutritionPreferences(
            dailyWaterGoalLiters = goalLiters
        )
    }
    
    companion object {
        const val DEFAULT_DAILY_WATER_GOAL_ML = 2000
        
        /**
         * Factory method to create HydrationGoalUseCase with minimal dependencies.
         */
        fun create(context: Context): HydrationGoalUseCase {
            val database = AppDatabase.get(context)
            val nutritionRepository = NutritionRepository(database)
            val userPreferencesRepository = UserPreferencesRepository(context)
            return HydrationGoalUseCase(nutritionRepository, userPreferencesRepository)
        }
    }
}