package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.domain.entities.PlateauDetectionResult
import com.example.fitapp.domain.entities.TrendDirection
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max

/**
 * AI Coaching Manager for intelligent workout guidance
 * Handles plateau detection, workout adjustments, rest day prediction, and movement analysis
 */
class AICoachingManager(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.get(context),
) {
    companion object {
        private const val TAG = "AICoachingManager"
        private const val PLATEAU_DETECTION_WEEKS = 3
        private const val MIN_PROGRESS_THRESHOLD = 0.05f // 5% improvement
        private const val FATIGUE_THRESHOLD = 0.7f
        private const val INJURY_RISK_THRESHOLD = 0.8f
    }

    /**
     * Detect training plateaus based on performance history
     */
    suspend fun detectTrainingPlateaus(
        userId: String,
        exerciseId: String,
        weeksToAnalyze: Int = PLATEAU_DETECTION_WEEKS,
    ): PlateauDetectionResult =
        withContext(Dispatchers.IO) {
            try {
                require(weeksToAnalyze > 0) { "Weeks to analyze must be positive" }

                // Simplified plateau detection - would use real performance data
                val performanceHistory = getPerformanceHistory(userId, exerciseId, weeksToAnalyze)

                val progressRate = calculateProgressRate(performanceHistory)
                val plateauScore = calculatePlateauScore(performanceHistory)
                val isPlateaued = progressRate < MIN_PROGRESS_THRESHOLD && plateauScore > 0.6f

                PlateauDetectionResult(
                    exerciseId = exerciseId,
                    isPlateaued = isPlateaued,
                    plateauDuration = if (isPlateaued) weeksToAnalyze else 0,
                    progressRate = progressRate,
                    plateauScore = plateauScore,
                    confidence = calculateConfidence(performanceHistory),
                    recommendations = generatePlateauRecommendations(isPlateaued, progressRate, plateauScore),
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.AI,
                    TAG,
                    "Failed to detect training plateau",
                    exception = e,
                )
                PlateauDetectionResult.error(exerciseId)
            }
        }

    /**
     * Suggest workout adjustments based on progress and performance
     */
    suspend fun suggestWorkoutAdjustments(
        userId: String,
        currentWorkout: WorkoutPlan,
        recentPerformance: List<WorkoutSession>,
    ): WorkoutAdjustmentSuggestion =
        withContext(Dispatchers.IO) {
            try {
                require(recentPerformance.isNotEmpty()) { "Recent performance data required" }

                val performanceAnalysis = analyzeRecentPerformance(recentPerformance)
                val fatigueLevel = calculateFatigueLevel(recentPerformance)
                val injuryRisk = assessInjuryRisk(recentPerformance)

                val adjustmentType = determineAdjustmentType(performanceAnalysis, fatigueLevel, injuryRisk)
                val specificAdjustments =
                    generateSpecificAdjustments(currentWorkout, performanceAnalysis, adjustmentType)

                WorkoutAdjustmentSuggestion(
                    adjustmentType = adjustmentType,
                    priority = calculateAdjustmentPriority(fatigueLevel, injuryRisk),
                    specificAdjustments = specificAdjustments,
                    reasoning = generateAdjustmentReasoning(performanceAnalysis, fatigueLevel, injuryRisk),
                    expectedOutcome = predictAdjustmentOutcome(adjustmentType, performanceAnalysis),
                    implementationNotes = generateImplementationNotes(adjustmentType),
                    confidence = calculateAdjustmentConfidence(performanceAnalysis),
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.AI,
                    TAG,
                    "Failed to suggest workout adjustments",
                    exception = e,
                )
                WorkoutAdjustmentSuggestion.error()
            }
        }

    /**
     * Predict optimal rest days based on training load and recovery metrics
     */
    suspend fun predictOptimalRestDays(
        userId: String,
        trainingSchedule: List<TrainingDay>,
        recoveryMetrics: RecoveryMetrics,
    ): RestDayPrediction =
        withContext(Dispatchers.IO) {
            try {
                val trainingLoad = calculateTrainingLoad(trainingSchedule)
                val recoveryScore = calculateRecoveryScore(recoveryMetrics)
                val fatigueAccumulation = calculateFatigueAccumulation(trainingSchedule)

                val recommendedRestDays = calculateOptimalRestDays(trainingLoad, recoveryScore, fatigueAccumulation)
                val restDayPlacement = optimizeRestDayPlacement(trainingSchedule, recommendedRestDays)

                RestDayPrediction(
                    recommendedRestDays = recommendedRestDays,
                    currentRecoveryScore = recoveryScore,
                    fatigueLevel = fatigueAccumulation,
                    optimalRestPlacement = restDayPlacement,
                    activeRecoveryOptions = generateActiveRecoveryOptions(trainingLoad, recoveryScore),
                    nutritionRecommendations = generateRecoveryNutritionAdvice(fatigueAccumulation),
                    sleepRecommendations = generateSleepRecommendations(recoveryScore),
                    confidence = calculateRestPredictionConfidence(recoveryMetrics),
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.AI,
                    TAG,
                    "Failed to predict optimal rest days",
                    exception = e,
                )
                RestDayPrediction.error()
            }
        }

    /**
     * Analyze movement patterns for injury prevention
     */
    suspend fun analyzeMovementPatterns(
        userId: String,
        exerciseId: String,
        movementData: List<MovementDataPoint>,
    ): MovementAnalysis =
        withContext(Dispatchers.IO) {
            try {
                require(movementData.isNotEmpty()) { "Movement data required" }

                val formQuality = analyzeFormQuality(movementData)
                val asymmetries = detectMovementAsymmetries(movementData)
                val injuryRiskFactors = identifyInjuryRiskFactors(movementData, formQuality, asymmetries)
                val compensationPatterns = detectCompensationPatterns(movementData)

                MovementAnalysis(
                    exerciseId = exerciseId,
                    overallFormScore = formQuality.overallScore,
                    formQuality = formQuality,
                    asymmetries = asymmetries,
                    injuryRiskFactors = injuryRiskFactors,
                    compensationPatterns = compensationPatterns,
                    correctiveExercises = recommendCorrectiveExercises(asymmetries, injuryRiskFactors),
                    mobilityRecommendations = generateMobilityRecommendations(asymmetries, compensationPatterns),
                    techniqueImprovement = generateTechniqueAdvice(formQuality),
                    riskLevel = calculateInjuryRiskLevel(injuryRiskFactors),
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.AI,
                    TAG,
                    "Failed to analyze movement patterns",
                    exception = e,
                )
                MovementAnalysis.error(exerciseId)
            }
        }

    /**
     * Generate personalized nutrition recommendations based on training and goals
     */
    suspend fun generatePersonalizedNutritionRecommendations(
        userId: String,
        currentNutrition: NutritionProfile,
        trainingSchedule: List<TrainingDay>,
        goals: List<FitnessGoal>,
    ): NutritionRecommendation =
        withContext(Dispatchers.IO) {
            try {
                val trainingDemands = analyzeTrainingDemands(trainingSchedule)
                val nutritionGaps = identifyNutritionGaps(currentNutrition, trainingDemands)
                val goalAlignment = assessGoalAlignment(currentNutrition, goals)

                val macroAdjustments = calculateMacroAdjustments(currentNutrition, trainingDemands, goals)
                val timingOptimization = optimizeNutrientTiming(trainingSchedule, currentNutrition)
                val supplementRecommendations = evaluateSupplementNeeds(nutritionGaps, trainingDemands)

                NutritionRecommendation(
                    macroAdjustments = macroAdjustments,
                    timingOptimization = timingOptimization,
                    hydrationGuidance = generateHydrationGuidance(trainingDemands),
                    supplementRecommendations = supplementRecommendations,
                    mealPlanSuggestions = generateMealPlanSuggestions(macroAdjustments, goals),
                    nutritionEducation = generateNutritionEducation(nutritionGaps),
                    implementationStrategy = createImplementationStrategy(macroAdjustments, timingOptimization),
                    expectedOutcomes = predictNutritionOutcomes(macroAdjustments, goals),
                    confidence = calculateNutritionConfidence(nutritionGaps, goalAlignment),
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.AI,
                    TAG,
                    "Failed to generate nutrition recommendations",
                    exception = e,
                )
                NutritionRecommendation.error()
            }
        }

    // Private helper methods

    private fun getPerformanceHistory(
        userId: String,
        exerciseId: String,
        weeks: Int,
    ): List<PerformanceDataPoint> {
        // Simplified - would fetch from database
        return listOf(
            PerformanceDataPoint(1, 100f, 8, 7.5f, 0.8f),
            PerformanceDataPoint(2, 100f, 8, 7.5f, 0.8f),
            PerformanceDataPoint(3, 100f, 8, 7.5f, 0.8f),
        )
    }

    private fun calculateProgressRate(history: List<PerformanceDataPoint>): Float {
        if (history.size < 2) return 0f

        val first = history.first()
        val last = history.last()
        val weightProgress = (last.weight - first.weight) / first.weight
        val repsProgress = (last.reps - first.reps).toFloat() / first.reps

        return max(weightProgress, repsProgress)
    }

    private fun calculatePlateauScore(history: List<PerformanceDataPoint>): Float {
        if (history.size < 2) return 0f

        val variations =
            history.zipWithNext { a, b ->
                abs(a.weight - b.weight) / a.weight + abs(a.reps - b.reps).toFloat() / a.reps
            }

        return 1f - (variations.average().toFloat() * 10f).coerceIn(0f, 1f)
    }

    private fun calculateConfidence(history: List<PerformanceDataPoint>): Float {
        return when {
            history.size >= 6 -> 0.9f
            history.size >= 4 -> 0.7f
            history.size >= 2 -> 0.5f
            else -> 0.2f
        }
    }

    private fun generatePlateauRecommendations(
        isPlateaued: Boolean,
        progressRate: Float,
        plateauScore: Float,
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (isPlateaued) {
            recommendations.add("Deload Week einlegen (20-30% Gewichtsreduktion)")
            recommendations.add("Übungsvariationen einführen")
            recommendations.add("Wiederholungsbereich ändern")

            if (plateauScore > 0.8f) {
                recommendations.add("Trainingspause von 5-7 Tagen")
                recommendations.add("Mobilität und Regeneration fokussieren")
            }
        } else if (progressRate < 0.02f) {
            recommendations.add("Trainingsvolumen leicht erhöhen")
            recommendations.add("Progression verlangsamen")
        }

        return recommendations
    }

    private fun analyzeRecentPerformance(sessions: List<WorkoutSession>): PerformanceAnalysis {
        val avgRpe = sessions.map { it.averageRpe }.average().toFloat()
        val volumeProgression = calculateVolumeProgression(sessions)
        val consistencyScore = calculateConsistencyScore(sessions)

        return PerformanceAnalysis(
            averageRpe = avgRpe,
            volumeProgression = volumeProgression,
            consistencyScore = consistencyScore,
            trendDirection =
                if (volumeProgression > 0.05f) {
                    TrendDirection.UP
                } else if (volumeProgression < -0.05f) {
                    TrendDirection.DOWN
                } else {
                    TrendDirection.STABLE
                },
        )
    }

    private fun calculateFatigueLevel(sessions: List<WorkoutSession>): Float {
        val recentRpes = sessions.takeLast(3).map { it.averageRpe }
        val avgRpe = recentRpes.average().toFloat()
        val rpeVariability = recentRpes.map { abs(it - avgRpe) }.average().toFloat()

        return (avgRpe / 10f) + (rpeVariability * 0.5f)
    }

    private fun assessInjuryRisk(sessions: List<WorkoutSession>): Float {
        val formScores = sessions.map { it.averageFormScore }
        val avgFormScore = formScores.average().toFloat()
        val formDecline =
            if (formScores.size >= 2) {
                (formScores.first() - formScores.last()) / formScores.first()
            } else {
                0f
            }

        return (1f - avgFormScore) + (formDecline * 0.5f)
    }

    private fun determineAdjustmentType(
        performance: PerformanceAnalysis,
        fatigue: Float,
        injuryRisk: Float,
    ): AdjustmentType {
        return when {
            injuryRisk > INJURY_RISK_THRESHOLD -> AdjustmentType.INJURY_PREVENTION
            fatigue > FATIGUE_THRESHOLD -> AdjustmentType.RECOVERY_FOCUS
            performance.trendDirection == TrendDirection.DOWN -> AdjustmentType.VOLUME_REDUCTION
            performance.trendDirection == TrendDirection.UP && fatigue < 0.5f -> AdjustmentType.PROGRESSION
            performance.consistencyScore < 0.6f -> AdjustmentType.CONSISTENCY_IMPROVEMENT
            else -> AdjustmentType.MAINTENANCE
        }
    }

    private fun generateSpecificAdjustments(
        workout: WorkoutPlan,
        performance: PerformanceAnalysis,
        adjustmentType: AdjustmentType,
    ): List<SpecificAdjustment> {
        return when (adjustmentType) {
            AdjustmentType.INJURY_PREVENTION ->
                listOf(
                    SpecificAdjustment("volume", "Volumen um 20% reduzieren"),
                    SpecificAdjustment("intensity", "Intensität auf 70% begrenzen"),
                    SpecificAdjustment("warmup", "Aufwärmen um 10 Minuten verlängern"),
                )
            AdjustmentType.RECOVERY_FOCUS ->
                listOf(
                    SpecificAdjustment("rest", "Zusätzlichen Ruhetag einlegen"),
                    SpecificAdjustment("intensity", "Intensität für 1 Woche reduzieren"),
                    SpecificAdjustment("sleep", "Schlafqualität priorisieren"),
                )
            AdjustmentType.PROGRESSION ->
                listOf(
                    SpecificAdjustment("weight", "Gewicht um 2-5% erhöhen"),
                    SpecificAdjustment("volume", "Ein zusätzlicher Satz bei Hauptübungen"),
                    SpecificAdjustment("complexity", "Übungsvariationen einführen"),
                )
            else ->
                listOf(
                    SpecificAdjustment("maintenance", "Aktuelles Training beibehalten"),
                )
        }
    }

    private fun calculateVolumeProgression(sessions: List<WorkoutSession>): Float {
        if (sessions.size < 2) return 0f
        val first = sessions.first().totalVolume
        val last = sessions.last().totalVolume
        return (last - first) / first
    }

    private fun calculateConsistencyScore(sessions: List<WorkoutSession>): Float {
        val scheduledCount = sessions.size
        val completedCount = sessions.count { it.isCompleted }
        return completedCount.toFloat() / scheduledCount
    }

    private fun generateAdjustmentReasoning(
        performance: PerformanceAnalysis,
        fatigue: Float,
        injuryRisk: Float,
    ): String {
        return when {
            injuryRisk > INJURY_RISK_THRESHOLD -> "Erhöhtes Verletzungsrisiko durch Form-Verschlechterung"
            fatigue > FATIGUE_THRESHOLD -> "Hohe Ermüdung durch intensives Training"
            performance.trendDirection == TrendDirection.UP -> "Positive Entwicklung - Progression möglich"
            else -> "Erhaltung der aktuellen Leistung"
        }
    }

    private fun predictAdjustmentOutcome(
        adjustmentType: AdjustmentType,
        performance: PerformanceAnalysis,
    ): String {
        return when (adjustmentType) {
            AdjustmentType.INJURY_PREVENTION -> "Reduziertes Verletzungsrisiko, verbesserte Bewegungsqualität"
            AdjustmentType.RECOVERY_FOCUS -> "Bessere Regeneration, erhöhte Trainingsbereitschaft"
            AdjustmentType.PROGRESSION -> "Kraftzuwachs von 3-8% in 4 Wochen erwartet"
            else -> "Erhaltung der aktuellen Leistung"
        }
    }

    private fun generateImplementationNotes(adjustmentType: AdjustmentType): List<String> {
        return when (adjustmentType) {
            AdjustmentType.INJURY_PREVENTION ->
                listOf(
                    "Anpassungen sofort umsetzen",
                    "Form-Qualität über Gewicht priorisieren",
                    "Bei Schmerzen sofort pausieren",
                )
            AdjustmentType.RECOVERY_FOCUS ->
                listOf(
                    "Mindestens 1 Woche beibehalten",
                    "Schlaf und Ernährung optimieren",
                    "Aktive Regeneration integrieren",
                )
            else -> listOf("Schrittweise Umsetzung über 2-3 Wochen")
        }
    }

    private fun calculateAdjustmentConfidence(performance: PerformanceAnalysis): Float {
        return when {
            performance.consistencyScore > 0.8f -> 0.9f
            performance.consistencyScore > 0.6f -> 0.7f
            else -> 0.5f
        }
    }

    private fun calculateAdjustmentPriority(
        fatigue: Float,
        injuryRisk: Float,
    ): AdjustmentPriority {
        return when {
            injuryRisk > 0.8f -> AdjustmentPriority.URGENT
            fatigue > 0.8f -> AdjustmentPriority.HIGH
            fatigue > 0.6f || injuryRisk > 0.6f -> AdjustmentPriority.MEDIUM
            else -> AdjustmentPriority.LOW
        }
    }

    // More helper methods would continue...
    private fun calculateTrainingLoad(schedule: List<TrainingDay>): Float = 0.7f

    private fun calculateRecoveryScore(metrics: RecoveryMetrics): Float = 0.8f

    private fun calculateFatigueAccumulation(schedule: List<TrainingDay>): Float = 0.5f

    private fun calculateOptimalRestDays(
        load: Float,
        recovery: Float,
        fatigue: Float,
    ): Int = 2

    private fun optimizeRestDayPlacement(
        schedule: List<TrainingDay>,
        restDays: Int,
    ): List<Int> = listOf(3, 6)

    private fun generateActiveRecoveryOptions(
        load: Float,
        recovery: Float,
    ): List<String> =
        listOf(
            "Leichtes Yoga",
            "Spaziergang",
        )

    private fun generateRecoveryNutritionAdvice(fatigue: Float): List<String> =
        listOf(
            "Mehr Protein",
            "Entzündungshemmende Lebensmittel",
        )

    private fun generateSleepRecommendations(recovery: Float): List<String> =
        listOf(
            "7-9 Stunden Schlaf",
            "Regelmäßige Schlafzeiten",
        )

    private fun calculateRestPredictionConfidence(metrics: RecoveryMetrics): Float = 0.8f

    private fun analyzeFormQuality(data: List<MovementDataPoint>): FormQualityAnalysis =
        FormQualityAnalysis(
            0.8f,
            emptyList(),
            emptyList(),
        )

    private fun detectMovementAsymmetries(data: List<MovementDataPoint>): List<MovementAsymmetry> = emptyList()

    private fun identifyInjuryRiskFactors(
        data: List<MovementDataPoint>,
        form: FormQualityAnalysis,
        asymmetries: List<MovementAsymmetry>,
    ): List<InjuryRiskFactor> = emptyList()

    private fun detectCompensationPatterns(data: List<MovementDataPoint>): List<CompensationPattern> = emptyList()

    private fun recommendCorrectiveExercises(
        asymmetries: List<MovementAsymmetry>,
        risks: List<InjuryRiskFactor>,
    ): List<String> = emptyList()

    private fun generateMobilityRecommendations(
        asymmetries: List<MovementAsymmetry>,
        patterns: List<CompensationPattern>,
    ): List<String> = emptyList()

    private fun generateTechniqueAdvice(form: FormQualityAnalysis): List<String> = emptyList()

    private fun calculateInjuryRiskLevel(factors: List<InjuryRiskFactor>): RiskLevel = RiskLevel.LOW

    private fun analyzeTrainingDemands(schedule: List<TrainingDay>): TrainingDemands =
        TrainingDemands(
            2500,
            150,
            300,
            80,
        )

    private fun identifyNutritionGaps(
        nutrition: NutritionProfile,
        demands: TrainingDemands,
    ): List<NutritionGap> = emptyList()

    private fun assessGoalAlignment(
        nutrition: NutritionProfile,
        goals: List<FitnessGoal>,
    ): Float = 0.8f

    private fun calculateMacroAdjustments(
        nutrition: NutritionProfile,
        demands: TrainingDemands,
        goals: List<FitnessGoal>,
    ): MacroAdjustments =
        MacroAdjustments(
            0,
            0,
            0,
        )

    private fun optimizeNutrientTiming(
        schedule: List<TrainingDay>,
        nutrition: NutritionProfile,
    ): NutrientTimingOptimization =
        NutrientTimingOptimization(
            emptyList(),
            emptyList(),
        )

    private fun evaluateSupplementNeeds(
        gaps: List<NutritionGap>,
        demands: TrainingDemands,
    ): List<SupplementRecommendation> = emptyList()

    private fun generateHydrationGuidance(demands: TrainingDemands): HydrationGuidance =
        HydrationGuidance(
            3.0f,
            emptyList(),
        )

    private fun generateMealPlanSuggestions(
        adjustments: MacroAdjustments,
        goals: List<FitnessGoal>,
    ): List<String> = emptyList()

    private fun generateNutritionEducation(gaps: List<NutritionGap>): List<String> = emptyList()

    private fun createImplementationStrategy(
        adjustments: MacroAdjustments,
        timing: NutrientTimingOptimization,
    ): ImplementationStrategy =
        ImplementationStrategy(
            emptyList(),
            4,
        )

    private fun predictNutritionOutcomes(
        adjustments: MacroAdjustments,
        goals: List<FitnessGoal>,
    ): List<String> = emptyList()

    private fun calculateNutritionConfidence(
        gaps: List<NutritionGap>,
        alignment: Float,
    ): Float = 0.8f
}

