package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.max
import kotlin.math.min

/**
 * Smart Rest Timer with AI-powered adaptive rest calculation
 * Integrates with existing workout execution system
 */
class SmartRestTimer(private val context: Context) {
    companion object {
        private const val TAG = "SmartRestTimer"
        private const val DEFAULT_REST_SECONDS = 90
        private const val MIN_REST_SECONDS = 30
        private const val MAX_REST_SECONDS = 300
    }

    private val audioPlayer = RestTimerAudioPlayer(context)

    private var timerJob: Job? = null
    private val _timerState = MutableStateFlow<RestTimerState>(RestTimerState.IDLE)
    val timerState: StateFlow<RestTimerState> = _timerState.asStateFlow()

    private val _restSuggestions = MutableStateFlow<RestSuggestion?>(null)
    val restSuggestions: StateFlow<RestSuggestion?> = _restSuggestions.asStateFlow()

    /**
     * Initialize audio system for coaching
     */
    suspend fun initializeAudio(): Boolean {
        return audioPlayer.initialize()
    }

    /**
     * Start adaptive rest timer based on exercise intensity and user metrics
     */
    suspend fun startAdaptiveRest(
        exerciseId: String,
        intensity: Float, // 0.0 to 1.0
        heartRate: Int? = null,
        perceivedExertion: Int? = null, // RPE 1-10
        previousSetData: SetData? = null,
    ) {
        val restSeconds =
            calculateAdaptiveRestTime(
                intensity = intensity,
                heartRate = heartRate,
                perceivedExertion = perceivedExertion,
                previousSetData = previousSetData,
            )

        val suggestion = generateRestSuggestion(exerciseId, restSeconds, intensity)
        _restSuggestions.value = suggestion

        startTimer(restSeconds, suggestion)

        StructuredLogger.info(
            StructuredLogger.LogCategory.USER_ACTION,
            TAG,
            "Started adaptive rest timer: ${restSeconds}s for $exerciseId",
        )
    }

    /**
     * Start timer with audio coaching and motivational countdown
     */
    private suspend fun startTimer(
        seconds: Int,
        suggestion: RestSuggestion,
    ) {
        timerJob?.cancel()

        timerJob =
            CoroutineScope(Dispatchers.Main).launch {
                _timerState.value = RestTimerState.RUNNING(seconds, seconds)

                for (remaining in seconds downTo 0) {
                    _timerState.value = RestTimerState.RUNNING(remaining, seconds)

                    // Audio coaching at specific intervals
                    when (remaining) {
                        seconds -> playAudioCue("rest_started")
                        30 -> if (seconds > 30) playAudioCue("thirty_seconds_left")
                        10 -> playAudioCue("ten_seconds_left")
                        3, 2, 1 -> playAudioCue("countdown_$remaining")
                        0 -> {
                            playAudioCue("rest_complete")
                            _timerState.value = RestTimerState.COMPLETED(suggestion.nextSetRecommendation)
                        }
                    }

                    if (remaining > 0) delay(1000)
                }
            }
    }

    /**
     * Calculate adaptive rest time based on multiple factors
     */
    private fun calculateAdaptiveRestTime(
        intensity: Float,
        heartRate: Int?,
        perceivedExertion: Int?,
        previousSetData: SetData?,
    ): Int {
        var baseRest = DEFAULT_REST_SECONDS

        // Adjust based on intensity
        baseRest = (baseRest * (0.5f + intensity)).toInt()

        // Adjust based on heart rate if available
        heartRate?.let { hr ->
            val hrFactor =
                when {
                    hr > 160 -> 1.3f // High HR needs more rest
                    hr > 140 -> 1.1f
                    hr > 120 -> 1.0f
                    else -> 0.9f // Lower HR needs less rest
                }
            baseRest = (baseRest * hrFactor).toInt()
        }

        // Adjust based on perceived exertion (RPE)
        perceivedExertion?.let { rpe ->
            val rpeFactor =
                when {
                    rpe >= 9 -> 1.4f // Very hard
                    rpe >= 7 -> 1.2f // Hard
                    rpe >= 5 -> 1.0f // Moderate
                    else -> 0.8f // Easy
                }
            baseRest = (baseRest * rpeFactor).toInt()
        }

        // Adjust based on previous set performance
        previousSetData?.let { prevSet ->
            if (prevSet.formQuality < 0.7f) {
                baseRest = (baseRest * 1.2f).toInt() // Poor form needs more rest
            }
        }

        return max(MIN_REST_SECONDS, min(MAX_REST_SECONDS, baseRest))
    }

    /**
     * Generate progression suggestions for next set
     */
    private fun generateRestSuggestion(
        exerciseId: String,
        restTime: Int,
        intensity: Float,
    ): RestSuggestion {
        val nextSetRecommendation =
            when {
                intensity > 0.8f ->
                    NextSetRecommendation(
                        suggestion = "Maintain current weight, focus on form",
                        weightAdjustment = 0f,
                        repAdjustment = 0,
                        reason = "High intensity set - prioritize recovery and form",
                    )
                intensity > 0.6f ->
                    NextSetRecommendation(
                        suggestion = "Consider slight weight increase if form was good",
                        weightAdjustment = 1.25f,
                        repAdjustment = 0,
                        reason = "Good intensity - room for progression",
                    )
                else ->
                    NextSetRecommendation(
                        suggestion = "Increase weight or reps for next set",
                        weightAdjustment = 2.5f,
                        repAdjustment = 1,
                        reason = "Low intensity - push harder for better stimulus",
                    )
            }

        return RestSuggestion(
            exerciseId = exerciseId,
            restTime = restTime,
            motivationalMessage = getMotivationalMessage(restTime),
            nextSetRecommendation = nextSetRecommendation,
            formTips = getFormTips(exerciseId),
        )
    }

