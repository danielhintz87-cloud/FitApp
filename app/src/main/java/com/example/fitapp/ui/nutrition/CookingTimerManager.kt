package com.example.fitapp.ui.nutrition

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.CookingTimerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Manages multiple cooking timers with persistence and background operation
 * 
 * Provides functionality for creating, updating, and monitoring cooking timers
 * that can survive app restarts and provide notifications when completed.
 */
class CookingTimerManager(private val context: Context) {
    
    private val database = AppDatabase.get(context)
    private val timerDao = database.cookingTimerDao()
    private val timerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _activeTimers = MutableStateFlow<List<CookingTimerState>>(emptyList())
    val activeTimers: Flow<List<CookingTimerState>> = _activeTimers.asStateFlow()
    
    data class CookingTimerState(
        val id: String,
        val sessionId: String,
        val name: String,
        val duration: Long,
        val remainingTime: Long,
        val isActive: Boolean,
        val stepIndex: Int?,
        val isCompleted: Boolean = false
    )
    
    init {
        // Start monitoring active timers
        timerScope.launch {
            loadActiveTimers()
            startTimerUpdates()
        }
    }
    
    /**
     * Create a new timer for a cooking session
     */
    suspend fun createTimer(
        sessionId: String,
        name: String,
        durationSeconds: Long,
        stepIndex: Int? = null
    ): String {
        val timerId = UUID.randomUUID().toString()
        
        val timer = CookingTimerEntity(
            id = timerId,
            sessionId = sessionId,
            name = name,
            duration = durationSeconds,
            remainingTime = durationSeconds,
            isActive = true,
            stepIndex = stepIndex
        )
        
        timerDao.insert(timer)
        loadActiveTimers()
        
        return timerId
    }
    
    /**
     * Start a timer
     */
    suspend fun startTimer(timerId: String) {
        timerDao.updateActiveStatus(timerId, true)
        loadActiveTimers()
    }
    
    /**
     * Pause a timer
     */
    suspend fun pauseTimer(timerId: String) {
        timerDao.updateActiveStatus(timerId, false)
        loadActiveTimers()
    }
    
    /**
     * Stop and remove a timer
     */
    suspend fun stopTimer(timerId: String) {
        timerDao.delete(timerId)
        loadActiveTimers()
    }
    
    /**
     * Update remaining time for a timer
     */
    suspend fun updateRemainingTime(timerId: String, remainingTime: Long) {
        timerDao.updateRemainingTime(timerId, remainingTime)
        loadActiveTimers()
    }
    
    /**
     * Get active timers for a specific session
     */
    fun getSessionTimers(sessionId: String): Flow<List<CookingTimerEntity>> {
        return timerDao.getBySessionIdFlow(sessionId)
    }
    
    /**
     * Clear all timers for a session
     */
    suspend fun clearSessionTimers(sessionId: String) {
        timerDao.clearSessionTimers(sessionId)
        loadActiveTimers()
    }
    
    /**
     * Load active timers from database
     */
    private suspend fun loadActiveTimers() {
        val timers = timerDao.getActiveTimers()
        val timerStates = timers.map { timer ->
            CookingTimerState(
                id = timer.id,
                sessionId = timer.sessionId,
                name = timer.name,
                duration = timer.duration,
                remainingTime = timer.remainingTime,
                isActive = timer.isActive,
                stepIndex = timer.stepIndex,
                isCompleted = timer.remainingTime <= 0
            )
        }
        _activeTimers.value = timerStates
    }
    
    /**
     * Start background timer updates
     */
    private fun startTimerUpdates() {
        timerScope.launch {
            while (true) {
                delay(1000) // Update every second
                
                val currentTimers = _activeTimers.value
                val updatedTimers = mutableListOf<CookingTimerState>()
                
                for (timer in currentTimers) {
                    if (timer.isActive && timer.remainingTime > 0) {
                        val newRemainingTime = maxOf(0, timer.remainingTime - 1)
                        val updatedTimer = timer.copy(
                            remainingTime = newRemainingTime,
                            isCompleted = newRemainingTime == 0L
                        )
                        
                        // Update database
                        timerDao.updateRemainingTime(timer.id, newRemainingTime)
                        
                        // If timer completed, deactivate it and potentially send notification
                        if (newRemainingTime == 0L) {
                            timerDao.updateActiveStatus(timer.id, false)
                            onTimerCompleted(updatedTimer)
                        }
                        
                        updatedTimers.add(updatedTimer)
                    } else {
                        updatedTimers.add(timer)
                    }
                }
                
                if (updatedTimers != currentTimers) {
                    _activeTimers.value = updatedTimers
                }
            }
        }
    }
    
    /**
     * Handle timer completion
     */
    private fun onTimerCompleted(timer: CookingTimerState) {
        // TODO: Implement notification system
        // For now, we'll just mark as completed
        // In a full implementation, this would trigger notifications
        android.util.Log.d("CookingTimerManager", "Timer completed: ${timer.name}")
    }
    
    /**
     * Format time for display
     */
    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
            else -> String.format("%d:%02d", minutes, secs)
        }
    }
    
    /**
     * Create suggested timers for common cooking steps
     */
    fun getSuggestedTimers(): List<Pair<String, Long>> {
        return listOf(
            "Kochen" to 900, // 15 minutes
            "Braten" to 600, // 10 minutes
            "Backen" to 1800, // 30 minutes
            "Ruhen lassen" to 300, // 5 minutes
            "Aufkochen" to 300, // 5 minutes
            "Köcheln" to 1200, // 20 minutes
            "Marinieren" to 3600 // 60 minutes
        )
    }
}