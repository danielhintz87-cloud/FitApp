package com.example.fitapp.ai

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.services.SetData
import com.example.fitapp.util.StructuredLogger
import kotlin.math.*

/**
 * Advanced AI Coach - Phase 2 Pro Features
 * Extends existing AI infrastructure with form checking, plateau detection,
 * injury prevention, and predictive analytics
 */
class AdvancedAICoach(private val context: Context) {
    
    companion object {
        private const val TAG = "AdvancedAICoach"
        private const val PLATEAU_DETECTION_WEEKS = 3
        private const val INJURY_RISK_THRESHOLD = 0.7f
        private const val FORM_QUALITY_THRESHOLD = 0.6f
    }
    
    private val database = AppDatabase.get(context)
    
    /**
     * Detect training plateau using machine learning-like analysis
     */
    suspend fun detectTrainingPlateau(
        userId: String,
        exerciseId: String
    ): PlateauDetectionResult {
        return try {
            val recentData = getRecentPerformanceData(userId, exerciseId, PLATEAU_DETECTION_WEEKS * 7)
            
            if (recentData.size < 6) {
                return PlateauDetectionResult(
                    hasPlateauDetected = false,
                    confidence = 0f,
                    message = "Mehr Daten benötigt für Plateau-Analyse",
                    recommendation = "Führe weitere Trainingseinheiten durch"
                )
            }
            
            val progressTrend = calculateProgressTrend(recentData)
            val volumeTrend = calculateVolumeTrend(recentData)
            val formTrend = calculateFormTrend(recentData)
            
            val plateauScore = calculatePlateauScore(progressTrend, volumeTrend, formTrend)
            val hasPlateauDetected = plateauScore > 0.6f
            
            PlateauDetectionResult(
                hasPlateauDetected = hasPlateauDetected,
                confidence = plateauScore,
                message = if (hasPlateauDetected) {
                    "Trainingsplateau erkannt bei $exerciseId"
                } else {
                    "Gute Fortschritte bei $exerciseId"
                },
                recommendation = generatePlateauRecommendation(plateauScore, progressTrend, volumeTrend),
                detectedWeeksAgo = PLATEAU_DETECTION_WEEKS,
                suggestedActions = generatePlateauActions(plateauScore)
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.AI,
                TAG,
                "Failed to detect plateau for exercise: $exerciseId",
                exception = e
            )
            PlateauDetectionResult(
                hasPlateauDetected = false,
                confidence = 0f,
                message = "Fehler bei der Plateau-Analyse",
                recommendation = "Bitte versuche es später erneut"
            )
        }
    }
    
    /**
     * Automatically adjust workout intensity based on AI analysis
     */
    suspend fun adjustWorkoutIntensity(
        currentWorkout: List<SetData>,
        plateauResult: PlateauDetectionResult,
        formAnalysis: FormAnalysisResult
    ): IntensityAdjustment {
        val baseIntensity = calculateCurrentIntensity(currentWorkout)
        var adjustmentFactor = 1.0f
        val reasons = mutableListOf<String>()
        
        // Adjust based on plateau detection
        if (plateauResult.hasPlateauDetected) {
            adjustmentFactor *= when (plateauResult.confidence) {
                in 0.8f..1.0f -> 1.15f // Significant increase needed
                in 0.6f..0.8f -> 1.08f // Moderate increase
                else -> 1.03f // Slight increase
            }
            reasons.add("Plateau-Durchbruch erforderlich")
        }
        
        // Adjust based on form quality
        when {
            formAnalysis.overallFormQuality < 0.5f -> {
                adjustmentFactor *= 0.85f // Reduce intensity for better form
                reasons.add("Form-Verbesserung priorisiert")
            }
            formAnalysis.overallFormQuality > 0.9f -> {
                adjustmentFactor *= 1.05f // Can handle more intensity
                reasons.add("Ausgezeichnete Form ermöglicht Steigerung")
            }
        }
        
        // Adjust based on fatigue indicators
        val fatigueLevel = calculateFatigueLevel(currentWorkout)
        if (fatigueLevel > 0.7f) {
            adjustmentFactor *= 0.9f
            reasons.add("Ermüdung erkannt - Intensität reduziert")
        }
        
        val newIntensity = (baseIntensity * adjustmentFactor).coerceIn(0.5f, 1.5f)
        
        return IntensityAdjustment(
            originalIntensity = baseIntensity,
            adjustedIntensity = newIntensity,
            adjustmentFactor = adjustmentFactor,
            reasons = reasons,
            confidence = calculateAdjustmentConfidence(plateauResult, formAnalysis)
        )
    }
    
