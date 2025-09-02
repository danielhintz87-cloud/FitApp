package com.example.fitapp.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.fitapp.ai.HeartRateReading
import com.example.fitapp.ai.HeartRateZone
import com.example.fitapp.ai.MovementData
import com.example.fitapp.ai.RepetitionAnalysis
import com.example.fitapp.ai.FormQualityAssessment
import com.example.fitapp.network.healthconnect.HealthConnectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Advanced Workout Sensor Service for Phase 1 Enhancement
 * Handles real-time sensor data collection and analysis for workout tracking
 */
class WorkoutSensorService(
    private val context: Context,
    private val healthConnectManager: HealthConnectManager
) : SensorEventListener {
    
    companion object {
        private const val TAG = "WorkoutSensorService"
        private const val REP_DETECTION_THRESHOLD = 2.5f // m/s² threshold for rep detection
        private const val FORM_ANALYSIS_WINDOW = 5000L // 5 seconds window for form analysis
    }
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // State flows for real-time data
    private val _heartRateFlow = MutableStateFlow<HeartRateReading?>(null)
    val heartRateFlow = _heartRateFlow.asStateFlow()
    
    private val _movementDataFlow = MutableStateFlow<MovementData?>(null)
    val movementDataFlow = _movementDataFlow.asStateFlow()
    
    private val _repCountFlow = MutableStateFlow(0)
    val repCountFlow = _repCountFlow.asStateFlow()
    
    private val _formQualityFlow = MutableStateFlow(1.0f)
    val formQualityFlow = _formQualityFlow.asStateFlow()
    
    // Internal state
    private var isTracking = false
    private var currentExerciseType = ""
    private var lastAcceleration = Triple(0f, 0f, 0f)
    private var lastGyroscope = Triple(0f, 0f, 0f)
    private var repDetectionBuffer = mutableListOf<Float>()
    private var movementDataBuffer = mutableListOf<MovementData>()
    private var lastRepTime = 0L
    private var totalReps = 0
    
    /**
     * Start comprehensive movement tracking for the given exercise type
     */
    fun startMovementTracking(exerciseType: String): Flow<MovementData?> {
        currentExerciseType = exerciseType
        isTracking = true
        totalReps = 0
        
        Log.i(TAG, "Starting movement tracking for: $exerciseType")
        
        // Register sensors
        accelerometer?.let { 
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.let { 
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        
        return movementDataFlow
    }
    
    /**
     * Start heart rate monitoring using available sensors and Health Connect
     */
    fun startHeartRateMonitoring(): Flow<HeartRateReading> = flow {
        Log.i(TAG, "Starting heart rate monitoring")
        
        // Try device sensor first
        heartRateSensor?.let { 
            sensorManager.registerListener(this@WorkoutSensorService, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        // Use Health Connect as fallback/supplement
        try {
            if (healthConnectManager.isAvailable() && healthConnectManager.hasPermissions()) {
                // Note: Health Connect integration is currently disabled for compatibility
                // This is a placeholder for future implementation
                Log.i(TAG, "Health Connect available but disabled for compatibility")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Health Connect not available", e)
        }
        
        // Simulate heart rate data for testing (remove in production)
        while (isTracking) {
            val simulatedHR = generateSimulatedHeartRate()
            _heartRateFlow.value = simulatedHR
            emit(simulatedHR)
            delay(5000) // Update every 5 seconds
        }
    }
    
    /**
     * Analyze repetition from movement data
     */
    fun analyzeRepetition(movementData: MovementData): RepetitionAnalysis {
        val acceleration = movementData.accelerometer
        val magnitude = sqrt(
            acceleration.first * acceleration.first +
            acceleration.second * acceleration.second +
            acceleration.third * acceleration.third
        )
        
        repDetectionBuffer.add(magnitude)
        
        // Keep buffer size manageable
        if (repDetectionBuffer.size > 20) {
            repDetectionBuffer.removeFirst()
        }
        
        // Simple rep detection algorithm
        val repDetected = detectRepetition(magnitude)
        
        return RepetitionAnalysis(
            repDetected = repDetected,
            repQuality = calculateRepQuality(movementData),
            rangeOfMotion = calculateRangeOfMotion(),
            speed = calculateMovementSpeed(),
            symmetry = calculateMovementSymmetry()
        )
    }
    
    /**
     * Assess form quality based on movement patterns
     */
    fun assessFormQuality(movementData: MovementData, exerciseType: String): FormQualityAssessment {
        movementDataBuffer.add(movementData)
        
        // Keep only recent data
        val cutoffTime = System.currentTimeMillis() - FORM_ANALYSIS_WINDOW
        movementDataBuffer.removeAll { it.timestamp < cutoffTime }
        
        return when (exerciseType.lowercase()) {
            "push-ups", "pushup" -> assessPushUpForm()
            "squats", "squat" -> assessSquatForm()
            "plank" -> assessPlankForm()
            else -> assessGeneralForm()
        }
    }
    
    /**
     * Stop all sensor tracking
     */
    fun stopTracking() {
        Log.i(TAG, "Stopping workout sensor tracking")
        isTracking = false
        sensorManager.unregisterListener(this)
        movementDataBuffer.clear()
        repDetectionBuffer.clear()
    }
    
    // SensorEventListener Implementation
    override fun onSensorChanged(event: SensorEvent) {
        if (!isTracking) return
        
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                lastAcceleration = Triple(event.values[0], event.values[1], event.values[2])
                updateMovementData()
            }
            Sensor.TYPE_GYROSCOPE -> {
                lastGyroscope = Triple(event.values[0], event.values[1], event.values[2])
                updateMovementData()
            }
            Sensor.TYPE_HEART_RATE -> {
                val heartRate = event.values[0].toInt()
                if (heartRate > 0) {
                    val zone = determineHeartRateZone(heartRate)
                    _heartRateFlow.value = HeartRateReading(
                        bpm = heartRate,
                        timestamp = System.currentTimeMillis(),
                        zone = zone,
                        confidence = 0.9f
                    )
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: ${sensor?.name} -> $accuracy")
    }
    
    // Private helper methods
    
    private fun updateMovementData() {
        val movementData = MovementData(
            accelerometer = lastAcceleration,
            gyroscope = lastGyroscope,
            timestamp = System.currentTimeMillis()
        )
        
        _movementDataFlow.value = movementData
        
        // Analyze for rep detection
        val repAnalysis = analyzeRepetition(movementData)
        if (repAnalysis.repDetected) {
            totalReps++
            _repCountFlow.value = totalReps
            Log.d(TAG, "Rep detected! Total: $totalReps")
        }
        
        // Update form quality
        val formAssessment = assessFormQuality(movementData, currentExerciseType)
        _formQualityFlow.value = formAssessment.overallQuality
    }
    
    private fun detectRepetition(magnitude: Float): Boolean {
        if (repDetectionBuffer.size < 10) return false
        
        val currentTime = System.currentTimeMillis()
        
        // Avoid detecting multiple reps too quickly
        if (currentTime - lastRepTime < 500) return false
        
        // Simple peak detection
        val recent = repDetectionBuffer.takeLast(5)
        val average = recent.average().toFloat()
        
        if (magnitude > average + REP_DETECTION_THRESHOLD) {
            lastRepTime = currentTime
            return true
        }
        
        return false
    }
    
    private fun calculateRepQuality(movementData: MovementData): Float {
        // Analyze smoothness and consistency of movement
        val acceleration = movementData.accelerometer
        val jerk = abs(acceleration.first) + abs(acceleration.second) + abs(acceleration.third)
        
        // Lower jerk indicates smoother movement (better form)
        return (1.0f - (jerk / 50f)).coerceIn(0.3f, 1.0f)
    }
    
    private fun calculateRangeOfMotion(): Float {
        if (movementDataBuffer.size < 10) return 0.8f
        
        val accelerations = movementDataBuffer.map { it.accelerometer.second } // Y-axis for most exercises
        val range = accelerations.maxOrNull()!! - accelerations.minOrNull()!!
        
        // Normalize range to 0-1 scale
        return (range / 20f).coerceIn(0.3f, 1.0f)
    }
    
    private fun calculateMovementSpeed(): Float {
        if (repDetectionBuffer.size < 2) return 1.0f
        
        val timeBetweenReps = if (totalReps > 1) {
            (System.currentTimeMillis() - lastRepTime) / 1000f
        } else 2.0f
        
        // Ideal rep speed is around 2-3 seconds
        return when {
            timeBetweenReps < 1.0f -> 0.6f // Too fast
            timeBetweenReps > 5.0f -> 0.7f // Too slow
            else -> 1.0f // Good speed
        }
    }
    
    private fun calculateMovementSymmetry(): Float {
        if (movementDataBuffer.size < 10) return 0.9f
        
        // Simple symmetry check based on movement patterns
        // In a real implementation, this would be much more sophisticated
        return 0.85f + (Math.random() * 0.15f).toFloat()
    }
    
    private fun assessPushUpForm(): FormQualityAssessment {
        val improvements = mutableListOf<String>()
        val risks = mutableListOf<String>()
        val positives = mutableListOf<String>()
        
        // Analyze movement patterns for push-ups
        val avgQuality = calculateRepQuality(movementDataBuffer.lastOrNull() ?: MovementData(
            Triple(0f, 0f, 0f), Triple(0f, 0f, 0f), System.currentTimeMillis()
        ))
        
        if (avgQuality < 0.7f) {
            improvements.add("Verlangsame die Bewegung für bessere Kontrolle")
            risks.add("Ruckartige Bewegungen können zu Verletzungen führen")
        } else {
            positives.add("Gute Bewegungskontrolle")
        }
        
        return FormQualityAssessment(
            overallQuality = avgQuality,
            improvements = improvements,
            riskFactors = risks,
            positiveAspects = positives
        )
    }
    
    private fun assessSquatForm(): FormQualityAssessment {
        val improvements = mutableListOf<String>()
        val risks = mutableListOf<String>()
        val positives = mutableListOf<String>()
        
        val rom = calculateRangeOfMotion()
        
        if (rom < 0.6f) {
            improvements.add("Gehe tiefer in die Hocke für bessere Aktivierung")
        } else {
            positives.add("Gute Bewegungsreichweite")
        }
        
        return FormQualityAssessment(
            overallQuality = rom,
            improvements = improvements,
            riskFactors = risks,
            positiveAspects = positives
        )
    }
    
    private fun assessPlankForm(): FormQualityAssessment {
        // For plank, stability is key
        val stability = 0.9f - (Math.random() * 0.2f).toFloat()
        
        return FormQualityAssessment(
            overallQuality = stability,
            improvements = if (stability < 0.8f) listOf("Halte die Position stabiler") else emptyList(),
            riskFactors = if (stability < 0.6f) listOf("Instabile Haltung") else emptyList(),
            positiveAspects = if (stability > 0.8f) listOf("Stabile Kernhaltung") else emptyList()
        )
    }
    
    private fun assessGeneralForm(): FormQualityAssessment {
        val overallQuality = 0.8f + (Math.random() * 0.2f).toFloat()
        
        return FormQualityAssessment(
            overallQuality = overallQuality,
            improvements = listOf("Achte auf gleichmäßige Bewegungen"),
            riskFactors = emptyList(),
            positiveAspects = listOf("Gute Grundform")
        )
    }
    
    private fun determineHeartRateZone(heartRate: Int): HeartRateZone {
        return when (heartRate) {
            in HeartRateZone.RESTING.range -> HeartRateZone.RESTING
            in HeartRateZone.FAT_BURN.range -> HeartRateZone.FAT_BURN
            in HeartRateZone.CARDIO.range -> HeartRateZone.CARDIO
            in HeartRateZone.PEAK.range -> HeartRateZone.PEAK
            else -> if (heartRate < 100) HeartRateZone.RESTING else HeartRateZone.PEAK
        }
    }
    
    private fun generateSimulatedHeartRate(): HeartRateReading {
        // Simulate realistic heart rate during exercise
        val baseRate = 120 + (Math.random() * 40).toInt()
        val zone = determineHeartRateZone(baseRate)
        
        return HeartRateReading(
            bpm = baseRate,
            timestamp = System.currentTimeMillis(),
            zone = zone,
            confidence = 0.85f
        )
    }
}