    /**
     * Get motivational message based on rest time
     */
    private fun getMotivationalMessage(restTime: Int): String {
        return when {
            restTime > 120 -> "Take your time to recover fully. Quality over quantity! 💪"
            restTime > 90 -> "Good rest leads to better performance. Stay focused! 🎯"
            restTime > 60 -> "Almost ready! Prepare mentally for the next set. 🔥"
            else -> "Quick recovery! You're getting stronger with each set! ⚡"
        }
    }

    /**
     * Get exercise-specific form tips
     */
    private fun getFormTips(exerciseId: String): List<String> {
        return when (exerciseId.lowercase()) {
            "squats", "squat" ->
                listOf(
                    "Keep chest up and core engaged",
                    "Descend until thighs are parallel",
                    "Drive through heels on the way up",
                )
            "bench_press", "bench" ->
                listOf(
                    "Maintain tight shoulder blades",
                    "Control the bar down to chest",
                    "Drive feet into ground for stability",
                )
            "deadlift" ->
                listOf(
                    "Keep bar close to body",
                    "Maintain neutral spine",
                    "Hinge at hips, not knees",
                )
            else ->
                listOf(
                    "Focus on controlled movement",
                    "Maintain proper breathing",
                    "Quality over quantity",
                )
        }
    }

    /**
     * Play audio coaching cues
     */
    private fun playAudioCue(cueType: String) {
        try {
            audioPlayer.playAudioCue(cueType)
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.USER_ACTION,
                TAG,
                "Failed to play audio cue: $cueType",
                exception = e,
            )
        }
    }

    /**
     * Play coaching message during rest
     */
    fun playCoachingMessage(message: String) {
        audioPlayer.playCoachingMessage(message)
    }

    /**
     * Configure audio settings
     */
    fun configureAudio(
        speechRate: Float = 1.0f,
        pitch: Float = 1.0f,
    ) {
        audioPlayer.setSpeechRate(speechRate)
        audioPlayer.setPitch(pitch)
    }

    /**
     * Pause the timer
     */
    fun pauseTimer() {
        timerJob?.cancel()
        val currentState = _timerState.value
        if (currentState is RestTimerState.RUNNING) {
            _timerState.value = RestTimerState.PAUSED(currentState.remaining, currentState.total)
        }
    }

    /**
     * Resume the timer
     */
    suspend fun resumeTimer() {
        val currentState = _timerState.value
        if (currentState is RestTimerState.PAUSED) {
            startTimer(currentState.remaining, _restSuggestions.value ?: RestSuggestion.empty())
        }
    }

    /**
     * Stop the timer
     */
    fun stopTimer() {
        timerJob?.cancel()
        _timerState.value = RestTimerState.IDLE
        _restSuggestions.value = null
    }

    /**
     * Skip remaining rest time
     */
    fun skipRest() {
        timerJob?.cancel()
        audioPlayer.playAudioCue("rest_skipped")
        _timerState.value = RestTimerState.COMPLETED(_restSuggestions.value?.nextSetRecommendation)
    }

    /**
     * Extend rest time by additional seconds
     */
    suspend fun extendRest(additionalSeconds: Int) {
        val currentState = _timerState.value
        if (currentState is RestTimerState.RUNNING) {
            val newTotal = currentState.remaining + additionalSeconds
            audioPlayer.playAudioCue("rest_extended")

            // Continue timer with extended time
            timerJob?.cancel()
            startTimer(currentState.remaining + additionalSeconds, _restSuggestions.value!!)
        }
    }

    /**
     * Release audio resources
     */
    fun release() {
        timerJob?.cancel()
        audioPlayer.release()
    }
}

/**
 * Rest timer state
 */
sealed class RestTimerState {
    object IDLE : RestTimerState()

    data class RUNNING(val remaining: Int, val total: Int) : RestTimerState()

    data class PAUSED(val remaining: Int, val total: Int) : RestTimerState()

    data class COMPLETED(val nextSetRecommendation: NextSetRecommendation?) : RestTimerState()
}

// Simple enum version for compatibility with build requirements
enum class RestTimerStateEnum {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
}

/**
 * Rest suggestion with AI-powered recommendations
 */
data class RestSuggestion(
    val exerciseId: String,
    val restTime: Int,
    val motivationalMessage: String,
    val nextSetRecommendation: NextSetRecommendation,
    val formTips: List<String>,
) {
    companion object {
        fun empty() = RestSuggestion("", 0, "", NextSetRecommendation.empty(), emptyList())
    }
}

/**
 * Next set recommendation
 */
data class NextSetRecommendation(
    val suggestion: String,
    val weightAdjustment: Float, // kg to add/subtract
    val repAdjustment: Int, // reps to add/subtract
    val reason: String,
) {
    companion object {
        fun empty() = NextSetRecommendation("", 0f, 0, "")
    }
}

/**
 * Set performance data
 */
data class SetData(
    val weight: Float,
    val reps: Int,
    val formQuality: Float, // 0.0 to 1.0
    val rpe: Int?, // Rate of Perceived Exertion 1-10
    val heartRate: Int?,
)
