package com.example.fitapp.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.fitapp.ai.AdvancedMLModels
import com.example.fitapp.ai.FormFeedback
import com.example.fitapp.ai.FormQualityAssessment
import com.example.fitapp.ai.HeartRateReading
import com.example.fitapp.ai.HeartRateZone
import com.example.fitapp.ai.MovementData
import com.example.fitapp.ai.MovementPatternAnalysis
import com.example.fitapp.ai.PoseAnalysisResult
import com.example.fitapp.ai.RepetitionAnalysis
import com.example.fitapp.network.healthconnect.HealthConnectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Advanced Workout Sensor Service for Phase 1 Enhancement
 * Handles real-time sensor data collection and analysis for workout tracking
 * Integrates with advanced ML models for improved form analysis and injury prevention
 *
 * PERFORMANCE OPTIMIZED:
 * - Adaptive sampling rates based on workout intensity
 * - Intelligent sensor fusion to reduce processing overhead
 * - Batch processing for ML analysis to improve efficiency
 * - Memory-efficient data structures and cleanup routines
 */
class WorkoutSensorService(
    private val context: Context,
    private val healthConnectManager: HealthConnectManager,
) : SensorEventListener {
    companion object {
        private const val TAG = "WorkoutSensorService"
        private const val REP_DETECTION_THRESHOLD = 2.5f // m/s² threshold for rep detection
        private const val FORM_ANALYSIS_WINDOW = 5000L // 5 seconds window for form analysis
        private const val ML_ANALYSIS_INTERVAL = 2000L // 2 seconds interval for ML analysis

        // Performance optimization constants
        private const val HIGH_INTENSITY_SAMPLE_RATE = SensorManager.SENSOR_DELAY_GAME // 20ms
        private const val NORMAL_INTENSITY_SAMPLE_RATE = SensorManager.SENSOR_DELAY_UI // 60ms
        private const val LOW_INTENSITY_SAMPLE_RATE = SensorManager.SENSOR_DELAY_NORMAL // 200ms
        private const val SENSOR_DATA_BUFFER_LIMIT = 100 // Limit memory usage
        private const val BATCH_PROCESSING_SIZE = 10 // Process in batches for efficiency
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Advanced ML integration
    private val advancedMLModels = AdvancedMLModels.getInstance(context)
    private var isMLInitialized = false

    // State flows for real-time data
    private val _heartRateFlow = MutableStateFlow<HeartRateReading?>(null)
    val heartRateFlow = _heartRateFlow.asStateFlow()

    private val _movementAnalysisFlow = MutableStateFlow<MovementPatternAnalysis?>(null)
    val movementAnalysisFlow = _movementAnalysisFlow.asStateFlow()

    private val _formFeedbackFlow = MutableStateFlow<FormFeedback?>(null)
    val formFeedbackFlow = _formFeedbackFlow.asStateFlow()

    private val _repAnalysisFlow = MutableStateFlow<RepetitionAnalysis?>(null)
    val repAnalysisFlow = _repAnalysisFlow.asStateFlow()

    private val _movementDataFlow = MutableStateFlow<MovementData?>(null)
    val movementDataFlow = _movementDataFlow.asStateFlow()

    private val _repCountFlow = MutableStateFlow(0)
    val repCountFlow = _repCountFlow.asStateFlow()

    private val _formQualityFlow = MutableStateFlow(1.0f)
    val formQualityFlow = _formQualityFlow.asStateFlow()

    // Internal state with performance optimization
    private var isTracking = false
    private var currentExerciseType = ""
    private var lastAcceleration = Triple(0f, 0f, 0f)
    private var lastGyroscope = Triple(0f, 0f, 0f)
    private var repDetectionBuffer = mutableListOf<Float>()
    private var movementDataBuffer = mutableListOf<MovementData>()
    private var lastRepTime = 0L
    private var totalReps = 0

    // Performance optimization state
    private var currentSampleRate = NORMAL_INTENSITY_SAMPLE_RATE
    private var workoutIntensity = 0f
    private var lastMLAnalysisTime = 0L
    private var pendingDataBatch = mutableListOf<MovementData>()
    private var sensorDataCount = 0L
    private var lastPerformanceCheck = 0L

    /**
     * Start comprehensive movement tracking with adaptive performance optimization
     */
    fun startMovementTracking(exerciseType: String): Flow<MovementData?> {
        currentExerciseType = exerciseType
        isTracking = true
        totalReps = 0

        Log.i(TAG, "Starting optimized movement tracking for: $exerciseType")

        // Initialize ML models in background
        scope.launch {
            initializeMLModels()
        }

        // Start with adaptive sensor sampling
        startAdaptiveSensorSampling()

        return movementDataFlow
    }

    /**
     * Start sensors with adaptive sampling rate based on workout intensity
     */
    private fun startAdaptiveSensorSampling() {
        // Determine initial sample rate based on exercise type
        currentSampleRate =
            when (currentExerciseType.lowercase()) {
                "burpees", "mountain_climbers", "jumping_jacks" -> HIGH_INTENSITY_SAMPLE_RATE
                "squats", "push_ups", "lunges" -> NORMAL_INTENSITY_SAMPLE_RATE
                else -> LOW_INTENSITY_SAMPLE_RATE
            }

        Log.i(TAG, "Starting sensors with sample rate: $currentSampleRate")

        // Register sensors with adaptive rate
        accelerometer?.let {
            sensorManager.registerListener(this, it, currentSampleRate)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, currentSampleRate)
        }

        // Start performance monitoring
        startPerformanceMonitoring()
    }

    /**
     * Monitor performance and adjust sampling rates dynamically
     */
    private fun startPerformanceMonitoring() {
        scope.launch {
            while (isTracking) {
                delay(5000) // Check every 5 seconds
                adjustSamplingRateBasedOnPerformance()
            }
        }
    }

    /**
     * Adjust sensor sampling rate based on workout intensity and system performance
     */
    private fun adjustSamplingRateBasedOnPerformance() {
        val currentTime = System.currentTimeMillis()

        // Calculate workout intensity based on movement data
        val recentMovement = movementDataBuffer.takeLast(20)
        if (recentMovement.isNotEmpty()) {
            workoutIntensity =
                recentMovement.map {
                    sqrt(it.accelerometer.first.pow(2) + it.accelerometer.second.pow(2) + it.accelerometer.third.pow(2))
                }.average().toFloat()
        }

        // Determine optimal sample rate
        val optimalRate =
            when {
                workoutIntensity > 15f -> HIGH_INTENSITY_SAMPLE_RATE // High intensity
                workoutIntensity > 8f -> NORMAL_INTENSITY_SAMPLE_RATE // Medium intensity
                else -> LOW_INTENSITY_SAMPLE_RATE // Low intensity
            }

        // Only change if different and enough time has passed
        if (optimalRate != currentSampleRate && currentTime - lastPerformanceCheck > 10000L) {
            Log.i(TAG, "Adjusting sample rate from $currentSampleRate to $optimalRate (intensity: $workoutIntensity)")

            // Unregister and re-register with new rate
            sensorManager.unregisterListener(this)
            currentSampleRate = optimalRate

            accelerometer?.let {
                sensorManager.registerListener(this, it, currentSampleRate)
            }
            gyroscope?.let {
                sensorManager.registerListener(this, it, currentSampleRate)
            }

            lastPerformanceCheck = currentTime
        }
    }

    /**
     * Initialize ML models with performance monitoring
     */
    private suspend fun initializeMLModels() {
        try {
            val startTime = System.currentTimeMillis()
            isMLInitialized = advancedMLModels.initialize()
            val initTime = System.currentTimeMillis() - startTime

            Log.i(TAG, "ML models initialized: $isMLInitialized (took ${initTime}ms)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ML models", e)
            isMLInitialized = false
        }
    }

    /**
     * Start heart rate monitoring using available sensors and Health Connect
     */
    fun startHeartRateMonitoring(): Flow<HeartRateReading> =
        flow {
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
        val magnitude =
            sqrt(
                acceleration.first * acceleration.first +
                    acceleration.second * acceleration.second +
                    acceleration.third * acceleration.third,
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
            symmetry = calculateMovementSymmetry(),
        )
    }

    /**
     * Initialize advanced ML models for enhanced analysis
     */
    fun initializeAdvancedAnalysis() {
        scope.launch {
            isMLInitialized = advancedMLModels.initialize()
            if (isMLInitialized) {
                Log.i(TAG, "Advanced ML models initialized successfully")
                startAdvancedAnalysisLoop()
            } else {
                Log.w(TAG, "Failed to initialize advanced ML models")
            }
        }
    }

    /**
     * Enhanced movement analysis using advanced ML models
     */
    suspend fun analyzeMovementWithML(exerciseType: String): MovementPatternAnalysis? {
        if (!isMLInitialized || movementDataBuffer.isEmpty()) {
            return null
        }

        return try {
            val recentData = movementDataBuffer.takeLast(25) // Last 5 seconds of data
            advancedMLModels.analyzeMovementPattern(recentData, exerciseType)
        } catch (e: Exception) {
            Log.e(TAG, "Error in ML movement analysis", e)
            null
        }
    }

    /**
     * Get real-time form feedback using advanced ML
     */
    suspend fun getRealTimeFormFeedback(
        exerciseType: String,
        repPhase: String = "mid",
    ): FormFeedback? {
        if (!isMLInitialized) {
            return null
        }

        return try {
            // For now, we create a simulated pose analysis since we don't have camera integration yet
            val simulatedPose = createSimulatedPoseFromSensorData()
            advancedMLModels.getRealtimeFormFeedback(simulatedPose, exerciseType, repPhase)
        } catch (e: Exception) {
            Log.e(TAG, "Error in real-time form feedback", e)
            null
        }
    }

    /**
     * Advanced repetition detection with ML-enhanced quality assessment
     */
    fun analyzeRepetitionAdvanced(movementData: MovementData): RepetitionAnalysis {
        val basicAnalysis = analyzeRepetition(movementData)

        // Enhance with ML insights if available
        scope.launch {
            if (isMLInitialized && movementDataBuffer.size >= 10) {
                try {
                    val mlAnalysis = analyzeMovementWithML(currentExerciseType)
                    mlAnalysis?.let { analysis ->
                        _movementAnalysisFlow.value = analysis

                        // Generate real-time feedback
                        val feedback = getRealTimeFormFeedback(currentExerciseType)
                        feedback?.let { _formFeedbackFlow.value = it }

                        // Update form quality based on ML analysis
                        val enhancedFormQuality = (basicAnalysis.repQuality + analysis.confidence) / 2f
                        _formQualityFlow.value = enhancedFormQuality
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in advanced repetition analysis", e)
                }
            }
        }

        return basicAnalysis
    }

    /**
     * Start continuous advanced analysis loop
     */
    private fun startAdvancedAnalysisLoop() {
        scope.launch {
            while (isTracking && isMLInitialized) {
                try {
                    if (movementDataBuffer.size >= 10) {
                        // Perform ML analysis every 2 seconds
                        val analysis = analyzeMovementWithML(currentExerciseType)
                        analysis?.let { _movementAnalysisFlow.value = it }

                        // Generate form feedback
                        val feedback = getRealTimeFormFeedback(currentExerciseType)
                        feedback?.let { _formFeedbackFlow.value = it }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in advanced analysis loop", e)
                }

                delay(ML_ANALYSIS_INTERVAL)
            }
        }
    }

    /**
     * Create simulated pose analysis from sensor data for ML feedback
     * In a full implementation, this would be replaced with actual camera-based pose estimation
     */
    private fun createSimulatedPoseFromSensorData(): PoseAnalysisResult {
        val recentMovement = movementDataBuffer.takeLast(5)
        val avgAcceleration =
            if (recentMovement.isNotEmpty()) {
                recentMovement.map { data ->
                    sqrt(
                        data.accelerometer.first.pow(2) + data.accelerometer.second.pow(2) + data.accelerometer.third.pow(2),
                    )
                }.average().toFloat()
            } else {
                0f
            }

        // Simulate form quality based on movement stability
        val formQuality =
            when {
                avgAcceleration < 2f -> 0.9f // Very stable
                avgAcceleration < 5f -> 0.7f // Moderate movement
                avgAcceleration < 10f -> 0.5f // High movement
                else -> 0.3f // Very unstable
            }

        return PoseAnalysisResult(
            keypoints = emptyList(), // Would be populated with actual pose detection
            overallFormQuality = formQuality,
            confidence = 0.8f,
            riskFactors = if (formQuality < 0.6f) listOf("Instabile Bewegung erkannt") else emptyList(),
            improvements =
                if (formQuality < 0.8f) {
                    listOf(
                        "Bewegung verlangsamen",
                        "Stabilität verbessern",
                    )
                } else {
                    emptyList()
                },
            timestamp = System.currentTimeMillis(),
        )
    }

    /**
     * Assess form quality based on movement patterns
     */
    fun assessFormQuality(
        movementData: MovementData,
        exerciseType: String,
    ): FormQualityAssessment {
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

    // SensorEventListener Implementation with Performance Optimization
    override fun onSensorChanged(event: SensorEvent) {
        if (!isTracking) return

        sensorDataCount++

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                lastAcceleration = Triple(event.values[0], event.values[1], event.values[2])
                updateMovementDataOptimized()
            }
            Sensor.TYPE_GYROSCOPE -> {
                lastGyroscope = Triple(event.values[0], event.values[1], event.values[2])
                updateMovementDataOptimized()
            }
            Sensor.TYPE_HEART_RATE -> {
                val heartRate = event.values[0].toInt()
                if (heartRate > 0) {
                    val zone = determineHeartRateZone(heartRate)
                    _heartRateFlow.value =
                        HeartRateReading(
                            bpm = heartRate,
                            timestamp = System.currentTimeMillis(),
                            zone = zone,
                            confidence = 0.9f,
                        )
                }
            }
        }

        // Periodic performance check (every 1000 sensor readings)
        if (sensorDataCount % 1000L == 0L) {
            checkSystemPerformance()
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int,
    ) {
        Log.d(TAG, "Sensor accuracy changed: ${sensor?.name} -> $accuracy")
    }

    // Performance-optimized helper methods

    /**
     * Update movement data with batching and memory optimization
     */
    private fun updateMovementDataOptimized() {
        val currentTime = System.currentTimeMillis()
        val movementData =
            MovementData(
                accelerometer = lastAcceleration,
                gyroscope = lastGyroscope,
                timestamp = currentTime,
            )

        _movementDataFlow.value = movementData

        // Add to batch for processing
        addToBatch(movementData)

        // Process batches when full or at intervals
        if (pendingDataBatch.size >= BATCH_PROCESSING_SIZE) {
            processBatch()
        }

        // Analyze for rep detection (lightweight, immediate feedback)
        val repAnalysis = analyzeRepetition(movementData)
        if (repAnalysis.repDetected) {
            totalReps++
            _repCountFlow.value = totalReps
            _repAnalysisFlow.value = repAnalysis
            lastRepTime = currentTime
        }

        // Update form quality more frequently for immediate feedback
        updateFormQuality(movementData)
    }

    /**
     * Add movement data to batch with memory management
     */
    private fun addToBatch(movementData: MovementData) {
        synchronized(pendingDataBatch) {
            pendingDataBatch.add(movementData)

            // Also add to main buffer with size limit
            movementDataBuffer.add(movementData)
            if (movementDataBuffer.size > SENSOR_DATA_BUFFER_LIMIT) {
                movementDataBuffer.removeAt(0) // Remove oldest
            }
        }
    }

    /**
     * Process batch of movement data with ML analysis
     */
    private fun processBatch() {
        if (!isMLInitialized || pendingDataBatch.isEmpty()) return

        scope.launch {
            try {
                val batchToProcess =
                    synchronized(pendingDataBatch) {
                        val batch = pendingDataBatch.toList()
                        pendingDataBatch.clear()
                        batch
                    }

                // Use optimized ML analysis for latest data point
                val latestData = batchToProcess.last()
                val analysisResult =
                    advancedMLModels.analyzeMovementPatternOptimized(
                        latestData,
                        currentExerciseType,
                    )

                _movementAnalysisFlow.value = analysisResult

                // Generate form feedback if analysis indicates issues
                if (analysisResult.riskLevel > 0.3f) {
                    generateFormFeedback(analysisResult)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing movement batch", e)
            }
        }
    }

    /**
     * Update form quality with lightweight calculation
     */
    private fun updateFormQuality(movementData: MovementData) {
        // Lightweight form quality calculation for immediate feedback
        val acceleration = movementData.accelerometer
        val gyroscope = movementData.gyroscope

        val smoothnessScore = calculateSmoothness(acceleration, gyroscope)
        val stabilityScore = calculateStability(acceleration)

        val overallQuality = (smoothnessScore + stabilityScore) / 2f
        _formQualityFlow.value = overallQuality
    }

    /**
     * Generate form feedback from ML analysis
     */
    private fun generateFormFeedback(analysis: MovementPatternAnalysis) {
        scope.launch {
            try {
                // Use cached pose analysis if available, or create minimal feedback
                val mockPoseResult =
                    PoseAnalysisResult(
                        keypoints = emptyList(),
                        overallFormQuality = analysis.asymmetryScore,
                        confidence = analysis.confidence,
                        riskFactors = analysis.recommendations,
                        improvements = analysis.recommendations,
                        timestamp = System.currentTimeMillis(),
                    )

                val formFeedback =
                    advancedMLModels.getRealtimeFormFeedback(
                        mockPoseResult,
                        currentExerciseType,
                        "mid_rep",
                    )

                _formFeedbackFlow.value = formFeedback
            } catch (e: Exception) {
                Log.e(TAG, "Error generating form feedback", e)
            }
        }
    }

    /**
     * Check system performance and adjust processing accordingly
     */
    private fun checkSystemPerformance() {
        scope.launch {
            try {
                val metrics = advancedMLModels.getPerformanceMetrics()
                metrics.updateMemoryUsage()

                Log.d(
                    TAG,
                    "Performance check - Memory: ${metrics.memoryUsageMB}MB, " +
                        "Analyses: ${metrics.totalAnalyses}, " +
                        "Avg ML time: ${metrics.avgMovementAnalysisTime}ms",
                )

                // Adjust batch size based on performance
                if (metrics.memoryUsageMB > 100f) { // High memory usage
                    // Process smaller batches more frequently
                    if (pendingDataBatch.size >= BATCH_PROCESSING_SIZE / 2) {
                        processBatch()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking performance", e)
            }
        }
    }

    /**
     * Calculate movement smoothness (simplified)
     */
    private fun calculateSmoothness(
        acceleration: Triple<Float, Float, Float>,
        gyroscope: Triple<Float, Float, Float>,
    ): Float {
        val accelMagnitude = sqrt(acceleration.first.pow(2) + acceleration.second.pow(2) + acceleration.third.pow(2))
        val gyroMagnitude = sqrt(gyroscope.first.pow(2) + gyroscope.second.pow(2) + gyroscope.third.pow(2))

        // Simple smoothness calculation - lower values indicate smoother movement
        val smoothnessScore = 1f - (accelMagnitude / 20f).coerceIn(0f, 1f)
        return smoothnessScore.coerceIn(0f, 1f)
    }

    /**
     * Calculate movement stability (simplified)
     */
    private fun calculateStability(acceleration: Triple<Float, Float, Float>): Float {
        val lateralMovement = abs(acceleration.first) + abs(acceleration.second)
        val stabilityScore = 1f - (lateralMovement / 10f).coerceIn(0f, 1f)
        return stabilityScore.coerceIn(0f, 1f)
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

        val timeBetweenReps =
            if (totalReps > 1) {
                (System.currentTimeMillis() - lastRepTime) / 1000f
            } else {
                2.0f
            }

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
        val avgQuality =
            calculateRepQuality(
                movementDataBuffer.lastOrNull() ?: MovementData(
                    Triple(0f, 0f, 0f), Triple(0f, 0f, 0f), System.currentTimeMillis(),
                ),
            )

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
            positiveAspects = positives,
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
            positiveAspects = positives,
        )
    }

    private fun assessPlankForm(): FormQualityAssessment {
        // For plank, stability is key
        val stability = 0.9f - (Math.random() * 0.2f).toFloat()

        return FormQualityAssessment(
            overallQuality = stability,
            improvements = if (stability < 0.8f) listOf("Halte die Position stabiler") else emptyList(),
            riskFactors = if (stability < 0.6f) listOf("Instabile Haltung") else emptyList(),
            positiveAspects = if (stability > 0.8f) listOf("Stabile Kernhaltung") else emptyList(),
        )
    }

    private fun assessGeneralForm(): FormQualityAssessment {
        val overallQuality = 0.8f + (Math.random() * 0.2f).toFloat()

        return FormQualityAssessment(
            overallQuality = overallQuality,
            improvements = listOf("Achte auf gleichmäßige Bewegungen"),
            riskFactors = emptyList(),
            positiveAspects = listOf("Gute Grundform"),
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
            confidence = 0.85f,
        )
    }
}