// Data classes for AI coaching

data class WorkoutAdjustmentSuggestion(
    val adjustmentType: AdjustmentType,
    val priority: AdjustmentPriority,
    val specificAdjustments: List<SpecificAdjustment>,
    val reasoning: String,
    val expectedOutcome: String,
    val implementationNotes: List<String>,
    val confidence: Float,
) {
    companion object {
        fun error() =
            WorkoutAdjustmentSuggestion(
                adjustmentType = AdjustmentType.MAINTENANCE,
                priority = AdjustmentPriority.LOW,
                specificAdjustments = emptyList(),
                reasoning = "Fehler bei Analyse",
                expectedOutcome = "Unbekannt",
                implementationNotes = emptyList(),
                confidence = 0f,
            )
    }
}

data class RestDayPrediction(
    val recommendedRestDays: Int,
    val currentRecoveryScore: Float,
    val fatigueLevel: Float,
    val optimalRestPlacement: List<Int>,
    val activeRecoveryOptions: List<String>,
    val nutritionRecommendations: List<String>,
    val sleepRecommendations: List<String>,
    val confidence: Float,
) {
    companion object {
        fun error() =
            RestDayPrediction(
                recommendedRestDays = 2,
                currentRecoveryScore = 0.5f,
                fatigueLevel = 0.5f,
                optimalRestPlacement = emptyList(),
                activeRecoveryOptions = emptyList(),
                nutritionRecommendations = emptyList(),
                sleepRecommendations = emptyList(),
                confidence = 0f,
            )
    }
}

