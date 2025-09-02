package com.example.fitapp.ui.fasting

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.max

/**
 * Intervallfasten (Intermittent Fasting) Manager
 * 
 * Supports popular fasting protocols with timer logic, status tracking,
 * and countdown calculations for a complete fasting experience.
 */
class FastingManager(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "fasting_prefs"
        private const val KEY_CURRENT_PROTOCOL = "current_protocol"
        private const val KEY_FAST_START_TIME = "fast_start_time"
        private const val KEY_IS_FASTING = "is_fasting"
        private const val KEY_EATING_WINDOW_START = "eating_window_start"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_LONGEST_STREAK = "longest_streak"
        private const val KEY_LAST_FAST_DATE = "last_fast_date"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _fastingState = MutableStateFlow(getCurrentFastingState())
    val fastingState: StateFlow<FastingState> = _fastingState.asStateFlow()
    
    private val _fastingStats = MutableStateFlow(loadFastingStats())
    val fastingStats: StateFlow<FastingStats> = _fastingStats.asStateFlow()
    
    /**
     * Available fasting protocols with eating/fasting windows
     */
    enum class FastingProtocol(
        val displayName: String,
        val fastingHours: Int,
        val eatingHours: Int,
        val description: String
    ) {
        SIXTEEN_EIGHT("16:8", 16, 8, "16 Stunden fasten, 8 Stunden essen"),
        FOURTEEN_TEN("14:10", 14, 10, "14 Stunden fasten, 10 Stunden essen"),
        EIGHTEEN_SIX("18:6", 18, 6, "18 Stunden fasten, 6 Stunden essen"),
        TWENTY_FOUR_HOUR("24:0", 24, 0, "24 Stunden fasten (1 Tag)"),
        FIVE_TWO("5:2", 0, 0, "5 Tage normal essen, 2 Tage reduzierte Kalorien"),
        SIX_ONE("6:1", 0, 0, "6 Tage normal essen, 1 Tag reduzierte Kalorien");
        
        fun isTimeBasedFasting(): Boolean {
            return this in listOf(SIXTEEN_EIGHT, FOURTEEN_TEN, EIGHTEEN_SIX, TWENTY_FOUR_HOUR)
        }
        
        fun isWeeklyFasting(): Boolean {
            return this in listOf(FIVE_TWO, SIX_ONE)
        }
    }
    
    /**
     * Current fasting state
     */
    data class FastingState(
        val protocol: FastingProtocol = FastingProtocol.SIXTEEN_EIGHT,
        val isFasting: Boolean = false,
        val fastStartTime: LocalDateTime? = null,
        val eatingWindowStart: LocalDateTime? = null,
        val currentPhase: FastingPhase = FastingPhase.NOT_STARTED,
        val timeRemaining: Long = 0L, // in seconds
        val progressPercentage: Float = 0f
    )
    
    /**
     * Fasting phases for timer logic
     */
    enum class FastingPhase(val displayName: String) {
        NOT_STARTED("Nicht gestartet"),
        FASTING("Fastenzeit"),
        EATING_WINDOW("Essenszeit"),
        COMPLETED("Abgeschlossen")
    }
    
    /**
     * Fasting statistics and progress tracking
     */
    data class FastingStats(
        val currentStreak: Int = 0,
        val longestStreak: Int = 0,
        val totalFastingDays: Int = 0,
        val averageFastingHours: Float = 0f,
        val lastFastDate: String? = null
    )
    
    /**
     * Start a fasting session with the selected protocol
     */
    fun startFasting(protocol: FastingProtocol) {
        val now = LocalDateTime.now()
        
        prefs.edit()
            .putString(KEY_CURRENT_PROTOCOL, protocol.name)
            .putLong(KEY_FAST_START_TIME, now.toEpochSecond(ZoneOffset.UTC))
            .putBoolean(KEY_IS_FASTING, true)
            .apply()
        
        updateFastingState()
    }
    
    /**
     * End the current fasting session
     */
    fun endFasting() {
        val currentState = _fastingState.value
        
        if (currentState.isFasting && currentState.fastStartTime != null) {
            updateStats()
        }
        
        prefs.edit()
            .putBoolean(KEY_IS_FASTING, false)
            .remove(KEY_FAST_START_TIME)
            .remove(KEY_EATING_WINDOW_START)
            .apply()
        
        updateFastingState()
    }
    
    /**
     * Update the current fasting state based on time
     */
    fun updateFastingState() {
        val newState = getCurrentFastingState()
        _fastingState.value = newState
    }
    
    /**
     * Switch to eating window (for time-based fasting)
     */
    fun startEatingWindow() {
        val now = LocalDateTime.now()
        
        prefs.edit()
            .putLong(KEY_EATING_WINDOW_START, now.toEpochSecond(ZoneOffset.UTC))
            .apply()
        
        updateFastingState()
    }
    
    /**
     * Get suggested eating window times for a protocol
     */
    fun getSuggestedEatingTimes(protocol: FastingProtocol): Pair<String, String>? {
        return when (protocol) {
            FastingProtocol.SIXTEEN_EIGHT -> "12:00" to "20:00"
            FastingProtocol.FOURTEEN_TEN -> "10:00" to "20:00"
            FastingProtocol.EIGHTEEN_SIX -> "14:00" to "20:00"
            else -> null
        }
    }
    
    /**
     * Calculate fasting progress and time remaining
     */
    private fun getCurrentFastingState(): FastingState {
        val protocolName = prefs.getString(KEY_CURRENT_PROTOCOL, FastingProtocol.SIXTEEN_EIGHT.name)
        val protocol = FastingProtocol.valueOf(protocolName ?: FastingProtocol.SIXTEEN_EIGHT.name)
        val isFasting = prefs.getBoolean(KEY_IS_FASTING, false)
        
        if (!isFasting) {
            return FastingState(protocol = protocol)
        }
        
        val fastStartTime = prefs.getLong(KEY_FAST_START_TIME, 0L)
        val eatingWindowStart = prefs.getLong(KEY_EATING_WINDOW_START, 0L)
        
        if (fastStartTime == 0L) {
            return FastingState(protocol = protocol)
        }
        
        val startTime = LocalDateTime.ofEpochSecond(fastStartTime, 0, ZoneOffset.UTC)
        val now = LocalDateTime.now()
        
        return when {
            protocol.isTimeBasedFasting() -> calculateTimeBasedState(protocol, startTime, eatingWindowStart, now)
            else -> FastingState(protocol = protocol, isFasting = true, fastStartTime = startTime, currentPhase = FastingPhase.FASTING)
        }
    }
    
    private fun calculateTimeBasedState(
        protocol: FastingProtocol, 
        startTime: LocalDateTime, 
        eatingWindowStart: Long,
        now: LocalDateTime
    ): FastingState {
        val fastingDurationHours = protocol.fastingHours
        val eatingDurationHours = protocol.eatingHours
        
        val hoursSinceStart = ChronoUnit.HOURS.between(startTime, now)
        val minutesSinceStart = ChronoUnit.MINUTES.between(startTime, now)
        
        return when {
            // Still in fasting phase
            hoursSinceStart < fastingDurationHours -> {
                val remainingMinutes = (fastingDurationHours * 60) - minutesSinceStart
                val progress = minutesSinceStart.toFloat() / (fastingDurationHours * 60f)
                
                FastingState(
                    protocol = protocol,
                    isFasting = true,
                    fastStartTime = startTime,
                    currentPhase = FastingPhase.FASTING,
                    timeRemaining = remainingMinutes * 60,
                    progressPercentage = max(0f, progress)
                )
            }
            
            // In eating window
            eatingDurationHours > 0 && hoursSinceStart < (fastingDurationHours + eatingDurationHours) -> {
                val eatingStart = if (eatingWindowStart > 0) {
                    LocalDateTime.ofEpochSecond(eatingWindowStart, 0, ZoneOffset.UTC)
                } else startTime.plusHours(fastingDurationHours.toLong())
                
                val eatingMinutes = ChronoUnit.MINUTES.between(eatingStart, now)
                val remainingEatingMinutes = (eatingDurationHours * 60) - eatingMinutes
                
                FastingState(
                    protocol = protocol,
                    isFasting = true,
                    fastStartTime = startTime,
                    eatingWindowStart = eatingStart,
                    currentPhase = FastingPhase.EATING_WINDOW,
                    timeRemaining = max(0L, remainingEatingMinutes * 60),
                    progressPercentage = 1f
                )
            }
            
            // Fasting completed
            else -> {
                FastingState(
                    protocol = protocol,
                    isFasting = false,
                    fastStartTime = startTime,
                    currentPhase = FastingPhase.COMPLETED,
                    timeRemaining = 0L,
                    progressPercentage = 1f
                )
            }
        }
    }
    
    private fun updateStats() {
        val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0) + 1
        val longestStreak = max(currentStreak, prefs.getInt(KEY_LONGEST_STREAK, 0))
        val today = LocalDateTime.now().toLocalDate().toString()
        
        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putInt(KEY_LONGEST_STREAK, longestStreak)
            .putString(KEY_LAST_FAST_DATE, today)
            .apply()
        
        _fastingStats.value = loadFastingStats()
    }
    
    private fun loadFastingStats(): FastingStats {
        return FastingStats(
            currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0),
            longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0),
            lastFastDate = prefs.getString(KEY_LAST_FAST_DATE, null)
        )
    }
    
    /**
     * Format time remaining for display
     */
    fun formatTimeRemaining(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "0m"
        }
    }
}