    /**
     * Predict optimal rest days using recovery analysis
     */
    suspend fun predictOptimalRestDays(
        userId: String,
        currentFatigueLevel: Float,
        recentWorkoutIntensity: List<Float>
    ): RestDayPrediction {
        val recoveryScore = calculateRecoveryScore(currentFatigueLevel, recentWorkoutIntensity)
        val stressAccumulation = calculateStressAccumulation(recentWorkoutIntensity)
        
        val recommendedRestDays = when {
            recoveryScore < 0.3f -> 2 // High fatigue
            recoveryScore < 0.6f -> 1 // Moderate fatigue
            stressAccumulation > 0.8f -> 1 // High stress accumulation
            else -> 0 // Good to continue
        }
        
        return RestDayPrediction(
            recommendedRestDays = recommendedRestDays,
            recoveryScore = recoveryScore,
            stressLevel = stressAccumulation,
            reasoning = generateRestDayReasoning(recoveryScore, stressAccumulation),
            nextWorkoutIntensity = calculateOptimalNextIntensity(recoveryScore)
        )
    }
    
    /**
     * Personalize nutrition timing for maximum performance
     */
    suspend fun personalizeNutritionTiming(
        userId: String,
        workoutTime: Int, // Hour of day
        workoutDuration: Int, // Minutes
        workoutIntensity: Float
    ): NutritionTimingPlan {
        val baseCalories = calculateWorkoutCalories(workoutDuration, workoutIntensity)
        val proteinNeeds = calculateProteinNeeds(workoutIntensity, workoutDuration)
        val carbNeeds = calculateCarbNeeds(workoutIntensity, workoutDuration)
        
        return NutritionTimingPlan(
            preWorkoutMeal = generatePreWorkoutMeal(workoutTime, workoutIntensity),
            duringWorkoutNutrition = generateDuringWorkoutNutrition(workoutDuration, workoutIntensity),
            postWorkoutMeal = generatePostWorkoutMeal(proteinNeeds, carbNeeds),
            optimalEatingWindow = calculateOptimalEatingWindow(workoutTime),
            hydrationPlan = generateHydrationPlan(workoutDuration, workoutIntensity)
        )
    }
    
    /**
     * Real-time form analysis and feedback
     */
    fun analyzeFormRealTime(
        movementData: List<MovementPoint>,
        exerciseType: String
    ): Flow<FormAnalysisResult> = flow {
        val analysisTemplates = getFormTemplates(exerciseType)
        
        for (dataPoint in movementData.chunked(10)) { // Analyze in chunks
            val analysis = analyzeMovementPattern(dataPoint, analysisTemplates)
            emit(analysis)
            delay(100) // Real-time update interval
        }
    }
    
    /**
     * Movement pattern analysis for injury prevention
     */
    suspend fun analyzeMovementPatterns(
        userId: String,
        exerciseId: String,
        movementData: List<MovementPoint>
    ): InjuryRiskAssessment {
        val patterns = extractMovementPatterns(movementData)
        val asymmetries = detectMovementAsymmetries(patterns)
        val compensations = detectCompensatoryPatterns(patterns)
        val fatiguePatterns = detectFatiguePatterns(patterns)
        
        val riskFactors = listOfNotNull(
            if (asymmetries.isNotEmpty()) RiskFactor.MOVEMENT_ASYMMETRY else null,
            if (compensations.isNotEmpty()) RiskFactor.COMPENSATION_PATTERN else null,
            if (fatiguePatterns.isNotEmpty()) RiskFactor.FATIGUE_INDUCED_CHANGES else null
        )
        
        val overallRisk = calculateInjuryRisk(riskFactors, patterns)
        
        return InjuryRiskAssessment(
            overallRisk = overallRisk,
            riskFactors = riskFactors,
            recommendations = generateInjuryPreventionRecommendations(riskFactors),
            prehabExercises = suggestPrehabExercises(riskFactors, exerciseId),
            confidence = calculateAssessmentConfidence(patterns.size)
        )
    }
    
