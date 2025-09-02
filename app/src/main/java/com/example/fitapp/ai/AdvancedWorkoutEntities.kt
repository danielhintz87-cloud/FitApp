package com.example.fitapp.ai

import androidx.compose.ui.graphics.Color

/**
 * Advanced Workout Execution Enhancement - Phase 1
 * Enhanced data models for real-time performance monitoring and AI-powered workout guidance
 */

// Heart Rate Monitoring
enum class HeartRateZone(val range: IntRange, val color: Color, val displayName: String) {
    RESTING(50..100, Color(0xFF2196F3), "Ruhezone"),
    FAT_BURN(100..140, Color(0xFF4CAF50), "Fettverbrennung"),
    CARDIO(140..170, Color(0xFFFF9800), "Cardio"),
    PEAK(170..200, Color(0xFFF44336), "Höchstleistung")
}

data class HeartRateReading(
    val bpm: Int,
    val timestamp: Long,
    val zone: HeartRateZone,
    val confidence: Float
)

// Session Metrics
data class SessionMetrics(
    val totalVolume: Float = 0f,
    val averageHeartRate: Int? = null,
    val workoutEfficiency: Float = 0f,
    val caloriesBurned: Int = 0,
    val personalRecords: Int = 0,
    val exercisesCompleted: Int = 0,
    val totalExercises: Int = 0
) {
    val completionPercentage: Float
        get() = if (totalExercises > 0) (exercisesCompleted.toFloat() / totalExercises) * 100f else 0f
}

// AI Coaching
enum class CoachingTipType {
    FORM_IMPROVEMENT,
    REST_OPTIMIZATION,
    PROGRESSION_SUGGESTION,
    MOTIVATION,
    SAFETY_WARNING,
    TECHNIQUE_TIP
}

enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class CoachingTip(
    val type: CoachingTipType,
    val message: String,
    val priority: Priority,
    val actionable: Boolean = false,
    val action: String? = null,
    val timestamp: Long = System.currentTimeMillis() / 1000
)

// Movement Analysis
data class MovementData(
    val accelerometer: Triple<Float, Float, Float>,
    val gyroscope: Triple<Float, Float, Float>,
    val timestamp: Long
)

data class RepetitionAnalysis(
    val repDetected: Boolean,
    val repQuality: Float,
    val rangeOfMotion: Float,
    val speed: Float,
    val symmetry: Float
)

data class FormQualityAssessment(
    val overallQuality: Float, // 0.0-1.0
    val improvements: List<String>,
    val riskFactors: List<String>,
    val positiveAspects: List<String>
)

// Workout Progression
data class ProgressionSuggestion(
    val exerciseId: String,
    val exerciseName: String,
    val currentWeight: Float,
    val recommendedWeight: Float,
    val currentReps: Int,
    val recommendedReps: Int,
    val reason: String,
    val confidence: Float,
    val alternatives: List<ProgressionAlternative>
)

data class ProgressionAlternative(
    val type: String, // "weight_increase", "rep_increase", "tempo_change", "range_change"
    val description: String,
    val weight: Float?,
    val reps: Int?,
    val difficulty: String // "easier", "same", "harder"
)

// Performance Predictions
data class PerformancePrediction(
    val expectedVolume: Float,
    val expectedDuration: Int, // in minutes
    val fatigueForecast: String, // "low", "medium", "high"
    val recommendedRestAdjustment: Int, // seconds to add/subtract from rest
    val confidence: Float
)

// Enhanced Training State
data class AdvancedTrainingUiState(
    // Existing basic state
    val plan: com.example.fitapp.data.db.PlanEntity? = null,
    val exercises: List<com.example.fitapp.ui.screens.ExerciseStep> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val isInTraining: Boolean = false,
    val completedExercises: Set<Int> = emptySet(),
    val isResting: Boolean = false,
    val restTimeRemaining: Int = 0,
    val guidedMode: Boolean = false,
    
    // Advanced Performance State
    val currentHeartRate: Int? = null,
    val heartRateZone: HeartRateZone? = null,
    val repCount: Int = 0,
    val isAutoCountingReps: Boolean = false,
    val formQuality: Float = 1.0f,
    val currentRPE: Int? = null, // Rate of Perceived Exertion 1-10
    
    // Session State
    val sessionId: String? = null,
    val sessionMetrics: SessionMetrics = SessionMetrics(),
    val progressionSuggestions: List<ProgressionSuggestion> = emptyList(),
    
    // AI State
    val isLoadingAIInsights: Boolean = false,
    val aiCoachingTips: List<CoachingTip> = emptyList(),
    val performancePrediction: PerformancePrediction? = null,
    
    // Sensor State
    val sensorDataAvailable: Boolean = false,
    val heartRateMonitorConnected: Boolean = false,
    val movementTrackingActive: Boolean = false,
    
    // Error State
    val error: String? = null
)

// Workout Context for AI Analysis
data class WorkoutContext(
    val currentExercise: com.example.fitapp.ui.screens.ExerciseStep?,
    val exerciseHistory: List<com.example.fitapp.data.db.WorkoutPerformanceEntity>,
    val sessionProgress: Float,
    val userFatigueLevel: String,
    val environmentalFactors: Map<String, Any> = emptyMap()
)

// User Profile for Workout Customization
data class UserWorkoutProfile(
    val userId: String,
    val fitnessLevel: String, // "beginner", "intermediate", "advanced"
    val primaryGoals: List<String>, // "strength", "endurance", "weight_loss", "muscle_gain"
    val maxHeartRate: Int,
    val restingHeartRate: Int,
    val preferredIntensity: String, // "low", "moderate", "high"
    val availableEquipment: List<String>,
    val injuries: List<String> = emptyList(),
    val preferences: Map<String, Any> = emptyMap()
)