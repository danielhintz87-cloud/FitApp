package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.util.StructuredLogger
import com.example.fitapp.domain.entities.PlateauDetectionResult
import com.example.fitapp.domain.entities.ProgressionRecommendation
import com.example.fitapp.domain.entities.ProgressionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Core Workout Manager for business logic
 * Handles progressive overload, training plateau detection, and form scoring
 */
class WorkoutManager(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.get(context)
) {
    
    companion object {
        private const val TAG = "WorkoutManager"
        private const val MIN_FORM_SCORE = 0.6f
        private const val PLATEAU_THRESHOLD_WEEKS = 3
        private const val REST_TIME_MULTIPLIER = 1.2f
    }
    
    /**
     * Calculate progressive overload based on performance metrics
     */
    suspend fun calculateProgressiveOverload(
        exerciseId: String,
        currentWeight: Float,
        currentReps: Int,
        currentSets: Int,
        formScore: Float,
        rpeScore: Float
    ): ProgressionRecommendation = withContext(Dispatchers.IO) {
        try {
            // Validate input parameters
            require(currentWeight > 0) { "Weight must be positive" }
            require(currentReps > 0) { "Reps must be positive" }
            require(currentSets > 0) { "Sets must be positive" }
            require(formScore in 0f..1f) { "Form score must be between 0 and 1" }
            require(rpeScore in 1f..10f) { "RPE score must be between 1 and 10" }
            
            val canProgressWeight = formScore >= MIN_FORM_SCORE && rpeScore <= 8.0f
            val canProgressReps = formScore >= MIN_FORM_SCORE && rpeScore <= 7.0f
            
            when {
                canProgressWeight && currentWeight < 100f -> {
                    ProgressionRecommendation.weightIncrease(
                        currentWeight + 2.5f,
                        currentReps,
                        currentSets,
                        "Form gut, Gewicht steigern"
                    )
                }
                canProgressReps && currentReps < 15 -> {
                    ProgressionRecommendation.repIncrease(
                        currentWeight,
                        currentReps + 1,
                        currentSets,
                        "Form gut, Wiederholungen steigern"
                    )
                }
                formScore < MIN_FORM_SCORE -> {
                    ProgressionRecommendation.maintain(
                        currentWeight,
                        currentReps,
                        currentSets,
                        "Fokus auf Bewegungsqualität"
                    )
                }
                else -> {
                    ProgressionRecommendation.deload(
                        currentWeight * 0.9f,
                        currentReps,
                        currentSets,
                        "Deload empfohlen"
                    )
                }
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.WORKOUT,
                TAG,
                "Failed to calculate progressive overload",
                exception = e
            )
            ProgressionRecommendation.maintain(
                currentWeight,
                currentReps,
                currentSets,
                "Fehler bei Berechnung"
            )
        }
    }
    
    /**
     * Log workout sets with proper validation
     */
    suspend fun logWorkoutSet(
        exerciseId: String,
        weight: Float,
        reps: Int,
        rpe: Float,
        formScore: Float
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            require(weight >= 0) { "Weight cannot be negative" }
            require(reps > 0) { "Reps must be positive" }
            require(rpe in 1f..10f) { "RPE must be between 1 and 10" }
            require(formScore in 0f..1f) { "Form score must be between 0 and 1" }
            
            // Log to database (simplified implementation)
            StructuredLogger.info(
                StructuredLogger.LogCategory.WORKOUT,
                TAG,
                "Logged set: Exercise=$exerciseId, Weight=$weight, Reps=$reps, RPE=$rpe, Form=$formScore"
            )
            
            true
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.WORKOUT,
                TAG,
                "Failed to log workout set",
                exception = e
            )
            false
        }
    }
    
    /**
     * Detect training plateau based on performance history
     */
    suspend fun detectTrainingPlateau(
        exerciseId: String,
        weeksToAnalyze: Int = PLATEAU_THRESHOLD_WEEKS
    ): PlateauDetectionResult = withContext(Dispatchers.IO) {
        try {
            // Simplified plateau detection logic
            val hasPlateaued = false // Would analyze actual performance data
            val plateauPercentage = 0.0f
            
            PlateauDetectionResult(
                hasPlateaued = hasPlateaued,
                plateauPercentage = plateauPercentage,
                weeksInPlateau = if (hasPlateaued) weeksToAnalyze else 0,
                recommendation = if (hasPlateaued) "Deload Week empfohlen" else "Weiter wie bisher"
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.WORKOUT,
                TAG,
                "Failed to detect training plateau",
                exception = e
            )
            PlateauDetectionResult(
                hasPlateaued = false,
                plateauPercentage = 0f,
                weeksInPlateau = 0,
                recommendation = "Fehler bei Analyse"
            )
        }
    }
    
    /**
     * Suggest rest periods based on training intensity
     */
    fun suggestRestPeriod(
        rpeScore: Float,
        muscleGroup: String,
        exerciseType: String
    ): RestPeriodRecommendation {
        return try {
            val baseRestTime = when (exerciseType.lowercase()) {
                "compound" -> 180 // 3 minutes
                "isolation" -> 90  // 1.5 minutes
                "cardio" -> 60     // 1 minute
                else -> 120        // 2 minutes default
            }
            
            val intensityMultiplier = when {
                rpeScore >= 9f -> REST_TIME_MULTIPLIER * 1.5f
                rpeScore >= 7f -> REST_TIME_MULTIPLIER
                else -> 1.0f
            }
            
            val recommendedRestTime = (baseRestTime * intensityMultiplier).toInt()
            
            RestPeriodRecommendation(
                recommendedSeconds = recommendedRestTime,
                reason = "Basierend auf RPE $rpeScore und Übungstyp $exerciseType",
                minSeconds = (recommendedRestTime * 0.8f).toInt(),
                maxSeconds = (recommendedRestTime * 1.3f).toInt()
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.WORKOUT,
                TAG,
                "Failed to suggest rest period",
                exception = e
            )
            RestPeriodRecommendation(
                recommendedSeconds = 120,
                reason = "Standard Pausenzeit",
                minSeconds = 90,
                maxSeconds = 180
            )
        }
    }
    
    /**
     * Handle workout completion flow
     */
    suspend fun completeWorkout(
        workoutId: String,
        totalSets: Int,
        totalVolume: Float,
        averageRpe: Float,
        duration: Long
    ): WorkoutCompletionResult = withContext(Dispatchers.IO) {
        try {
            require(totalSets > 0) { "Total sets must be positive" }
            require(totalVolume >= 0) { "Total volume cannot be negative" }
            require(averageRpe in 1f..10f) { "Average RPE must be between 1 and 10" }
            require(duration > 0) { "Duration must be positive" }
            
            // Calculate workout quality metrics
            val workoutQuality = calculateWorkoutQuality(averageRpe, totalSets, duration)
            
            WorkoutCompletionResult(
                workoutId = workoutId,
                completedAt = System.currentTimeMillis(),
                totalSets = totalSets,
                totalVolume = totalVolume,
                averageRpe = averageRpe,
                duration = duration,
                workoutQuality = workoutQuality,
                nextWorkoutRecommendation = generateNextWorkoutRecommendation(workoutQuality, averageRpe)
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.WORKOUT,
                TAG,
                "Failed to complete workout",
                exception = e
            )
            WorkoutCompletionResult(
                workoutId = workoutId,
                completedAt = System.currentTimeMillis(),
                totalSets = 0,
                totalVolume = 0f,
                averageRpe = 5f,
                duration = 0L,
                workoutQuality = 0.5f,
                nextWorkoutRecommendation = "Nächstes Training nach Plan"
            )
        }
    }
    
    /**
     * Validate exercise form scoring
     */
    fun validateExerciseForm(
        movementPattern: MovementPattern,
        jointAngles: List<Float>,
        tempo: Float
    ): FormValidationResult {
        return try {
            // Simplified form validation
            val isFormValid = jointAngles.all { it in movementPattern.validAngleRange }
            val tempoScore = if (tempo in movementPattern.optimalTempo) 1.0f else 0.7f
            val overallScore = if (isFormValid) tempoScore else tempoScore * 0.5f
            
            FormValidationResult(
                isValid = isFormValid,
                overallScore = overallScore,
                feedback = generateFormFeedback(isFormValid, tempoScore),
                corrections = if (!isFormValid) listOf("Winkel korrigieren") else emptyList()
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.WORKOUT,
                TAG,
                "Failed to validate exercise form",
                exception = e
            )
            FormValidationResult(
                isValid = false,
                overallScore = 0.5f,
                feedback = "Fehler bei Form-Analyse",
                corrections = emptyList()
            )
        }
    }
    
    private fun calculateWorkoutQuality(averageRpe: Float, totalSets: Int, duration: Long): Float {
        val rpeScore = (averageRpe - 1f) / 9f // Normalize to 0-1
        val volumeScore = minOf(totalSets / 20f, 1f) // Normalize sets
        val durationScore = when (duration) {
            in 30..90 -> 1f // Optimal 30-90 minutes
            in 20..120 -> 0.8f
            else -> 0.6f
        }
        return (rpeScore + volumeScore + durationScore) / 3f
    }
    
    private fun generateNextWorkoutRecommendation(workoutQuality: Float, averageRpe: Float): String {
        return when {
            workoutQuality >= 0.8f && averageRpe >= 8f -> "Ausgezeichnetes Training! Nächste Steigerung möglich."
            workoutQuality >= 0.6f -> "Gutes Training! Weiter wie bisher."
            averageRpe <= 5f -> "Training zu leicht. Intensität steigern."
            else -> "Training anpassen. Regeneration beachten."
        }
    }
    
    private fun generateFormFeedback(isFormValid: Boolean, tempoScore: Float): String {
        return when {
            isFormValid && tempoScore >= 0.9f -> "Exzellente Form!"
            isFormValid -> "Gute Form, Tempo optimieren"
            tempoScore >= 0.9f -> "Gutes Tempo, Winkel korrigieren"
            else -> "Form und Tempo verbessern"
        }
    }
}

// Data classes for workout management

data class RestPeriodRecommendation(
    val recommendedSeconds: Int,
    val reason: String,
    val minSeconds: Int,
    val maxSeconds: Int
)

data class WorkoutCompletionResult(
    val workoutId: String,
    val completedAt: Long,
    val totalSets: Int,
    val totalVolume: Float,
    val averageRpe: Float,
    val duration: Long,
    val workoutQuality: Float,
    val nextWorkoutRecommendation: String
)

data class FormValidationResult(
    val isValid: Boolean,
    val overallScore: Float,
    val feedback: String,
    val corrections: List<String>
)

data class MovementPattern(
    val name: String,
    val validAngleRange: ClosedFloatingPointRange<Float>,
    val optimalTempo: ClosedFloatingPointRange<Float>
)