data class MovementAnalysis(
    val exerciseId: String,
    val overallFormScore: Float,
    val formQuality: FormQualityAnalysis,
    val asymmetries: List<MovementAsymmetry>,
    val injuryRiskFactors: List<InjuryRiskFactor>,
    val compensationPatterns: List<CompensationPattern>,
    val correctiveExercises: List<String>,
    val mobilityRecommendations: List<String>,
    val techniqueImprovement: List<String>,
    val riskLevel: RiskLevel,
) {
    companion object {
        fun error(exerciseId: String) =
            MovementAnalysis(
                exerciseId = exerciseId,
                overallFormScore = 0.5f,
                formQuality = FormQualityAnalysis(0.5f, emptyList(), emptyList()),
                asymmetries = emptyList(),
                injuryRiskFactors = emptyList(),
                compensationPatterns = emptyList(),
                correctiveExercises = emptyList(),
                mobilityRecommendations = emptyList(),
                techniqueImprovement = emptyList(),
                riskLevel = RiskLevel.UNKNOWN,
            )
    }
}

data class NutritionRecommendation(
    val macroAdjustments: MacroAdjustments,
    val timingOptimization: NutrientTimingOptimization,
    val hydrationGuidance: HydrationGuidance,
    val supplementRecommendations: List<SupplementRecommendation>,
    val mealPlanSuggestions: List<String>,
    val nutritionEducation: List<String>,
    val implementationStrategy: ImplementationStrategy,
    val expectedOutcomes: List<String>,
    val confidence: Float,
) {
    companion object {
        fun error() =
            NutritionRecommendation(
                macroAdjustments = MacroAdjustments(0, 0, 0),
                timingOptimization = NutrientTimingOptimization(emptyList(), emptyList()),
                hydrationGuidance = HydrationGuidance(2.5f, emptyList()),
                supplementRecommendations = emptyList(),
                mealPlanSuggestions = emptyList(),
                nutritionEducation = emptyList(),
                implementationStrategy = ImplementationStrategy(emptyList(), 0),
                expectedOutcomes = emptyList(),
                confidence = 0f,
            )
    }
}