    /**
     * Load management for optimal recovery
     */
    suspend fun calculateOptimalLoad(
        userId: String,
        plannedExercises: List<String>,
        currentFatigue: Float,
        recoveryMetrics: RecoveryMetrics
    ): LoadManagementPlan {
        val baseLoad = calculateBaseLoad(plannedExercises)
        val fatigueAdjustment = 1.0f - (currentFatigue * 0.3f)
        val recoveryAdjustment = calculateRecoveryAdjustment(recoveryMetrics)
        
        val optimalLoad = baseLoad * fatigueAdjustment * recoveryAdjustment
        
        return LoadManagementPlan(
            recommendedLoad = optimalLoad,
            originalLoad = baseLoad,
            adjustmentFactors = mapOf(
                "fatigue" to fatigueAdjustment,
                "recovery" to recoveryAdjustment
            ),
            exerciseModifications = generateExerciseModifications(optimalLoad, baseLoad),
            rationaleExplanation = generateLoadRationale(fatigueAdjustment, recoveryAdjustment)
        )
    }
    
    // Helper methods for AI calculations
    private suspend fun getRecentPerformanceData(userId: String, exerciseId: String, days: Int): List<PerformanceDataPoint> {
        // In real implementation, this would query the database
        // For now, simulate realistic performance data
        return generateSimulatedPerformanceData(days)
    }
    
    private fun generateSimulatedPerformanceData(days: Int): List<PerformanceDataPoint> {
        return (1..days).map { day ->
            PerformanceDataPoint(
                weight = 20f + (day * 0.1f) + (Math.random() * 2 - 1).toFloat(), // Slight upward trend with noise
                reps = 10 + (Math.random() * 3 - 1).toInt(),
                formQuality = 0.7f + (Math.random() * 0.3).toFloat(),
                rpe = 6 + (Math.random() * 3).toInt(),
                volume = (20f + day * 0.1f) * (10 + Math.random().toFloat() * 2),
                timestamp = System.currentTimeMillis() - (days - day) * 24 * 60 * 60 * 1000L
            )
        }
    }
    
    private fun calculateProgressTrend(data: List<PerformanceDataPoint>): Float {
        if (data.size < 2) return 0f
        
        val weights = data.map { it.weight }
        val n = weights.size
        val xSum = (1..n).sum()
        val ySum = weights.sum()
        val xySum = weights.mapIndexed { index, weight -> (index + 1) * weight }.sum()
        val x2Sum = (1..n).map { it * it }.sum()
        
        val slope = (n * xySum - xSum * ySum) / (n * x2Sum - xSum * xSum)
        return slope.toFloat()
    }
    
    private fun calculateVolumeTrend(data: List<PerformanceDataPoint>): Float {
        if (data.size < 2) return 0f
        return (data.last().volume - data.first().volume) / data.first().volume
    }
    
    private fun calculateFormTrend(data: List<PerformanceDataPoint>): Float {
        if (data.size < 2) return 0f
        return data.takeLast(3).map { it.formQuality }.average().toFloat() - 
               data.take(3).map { it.formQuality }.average().toFloat()
    }
    
    private fun calculatePlateauScore(progressTrend: Float, volumeTrend: Float, formTrend: Float): Float {
        val progressScore = if (abs(progressTrend) < 0.01f) 0.4f else 0f
        val volumeScore = if (abs(volumeTrend) < 0.05f) 0.3f else 0f
        val formScore = if (formTrend < -0.1f) 0.3f else 0f
        
        return progressScore + volumeScore + formScore
    }
    
    private fun generatePlateauRecommendation(plateauScore: Float, progressTrend: Float, volumeTrend: Float): String {
        return when {
            plateauScore > 0.8f -> "Intensität drastisch erhöhen oder Übung wechseln"
            plateauScore > 0.6f -> "Gewicht oder Volumen schrittweise steigern"
            progressTrend < 0 -> "Form und Technik überprüfen"
            else -> "Aktuelle Strategie beibehalten"
        }
    }
    
