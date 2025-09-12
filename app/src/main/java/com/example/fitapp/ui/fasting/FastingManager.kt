package com.example.fitapp.ui.fasting

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.fitapp.data.prefs.UserPreferencesProto
import com.example.fitapp.data.prefs.UserPreferencesSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.max

/**
 * Intervallfasten (Intermittent Fasting) Manager
 * 
 * Supports popular fasting protocols with timer logic, status tracking,
 * and countdown calculations using Proto DataStore for persistence.
 */
class FastingManager(private val context: Context) {
    
    // Use the same proto DataStore instance
    private val Context.dataStore: DataStore<UserPreferencesProto> by dataStore(
        fileName = "user_preferences.pb",
        serializer = UserPreferencesSerializer
    )
    
    private val _fastingState = MutableStateFlow(FastingState())
    val fastingState: StateFlow<FastingState> = _fastingState.asStateFlow()
    
    private val _fastingStats = MutableStateFlow(FastingStats())
    val fastingStats: StateFlow<FastingStats> = _fastingStats.asStateFlow()
    
    /**
     * Initialize and load state from DataStore
     */
    suspend fun initialize() {
        val state = getCurrentFastingState()
        _fastingState.value = state
        
        val stats = loadFastingStats()
        _fastingStats.value = stats
    }
    
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
    suspend fun startFasting(protocol: FastingProtocol) {
        val now = LocalDateTime.now()
        
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setFastingEnabled(true)
                .setFastingStartTimeMillis(now.toEpochSecond(ZoneOffset.UTC) * 1000)
                .setFastingDurationHours(protocol.fastingHours)
                .setFastingNotificationsEnabled(true)
                .build()
        }
        
        updateFastingState()
    }
    
    /**
     * End the current fasting session
     */
    suspend fun endFasting() {
        val currentState = _fastingState.value
        
        if (currentState.isFasting && currentState.fastStartTime != null) {
            updateStats()
        }
        
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setFastingEnabled(false)
                .setFastingStartTimeMillis(0)
                .setFastingDurationHours(0)
                .build()
        }
        
        updateFastingState()
    }
    
    /**
     * Update the current fasting state based on time
     */
    suspend fun updateFastingState() {
        val newState = getCurrentFastingState()
        _fastingState.value = newState
    }
    
    /**
     * Switch to eating window (for time-based fasting)
     */
    suspend fun startEatingWindow() {
        // For now, this just updates the state without specific eating window tracking
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
    private suspend fun getCurrentFastingState(): FastingState {
        val prefs = context.dataStore.data.first()
        
        if (!prefs.fastingEnabled) {
            return FastingState()
        }
        
        val fastStartTimeMillis = prefs.fastingStartTimeMillis
        val fastingDurationHours = prefs.fastingDurationHours
        
        if (fastStartTimeMillis == 0L || fastingDurationHours == 0) {
            return FastingState()
        }
        
        val startTime = LocalDateTime.ofEpochSecond(fastStartTimeMillis / 1000, 0, ZoneOffset.UTC)
        val now = LocalDateTime.now()
        
        // Determine the protocol based on duration (simple mapping)
        val protocol = when (fastingDurationHours) {
            16 -> FastingProtocol.SIXTEEN_EIGHT
            14 -> FastingProtocol.FOURTEEN_TEN
            18 -> FastingProtocol.EIGHTEEN_SIX
            24 -> FastingProtocol.TWENTY_FOUR_HOUR
            else -> FastingProtocol.SIXTEEN_EIGHT
        }
        
        return calculateTimeBasedState(protocol, startTime, now)
    }
    
    private fun calculateTimeBasedState(
        protocol: FastingProtocol, 
        startTime: LocalDateTime, 
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
                val progress = if (fastingDurationHours > 0) {
                    minutesSinceStart.toFloat() / (fastingDurationHours * 60f)
                } else 0f
                
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
                val eatingStart = startTime.plusHours(fastingDurationHours.toLong())
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
    
    private suspend fun updateStats() {
        // For simplicity, we're not tracking detailed stats in the proto yet
        // This could be extended later with additional proto fields if needed
        _fastingStats.value = loadFastingStats()
    }
    
    private suspend fun loadFastingStats(): FastingStats {
        // Simplified stats loading - could be enhanced with more proto fields
        return FastingStats(
            currentStreak = 0,
            longestStreak = 0,
            lastFastDate = null
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