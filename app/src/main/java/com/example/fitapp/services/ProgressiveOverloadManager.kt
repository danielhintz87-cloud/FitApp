package com.example.fitapp.services

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.ai.AdvancedAICoach
import com.example.fitapp.domain.entities.PlateauDetectionResult
import com.example.fitapp.util.StructuredLogger
import kotlin.math.*

/**
 * Progressive Overload Automation System
 * Automatically adjusts workout parameters for optimal progression
 */
class ProgressiveOverloadManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ProgressiveOverloadManager"
        private const val MIN_PROGRESSION_WEEKS = 2
        private const val MAX_WEIGHT_INCREASE_PERCENT = 0.1f // 10% max increase
        private const val MIN_WEIGHT_INCREASE = 1.25f // kg
    }
    
    private val database = AppDatabase.get(context)
    private val aiCoach = AdvancedAICoach(context)
    
    /**
     * Intelligent load progression based on RPE, form, and historical data
     */
    suspend fun calculateIntelligentProgression(
        userId: String,
        exerciseId: String,
        currentWeight: Float,
        currentReps: Int,
        currentSets: Int,
        formQuality: Float,
        rpe: Int?
    ): ProgressionRecommendation {
        return try {
            val plateauResult = aiCoach.detectTrainingPlateau(userId, exerciseId)
            val historicalData = getHistoricalProgressionData(userId, exerciseId)
            val readinessScore = calculateProgressionReadiness(formQuality, rpe, historicalData)
            
            when {
                plateauResult.isPlateaued -> handlePlateauProgression(plateauResult, currentWeight, currentReps, currentSets)
                readinessScore > 0.8f -> calculateAggressiveProgression(currentWeight, currentReps, currentSets, historicalData)
                readinessScore > 0.6f -> calculateModerateProgression(currentWeight, currentReps, currentSets)
                readinessScore > 0.4f -> calculateConservativeProgression(currentWeight, currentReps, currentSets)
                else -> ProgressionRecommendation.maintain(currentWeight, currentReps, currentSets, "Form oder RPE zu niedrig für Progression")
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.AI,
                TAG,
                "Failed to calculate progression for exercise: $exerciseId",
                exception = e
            )
            ProgressionRecommendation.maintain(currentWeight, currentReps, currentSets, "Fehler bei Progressionsberechnung")
        }
    }
    
    /**
     * Deload week detection based on performance metrics
     */
    suspend fun detectDeloadWeek(
        userId: String,
        exerciseIds: List<String>
    ): DeloadRecommendation {
        var totalFatigueScore = 0f
        var plateauCount = 0
        var performanceDeclineCount = 0
        
        for (exerciseId in exerciseIds) {
            val plateauResult = aiCoach.detectTrainingPlateau(userId, exerciseId)
            val recentPerformance = getRecentPerformanceMetrics(userId, exerciseId)
            
            if (plateauResult.isPlateaued && plateauResult.confidence > 0.7f) {
                plateauCount++
            }
            
            if (recentPerformance.hasPerformanceDecline) {
                performanceDeclineCount++
            }
            
            totalFatigueScore += recentPerformance.fatigueScore
        }
        
        val avgFatigueScore = totalFatigueScore / exerciseIds.size
        val plateauPercentage = plateauCount.toFloat() / exerciseIds.size
        val declinePercentage = performanceDeclineCount.toFloat() / exerciseIds.size
        
        val deloadNeeded = avgFatigueScore > 0.7f || plateauPercentage > 0.5f || declinePercentage > 0.3f
        
        return DeloadRecommendation(
            deloadNeeded = deloadNeeded,
            deloadPercentage = if (deloadNeeded) calculateDeloadPercentage(avgFatigueScore, plateauPercentage) else 0f,
            deloadDuration = if (deloadNeeded) calculateDeloadDuration(avgFatigueScore) else 0,
            reasoning = generateDeloadReasoning(avgFatigueScore, plateauPercentage, declinePercentage),
            specificActions = generateDeloadActions(avgFatigueScore, plateauPercentage)
        )
    }
    
    /**
     * Volume and intensity balancing for optimal recovery
     */
    suspend fun balanceVolumeIntensity(
        userId: String,
        plannedExercises: List<PlannedExercise>,
        currentFatigueLevel: Float,
        recoveryCapacity: Float
    ): BalancedWorkoutPlan {
        val totalPlannedVolume = plannedExercises.sumOf { (it.sets * it.reps * it.weight).toDouble() }.toFloat()
        val maxRecoveryVolume = recoveryCapacity * 100f // Simplified calculation
        
        return if (totalPlannedVolume > maxRecoveryVolume) {
            // Need to reduce volume or intensity
            val volumeReduction = (totalPlannedVolume - maxRecoveryVolume) / totalPlannedVolume
            applyVolumeIntensityReduction(plannedExercises, volumeReduction, currentFatigueLevel)
        } else {
            // Can maintain or even increase
            val potentialIncrease = (maxRecoveryVolume - totalPlannedVolume) / totalPlannedVolume
            if (potentialIncrease > 0.1f && currentFatigueLevel < 0.5f) {
                applyVolumeIntensityIncrease(plannedExercises, potentialIncrease.coerceAtMost(0.2f))
            } else {
                BalancedWorkoutPlan(
                    exercises = plannedExercises,
                    adjustmentMade = false,
                    reasoning = "Optimales Volumen-Intensitäts-Verhältnis"
                )
            }
        }
    }
    
    /**
     * Periodization templates (Linear, Wave, Block)
     */
    fun generatePeriodizationTemplate(
        type: PeriodizationType,
        durationWeeks: Int,
        baseExercises: List<String>,
        userLevel: ExperienceLevel
    ): PeriodizationPlan {
        return when (type) {
            PeriodizationType.LINEAR -> generateLinearPeriodization(durationWeeks, baseExercises, userLevel)
            PeriodizationType.UNDULATING -> generateUndulatingPeriodization(durationWeeks, baseExercises, userLevel)
            PeriodizationType.BLOCK -> generateBlockPeriodization(durationWeeks, baseExercises, userLevel)
            PeriodizationType.CONJUGATE -> generateConjugatePeriodization(durationWeeks, baseExercises, userLevel)
        }
    }
    
    /**
     * Real-time progression monitoring
     */
    fun monitorProgressionRealTime(
        userId: String,
        exerciseId: String
    ): Flow<ProgressionUpdate> = flow {
        while (true) {
            val currentMetrics = getCurrentExerciseMetrics(userId, exerciseId)
            val progressionTrend = calculateProgressionTrend(currentMetrics)
            val nextRecommendation = getNextProgressionStep(currentMetrics, progressionTrend)
            
            emit(ProgressionUpdate(
                exerciseId = exerciseId,
                currentMetrics = currentMetrics,
                trend = progressionTrend,
                nextRecommendation = nextRecommendation,
                confidence = calculateProgressionConfidence(currentMetrics)
            ))
            
            delay(10000) // Update every 10 seconds during workout
        }
    }
    
    // Helper methods
    private suspend fun getHistoricalProgressionData(userId: String, exerciseId: String): HistoricalProgressionData {
        // In real implementation, query database for historical data
        return HistoricalProgressionData(
            averageWeeklyProgression = 2.5f, // kg per week
            consistencyScore = 0.85f,
            plateauHistory = listOf(),
            bestProgressionPeriod = 4 // weeks
        )
    }
    
    private fun calculateProgressionReadiness(
        formQuality: Float,
        rpe: Int?,
        historicalData: HistoricalProgressionData
    ): Float {
        val formScore = formQuality
        val rpeScore = rpe?.let { (10 - it) / 10f } ?: 0.5f // Higher RPE = lower readiness
        val consistencyScore = historicalData.consistencyScore
        
        return (formScore * 0.4f + rpeScore * 0.4f + consistencyScore * 0.2f).coerceIn(0f, 1f)
    }
    
    private fun handlePlateauProgression(
        plateauResult: PlateauDetectionResult,
        currentWeight: Float,
        currentReps: Int,
        currentSets: Int
    ): ProgressionRecommendation {
        return when {
            plateauResult.confidence > 0.8f -> {
                // Major plateau - significant change needed
                ProgressionRecommendation(
                    newWeight = currentWeight * 0.9f, // Deload 10%
                    newReps = currentReps + 2, // Increase reps
                    newSets = currentSets,
                    progressionType = ProgressionType.DELOAD_VOLUME,
                    reasoning = "Starkes Plateau erkannt - Deload mit Volumenerhöhung",
                    confidence = plateauResult.confidence,
                    nextEvaluationWeeks = 2
                )
            }
            plateauResult.confidence > 0.6f -> {
                // Moderate plateau - variation needed
                ProgressionRecommendation(
                    newWeight = currentWeight,
                    newReps = currentReps,
                    newSets = currentSets + 1, // Add a set
                    progressionType = ProgressionType.VOLUME_INCREASE,
                    reasoning = "Moderates Plateau - zusätzlicher Satz für neuen Stimulus",
                    confidence = plateauResult.confidence,
                    nextEvaluationWeeks = 2
                )
            }
            else -> {
                // Mild plateau - small weight increase
                ProgressionRecommendation(
                    newWeight = currentWeight + MIN_WEIGHT_INCREASE,
                    newReps = currentReps,
                    newSets = currentSets,
                    progressionType = ProgressionType.WEIGHT_INCREASE,
                    reasoning = "Leichtes Plateau - kleine Gewichtssteigerung",
                    confidence = plateauResult.confidence,
                    nextEvaluationWeeks = 1
                )
            }
        }
    }
    
    private fun calculateAggressiveProgression(
        currentWeight: Float,
        currentReps: Int,
        currentSets: Int,
        historicalData: HistoricalProgressionData
    ): ProgressionRecommendation {
        val weightIncrease = maxOf(MIN_WEIGHT_INCREASE, currentWeight * 0.05f) // 5% or minimum
        
        return ProgressionRecommendation(
            newWeight = currentWeight + weightIncrease,
            newReps = currentReps,
            newSets = currentSets,
            progressionType = ProgressionType.WEIGHT_INCREASE,
            reasoning = "Ausgezeichnete Form und niedrige RPE - aggressive Progression möglich",
            confidence = 0.9f,
            nextEvaluationWeeks = 1
        )
    }
    
    private fun calculateModerateProgression(
        currentWeight: Float,
        currentReps: Int,
        currentSets: Int
    ): ProgressionRecommendation {
        return ProgressionRecommendation(
            newWeight = currentWeight + MIN_WEIGHT_INCREASE,
            newReps = currentReps,
            newSets = currentSets,
            progressionType = ProgressionType.WEIGHT_INCREASE,
            reasoning = "Moderate Progression basierend auf aktueller Performance",
            confidence = 0.75f,
            nextEvaluationWeeks = 1
        )
    }
    
    private fun calculateConservativeProgression(
        currentWeight: Float,
        currentReps: Int,
        currentSets: Int
    ): ProgressionRecommendation {
        return if (currentReps < 12) {
            // Increase reps first
            ProgressionRecommendation(
                newWeight = currentWeight,
                newReps = currentReps + 1,
                newSets = currentSets,
                progressionType = ProgressionType.REP_INCREASE,
                reasoning = "Konservative Progression - erst Wiederholungen erhöhen",
                confidence = 0.6f,
                nextEvaluationWeeks = 2
            )
        } else {
            // Small weight increase
            ProgressionRecommendation(
                newWeight = currentWeight + (MIN_WEIGHT_INCREASE * 0.5f),
                newReps = currentReps - 1, // Reduce reps when increasing weight
                newSets = currentSets,
                progressionType = ProgressionType.WEIGHT_INCREASE,
                reasoning = "Konservative Gewichtssteigerung mit Wiederholungsreduktion",
                confidence = 0.6f,
                nextEvaluationWeeks = 2
            )
        }
    }
    
    private suspend fun getRecentPerformanceMetrics(userId: String, exerciseId: String): PerformanceMetrics {
        // Simplified performance metrics calculation
        return PerformanceMetrics(
            hasPerformanceDecline = false, // Would calculate from real data
            fatigueScore = 0.4f + (Math.random() * 0.4f).toFloat(),
            formQualityTrend = 0.1f, // Slightly improving
            volumeTrend = 0.05f // Slightly increasing
        )
    }
    
    private fun calculateDeloadPercentage(fatigueScore: Float, plateauPercentage: Float): Float {
        return when {
            fatigueScore > 0.8f || plateauPercentage > 0.7f -> 0.25f // 25% deload
            fatigueScore > 0.7f || plateauPercentage > 0.5f -> 0.20f // 20% deload
            else -> 0.15f // 15% deload
        }
    }
    
    private fun calculateDeloadDuration(fatigueScore: Float): Int {
        return when {
            fatigueScore > 0.8f -> 2 // 2 weeks
            fatigueScore > 0.7f -> 1 // 1 week
            else -> 1 // 1 week
        }
    }
    
    private fun generateDeloadReasoning(
        fatigueScore: Float,
        plateauPercentage: Float,
        declinePercentage: Float
    ): String {
        return when {
            fatigueScore > 0.7f -> "Hohe Ermüdungsanzeichen erkannt"
            plateauPercentage > 0.5f -> "Mehrere Übungen zeigen Plateaus"
            declinePercentage > 0.3f -> "Leistungsabfall in mehreren Bereichen"
            else -> "Präventive Deload-Woche für optimale Erholung"
        }
    }
    
    private fun generateDeloadActions(fatigueScore: Float, plateauPercentage: Float): List<String> {
        return listOfNotNull(
            "Gewichte um 15-25% reduzieren",
            "Volumen um 20-30% verringern",
            if (fatigueScore > 0.7f) "Zusätzlichen Ruhetag einlegen" else null,
            if (plateauPercentage > 0.5f) "Übungsvariationen nach Deload einführen" else null,
            "Fokus auf Bewegungsqualität und Mobilität"
        )
    }
    
    private fun applyVolumeIntensityReduction(
        exercises: List<PlannedExercise>,
        reductionFactor: Float,
        fatigueLevel: Float
    ): BalancedWorkoutPlan {
        val adjustedExercises = exercises.map { exercise ->
            if (fatigueLevel > 0.7f) {
                // Reduce both weight and volume
                exercise.copy(
                    weight = exercise.weight * (1f - reductionFactor * 0.5f),
                    sets = maxOf(1, exercise.sets - 1)
                )
            } else {
                // Reduce primarily volume
                exercise.copy(
                    sets = maxOf(1, (exercise.sets * (1f - reductionFactor)).toInt())
                )
            }
        }
        
        return BalancedWorkoutPlan(
            exercises = adjustedExercises,
            adjustmentMade = true,
            reasoning = "Volumen/Intensität reduziert für optimale Erholung"
        )
    }
    
    private fun applyVolumeIntensityIncrease(
        exercises: List<PlannedExercise>,
        increaseFactor: Float
    ): BalancedWorkoutPlan {
        val adjustedExercises = exercises.map { exercise ->
            exercise.copy(
                weight = exercise.weight * (1f + increaseFactor * 0.3f), // Modest weight increase
                reps = exercise.reps + 1 // Add one rep
            )
        }
        
        return BalancedWorkoutPlan(
            exercises = adjustedExercises,
            adjustmentMade = true,
            reasoning = "Volumen/Intensität erhöht basierend auf guter Erholung"
        )
    }
    
    private fun generateLinearPeriodization(
        weeks: Int,
        exercises: List<String>,
        level: ExperienceLevel
    ): PeriodizationPlan {
        val phases = mutableListOf<PeriodizationPhase>()
        
        // Hypertrophy phase (first 40% of time)
        phases.add(PeriodizationPhase(
            name = "Hypertrophy",
            durationWeeks = (weeks * 0.4).toInt(),
            intensityRange = 0.65f..0.75f,
            repRange = 8..12,
            setRange = 3..4,
            focus = "Muskelaufbau und Volumen"
        ))
        
        // Strength phase (next 40% of time)
        phases.add(PeriodizationPhase(
            name = "Strength",
            durationWeeks = (weeks * 0.4).toInt(),
            intensityRange = 0.80f..0.90f,
            repRange = 3..6,
            setRange = 3..5,
            focus = "Maximalkraft und neurale Adaptation"
        ))
        
        // Peak/Test phase (final 20% of time)
        phases.add(PeriodizationPhase(
            name = "Peak",
            durationWeeks = (weeks * 0.2).toInt(),
            intensityRange = 0.90f..1.0f,
            repRange = 1..3,
            setRange = 3..6,
            focus = "Maximalkraft-Test und Technik"
        ))
        
        return PeriodizationPlan(
            type = PeriodizationType.LINEAR,
            totalWeeks = weeks,
            phases = phases,
            exercises = exercises,
            description = "Lineare Periodisierung von Hypertrophy zu Maximalkraft"
        )
    }
    
    private fun generateUndulatingPeriodization(
        weeks: Int,
        exercises: List<String>,
        level: ExperienceLevel
    ): PeriodizationPlan {
        val phases = mutableListOf<PeriodizationPhase>()
        
        // Daily undulating pattern repeated
        val basePattern = listOf(
            PeriodizationPhase("Heavy", 1, 0.80f..0.90f, 3..6, 3..5, "Kraft"),
            PeriodizationPhase("Light", 1, 0.60f..0.70f, 12..15, 2..3, "Erholung"),
            PeriodizationPhase("Medium", 1, 0.70f..0.80f, 8..10, 3..4, "Hypertrophy")
        )
        
        repeat(weeks / 3) { phases.addAll(basePattern) }
        
        return PeriodizationPlan(
            type = PeriodizationType.UNDULATING,
            totalWeeks = weeks,
            phases = phases,
            exercises = exercises,
            description = "Wellenförmige Periodisierung mit täglicher Variation"
        )
    }
    
    private fun generateBlockPeriodization(
        weeks: Int,
        exercises: List<String>,
        level: ExperienceLevel
    ): PeriodizationPlan {
        val phases = mutableListOf<PeriodizationPhase>()
        val blockLength = 4 // 4-week blocks
        
        val blocks = listOf(
            PeriodizationPhase("Accumulation", blockLength, 0.60f..0.75f, 10..15, 3..5, "Volumen und Kapazität"),
            PeriodizationPhase("Intensification", blockLength, 0.75f..0.90f, 4..8, 3..4, "Intensität und Kraft"),
            PeriodizationPhase("Realization", blockLength, 0.85f..1.0f, 1..4, 2..4, "Peak und Technik")
        )
        
        repeat(weeks / (blockLength * 3)) { phases.addAll(blocks) }
        
        return PeriodizationPlan(
            type = PeriodizationType.BLOCK,
            totalWeeks = weeks,
            phases = phases,
            exercises = exercises,
            description = "Block-Periodisierung mit spezifischen Adaptationsphasen"
        )
    }
    
    private fun generateConjugatePeriodization(
        weeks: Int,
        exercises: List<String>,
        level: ExperienceLevel
    ): PeriodizationPlan {
        val phases = listOf(
            PeriodizationPhase(
                name = "Conjugate Method",
                durationWeeks = weeks,
                intensityRange = 0.50f..1.0f,
                repRange = 1..15,
                setRange = 3..8,
                focus = "Gleichzeitige Entwicklung aller Kraftqualitäten"
            )
        )
        
        return PeriodizationPlan(
            type = PeriodizationType.CONJUGATE,
            totalWeeks = weeks,
            phases = phases,
            exercises = exercises,
            description = "Konjugat-Methode mit simultaner Kraft- und Geschwindigkeitsentwicklung"
        )
    }
    
    private fun getCurrentExerciseMetrics(userId: String, exerciseId: String): ExerciseMetrics {
        // Simplified current metrics
        return ExerciseMetrics(
            currentWeight = 50f + (Math.random() * 50).toFloat(),
            currentReps = 8 + (Math.random() * 4).toInt(),
            currentSets = 3,
            formQuality = 0.7f + (Math.random() * 0.3).toFloat(),
            rpe = 6 + (Math.random() * 3).toInt(),
            volume = 1200f + (Math.random() * 400).toFloat()
        )
    }
    
    private fun calculateProgressionTrend(metrics: ExerciseMetrics): ProgressionTrend {
        // Simplified trend calculation
        return ProgressionTrend(
            direction = if (Math.random() > 0.3) TrendDirection.IMPROVING else TrendDirection.STABLE,
            strength = 0.6f + (Math.random() * 0.4).toFloat(),
            velocity = 0.05f + (Math.random() * 0.1).toFloat() // kg per week
        )
    }
    
    private fun getNextProgressionStep(metrics: ExerciseMetrics, trend: ProgressionTrend): String {
        return when (trend.direction) {
            TrendDirection.IMPROVING -> "Gewicht um ${MIN_WEIGHT_INCREASE}kg erhöhen"
            TrendDirection.STABLE -> "Wiederholungen um 1 erhöhen"
            TrendDirection.DECLINING -> "Form fokussieren, Gewicht beibehalten"
        }
    }
    
    private fun calculateProgressionConfidence(metrics: ExerciseMetrics): Float {
        return (metrics.formQuality + (10 - metrics.rpe) / 10f) * 0.5f
    }
}

