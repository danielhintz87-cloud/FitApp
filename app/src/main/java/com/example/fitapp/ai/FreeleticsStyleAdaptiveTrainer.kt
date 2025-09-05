package com.example.fitapp.ai

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import com.example.fitapp.util.StructuredLogger
import com.example.fitapp.services.SetData
import com.example.fitapp.ui.screens.ExerciseStep
import kotlin.math.*

/**
 * Freeletics-Style Adaptive Training Engine
 * 
 * Implements real-time workout personalization and adaptation based on the Freeletics model:
 * - Real-time difficulty adjustment during exercises
 * - Performance-based exercise substitution  
 * - Dynamic rep/time target modifications
 * - Adaptive rest time calculation
 * - Session-based learning that immediately affects current workout
 */
class FreeleticsStyleAdaptiveTrainer(private val context: Context) {
    
    companion object {
        private const val TAG = "FreeleticsAdaptiveTrainer"
        
        // Performance thresholds for adaptive decisions
        private const val EXCELLENT_FORM_THRESHOLD = 0.9f
        private const val GOOD_FORM_THRESHOLD = 0.7f
        private const val POOR_FORM_THRESHOLD = 0.5f
        
        // Fatigue detection thresholds
        private const val HIGH_FATIGUE_THRESHOLD = 0.8f
        private const val MODERATE_FATIGUE_THRESHOLD = 0.6f
        
        // Adaptation sensitivity settings
        private const val ADAPTATION_SENSITIVITY = 0.8f // How quickly to adapt (0.0-1.0)
        private const val MIN_PERFORMANCE_SAMPLES = 3 // Minimum samples before adapting
    }
    
    private val advancedAICoach = AdvancedAICoach(context)
    private val performanceHistory = mutableListOf<PerformanceSnapshot>()
    private var currentAdaptationState = AdaptationState()
    
    /**
     * Real-time workout adaptation based on current performance
     */
    suspend fun adaptWorkoutRealTime(
        currentExercise: ExerciseStep,
        currentPerformance: RealTimePerformance,
        sessionContext: WorkoutSessionContext
    ): WorkoutAdaptation {
        
        // Record performance snapshot
        val snapshot = PerformanceSnapshot(
            timestamp = System.currentTimeMillis(),
            exerciseId = currentExercise.name,
            formQuality = currentPerformance.formQuality,
            perceivedExertion = currentPerformance.rpe,
            heartRate = currentPerformance.heartRate,
            repCount = currentPerformance.currentRep,
            movementSpeed = currentPerformance.movementSpeed,
            fatigueMeasure = calculateFatigueMeasure(currentPerformance)
        )
        
        performanceHistory.add(snapshot)
        
        // Keep only recent history (last 10 snapshots)
        if (performanceHistory.size > 10) {
            performanceHistory.removeAt(0)
        }
        
        // Wait for minimum samples before adapting
        if (performanceHistory.size < MIN_PERFORMANCE_SAMPLES) {
            return WorkoutAdaptation.noChange()
        }
        
        // Analyze performance trends
        val performanceTrend = analyzePerformanceTrend()
        val fatigueLevel = calculateCurrentFatigueLevel()
        val formDegradation = calculateFormDegradation()
        
        // Make adaptation decisions
        val adaptationDecision = makeAdaptationDecision(
            currentExercise,
            performanceTrend,
            fatigueLevel,
            formDegradation,
            sessionContext
        )
        
        // Log adaptation decision
        StructuredLogger.info(
            StructuredLogger.LogCategory.AI,
            TAG,
            "Adaptive decision: ${adaptationDecision.type} - ${adaptationDecision.reasoning}"
        )
        
        return adaptationDecision
    }
    