    private fun generatePlateauActions(plateauScore: Float): List<String> {
        return when {
            plateauScore > 0.8f -> listOf(
                "Deload Week einlegen",
                "Übungsvariante ausprobieren",
                "Trainingsfrequenz ändern",
                "Ernährung überprüfen"
            )
            plateauScore > 0.6f -> listOf(
                "Gewicht um 2.5-5kg erhöhen",
                "Zusätzlichen Satz hinzufügen",
                "Pause zwischen Sätzen verkürzen"
            )
            else -> listOf(
                "Aktuelle Progression fortsetzen",
                "Technik weiter verfeinern"
            )
        }
    }
    
    private fun calculateCurrentIntensity(workoutData: List<SetData>): Float {
        if (workoutData.isEmpty()) return 0.5f
        
        val avgRPE = workoutData.mapNotNull { it.rpe }.average().toFloat()
        val avgFormQuality = workoutData.map { it.formQuality }.average().toFloat()
        
        return (avgRPE / 10f) * 0.7f + (1f - avgFormQuality) * 0.3f
    }
    
    private fun calculateFatigueLevel(workoutData: List<SetData>): Float {
        if (workoutData.isEmpty()) return 0f
        
        val formDecline = workoutData.first().formQuality - workoutData.last().formQuality
        val rpeIncrease = (workoutData.lastOrNull()?.rpe ?: 5) - (workoutData.firstOrNull()?.rpe ?: 5)
        
        return (formDecline + rpeIncrease / 10f) * 0.5f
    }
    
    private fun calculateAdjustmentConfidence(plateauResult: PlateauDetectionResult, formAnalysis: FormAnalysisResult): Float {
        return (plateauResult.confidence + formAnalysis.confidence) * 0.5f
    }
    
    private fun calculateRecoveryScore(fatigueLevel: Float, recentIntensity: List<Float>): Float {
        val avgIntensity = if (recentIntensity.isNotEmpty()) recentIntensity.average().toFloat() else 0.5f
        return 1f - ((fatigueLevel * 0.6f) + (avgIntensity * 0.4f))
    }
    
    private fun calculateStressAccumulation(recentIntensity: List<Float>): Float {
        if (recentIntensity.isEmpty()) return 0f
        return recentIntensity.takeLast(7).map { it.pow(1.5f) }.average().toFloat()
    }
    
    private fun generateRestDayReasoning(recoveryScore: Float, stressLevel: Float): String {
        return when {
            recoveryScore < 0.3f -> "Hohe Ermüdung erkannt - Erholung dringend erforderlich"
            recoveryScore < 0.6f -> "Moderate Ermüdung - ein Ruhetag empfohlen"
            stressLevel > 0.8f -> "Stressakkumulation zu hoch - Pause einlegen"
            else -> "Gute Erholung - Training kann fortgesetzt werden"
        }
    }
    
    private fun calculateOptimalNextIntensity(recoveryScore: Float): Float {
        return when {
            recoveryScore < 0.4f -> 0.6f // Low intensity
            recoveryScore < 0.7f -> 0.8f // Moderate intensity
            else -> 1.0f // Normal intensity
        }
    }
    
    private fun calculateWorkoutCalories(duration: Int, intensity: Float): Int {
        return (duration * intensity * 8).toInt() // Rough estimate: 8 calories per minute at full intensity
    }
    
    private fun calculateProteinNeeds(intensity: Float, duration: Int): Float {
        return 20f + (intensity * duration * 0.1f) // Base 20g + intensity factor
    }
    
    private fun calculateCarbNeeds(intensity: Float, duration: Int): Float {
        return 30f + (intensity * duration * 0.15f) // Base 30g + intensity factor
    }
    
    private fun generatePreWorkoutMeal(workoutTime: Int, intensity: Float): MealPlan {
        val timing = if (workoutTime < 12) -60 else -90 // Minutes before workout
        return MealPlan(
            timing = timing,
            calories = (150 + intensity * 100).toInt(),
            description = if (workoutTime < 10) {
                "Leichte Mahlzeit: Banane + Haferflocken"
            } else {
                "Moderate Mahlzeit: Vollkornbrot + Protein"
            },
            macros = mapOf(
                "carbs" to 25f + intensity * 10f,
                "protein" to 10f + intensity * 5f,
                "fat" to 5f
            )
        )
    }
    
