package com.example.fitapp.domain.usecases

import com.example.fitapp.core.threading.DispatcherProvider
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.WaterEntryEntity
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.util.time.TimeZoneUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class representing current hydration status
 */
data class HydrationStatus(
    val currentDate: LocalDate,
    val goalLiters: Double,
    val consumedMl: Int,
    val progressPercentage: Float,
    val remainingMl: Int,
    val isGoalReached: Boolean
) {
    val goalMl: Int get() = (goalLiters * 1000).toInt()
}

/**
 * Use case for managing hydration goals and tracking progress.
 * Provides reactive flows for immediate UI updates and handles timezone-safe day boundaries.
 * 
 * ## Priority Order for Hydration Goals
 * The hydration goal is determined using the following priority:
 * 1. **DailyGoalEntity.targetWaterMl** (if set and > 0 for the specific date)
 * 2. **UserPreferencesProto.dailyWaterGoalLiters** (converted to ml, if > 0)
 * 3. **Default fallback** (2000ml)
 *
 * ## Reactive Updates
 * This use case provides reactive flows that automatically update when:
 * - Daily goals are added, modified, or removed
 * - User preferences for daily water goal change
 * - Water intake entries are added or modified
 * - The system date changes (for "today" methods)
 *
 * ## Timezone Handling
 * All date-based methods use timezone-safe calculations with `ZoneId.systemDefault()`.
 * This ensures consistent behavior across DST transitions and timezone changes.
 */