    /**
     * Dynamic difficulty adjustment within current exercise
     */
    suspend fun adjustDifficultyRealTime(
        currentExercise: ExerciseStep,
        performanceIndicators: PerformanceIndicators
    ): DifficultyAdjustment {
        
        val currentDifficulty = calculateCurrentDifficulty(currentExercise)
        var newDifficulty = currentDifficulty
        val adjustmentReasons = mutableListOf<String>()
        
        // Form-based adjustments
        when {
            performanceIndicators.formQuality > EXCELLENT_FORM_THRESHOLD -> {
                newDifficulty = minOf(currentDifficulty + 0.15f, 1.0f)
                adjustmentReasons.add("Ausgezeichnete Form - Intensität erhöht")
            }
            performanceIndicators.formQuality < POOR_FORM_THRESHOLD -> {
                newDifficulty = maxOf(currentDifficulty - 0.2f, 0.3f)
                adjustmentReasons.add("Form-Probleme erkannt - Intensität reduziert")
            }
        }
        
        // Fatigue-based adjustments
        when {
            performanceIndicators.fatigueLevel > HIGH_FATIGUE_THRESHOLD -> {
                newDifficulty = maxOf(newDifficulty - 0.25f, 0.3f)
                adjustmentReasons.add("Hohe Ermüdung - deutliche Reduktion")
            }
            performanceIndicators.fatigueLevel < 0.3f && performanceIndicators.formQuality > GOOD_FORM_THRESHOLD -> {
                newDifficulty = minOf(newDifficulty + 0.1f, 1.0f)
                adjustmentReasons.add("Geringe Ermüdung bei guter Form - Steigerung möglich")
            }
        }
        
        // Heart rate zone adjustments
        performanceIndicators.heartRateZone?.let { zone ->
            when (zone) {
                HeartRateZone.PEAK -> {
                    newDifficulty = maxOf(newDifficulty - 0.15f, 0.4f)
                    adjustmentReasons.add("Maximale HR-Zone erreicht - Intensität reduziert")
                }
                HeartRateZone.RESTING -> {
                    if (performanceIndicators.formQuality > GOOD_FORM_THRESHOLD) {
                        newDifficulty = minOf(newDifficulty + 0.1f, 1.0f)
                        adjustmentReasons.add("Niedrige HR bei guter Form - Intensität erhöht")
                    }
                }
                else -> { /* No adjustment needed for CARDIO and FAT_BURN zones */ }
            }
        }
        
        return DifficultyAdjustment(
            originalDifficulty = currentDifficulty,
            newDifficulty = newDifficulty,
            adjustmentFactor = newDifficulty / currentDifficulty,
            reasons = adjustmentReasons,
            recommendedChanges = generateDifficultyChanges(currentExercise, newDifficulty)
        )
    }
    
    /**
     * Performance-based exercise substitution during workout
     */
    suspend fun suggestExerciseSubstitution(
        currentExercise: ExerciseStep,
        performanceIssues: List<PerformanceIssue>,
        availableEquipment: List<String>
    ): ExerciseSubstitution? {
        
        // Only suggest substitution if there are significant performance issues
        val criticalIssues = performanceIssues.filter { it.severity >= IssueSeverity.HIGH }
        if (criticalIssues.isEmpty()) return null
        
        val substitutionReason = criticalIssues.joinToString(", ") { it.description }
        
        // Find suitable alternative exercises
        val alternatives = findAlternativeExercises(
            currentExercise,
            performanceIssues,
            availableEquipment
        )
        
        if (alternatives.isEmpty()) return null
        
        // Select best alternative based on user's current state
        val bestAlternative = selectBestAlternative(alternatives, performanceIssues)
        
        return ExerciseSubstitution(
            originalExercise = currentExercise,
            substitution = bestAlternative,
            reason = substitutionReason,
            expectedBenefit = calculateExpectedBenefit(bestAlternative, performanceIssues),
            confidence = calculateSubstitutionConfidence(bestAlternative, currentExercise)
        )
    }
    