// Data classes for progressive overload
data class ProgressionRecommendation(
    val newWeight: Float,
    val newReps: Int,
    val newSets: Int,
    val progressionType: ProgressionType,
    val reasoning: String,
    val confidence: Float,
    val nextEvaluationWeeks: Int
) {
    companion object {
        fun maintain(weight: Float, reps: Int, sets: Int, reason: String) = ProgressionRecommendation(
            newWeight = weight,
            newReps = reps,
            newSets = sets,
            progressionType = ProgressionType.MAINTAIN,
            reasoning = reason,
            confidence = 1.0f,
            nextEvaluationWeeks = 1
        )
    }
}

enum class ProgressionType {
    WEIGHT_INCREASE,
    REP_INCREASE,
    SET_INCREASE,
    VOLUME_INCREASE,
    DELOAD_VOLUME,
    MAINTAIN
}

data class DeloadRecommendation(
    val deloadNeeded: Boolean,
    val deloadPercentage: Float,
    val deloadDuration: Int, // weeks
    val reasoning: String,
    val specificActions: List<String>
)

data class BalancedWorkoutPlan(
    val exercises: List<PlannedExercise>,
    val adjustmentMade: Boolean,
    val reasoning: String
)

data class PlannedExercise(
    val name: String,
    val weight: Float,
    val reps: Int,
    val sets: Int
)

