package com.example.fitapp.domain.usecases

import com.example.fitapp.core.threading.DispatcherProvider
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.WaterEntryEntity
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.util.time.TimeZoneUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.time.LocalDate
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
 */
@Singleton
class HydrationGoalUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
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
     * Updates the daily hydration goal
     */
    suspend fun updateHydrationGoal(goalLiters: Double) = withContext(dispatchers.io) {
        userPreferencesRepository.updateNutritionPreferences(
            dailyWaterGoalLiters = goalLiters
        )
    }
    
    /**
     * Gets hydration status for a specific date
     */
    suspend fun getHydrationStatusForDate(date: LocalDate): HydrationStatus = withContext(dispatchers.default) {
        val goal = hydrationGoal.first()
        val consumed = getWaterIntakeForDate(date)
        val goalMl = (goal * 1000).toInt()
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
}