    /**
     * Adaptive rest time calculation based on real-time recovery indicators
     */
    suspend fun calculateAdaptiveRestTime(
        lastSetPerformance: SetPerformance,
        currentFatigueLevel: Float,
        targetIntensity: Float,
        exerciseType: String
    ): AdaptiveRestCalculation {
        
        // Base rest time for exercise type
        val baseRestTime = getBaseRestTime(exerciseType)
        
        // Adjustment factors
        val fatigueAdjustment = calculateFatigueRestAdjustment(currentFatigueLevel)
        val performanceAdjustment = calculatePerformanceRestAdjustment(lastSetPerformance)
        val intensityAdjustment = calculateIntensityRestAdjustment(targetIntensity)
        
        // Calculate adjusted rest time
        val adjustedRestTime = (baseRestTime * fatigueAdjustment * performanceAdjustment * intensityAdjustment)
            .coerceIn(30f, 300f) // 30 seconds to 5 minutes
        
        return AdaptiveRestCalculation(
            baseRestTime = baseRestTime,
            adjustedRestTime = adjustedRestTime,
            fatigueAdjustment = fatigueAdjustment,
            performanceAdjustment = performanceAdjustment,
            intensityAdjustment = intensityAdjustment,
            reasoning = generateRestTimeReasoning(
                currentFatigueLevel,
                lastSetPerformance,
                targetIntensity
            )
        )
    }
    
    /**
     * Session-based learning that immediately affects current workout
     */
    suspend fun applySessionLearning(
        sessionId: String,
        currentProgress: SessionProgress,
        remainingExercises: List<ExerciseStep>
    ): SessionLearningApplication {
        
        // Analyze session patterns
        val sessionInsights = analyzeSessionPatterns(currentProgress)
        
        // Generate improvements for remaining exercises
        val modifications = mutableListOf<AdaptiveExerciseModification>()
        
        remainingExercises.forEachIndexed { index, exercise ->
            val modification = generateExerciseModification(
                exercise,
                sessionInsights,
                index
            )
            if (modification.hasChanges()) {
                modifications.add(modification)
            }
        }
        
        return SessionLearningApplication(
            sessionInsights = sessionInsights,
            exerciseModifications = modifications,
            overallSessionAdjustment = calculateOverallSessionAdjustment(sessionInsights),
            learningConfidence = calculateLearningConfidence(currentProgress)
        )
    }
    
    /**
     * Real-time coaching adaptation flow
     */
    fun adaptiveCoachingFlow(
        exerciseId: String,
        realTimeData: Flow<RealTimePerformance>
    ): Flow<AdaptiveCoachingFeedback> = flow {
        
        realTimeData.collect { performance ->
            // Analyze current performance
            val adaptiveFeedback = generateAdaptiveCoachingFeedback(
                exerciseId,
                performance,
                performanceHistory
            )
            
            emit(adaptiveFeedback)
            
            // Small delay to prevent overwhelming the user
            delay(1000)
        }
    }
    
    // Private helper methods
    
    private fun calculateFatigueMeasure(performance: RealTimePerformance): Float {
        val formDrop = maxOf(0f, 1f - performance.formQuality)
        val rpeIncrease = (performance.rpe - 5f) / 5f // Normalize RPE 5-10 to 0-1
        val speedDrop = maxOf(0f, 1f - performance.movementSpeed)
        
        return (formDrop + rpeIncrease + speedDrop) / 3f
    }
    
    private fun analyzePerformanceTrend(): PerformanceTrend {
        if (performanceHistory.size < 3) {
            return PerformanceTrend.STABLE
        }
        
        val recent = performanceHistory.takeLast(3)
        val baseline = performanceHistory.take(3)
        
        val recentAvgForm = recent.map { it.formQuality }.average()
        val baselineAvgForm = baseline.map { it.formQuality }.average()
        
        val recentAvgFatigue = recent.map { it.fatigueMeasure }.average()
        val baselineAvgFatigue = baseline.map { it.fatigueMeasure }.average()
        
        return when {
            recentAvgForm < baselineAvgForm - 0.1 || recentAvgFatigue > baselineAvgFatigue + 0.1 -> 
                PerformanceTrend.DECLINING
            recentAvgForm > baselineAvgForm + 0.1 && recentAvgFatigue < baselineAvgFatigue - 0.1 -> 
                PerformanceTrend.IMPROVING
            else -> PerformanceTrend.STABLE
        }
    }
    
    private fun calculateCurrentFatigueLevel(): Float {
        if (performanceHistory.isEmpty()) return 0.3f
        
        return performanceHistory.takeLast(3).map { it.fatigueMeasure }.average().toFloat()
    }
    