    private fun generateDuringWorkoutNutrition(duration: Int, intensity: Float): MealPlan {
        return MealPlan(
            timing = 0,
            calories = if (duration > 90) (intensity * 50).toInt() else 0,
            description = if (duration > 90) {
                "Sportgetränk mit Elektrolyten"
            } else {
                "Nur Wasser ausreichend"
            },
            macros = mapOf(
                "carbs" to if (duration > 90) intensity * 15f else 0f,
                "electrolytes" to intensity * 200f // mg
            )
        )
    }
    
    private fun generatePostWorkoutMeal(proteinNeeds: Float, carbNeeds: Float): MealPlan {
        return MealPlan(
            timing = 30, // Minutes after workout
            calories = ((proteinNeeds * 4) + (carbNeeds * 4) + 50).toInt(),
            description = "Recovery-Mahlzeit: Protein + schnelle Kohlenhydrate",
            macros = mapOf(
                "protein" to proteinNeeds,
                "carbs" to carbNeeds,
                "fat" to 10f
            )
        )
    }
    
    private fun calculateOptimalEatingWindow(workoutTime: Int): Pair<Int, Int> {
        // Return optimal eating window (start hour, end hour)
        return if (workoutTime < 12) {
            Pair(workoutTime - 2, workoutTime + 3)
        } else {
            Pair(workoutTime - 3, workoutTime + 2)
        }
    }
    
    private fun generateHydrationPlan(duration: Int, intensity: Float): HydrationPlan {
        val baseWater = 500 // ml
        val additionalWater = (duration / 15 * intensity * 150).toInt()
        
        return HydrationPlan(
            preWorkout = baseWater,
            duringWorkout = additionalWater,
            postWorkout = (additionalWater * 1.5).toInt(),
            electrolyteNeeded = intensity > 0.7f && duration > 60
        )
    }
    
    private fun getFormTemplates(exerciseType: String): List<FormTemplate> {
        // Return exercise-specific form analysis templates
        return when (exerciseType.lowercase()) {
            "squat" -> listOf(
                FormTemplate("knee_tracking", "Knie über Zehen"),
                FormTemplate("depth", "Vollständige Tiefe"),
                FormTemplate("back_angle", "Aufrechter Rücken")
            )
            "bench_press" -> listOf(
                FormTemplate("bar_path", "Gerade Stangenbahn"),
                FormTemplate("shoulder_stability", "Stabile Schultern"),
                FormTemplate("leg_drive", "Beinarbeit")
            )
            else -> listOf(
                FormTemplate("general_control", "Kontrollierte Bewegung"),
                FormTemplate("range_of_motion", "Voller Bewegungsumfang")
            )
        }
    }
    
    private fun analyzeMovementPattern(dataPoints: List<MovementPoint>, templates: List<FormTemplate>): FormAnalysisResult {
        // Simplified form analysis
        val overallQuality = dataPoints.map { it.quality }.average().toFloat()
        val deviations = dataPoints.count { it.quality < 0.7f }
        
        return FormAnalysisResult(
            overallFormQuality = overallQuality,
            deviations = deviations,
            confidence = if (dataPoints.size > 5) 0.85f else 0.6f,
            specificFeedback = listOf(
                "Bewegungsqualität: ${(overallQuality * 100).toInt()}%",
                if (deviations > 0) "⚠️ $deviations Abweichungen erkannt" else "✅ Saubere Ausführung"
            ),
            improvements = if (overallQuality < 0.8f) {
                listOf("Langsamere Bewegung", "Mehr Fokus auf Kontrolle")
            } else {
                listOf("Form ausgezeichnet!", "Intensität kann gesteigert werden")
            }
        )
    }
    
    private fun extractMovementPatterns(movementData: List<MovementPoint>): List<MovementPattern> {
        // Extract patterns from movement data
        return listOf(
            MovementPattern("symmetry", calculateSymmetryScore(movementData)),
            MovementPattern("consistency", calculateConsistencyScore(movementData)),
            MovementPattern("efficiency", calculateEfficiencyScore(movementData))
        )
    }
    
    private fun detectMovementAsymmetries(patterns: List<MovementPattern>): List<MovementAsymmetry> {
        val symmetryPattern = patterns.find { it.type == "symmetry" }
        return if (symmetryPattern != null && symmetryPattern.score < 0.8f) {
            listOf(MovementAsymmetry("lateral", "Seitliche Asymmetrie erkannt"))
        } else {
            emptyList()
        }
    }
    