data class PeriodizationPlan(
    val type: PeriodizationType,
    val totalWeeks: Int,
    val phases: List<PeriodizationPhase>,
    val exercises: List<String>,
    val description: String
)

data class PeriodizationPhase(
    val name: String,
    val durationWeeks: Int,
    val intensityRange: ClosedFloatingPointRange<Float>,
    val repRange: IntRange,
    val setRange: IntRange,
    val focus: String
)

enum class PeriodizationType {
    LINEAR,
    UNDULATING,
    BLOCK,
    CONJUGATE
}

enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ELITE
}

data class ProgressionUpdate(
    val exerciseId: String,
    val currentMetrics: ExerciseMetrics,
    val trend: ProgressionTrend,
    val nextRecommendation: String,
    val confidence: Float
)

data class ExerciseMetrics(
    val currentWeight: Float,
    val currentReps: Int,
    val currentSets: Int,
    val formQuality: Float,
    val rpe: Int,
    val volume: Float
)

data class ProgressionTrend(
    val direction: TrendDirection,
    val strength: Float, // 0-1, how strong the trend is
    val velocity: Float // kg per week progression rate
)

enum class TrendDirection {
    IMPROVING,
    STABLE,
    DECLINING
}

data class HistoricalProgressionData(
    val averageWeeklyProgression: Float, // kg per week
    val consistencyScore: Float, // 0-1
    val plateauHistory: List<String>,
    val bestProgressionPeriod: Int // weeks
)

data class PerformanceMetrics(
    val hasPerformanceDecline: Boolean,
    val fatigueScore: Float, // 0-1
    val formQualityTrend: Float, // -1 to 1
    val volumeTrend: Float // -1 to 1
)