    private fun calculateFormDegradation(): Float {
        if (performanceHistory.size < 2) return 0f
        
        val first = performanceHistory.first().formQuality
        val last = performanceHistory.last().formQuality
        
        return maxOf(0f, first - last)
    }
    
    private fun makeAdaptationDecision(
        currentExercise: ExerciseStep,
        performanceTrend: PerformanceTrend,
        fatigueLevel: Float,
        formDegradation: Float,
        sessionContext: WorkoutSessionContext
    ): WorkoutAdaptation {
        
        // High fatigue or significant form degradation
        if (fatigueLevel > HIGH_FATIGUE_THRESHOLD || formDegradation > 0.3f) {
            return WorkoutAdaptation(
                type = AdaptationType.REDUCE_INTENSITY,
                modifications = listOf(
                    AdaptiveExerciseModification(
                        exerciseId = currentExercise.name,
                        weightAdjustment = -0.15f,
                        repAdjustment = -2,
                        restTimeAdjustment = 1.3f
                    )
                ),
                reasoning = "Hohe Ermüdung oder Formverschlechterung erkannt",
                confidence = 0.85f
            )
        }
        
        // Excellent performance - increase difficulty
        if (performanceTrend == PerformanceTrend.IMPROVING && fatigueLevel < 0.4f) {
            return WorkoutAdaptation(
                type = AdaptationType.INCREASE_INTENSITY,
                modifications = listOf(
                    AdaptiveExerciseModification(
                        exerciseId = currentExercise.name,
                        weightAdjustment = 0.1f,
                        repAdjustment = 1,
                        restTimeAdjustment = 0.9f
                    )
                ),
                reasoning = "Ausgezeichnete Leistung - Steigerung möglich",
                confidence = 0.75f
            )
        }
        
        // Performance declining but not severely
        if (performanceTrend == PerformanceTrend.DECLINING && fatigueLevel > MODERATE_FATIGUE_THRESHOLD) {
            return WorkoutAdaptation(
                type = AdaptationType.MODIFY_TECHNIQUE,
                modifications = listOf(
                    AdaptiveExerciseModification(
                        exerciseId = currentExercise.name,
                        tempoAdjustment = 0.8f, // Slower tempo for better control
                        restTimeAdjustment = 1.1f
                    )
                ),
                reasoning = "Leichte Leistungsabnahme - Fokus auf Technik",
                confidence = 0.7f
            )
        }
        
        return WorkoutAdaptation.noChange()
    }
    
    private fun calculateCurrentDifficulty(exercise: ExerciseStep): Float {
        // Extract difficulty from exercise description/value
        // This is a simplified implementation
        val value = exercise.value.lowercase()
        return when {
            value.contains("schwer") || value.contains("hard") -> 0.8f
            value.contains("mittel") || value.contains("medium") -> 0.6f
            value.contains("leicht") || value.contains("easy") -> 0.4f
            else -> 0.6f // Default medium difficulty
        }
    }
    
    private fun generateDifficultyChanges(
        exercise: ExerciseStep,
        newDifficulty: Float
    ): List<String> {
        val changes = mutableListOf<String>()
        
        when {
            newDifficulty > 0.8f -> {
                changes.add("Gewicht um 10-15% erhöhen")
                changes.add("2-3 zusätzliche Wiederholungen")
                changes.add("Tempo erhöhen")
            }
            newDifficulty < 0.4f -> {
                changes.add("Gewicht um 15-20% reduzieren")
                changes.add("Wiederholungen um 2-3 reduzieren")
                changes.add("Langsameres, kontrolliertes Tempo")
            }
            else -> {
                changes.add("Aktuelle Einstellungen beibehalten")
            }
        }
        
        return changes
    }
    
