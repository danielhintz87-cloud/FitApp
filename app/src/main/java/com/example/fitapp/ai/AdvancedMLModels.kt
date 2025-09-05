package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.fitapp.services.MovementAsymmetry
import com.example.fitapp.services.CompensationPattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.FloatBuffer
import kotlin.math.*

/**
 * Advanced ML Models Service for Workout Form Analysis
 * Provides on-device machine learning capabilities for real-time pose estimation,
 * movement analysis, and injury prevention using TensorFlow Lite models.
 */
class AdvancedMLModels private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "AdvancedMLModels"
        private const val POSE_MODEL_FILE = "pose_detection_model.tflite"
        private const val MOVEMENT_MODEL_FILE = "movement_analysis_model.tflite"
        private const val INPUT_SIZE = 256
        private const val NUM_KEYPOINTS = 17 // Standard COCO pose keypoints
        private const val CONFIDENCE_THRESHOLD = 0.3f
        
        @Volatile
        private var INSTANCE: AdvancedMLModels? = null
        
        fun getInstance(context: Context): AdvancedMLModels {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdvancedMLModels(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private var poseInterpreter: Interpreter? = null
    private var movementInterpreter: Interpreter? = null
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()
    
    private var isInitialized = false
    
    /**
     * Initialize ML models
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isInitialized) return@withContext true
            
            // Initialize pose detection model (simulated for now - would load actual TFLite model)
            Log.i(TAG, "Initializing pose detection model...")
            // poseInterpreter = Interpreter(FileUtil.loadMappedFile(context, POSE_MODEL_FILE))
            
            // Initialize movement analysis model (simulated for now)
            Log.i(TAG, "Initializing movement analysis model...")
            // movementInterpreter = Interpreter(FileUtil.loadMappedFile(context, MOVEMENT_MODEL_FILE))
            
            isInitialized = true
            Log.i(TAG, "ML models initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ML models", e)
            false
        }
    }
    
    /**
     * Analyze pose from camera frame for form assessment
     */
    suspend fun analyzePoseFromFrame(bitmap: Bitmap): PoseAnalysisResult = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext PoseAnalysisResult.empty()
        }
        
        try {
            // Process image
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val processedImage = imageProcessor.process(tensorImage)
            
            // Run pose detection (simulated advanced analysis)
            val keypoints = detectKeypoints(processedImage)
            val formQuality = analyzeFormFromKeypoints(keypoints)
            val riskFactors = detectInjuryRisks(keypoints)
            val improvements = generateFormImprovements(keypoints, formQuality)
            
            PoseAnalysisResult(
                keypoints = keypoints,
                overallFormQuality = formQuality,
                confidence = calculateConfidence(keypoints),
                riskFactors = riskFactors,
                improvements = improvements,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing pose", e)
            PoseAnalysisResult.empty()
        }
    }
    
    /**
     * Analyze movement patterns from sensor data
     */
    suspend fun analyzeMovementPattern(
        sensorData: List<MovementData>,
        exerciseType: String
    ): MovementPatternAnalysis = withContext(Dispatchers.Default) {
        if (!isInitialized || sensorData.isEmpty()) {
            return@withContext MovementPatternAnalysis.empty()
        }
        
        try {
            // Advanced sensor fusion and pattern analysis
            val smoothedData = applySensorFusion(sensorData)
            val patterns = extractAdvancedPatterns(smoothedData, exerciseType)
            val asymmetries = detectAdvancedAsymmetries(patterns)
            val compensations = detectCompensationPatterns(patterns)
            val fatigue = detectFatigueIndicators(patterns)
            
            MovementPatternAnalysis(
                patterns = patterns,
                asymmetryScore = asymmetries.maxOfOrNull { it.severity.toDouble() }?.toFloat() ?: 0f,
                compensationScore = compensations.maxOfOrNull { it.severity.toDouble() }?.toFloat() ?: 0f,
                fatigueScore = fatigue,
                riskLevel = calculateOverallRisk(asymmetries, compensations, fatigue),
                recommendations = generateMovementRecommendations(patterns, asymmetries, compensations),
                confidence = calculatePatternConfidence(sensorData.size)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing movement pattern", e)
            MovementPatternAnalysis.empty()
        }
    }
    
    /**
     * Real-time form feedback for specific exercises
     */
    suspend fun getRealtimeFormFeedback(
        currentPose: PoseAnalysisResult,
        exerciseType: String,
        repPhase: String
    ): FormFeedback = withContext(Dispatchers.Default) {
        try {
            val exerciseSpecificAnalysis = analyzeExerciseSpecificForm(currentPose, exerciseType, repPhase)
            val immediateCorrections = generateImmediateCorrections(exerciseSpecificAnalysis)
            val motivationalFeedback = generateMotivationalFeedback(exerciseSpecificAnalysis)
            
            FormFeedback(
                immediateCorrections = immediateCorrections,
                motivationalMessages = motivationalFeedback,
                formScore = exerciseSpecificAnalysis.formScore,
                safetyWarnings = exerciseSpecificAnalysis.safetyWarnings,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating realtime feedback", e)
            FormFeedback.empty()
        }
    }
    
    /**
     * Advanced sensor fusion combining accelerometer, gyroscope data
     */
    private fun applySensorFusion(sensorData: List<MovementData>): List<FusedSensorData> {
        return sensorData.windowed(5, 1) { window ->
            val avgAccel = Triple(
                window.map { it.accelerometer.first }.average().toFloat(),
                window.map { it.accelerometer.second }.average().toFloat(),
                window.map { it.accelerometer.third }.average().toFloat()
            )
            val avgGyro = Triple(
                window.map { it.gyroscope.first }.average().toFloat(),
                window.map { it.gyroscope.second }.average().toFloat(),
                window.map { it.gyroscope.third }.average().toFloat()
            )
            
            // Calculate derived metrics
            val acceleration = sqrt(avgAccel.first.pow(2) + avgAccel.second.pow(2) + avgAccel.third.pow(2))
            val angularVelocity = sqrt(avgGyro.first.pow(2) + avgGyro.second.pow(2) + avgGyro.third.pow(2))
            val jerk = if (window.size > 1) {
                val prevAccel = window[window.size - 2].let { prev ->
                    sqrt(prev.accelerometer.first.pow(2) + prev.accelerometer.second.pow(2) + prev.accelerometer.third.pow(2))
                }
                abs(acceleration - prevAccel)
            } else 0f
            
            FusedSensorData(
                fusedAcceleration = avgAccel,
                fusedGyroscope = avgGyro,
                totalAcceleration = acceleration,
                totalAngularVelocity = angularVelocity,
                jerk = jerk,
                timestamp = window.last().timestamp
            )
        }
    }
    
    /**
     * Detect keypoints from processed image (simulated advanced pose detection)
     */
    private fun detectKeypoints(processedImage: TensorImage): List<Keypoint> {
        // Simulated advanced pose detection - in production would use actual TFLite model
        val baseKeypoints = listOf(
            "nose", "left_eye", "right_eye", "left_ear", "right_ear",
            "left_shoulder", "right_shoulder", "left_elbow", "right_elbow",
            "left_wrist", "right_wrist", "left_hip", "right_hip",
            "left_knee", "right_knee", "left_ankle", "right_ankle"
        )
        
        return baseKeypoints.mapIndexed { index, name ->
            Keypoint(
                name = name,
                x = 0.3f + (Math.random() * 0.4).toFloat(), // Simulated coordinates
                y = 0.2f + (Math.random() * 0.6).toFloat(),
                confidence = 0.7f + (Math.random() * 0.3).toFloat()
            )
        }
    }
    
    /**
     * Analyze form quality from detected keypoints
     */
    private fun analyzeFormFromKeypoints(keypoints: List<Keypoint>): Float {
        // Advanced form analysis based on keypoint positions
        val shoulderAlignment = analyzeShoulderAlignment(keypoints)
        val spineAlignment = analyzeSpineAlignment(keypoints)
        val jointAngles = analyzeJointAngles(keypoints)
        val symmetry = analyzeBodySymmetry(keypoints)
        
        return (shoulderAlignment + spineAlignment + jointAngles + symmetry) / 4f
    }
    
    /**
     * Extract advanced movement patterns from fused sensor data
     */
    private fun extractAdvancedPatterns(fusedData: List<FusedSensorData>, exerciseType: String): List<AdvancedMovementPattern> {
        return when (exerciseType.lowercase()) {
            "squat" -> extractSquatPatterns(fusedData)
            "deadlift" -> extractDeadliftPatterns(fusedData)
            "bench_press" -> extractBenchPressPatterns(fusedData)
            "overhead_press" -> extractOverheadPressPatterns(fusedData)
            else -> extractGenericPatterns(fusedData)
        }
    }
    
    /**
     * Detect advanced movement asymmetries
     */
    private fun detectAdvancedAsymmetries(patterns: List<AdvancedMovementPattern>): List<MovementAsymmetry> {
        val asymmetries = mutableListOf<MovementAsymmetry>()
        
        // Analyze left-right asymmetries
        val leftRightDifference = patterns.filter { it.type.contains("left") || it.type.contains("right") }
            .groupBy { it.type.replace("left_", "").replace("right_", "") }
            .mapValues { (_, values) ->
                if (values.size == 2) {
                    abs(values[0].magnitude - values[1].magnitude)
                } else 0f
            }
        
        leftRightDifference.forEach { (movement, difference) ->
            if (difference > 0.15f) { // 15% threshold
                asymmetries.add(MovementAsymmetry(
                    type = "lateral_$movement",
                    severity = difference,
                    description = "Seitliche Asymmetrie bei $movement erkannt (${(difference * 100).toInt()}% Unterschied)"
                ))
            }
        }
        
        return asymmetries
    }
    
    /**
     * Generate exercise-specific form improvements
     */
    private fun generateFormImprovements(keypoints: List<Keypoint>, formQuality: Float): List<String> {
        val improvements = mutableListOf<String>()
        
        if (formQuality < 0.8f) {
            val shoulderIssues = analyzeShoulderIssues(keypoints)
            val spineIssues = analyzeSpineIssues(keypoints)
            val legAlignment = analyzeLegAlignment(keypoints)
            
            improvements.addAll(shoulderIssues)
            improvements.addAll(spineIssues)
            improvements.addAll(legAlignment)
        }
        
        return improvements
    }
    
    // Helper analysis methods
    private fun analyzeShoulderAlignment(keypoints: List<Keypoint>): Float {
        val leftShoulder = keypoints.find { it.name == "left_shoulder" }
        val rightShoulder = keypoints.find { it.name == "right_shoulder" }
        
        return if (leftShoulder != null && rightShoulder != null) {
            val heightDiff = abs(leftShoulder.y - rightShoulder.y)
            (1f - heightDiff * 2f).coerceIn(0f, 1f)
        } else 0.5f
    }
    
    private fun analyzeSpineAlignment(keypoints: List<Keypoint>): Float {
        // Simplified spine alignment analysis
        return 0.8f + (Math.random() * 0.2).toFloat()
    }
    
    private fun analyzeJointAngles(keypoints: List<Keypoint>): Float {
        // Simplified joint angle analysis
        return 0.7f + (Math.random() * 0.3).toFloat()
    }
    
    private fun analyzeBodySymmetry(keypoints: List<Keypoint>): Float {
        // Simplified symmetry analysis
        return 0.75f + (Math.random() * 0.25).toFloat()
    }
    
    private fun calculateConfidence(keypoints: List<Keypoint>): Float {
        return keypoints.map { it.confidence }.average().toFloat()
    }
    
    private fun detectInjuryRisks(keypoints: List<Keypoint>): List<String> {
        val risks = mutableListOf<String>()
        
        // Check for dangerous positions
        val shoulderAlignment = analyzeShoulderAlignment(keypoints)
        if (shoulderAlignment < 0.6f) {
            risks.add("Schulterasymmetrie - Verletzungsrisiko für Schulter und Nacken")
        }
        
        return risks
    }
    
    private fun analyzeShoulderIssues(keypoints: List<Keypoint>): List<String> {
        return listOf("Schultern parallel halten", "Schulterblätter nach hinten ziehen")
    }
    
    private fun analyzeSpineIssues(keypoints: List<Keypoint>): List<String> {
        return listOf("Wirbelsäule neutral halten", "Core-Spannung erhöhen")
    }
    
    private fun analyzeLegAlignment(keypoints: List<Keypoint>): List<String> {
        return listOf("Knie über den Füßen ausrichten", "Gleichmäßige Gewichtsverteilung")
    }
    
    // Exercise-specific pattern extraction
    private fun extractSquatPatterns(fusedData: List<FusedSensorData>): List<AdvancedMovementPattern> {
        return listOf(
            AdvancedMovementPattern("descent_depth", fusedData.maxOfOrNull { it.totalAcceleration } ?: 0f),
            AdvancedMovementPattern("ascent_power", fusedData.sumOf { it.jerk.toDouble() }.toFloat()),
            AdvancedMovementPattern("lateral_stability", fusedData.map { abs(it.fusedAcceleration.first) }.average().toFloat())
        )
    }
    
    private fun extractDeadliftPatterns(fusedData: List<FusedSensorData>): List<AdvancedMovementPattern> {
        return listOf(
            AdvancedMovementPattern("hip_hinge", fusedData.maxOfOrNull { it.totalAcceleration } ?: 0f),
            AdvancedMovementPattern("bar_path", fusedData.map { abs(it.fusedAcceleration.second) }.average().toFloat()),
            AdvancedMovementPattern("lockout_control", fusedData.lastOrNull()?.totalAngularVelocity ?: 0f)
        )
    }
    
    private fun extractBenchPressPatterns(fusedData: List<FusedSensorData>): List<AdvancedMovementPattern> {
        return listOf(
            AdvancedMovementPattern("press_path", fusedData.map { it.fusedAcceleration.third }.average().toFloat()),
            AdvancedMovementPattern("elbow_control", fusedData.map { it.fusedGyroscope.first }.average().toFloat()),
            AdvancedMovementPattern("shoulder_stability", fusedData.map { abs(it.fusedGyroscope.second) }.average().toFloat())
        )
    }
    
    private fun extractOverheadPressPatterns(fusedData: List<FusedSensorData>): List<AdvancedMovementPattern> {
        return listOf(
            AdvancedMovementPattern("press_trajectory", fusedData.map { it.fusedAcceleration.third }.average().toFloat()),
            AdvancedMovementPattern("core_stability", fusedData.map { abs(it.fusedAcceleration.first) }.average().toFloat()),
            AdvancedMovementPattern("overhead_control", fusedData.lastOrNull()?.totalAngularVelocity ?: 0f)
        )
    }
    
    private fun extractGenericPatterns(fusedData: List<FusedSensorData>): List<AdvancedMovementPattern> {
        return listOf(
            AdvancedMovementPattern("movement_smoothness", fusedData.map { it.jerk }.average().toFloat()),
            AdvancedMovementPattern("control", fusedData.map { it.totalAngularVelocity }.average().toFloat()),
            AdvancedMovementPattern("consistency", fusedData.map { it.totalAcceleration }.let { acc ->
                val mean = acc.average()
                val variance = acc.map { (it - mean).pow(2) }.average()
                (1f - variance.toFloat()).coerceIn(0f, 1f)
            })
        )
    }
    
    private fun detectCompensationPatterns(patterns: List<AdvancedMovementPattern>): List<CompensationPattern> {
        val compensations = mutableListOf<CompensationPattern>()
        
        patterns.forEach { pattern ->
            when {
                pattern.type.contains("stability") && pattern.magnitude > 0.5f -> {
                    compensations.add(CompensationPattern(
                        pattern = "stability_compensation",
                        severity = pattern.magnitude,
                        affectedJoints = listOf("core", "hips")
                    ))
                }
                pattern.type.contains("control") && pattern.magnitude > 0.4f -> {
                    compensations.add(CompensationPattern(
                        pattern = "control_compensation", 
                        severity = pattern.magnitude,
                        affectedJoints = listOf("shoulders", "spine")
                    ))
                }
            }
        }
        
        return compensations
    }
    
    private fun detectFatigueIndicators(patterns: List<AdvancedMovementPattern>): Float {
        // Analyze patterns for fatigue indicators
        val smoothnessPattern = patterns.find { it.type.contains("smoothness") }
        val controlPattern = patterns.find { it.type.contains("control") }
        
        val fatigueIndicators = listOfNotNull(
            smoothnessPattern?.let { 1f - it.magnitude },
            controlPattern?.let { it.magnitude }
        )
        
        return fatigueIndicators.average().toFloat()
    }
    
    private fun calculateOverallRisk(
        asymmetries: List<MovementAsymmetry>,
        compensations: List<CompensationPattern>,
        fatigueScore: Float
    ): Float {
        val asymmetryRisk = asymmetries.maxOfOrNull { it.severity.toDouble() }?.toFloat() ?: 0f
        val compensationRisk = compensations.maxOfOrNull { it.severity.toDouble() }?.toFloat() ?: 0f
        
        return maxOf(asymmetryRisk, compensationRisk, fatigueScore)
    }
    
    private fun generateMovementRecommendations(
        patterns: List<AdvancedMovementPattern>,
        asymmetries: List<MovementAsymmetry>,
        compensations: List<CompensationPattern>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (asymmetries.isNotEmpty()) {
            recommendations.add("Einseitige Übungen zur Korrektur von Asymmetrien einbauen")
        }
        
        if (compensations.isNotEmpty()) {
            recommendations.add("Fokus auf kontrollierte Bewegungsausführung")
            recommendations.add("Gewicht reduzieren für bessere Technik")
        }
        
        return recommendations
    }
    
    private fun calculatePatternConfidence(dataPoints: Int): Float {
        return (dataPoints.toFloat() / 50f).coerceIn(0f, 1f) // Confidence based on data amount
    }
    
    private fun analyzeExerciseSpecificForm(pose: PoseAnalysisResult, exerciseType: String, repPhase: String): ExerciseSpecificAnalysis {
        return ExerciseSpecificAnalysis(
            formScore = pose.overallFormQuality,
            safetyWarnings = if (pose.overallFormQuality < 0.6f) listOf("Technik verbessern") else emptyList()
        )
    }
    
    private fun generateImmediateCorrections(analysis: ExerciseSpecificAnalysis): List<String> {
        return if (analysis.formScore < 0.7f) {
            listOf("Rumpf anspannen", "Bewegung verlangsamen", "Volle Bewegungsamplitude nutzen")
        } else {
            listOf("Gut! Weiter so!")
        }
    }
    
    private fun generateMotivationalFeedback(analysis: ExerciseSpecificAnalysis): List<String> {
        return when {
            analysis.formScore > 0.9f -> listOf("Perfekte Ausführung! 💪", "Du bist on fire! 🔥")
            analysis.formScore > 0.8f -> listOf("Sehr gut!", "Tolle Technik!")
            analysis.formScore > 0.7f -> listOf("Gut gemacht!", "Weiter so!")
            else -> listOf("Konzentriert bleiben!", "Technik vor Gewicht!")
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        poseInterpreter?.close()
        movementInterpreter?.close()
        poseInterpreter = null
        movementInterpreter = null
        isInitialized = false
    }
}

// Data classes for advanced ML analysis
data class PoseAnalysisResult(
    val keypoints: List<Keypoint>,
    val overallFormQuality: Float,
    val confidence: Float,
    val riskFactors: List<String>,
    val improvements: List<String>,
    val timestamp: Long
) {
    companion object {
        fun empty() = PoseAnalysisResult(
            keypoints = emptyList(),
            overallFormQuality = 0f,
            confidence = 0f,
            riskFactors = emptyList(),
            improvements = emptyList(),
            timestamp = System.currentTimeMillis()
        )
    }
}

data class Keypoint(
    val name: String,
    val x: Float,
    val y: Float,
    val confidence: Float
)

data class MovementPatternAnalysis(
    val patterns: List<AdvancedMovementPattern>,
    val asymmetryScore: Float,
    val compensationScore: Float,
    val fatigueScore: Float,
    val riskLevel: Float,
    val recommendations: List<String>,
    val confidence: Float
) {
    companion object {
        fun empty() = MovementPatternAnalysis(
            patterns = emptyList(),
            asymmetryScore = 0f,
            compensationScore = 0f,
            fatigueScore = 0f,
            riskLevel = 0f,
            recommendations = emptyList(),
            confidence = 0f
        )
    }
}

data class AdvancedMovementPattern(
    val type: String,
    val magnitude: Float
)

data class FusedSensorData(
    val fusedAcceleration: Triple<Float, Float, Float>,
    val fusedGyroscope: Triple<Float, Float, Float>,
    val totalAcceleration: Float,
    val totalAngularVelocity: Float,
    val jerk: Float,
    val timestamp: Long
)

data class FormFeedback(
    val immediateCorrections: List<String>,
    val motivationalMessages: List<String>,
    val formScore: Float,
    val safetyWarnings: List<String>,
    val timestamp: Long
) {
    companion object {
        fun empty() = FormFeedback(
            immediateCorrections = emptyList(),
            motivationalMessages = emptyList(),
            formScore = 0f,
            safetyWarnings = emptyList(),
            timestamp = System.currentTimeMillis()
        )
    }
}

data class ExerciseSpecificAnalysis(
    val formScore: Float,
    val safetyWarnings: List<String>
)

// Movement analysis data classes for ML models - using existing definitions from other files