// Supporting data classes
data class PerformanceDataPoint(val week: Int, val weight: Float, val reps: Int, val rpe: Float, val formScore: Float)

data class WorkoutSession(val date: Long, val averageRpe: Float, val totalVolume: Float, val averageFormScore: Float, val isCompleted: Boolean)

data class PerformanceAnalysis(val averageRpe: Float, val volumeProgression: Float, val consistencyScore: Float, val trendDirection: TrendDirection)

data class SpecificAdjustment(val category: String, val description: String)

data class WorkoutPlan(val exercises: List<String>, val totalVolume: Float, val estimatedDuration: Int)

data class TrainingDay(val exercises: List<String>, val intensity: Float, val volume: Float)

data class RecoveryMetrics(val sleepQuality: Float, val hrv: Float, val subjectiveRecovery: Float)

data class MovementDataPoint(val timestamp: Long, val jointAngles: List<Float>, val velocity: Float, val acceleration: Float)

data class FormQualityAnalysis(val overallScore: Float, val keyPoints: List<String>, val improvements: List<String>)

data class MovementAsymmetry(val type: String, val severity: Float, val description: String)

data class InjuryRiskFactor(val factor: String, val riskLevel: Float, val description: String)

data class CompensationPattern(val pattern: String, val severity: Float, val affectedJoints: List<String>)