    private fun findAlternativeExercises(
        currentExercise: ExerciseStep,
        performanceIssues: List<PerformanceIssue>,
        availableEquipment: List<String>
    ): List<ExerciseStep> {
        // Simplified exercise substitution logic
        val alternatives = mutableListOf<ExerciseStep>()
        
        // If form issues, suggest easier variations
        if (performanceIssues.any { it.type == IssueType.FORM_DEGRADATION }) {
            alternatives.add(ExerciseStep(
                name = "${currentExercise.name} (Vereinfacht)",
                type = currentExercise.type,
                value = "Leichtere Variante",
                description = "Vereinfachte Version mit Fokus auf korrekte Ausführung",
                restTime = currentExercise.restTime + 10
            ))
        }
        
        // If fatigue issues, suggest different muscle groups
        if (performanceIssues.any { it.type == IssueType.EXCESSIVE_FATIGUE }) {
            alternatives.add(ExerciseStep(
                name = "Alternative: Leichtere Übung",
                type = "alternative",
                value = "Nach aktuellem Zustand",
                description = "Wechsel zu weniger belastender Übung",
                restTime = currentExercise.restTime + 20
            ))
        }
        
        return alternatives
    }
    
    private fun selectBestAlternative(
        alternatives: List<ExerciseStep>,
        performanceIssues: List<PerformanceIssue>
    ): ExerciseStep {
        // Simple selection: prefer form-focused alternatives for form issues
        return alternatives.firstOrNull { it.description.contains("Ausführung") }
            ?: alternatives.first()
    }
    
    private fun calculateExpectedBenefit(
        alternative: ExerciseStep,
        performanceIssues: List<PerformanceIssue>
    ): Float {
        // Simplified benefit calculation
        return when {
            alternative.description.contains("Vereinfacht") && 
            performanceIssues.any { it.type == IssueType.FORM_DEGRADATION } -> 0.8f
            alternative.description.contains("weniger belastend") && 
            performanceIssues.any { it.type == IssueType.EXCESSIVE_FATIGUE } -> 0.7f
            else -> 0.6f
        }
    }
    
    private fun calculateSubstitutionConfidence(
        alternative: ExerciseStep,
        original: ExerciseStep
    ): Float {
        // Higher confidence for same exercise type
        return if (alternative.type == original.type) 0.8f else 0.6f
    }
    
    private fun getBaseRestTime(exerciseType: String): Float {
        return when (exerciseType.lowercase()) {
            "strength", "krafttraining" -> 90f
            "cardio", "ausdauer" -> 45f
            "flexibility", "beweglichkeit" -> 30f
            else -> 60f
        }
    }
    
    private fun calculateFatigueRestAdjustment(fatigueLevel: Float): Float {
        return 1f + (fatigueLevel * 0.8f) // 0-80% increase based on fatigue
    }
    
    private fun calculatePerformanceRestAdjustment(setPerformance: SetPerformance): Float {
        return when {
            setPerformance.formQuality < 0.6f -> 1.3f // 30% more rest for poor form
            setPerformance.perceivedExertion > 8 -> 1.2f // 20% more rest for high RPE
            else -> 1f
        }
    }
    
    private fun calculateIntensityRestAdjustment(targetIntensity: Float): Float {
        return 0.8f + (targetIntensity * 0.4f) // Rest scales with target intensity
    }
    
    private fun generateRestTimeReasoning(
        fatigueLevel: Float,
        setPerformance: SetPerformance,
        targetIntensity: Float
    ): String {
        val reasons = mutableListOf<String>()
        
        when {
            fatigueLevel > 0.7f -> reasons.add("Hohe Ermüdung erkannt")
            fatigueLevel < 0.3f -> reasons.add("Geringe Ermüdung")
        }
        
        when {
            setPerformance.formQuality < 0.6f -> reasons.add("Form-Erholung erforderlich")
            setPerformance.perceivedExertion > 8 -> reasons.add("Hohe wahrgenommene Anstrengung")
        }
        
        when {
            targetIntensity > 0.8f -> reasons.add("Hohe Zielintensität")
            targetIntensity < 0.5f -> reasons.add("Moderate Zielintensität")
        }
        
        return if (reasons.isNotEmpty()) {
            "Anpassung basierend auf: ${reasons.joinToString(", ")}"
        } else {
            "Standard-Pausenzeit basierend auf Übungstyp"
        }
    }
    