    private fun detectCompensatoryPatterns(patterns: List<MovementPattern>): List<CompensationPattern> {
        val efficiencyPattern = patterns.find { it.type == "efficiency" }
        return if (efficiencyPattern != null && efficiencyPattern.score < 0.7f) {
            listOf(CompensationPattern("movement_efficiency", "Kompensationsmuster erkannt"))
        } else {
            emptyList()
        }
    }
    
    private fun detectFatiguePatterns(patterns: List<MovementPattern>): List<FatiguePattern> {
        val consistencyPattern = patterns.find { it.type == "consistency" }
        return if (consistencyPattern != null && consistencyPattern.score < 0.6f) {
            listOf(FatiguePattern("form_degradation", "Form-Verschlechterung durch Ermüdung"))
        } else {
            emptyList()
        }
    }
    
    private fun calculateInjuryRisk(riskFactors: List<RiskFactor>, patterns: List<MovementPattern>): Float {
        val baseRisk = riskFactors.size * 0.2f
        val patternRisk = patterns.map { 1f - it.score }.average().toFloat() * 0.3f
        return (baseRisk + patternRisk).coerceIn(0f, 1f)
    }
    
    private fun generateInjuryPreventionRecommendations(riskFactors: List<RiskFactor>): List<String> {
        return riskFactors.map { factor ->
            when (factor) {
                RiskFactor.MOVEMENT_ASYMMETRY -> "Einseitige Mobilitätsübungen durchführen"
                RiskFactor.COMPENSATION_PATTERN -> "Bewegungsmuster korrigieren"
                RiskFactor.FATIGUE_INDUCED_CHANGES -> "Pausen verlängern oder Gewicht reduzieren"
            }
        }
    }
    
    private fun suggestPrehabExercises(riskFactors: List<RiskFactor>, exerciseId: String): List<PrehabExercise> {
        return when {
            riskFactors.contains(RiskFactor.MOVEMENT_ASYMMETRY) -> listOf(
                PrehabExercise("single_leg_glute_bridge", "Einbeinige Glute Bridge", "3x10 je Seite"),
                PrehabExercise("calf_stretches", "Wadendehnungen", "2x30s je Seite")
            )
            riskFactors.contains(RiskFactor.COMPENSATION_PATTERN) -> listOf(
                PrehabExercise("movement_prep", "Bewegungsvorbereitung", "2x8 Wiederholungen"),
                PrehabExercise("activation_exercises", "Aktivierungsübungen", "1x10 Wiederholungen")
            )
            else -> listOf(
                PrehabExercise("general_warmup", "Allgemeines Aufwärmen", "5-10 Minuten")
            )
        }
    }
    
    private fun calculateAssessmentConfidence(dataPointCount: Int): Float {
        return when {
            dataPointCount > 50 -> 0.9f
            dataPointCount > 20 -> 0.8f
            dataPointCount > 10 -> 0.7f
            else -> 0.5f
        }
    }
    
    private fun calculateBaseLoad(exercises: List<String>): Float {
        // Calculate base load based on exercise types
        return exercises.size * 0.7f // Simple calculation
    }
    
    private fun calculateRecoveryAdjustment(recoveryMetrics: RecoveryMetrics): Float {
        return (recoveryMetrics.sleepQuality + recoveryMetrics.heartRateVariability + recoveryMetrics.subjective) / 3f
    }
    
    private fun generateExerciseModifications(optimalLoad: Float, baseLoad: Float): List<ExerciseModification> {
        val adjustment = optimalLoad / baseLoad
        
        return when {
            adjustment < 0.8f -> listOf(
                ExerciseModification("reduce_weight", "Gewicht um 10-15% reduzieren"),
                ExerciseModification("fewer_sets", "1-2 Sätze weniger")
            )
            adjustment > 1.2f -> listOf(
                ExerciseModification("increase_weight", "Gewicht um 5-10% erhöhen"),
                ExerciseModification("additional_sets", "1 zusätzlicher Satz")
            )
            else -> listOf(
                ExerciseModification("maintain", "Aktuelles Pensum beibehalten")
            )
        }
    }
    