data class NutritionProfile(val currentCalories: Int, val currentProtein: Int, val currentCarbs: Int, val currentFat: Int)

data class FitnessGoal(val type: String, val target: Float, val timeframe: Int)

data class TrainingDemands(val calories: Int, val protein: Int, val carbs: Int, val fat: Int)

data class NutritionGap(val nutrient: String, val currentAmount: Float, val recommendedAmount: Float)

data class MacroAdjustments(val proteinAdjustment: Int, val carbsAdjustment: Int, val fatAdjustment: Int)

data class NutrientTimingOptimization(val preWorkout: List<String>, val postWorkout: List<String>)

data class SupplementRecommendation(val supplement: String, val dosage: String, val timing: String, val reasoning: String)

data class HydrationGuidance(val dailyTarget: Float, val timingRecommendations: List<String>)

data class ImplementationStrategy(val phases: List<String>, val durationWeeks: Int)

enum class AdjustmentType { PROGRESSION, MAINTENANCE, VOLUME_REDUCTION, RECOVERY_FOCUS, INJURY_PREVENTION, CONSISTENCY_IMPROVEMENT }

enum class AdjustmentPriority { LOW, MEDIUM, HIGH, URGENT }

enum class RiskLevel { LOW, MEDIUM, HIGH, CRITICAL, UNKNOWN }