@Singleton
class HydrationGoalUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val nutritionRepository: NutritionRepository,
    private val database: AppDatabase,
    private val dispatchers: DispatcherProvider
) {
    
    /**
     * Current hydration goal in liters as a reactive flow
     */
    val hydrationGoal: Flow<Double> = userPreferencesRepository.nutritionPreferences
        .map { it.dailyWaterGoalLiters }
        .distinctUntilChanged()
    
    /**
     * Current hydration status for today as a reactive flow
     */
    val hydrationStatus: Flow<HydrationStatus> = combine(
        hydrationGoal,
        getCurrentDayWaterIntakeFlow()
    ) { goal, consumedMl ->
        val currentDate = TimeZoneUtils.getCurrentLocalDate()
        val goalMl = (goal * 1000).toInt()
        val progress = if (goalMl > 0) (consumedMl.toFloat() / goalMl) * 100f else 0f
        val remaining = maxOf(0, goalMl - consumedMl)
        
        HydrationStatus(
            currentDate = currentDate,
            goalLiters = goal,
            consumedMl = consumedMl,
            progressPercentage = progress,
            remainingMl = remaining,
            isGoalReached = consumedMl >= goalMl
        )
    }.flowOn(dispatchers.default)
    
    /**
     * Gets the hydration goal for a specific date in milliliters.
     * Uses the priority order: DailyGoalEntity -> UserPreferences -> Default
     *
     * @param date The date to get the goal for
     * @return The hydration goal in milliliters
     */
    suspend fun getHydrationGoalMl(date: LocalDate): Int = withContext(dispatchers.default) {
        // First try to get daily goal for the specific date
        val dailyGoal = nutritionRepository.goalFlow(date).first()
        dailyGoal?.targetWaterMl?.let { targetWaterMl ->
            if (targetWaterMl > 0) {
                return@withContext targetWaterMl
            }
        }

        // Fallback to user preferences (convert liters to ml)
        val nutritionPrefs = userPreferencesRepository.nutritionPreferences.first()
        val waterGoalLiters = nutritionPrefs.dailyWaterGoalLiters
        if (waterGoalLiters > 0) {
            return@withContext (waterGoalLiters * 1000).toInt()
        }

        // Final fallback to default
        return@withContext DEFAULT_DAILY_WATER_GOAL_ML
    }

    /**
     * Gets the hydration goal for today in milliliters.
     * Uses timezone-safe current date calculation.
     *
     * @return The hydration goal in milliliters for today
     */
    suspend fun getTodaysHydrationGoalMl(): Int {
        val today = LocalDate.now(ZoneId.systemDefault())
        return getHydrationGoalMl(today)
    }

    /**
     * Gets a reactive flow of the hydration goal for a specific date.
     * Updates when either daily goals or user preferences change.
     *
     * @param date The date to get the goal for
     * @return Flow emitting the hydration goal in milliliters
     */
    fun getHydrationGoalMlFlow(date: LocalDate): Flow<Int> {
        return combine(
            nutritionRepository.goalFlow(date),
            userPreferencesRepository.nutritionPreferences,
        ) { dailyGoal, nutritionPrefs ->
            // Apply the same priority logic as getHydrationGoalMl
            dailyGoal?.targetWaterMl?.let { targetWaterMl ->
                if (targetWaterMl > 0) {
                    return@combine targetWaterMl
                }
            }

            // Fallback to user preferences (convert liters to ml)
            val waterGoalLiters = nutritionPrefs.dailyWaterGoalLiters
            if (waterGoalLiters > 0) {
                return@combine (waterGoalLiters * 1000).toInt()
            }

            // Final fallback to default
            DEFAULT_DAILY_WATER_GOAL_ML
        }.flowOn(dispatchers.default)
    }

    /**
     * Gets a reactive flow of today's hydration goal.
     * Uses timezone-safe current date calculation.
     *
     * @return Flow emitting today's hydration goal in milliliters
     */
    fun getTodaysHydrationGoalMlFlow(): Flow<Int> {
        val today = LocalDate.now(ZoneId.systemDefault())
        return getHydrationGoalMlFlow(today)
    }
    
    /**
     * Gets water intake for current day as a reactive flow
     */
    private fun getCurrentDayWaterIntakeFlow(): Flow<Int> {
        return flow {
            while (true) {
                val currentDate = TimeZoneUtils.getCurrentLocalDate()
                val dateString = TimeZoneUtils.formatDate(currentDate)
                
                // Emit current water intake and then listen for changes
                database.waterEntryDao()
                    .getTotalWaterForDateFlow(dateString)
                    .collect { intake ->
                        emit(intake ?: 0)
                    }
            }
        }.distinctUntilChanged()
            .flowOn(dispatchers.io)
    }
    
    /**
     * Gets water intake for a specific date
     */
    suspend fun getWaterIntakeForDate(date: LocalDate): Int = withContext(dispatchers.io) {
        val dateString = TimeZoneUtils.formatDate(date)
        database.waterEntryDao().getTotalWaterForDate(dateString) ?: 0
    }
    
    /**
     * Gets water intake for a specific date as a flow
     */
    fun getWaterIntakeForDateFlow(date: LocalDate): Flow<Int> {
        val dateString = TimeZoneUtils.formatDate(date)
        return database.waterEntryDao()
            .getTotalWaterForDateFlow(dateString)
            .map { it ?: 0 }
            .flowOn(dispatchers.io)
    }
    
    /**
     * Adds water consumption for today
     */
    suspend fun addWaterConsumption(amountMl: Int) = withContext(dispatchers.io) {
        val currentDate = TimeZoneUtils.getCurrentLocalDate()
        val dateString = TimeZoneUtils.formatDate(currentDate)
        
        val waterEntry = WaterEntryEntity(
            date = dateString,
            amountMl = amountMl,
            timestamp = System.currentTimeMillis() / 1000
        )
        
        database.waterEntryDao().insert(waterEntry)
    }
    
    /**
     * Updates the daily hydration goal in user preferences.
     * This will be the fallback for dates without specific daily goals.
     */
    suspend fun updateHydrationGoal(goalLiters: Double) = withContext(dispatchers.io) {
        userPreferencesRepository.updateNutritionPreferences(
            dailyWaterGoalLiters = goalLiters
        )
    }

    /**
     * Updates the default hydration goal in user preferences (convenience method for ml input).
     * Also triggers a reschedule of water reminders to ensure they use the updated goal.
     *
     * @param goalMl The new default hydration goal in milliliters
     */
    suspend fun updateDefaultHydrationGoalMl(goalMl: Int) {
        val goalLiters = goalMl / 1000.0
        updateHydrationGoal(goalLiters)
    }
    
    /**
     * Gets hydration status for a specific date
     */
    suspend fun getHydrationStatusForDate(date: LocalDate): HydrationStatus = withContext(dispatchers.default) {
        val goalMl = getHydrationGoalMl(date)
        val goal = goalMl / 1000.0
        val consumed = getWaterIntakeForDate(date)
        val progress = if (goalMl > 0) (consumed.toFloat() / goalMl) * 100f else 0f
        val remaining = maxOf(0, goalMl - consumed)
        
        HydrationStatus(
            currentDate = date,
            goalLiters = goal,
            consumedMl = consumed,
            progressPercentage = progress,
            remainingMl = remaining,
            isGoalReached = consumed >= goalMl
        )
    }
    
    /**
     * Clears all water entries for a specific date
     */
    suspend fun clearWaterForDate(date: LocalDate) = withContext(dispatchers.io) {
        val dateString = TimeZoneUtils.formatDate(date)
        database.waterEntryDao().clearForDate(dateString)
    }
    
    /**
     * Gets water entries for a specific date
     */
    suspend fun getWaterEntriesForDate(date: LocalDate) = withContext(dispatchers.io) {
        val dateString = TimeZoneUtils.formatDate(date)
        database.waterEntryDao().getByDate(dateString)
    }
    
    /**
     * Gets water entries for a specific date as a flow
     */
    fun getWaterEntriesForDateFlow(date: LocalDate): Flow<List<WaterEntryEntity>> {
        val dateString = TimeZoneUtils.formatDate(date)
        return database.waterEntryDao()
            .getByDateFlow(dateString)
            .flowOn(dispatchers.io)
    }
    
    /**
     * Deletes a specific water entry
     */
    suspend fun deleteWaterEntry(entryId: Long) = withContext(dispatchers.io) {
        database.waterEntryDao().delete(entryId)
    }
    
    /**
     * Updates a water entry
     */
    suspend fun updateWaterEntry(entry: WaterEntryEntity) = withContext(dispatchers.io) {
        database.waterEntryDao().update(entry)
    }
    
    /**
     * Checks if the day has changed since last check and handles day rollover
     */
    suspend fun handleDayRollover(lastCheckDate: LocalDate?): Boolean = withContext(dispatchers.default) {
        val currentDate = TimeZoneUtils.getCurrentLocalDate()
        
        if (lastCheckDate == null) {
            return@withContext true // First time check
        }
        
        return@withContext TimeZoneUtils.hasDayChanged(lastCheckDate, currentDate)
    }
    
    /**
     * Gets hydration progress for the last N days
     */
    suspend fun getHydrationProgressHistory(days: Int): List<HydrationStatus> = withContext(dispatchers.default) {
        val currentDate = TimeZoneUtils.getCurrentLocalDate()
        val statuses = mutableListOf<HydrationStatus>()
        
        for (i in 0 until days) {
            val date = currentDate.minusDays(i.toLong())
            val status = getHydrationStatusForDate(date)
            statuses.add(status)
        }
        
        return@withContext statuses.reversed() // Return chronological order
    }
    
    /**
     * Calculates average daily consumption over the last N days
     */
    suspend fun getAverageConsumption(days: Int): Double = withContext(dispatchers.default) {
        val history = getHydrationProgressHistory(days)
        if (history.isEmpty()) return@withContext 0.0
        
        return@withContext history.map { it.consumedMl }.average()
    }
    
    /**
     * Gets the percentage of days where the goal was reached in the last N days
     */
    suspend fun getGoalAchievementRate(days: Int): Float = withContext(dispatchers.default) {
        val history = getHydrationProgressHistory(days)
        if (history.isEmpty()) return@withContext 0f
        
        val achievedDays = history.count { it.isGoalReached }
        return@withContext (achievedDays.toFloat() / history.size) * 100f
    }

    companion object {
        /** Default hydration goal in milliliters when no other source is available */
        const val DEFAULT_DAILY_WATER_GOAL_ML = 2000
    }
}