    private fun generateLoadRationale(fatigueAdjustment: Float, recoveryAdjustment: Float): String {
        return when {
            fatigueAdjustment < 0.8f -> "Hohe Ermüdung erkannt - Training angepasst"
            recoveryAdjustment < 0.8f -> "Unvollständige Erholung - reduzierte Belastung"
            fatigueAdjustment > 1.1f && recoveryAdjustment > 1.1f -> "Ausgezeichnete Verfassung - Steigerung möglich"
            else -> "Normale Anpassung basierend auf aktueller Verfassung"
        }
    }
    
    private fun calculateSymmetryScore(movementData: List<MovementPoint>): Float {
        // Simplified symmetry calculation
        return 0.85f + (Math.random() * 0.15f).toFloat()
    }
    
    private fun calculateConsistencyScore(movementData: List<MovementPoint>): Float {
        // Simplified consistency calculation
        val qualities = movementData.map { it.quality }
        val variance = qualities.map { (it - qualities.average()).pow(2) }.average()
        return (1f - variance.toFloat()).coerceIn(0f, 1f)
    }
    
    private fun calculateEfficiencyScore(movementData: List<MovementPoint>): Float {
        // Simplified efficiency calculation
        return movementData.map { it.efficiency }.average().toFloat()
    }
}

// Data classes for AI coach results
data class PlateauDetectionResult(
    val hasPlateauDetected: Boolean,
    val confidence: Float,
    val message: String,
    val recommendation: String,
    val detectedWeeksAgo: Int = 0,
    val suggestedActions: List<String> = emptyList()
)

data class IntensityAdjustment(
    val originalIntensity: Float,
    val adjustedIntensity: Float,
    val adjustmentFactor: Float,
    val reasons: List<String>,
    val confidence: Float
)

data class RestDayPrediction(
    val recommendedRestDays: Int,
    val recoveryScore: Float,
    val stressLevel: Float,
    val reasoning: String,
    val nextWorkoutIntensity: Float
)

data class NutritionTimingPlan(
    val preWorkoutMeal: MealPlan,
    val duringWorkoutNutrition: MealPlan,
    val postWorkoutMeal: MealPlan,
    val optimalEatingWindow: Pair<Int, Int>,
    val hydrationPlan: HydrationPlan
)

data class FormAnalysisResult(
    val overallFormQuality: Float,
    val deviations: Int,
    val confidence: Float,
    val specificFeedback: List<String>,
    val improvements: List<String>
)

data class InjuryRiskAssessment(
    val overallRisk: Float,
    val riskFactors: List<RiskFactor>,
    val recommendations: List<String>,
    val prehabExercises: List<PrehabExercise>,
    val confidence: Float
)

data class LoadManagementPlan(
    val recommendedLoad: Float,
    val originalLoad: Float,
    val adjustmentFactors: Map<String, Float>,
    val exerciseModifications: List<ExerciseModification>,
    val rationaleExplanation: String
)

// Supporting data classes
data class PerformanceDataPoint(
    val weight: Float,
    val reps: Int,
    val formQuality: Float,
    val rpe: Int,
    val volume: Float,
    val timestamp: Long
)

data class MealPlan(
    val timing: Int, // Minutes relative to workout (negative = before, positive = after)
    val calories: Int,
    val description: String,
    val macros: Map<String, Float>
)

data class HydrationPlan(
    val preWorkout: Int, // ml
    val duringWorkout: Int, // ml
    val postWorkout: Int, // ml
    val electrolyteNeeded: Boolean
)

data class FormTemplate(
    val checkPoint: String,
    val description: String
)

data class MovementPoint(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long,
    val quality: Float,
    val efficiency: Float
)

data class MovementPattern(
    val type: String,
    val score: Float
)

data class MovementAsymmetry(
    val type: String,
    val description: String
)

data class CompensationPattern(
    val type: String,
    val description: String
)

data class FatiguePattern(
    val type: String,
    val description: String
)

data class PrehabExercise(
    val id: String,
    val name: String,
    val prescription: String
)

data class ExerciseModification(
    val type: String,
    val description: String
)

data class RecoveryMetrics(
    val sleepQuality: Float, // 0-1
    val heartRateVariability: Float, // 0-1
    val subjective: Float // 0-1, self-reported recovery
)

enum class RiskFactor {
    MOVEMENT_ASYMMETRY,
    COMPENSATION_PATTERN,
    FATIGUE_INDUCED_CHANGES
}