    private fun analyzeSessionPatterns(sessionProgress: SessionProgress): SessionInsights {
        return SessionInsights(
            averageFormQuality = sessionProgress.performanceData.map { it.formQuality }.average().toFloat(),
            fatigueProgression = calculateFatigueProgression(sessionProgress),
            strengths = identifySessionStrengths(sessionProgress),
            improvements = identifySessionImprovements(sessionProgress),
            overallTrend = determineOverallSessionTrend(sessionProgress)
        )
    }
    
    private fun calculateFatigueProgression(sessionProgress: SessionProgress): Float {
        val performances = sessionProgress.performanceData
        if (performances.size < 2) return 0f
        
        val early = performances.take(performances.size / 3)
        val late = performances.takeLast(performances.size / 3)
        
        val earlyAvgFatigue = early.map { calculateFatigueMeasure(it) }.average()
        val lateAvgFatigue = late.map { calculateFatigueMeasure(it) }.average()
        
        return (lateAvgFatigue - earlyAvgFatigue).toFloat()
    }
    
    private fun identifySessionStrengths(sessionProgress: SessionProgress): List<String> {
        val strengths = mutableListOf<String>()
        
        val avgFormQuality = sessionProgress.performanceData.map { it.formQuality }.average()
        if (avgFormQuality > 0.8) {
            strengths.add("Ausgezeichnete Bewegungsausführung")
        }
        
        val consistentPerformance = sessionProgress.performanceData
            .map { it.formQuality }
            .zipWithNext { a, b -> abs(a - b) }
            .average()
        
        if (consistentPerformance < 0.15) {
            strengths.add("Konstante Leistung über die Session")
        }
        
        return strengths
    }
    
    private fun identifySessionImprovements(sessionProgress: SessionProgress): List<String> {
        val improvements = mutableListOf<String>()
        
        val avgFormQuality = sessionProgress.performanceData.map { it.formQuality }.average()
        if (avgFormQuality < 0.6) {
            improvements.add("Bewegungsqualität verbessern")
        }
        
        val highRPE = sessionProgress.performanceData.count { it.rpe > 8 }
        if (highRPE > sessionProgress.performanceData.size * 0.5) {
            improvements.add("Belastungsmanagement optimieren")
        }
        
        return improvements
    }
    
    private fun determineOverallSessionTrend(sessionProgress: SessionProgress): SessionTrend {
        val fatigueProgression = calculateFatigueProgression(sessionProgress)
        val avgFormQuality = sessionProgress.performanceData.map { it.formQuality }.average()
        
        return when {
            fatigueProgression > 0.3 || avgFormQuality < 0.5 -> SessionTrend.DECLINING
            fatigueProgression < 0.1 && avgFormQuality > 0.8 -> SessionTrend.EXCELLENT
            else -> SessionTrend.STABLE
        }
    }
    
    private fun generateExerciseModification(
        exercise: ExerciseStep,
        sessionInsights: SessionInsights,
        exerciseIndex: Int
    ): AdaptiveExerciseModification {
        
        return when (sessionInsights.overallTrend) {
            SessionTrend.DECLINING -> AdaptiveExerciseModification(
                exerciseId = exercise.name,
                weightAdjustment = -0.1f,
                restTimeAdjustment = 1.2f,
                tempoAdjustment = 0.8f,
                reason = "Session-Trend zeigt Ermüdung - Anpassung für bessere Ausführung"
            )
            SessionTrend.EXCELLENT -> AdaptiveExerciseModification(
                exerciseId = exercise.name,
                weightAdjustment = 0.05f,
                repAdjustment = if (exerciseIndex < 2) 1 else 0, // Only first few exercises
                reason = "Hervorragende Session-Leistung - moderate Steigerung möglich"
            )
            else -> AdaptiveExerciseModification.noChange(exercise.name)
        }
    }
    
    private fun calculateOverallSessionAdjustment(sessionInsights: SessionInsights): Float {
        return when (sessionInsights.overallTrend) {
            SessionTrend.DECLINING -> -0.15f // Reduce intensity by 15%
            SessionTrend.EXCELLENT -> 0.1f   // Increase intensity by 10%
            SessionTrend.STABLE -> 0f        // No change
        }
    }
    
    private fun calculateLearningConfidence(sessionProgress: SessionProgress): Float {
        val dataPoints = sessionProgress.performanceData.size
        return when {
            dataPoints > 15 -> 0.9f
            dataPoints > 10 -> 0.8f
            dataPoints > 5 -> 0.7f
            else -> 0.5f
        }
    }
    
    private fun generateAdaptiveCoachingFeedback(
        exerciseId: String,
        performance: RealTimePerformance,
        history: List<PerformanceSnapshot>
    ): AdaptiveCoachingFeedback {
        
        val immediateMessages = mutableListOf<String>()
        val adaptiveActions = mutableListOf<String>()
        val priority = determineFeedbackPriority(performance)
        
        // Form-based feedback
        when {
            performance.formQuality > 0.9f -> {
                immediateMessages.add("Perfekte Ausführung! 💪")
                if (history.size > 3 && history.takeLast(3).all { it.formQuality > 0.85f }) {
                    adaptiveActions.add("Bereit für Intensitätssteigerung")
                }
            }
            performance.formQuality < 0.6f -> {
                immediateMessages.add("Fokus auf saubere Technik! 🎯")
                adaptiveActions.add("Gewicht reduzieren empfohlen")
            }
            performance.formQuality < 0.8f -> {
                immediateMessages.add("Gute Arbeit, halte die Form! 👍")
            }
        }
        
        // Fatigue-based feedback
        val fatigue = calculateFatigueMeasure(performance)
        when {
            fatigue > 0.8f -> {
                immediateMessages.add("Hohe Belastung - achte auf deine Grenzen ⚠️")
                adaptiveActions.add("Pausenzeit verlängern")
            }
            fatigue < 0.3f && performance.formQuality > 0.8f -> {
                immediateMessages.add("Du hast noch Reserven! 🚀")
                adaptiveActions.add("Intensität kann gesteigert werden")
            }
        }
        
        return AdaptiveCoachingFeedback(
            exerciseId = exerciseId,
            immediateMessages = immediateMessages,
            adaptiveActions = adaptiveActions,
            priority = priority,
            confidence = calculateFeedbackConfidence(performance, history),
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun determineFeedbackPriority(performance: RealTimePerformance): FeedbackPriority {
        return when {
            performance.formQuality < 0.5f -> FeedbackPriority.CRITICAL
            performance.rpe > 9 -> FeedbackPriority.HIGH
            performance.formQuality < 0.7f || performance.rpe > 8 -> FeedbackPriority.MEDIUM
            else -> FeedbackPriority.LOW
        }
    }
    
    private fun calculateFeedbackConfidence(
        performance: RealTimePerformance,
        history: List<PerformanceSnapshot>
    ): Float {
        val dataQuality = when {
            performance.heartRate != null && performance.formQuality > 0 -> 1f
            performance.formQuality > 0 -> 0.8f
            else -> 0.5f
        }
        
        val historyQuality = when {
            history.size > 5 -> 0.9f
            history.size > 2 -> 0.7f
            else -> 0.5f
        }
        
        return (dataQuality + historyQuality) / 2f
    }
}

// Data classes for Freeletics-style adaptive training

data class RealTimePerformance(
    val formQuality: Float,
    val rpe: Int, // Rate of Perceived Exertion (1-10)
    val heartRate: Int?,
    val currentRep: Int,
    val movementSpeed: Float, // Relative to optimal speed
    val timestamp: Long = System.currentTimeMillis()
)

data class PerformanceSnapshot(
    val timestamp: Long,
    val exerciseId: String,
    val formQuality: Float,
    val perceivedExertion: Int,
    val heartRate: Int?,
    val repCount: Int,
    val movementSpeed: Float,
    val fatigueMeasure: Float
)

data class WorkoutSessionContext(
    val sessionId: String,
    val sessionDuration: Long, // minutes
    val totalExercises: Int,
    val completedExercises: Int,
    val overallIntensity: Float,
    val userEnergyLevel: Float
)

data class WorkoutAdaptation(
    val type: AdaptationType,
    val modifications: List<AdaptiveExerciseModification>,
    val reasoning: String,
    val confidence: Float
) {
    companion object {
        fun noChange() = WorkoutAdaptation(
            type = AdaptationType.NO_CHANGE,
            modifications = emptyList(),
            reasoning = "Keine Anpassung erforderlich",
            confidence = 1f
        )
    }
}

data class DifficultyAdjustment(
    val originalDifficulty: Float,
    val newDifficulty: Float,
    val adjustmentFactor: Float,
    val reasons: List<String>,
    val recommendedChanges: List<String>
)

data class PerformanceIndicators(
    val formQuality: Float,
    val fatigueLevel: Float,
    val heartRateZone: HeartRateZone?,
    val movementConsistency: Float
)

data class ExerciseSubstitution(
    val originalExercise: ExerciseStep,
    val substitution: ExerciseStep,
    val reason: String,
    val expectedBenefit: Float,
    val confidence: Float
)

data class PerformanceIssue(
    val type: IssueType,
    val severity: IssueSeverity,
    val description: String,
    val detectionConfidence: Float
)

data class AdaptiveRestCalculation(
    val baseRestTime: Float,
    val adjustedRestTime: Float,
    val fatigueAdjustment: Float,
    val performanceAdjustment: Float,
    val intensityAdjustment: Float,
    val reasoning: String
)

data class SetPerformance(
    val formQuality: Float,
    val perceivedExertion: Int,
    val actualReps: Int,
    val targetReps: Int,
    val weight: Float
)

data class SessionProgress(
    val sessionId: String,
    val elapsedTime: Long,
    val performanceData: List<RealTimePerformance>,
    val completedExercises: List<String>,
    val currentFatigueLevel: Float
)

data class SessionLearningApplication(
    val sessionInsights: SessionInsights,
    val exerciseModifications: List<AdaptiveExerciseModification>,
    val overallSessionAdjustment: Float,
    val learningConfidence: Float
)

data class SessionInsights(
    val averageFormQuality: Float,
    val fatigueProgression: Float,
    val strengths: List<String>,
    val improvements: List<String>,
    val overallTrend: SessionTrend
)

data class AdaptiveExerciseModification(
    val exerciseId: String,
    val weightAdjustment: Float = 0f,
    val repAdjustment: Int = 0,
    val restTimeAdjustment: Float = 1f,
    val tempoAdjustment: Float = 1f,
    val reason: String = ""
) {
    fun hasChanges(): Boolean = 
        weightAdjustment != 0f || repAdjustment != 0 || 
        restTimeAdjustment != 1f || tempoAdjustment != 1f
    
    companion object {
        fun noChange(exerciseId: String) = AdaptiveExerciseModification(
            exerciseId = exerciseId,
            reason = "Keine Änderung erforderlich"
        )
    }
}

data class AdaptiveCoachingFeedback(
    val exerciseId: String,
    val immediateMessages: List<String>,
    val adaptiveActions: List<String>,
    val priority: FeedbackPriority,
    val confidence: Float,
    val timestamp: Long
)

data class AdaptationState(
    val lastAdaptationTime: Long = 0L,
    val adaptationCount: Int = 0,
    val adaptationHistory: List<AdaptationType> = emptyList()
)

// Enums

enum class AdaptationType {
    NO_CHANGE,
    INCREASE_INTENSITY,
    REDUCE_INTENSITY,
    MODIFY_TECHNIQUE,
    SUBSTITUTE_EXERCISE,
    ADJUST_REST_TIME,
    CHANGE_TEMPO
}

enum class PerformanceTrend {
    IMPROVING,
    STABLE,
    DECLINING
}

enum class IssueType {
    FORM_DEGRADATION,
    EXCESSIVE_FATIGUE,
    INSUFFICIENT_INTENSITY,
    MOVEMENT_ASYMMETRY,
    SAFETY_CONCERN
}

enum class IssueSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class SessionTrend {
    EXCELLENT,
    STABLE,
    DECLINING
}

enum class FeedbackPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}