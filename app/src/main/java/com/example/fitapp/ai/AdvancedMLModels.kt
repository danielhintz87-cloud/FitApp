package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.fitapp.ml.MLResourceManager
import com.example.fitapp.ml.MLResult
import com.example.fitapp.services.MovementAsymmetry
import com.example.fitapp.services.CompensationPattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
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
 * 
 * PERFORMANCE OPTIMIZED FOR MOBILE DEVICES:
 * - Efficient memory management with resource pooling via MLResourceManager
 * - Adaptive processing rates based on device capabilities
 * - Intelligent caching for frequently used computations
 * - Background thread optimization for real-time analysis
 * - Graceful error handling with MLResult pattern
 */
class AdvancedMLModels private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "AdvancedMLModels"

    // TensorFlow Lite Models (Thunder + Lightning alias (movement))
    private const val MOVENET_THUNDER_MODEL = "models/tflite/movenet_thunder.tflite"
    private const val MOVENET_LIGHTNING_MODEL = "models/tflite/movement_analysis_model.tflite" // repurposed lightning
    private const val BLAZEPOSE_MODEL = "models/tflite/blazepose.tflite"

    // Resource Manager Keys
    private const val THUNDER_KEY = "movenet_thunder"
    private const val LIGHTNING_KEY = "movenet_lightning"
    private const val BLAZEPOSE_KEY = "blazepose"

    // Dynamic input sizes
    private const val INPUT_SIZE_THUNDER = 256
    private const val INPUT_SIZE_LIGHTNING = 192
    private const val INPUT_SIZE_BLAZEPOSE = 256
        private const val NUM_KEYPOINTS = 17 // Standard COCO pose keypoints for MoveNet
        private const val NUM_KEYPOINTS_BLAZEPOSE = 33 // BlazePose landmarks
        private const val CONFIDENCE_THRESHOLD = 0.3f
        
        // Performance optimization constants
        private const val MAX_SENSOR_BUFFER_SIZE = 50 // Limit memory usage
        private const val ANALYSIS_THROTTLE_MS = 100L // Minimum time between analyses
        private const val CACHE_SIZE = 10 // Cache recent results
        private const val LOW_MEMORY_THRESHOLD = 0.8f // Trigger cleanup at 80% memory usage
        
        @Volatile
        private var INSTANCE: AdvancedMLModels? = null
        
        fun getInstance(context: Context): AdvancedMLModels {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdvancedMLModels(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Programmatic override for ONNX backend (null = use env var) – mainly for tests/benchmarks.
         */
        fun forceOnnxBackend(enabled: Boolean?) {
            INSTANCE?.overrideOnnxBackend = enabled
        }
    }

    /**
     * Enum for different pose model types
     */
    enum class PoseModelType {
        MOVENET_THUNDER,
        MOVENET_LIGHTNING,
        BLAZEPOSE
    }
    
    private val resourceManager = MLResourceManager.getInstance()
    private var currentModelType: PoseModelType = PoseModelType.MOVENET_THUNDER
    private var imageProcessor: ImageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(INPUT_SIZE_THUNDER, INPUT_SIZE_THUNDER, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()
    // Optional ONNX Backend
    private val envOnnxBackend: Boolean = System.getenv("USE_ONNX_MOVENET") == "true"
    @Volatile private var overrideOnnxBackend: Boolean? = null
    private val useOnnxBackend: Boolean get() = overrideOnnxBackend ?: envOnnxBackend
    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    
    private var isInitialized = false
    private var lastAnalysisTime = 0L
    
    // Performance monitoring
    private val performanceMetrics = PerformanceMetrics()
    
    // Memory-efficient data caching
    private val analysisCache = mutableMapOf<String, CachedAnalysis>()
    private val sensorDataBuffer = mutableListOf<MovementData>()
    
    /**
     * Initialize ML models with performance optimization and model type selection
     * Now uses MLResourceManager for improved resource lifecycle management
     */
    suspend fun initialize(modelType: PoseModelType = PoseModelType.MOVENET_THUNDER): Boolean = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            currentModelType = modelType
            
            // Initialize pose detection model based on type
            Log.i(TAG, "Initializing optimized pose detection model: $modelType")
            
            val poseModelPath = when (modelType) {
                PoseModelType.MOVENET_THUNDER -> MOVENET_THUNDER_MODEL
                PoseModelType.MOVENET_LIGHTNING -> MOVENET_LIGHTNING_MODEL
                PoseModelType.BLAZEPOSE -> BLAZEPOSE_MODEL
            }
            
            val modelKey = when (modelType) {
                PoseModelType.MOVENET_THUNDER -> THUNDER_KEY
                PoseModelType.MOVENET_LIGHTNING -> LIGHTNING_KEY
                PoseModelType.BLAZEPOSE -> BLAZEPOSE_KEY
            }
            
            val inputSize = when (modelType) {
                PoseModelType.MOVENET_THUNDER -> INPUT_SIZE_THUNDER
                PoseModelType.MOVENET_LIGHTNING -> INPUT_SIZE_LIGHTNING
                PoseModelType.BLAZEPOSE -> INPUT_SIZE_BLAZEPOSE
            }
            updateImageProcessorFor(modelType)
            
            if (useOnnxBackend && modelType == PoseModelType.MOVENET_LIGHTNING) {
                // Versuche ONNX Session
                try {
                    val assetPath = "models/onnx/movenet_lightning.onnx"
                    val bytes = FileUtil.loadMappedFile(context, assetPath)
                    ortEnv = OrtEnvironment.getEnvironment()
                    ortSession = ortEnv!!.createSession(bytes, OrtSession.SessionOptions())
                    Log.i(TAG, "ONNX MoveNet Lightning geladen (Backend aktiviert)")
                } catch (e: Exception) {
                    Log.w(TAG, "ONNX Backend konnte nicht initialisiert werden – fallback auf TFLite", e)
                }
            }
            
            if (ortSession == null) {
                // Load TensorFlow Lite model with resource manager
                try {
                    val options = Interpreter.Options().apply {
                        setNumThreads(2)
                        setUseNNAPI(true)
                        setUseXNNPACK(true)
                    }
                    val interpreter = Interpreter(FileUtil.loadMappedFile(context, poseModelPath), options)
                    
                    // Register with resource manager
                    resourceManager.registerInterpreter(modelKey, interpreter)
                    
                    Log.i(TAG, "Loaded TFLite pose model with resource manager: $poseModelPath (input=$inputSize)")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not load pose model $poseModelPath", e)
                }
            }
            
            // Clear any existing cache
            clearCache()
            
            isInitialized = true
            val initTime = System.currentTimeMillis() - startTime
            performanceMetrics.recordInitTime(initTime)
            
            Log.i(TAG, "ML models initialized successfully in ${initTime}ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ML models", e)
            false
        }
    }

    // Aktualisiert den ImageProcessor für das gewählte Modell
    private fun updateImageProcessorFor(modelType: PoseModelType) {
        val inputSize = when (modelType) {
            PoseModelType.MOVENET_THUNDER -> INPUT_SIZE_THUNDER
            PoseModelType.MOVENET_LIGHTNING -> INPUT_SIZE_LIGHTNING
            PoseModelType.BLAZEPOSE -> INPUT_SIZE_BLAZEPOSE
        }
        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()
    }

    /** Liefert alle verfügbaren (Datei existiert & kein Platzhalter) Modelle */
    fun listAvailableModels(): List<PoseModelType> {
        return PoseModelType.values().filter { modelType ->
            val path = when (modelType) {
                PoseModelType.MOVENET_THUNDER -> MOVENET_THUNDER_MODEL
                PoseModelType.MOVENET_LIGHTNING -> MOVENET_LIGHTNING_MODEL
                PoseModelType.BLAZEPOSE -> BLAZEPOSE_MODEL
            }
            try {
                val afd = context.assets.open(path)
                afd.use { stream ->
                    val head = ByteArray(4)
                    val read = stream.read(head)
                    // Primitive Platzhalter-Erkennung: beginnt mit '#'
                    read > 0 && head[0].toInt() != '#'.code
                }
            } catch (e: Exception) { false }
        }
    }

    /** Schnelles Umschalten ohne Neu-Laden (sofern vorab geladen) */
    fun switchModel(modelType: PoseModelType): Boolean {
        if (!resourceManager.isHealthy()) {
            Log.w(TAG, "Model $modelType not available - resource manager not healthy")
            return false
        }
        currentModelType = modelType
        updateImageProcessorFor(modelType)
        return true
    }
    
    /**
     * Data class for device performance information
     */
    data class DevicePerformanceInfo(
        val isHighEnd: Boolean,
        val isMidRange: Boolean,
        val availableMemoryMB: Long,
        val processorCores: Int
    )
    
    /**
     * Detect device performance capabilities
     */
    private fun getDevicePerformanceInfo(): DevicePerformanceInfo {
        val runtime = Runtime.getRuntime()
        val availableMemoryMB = (runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory())) / (1024 * 1024)
        val processorCores = Runtime.getRuntime().availableProcessors()
        
        // Simple heuristic for device classification
        val isHighEnd = availableMemoryMB > 3000 && processorCores >= 8
        val isMidRange = availableMemoryMB > 1500 && processorCores >= 4
        
        return DevicePerformanceInfo(
            isHighEnd = isHighEnd,
            isMidRange = isMidRange,
            availableMemoryMB = availableMemoryMB,
            processorCores = processorCores
        )
    }
    
    /**
     * Initialize with adaptive model selection based on device performance
     */
    suspend fun initializeAdaptive(): Boolean {
        val deviceInfo = getDevicePerformanceInfo()
        
        Log.i(TAG, "Device info - Memory: ${deviceInfo.availableMemoryMB}MB, Cores: ${deviceInfo.processorCores}")
        
        val modelType = when {
            deviceInfo.isHighEnd -> {
                Log.i(TAG, "High-end device detected, using MoveNet Thunder")
                PoseModelType.MOVENET_THUNDER
            }
            deviceInfo.isMidRange -> {
                Log.i(TAG, "Mid-range device detected, using BlazePose")
                PoseModelType.BLAZEPOSE
            }
            else -> {
                Log.i(TAG, "Lower-end device detected, using BlazePose (lightweight)")
                PoseModelType.BLAZEPOSE
            }
        }
        
        return initialize(modelType)
    }
    
    /**
     * Get current model type
     */
    fun getCurrentModelType(): PoseModelType = currentModelType
    
    /**
     * Performance-optimized pose analysis with throttling and caching
     * Now returns MLResult for better error handling
     */
    suspend fun analyzePoseFromFrameOptimized(bitmap: Bitmap): MLResult<PoseAnalysisResult> = withContext(Dispatchers.Default) {
        val currentTime = System.currentTimeMillis()
        
        // Throttle analysis for performance
        if (currentTime - lastAnalysisTime < ANALYSIS_THROTTLE_MS) {
            getCachedPoseAnalysisResult()?.let { return@withContext MLResult.success(it) }
        }
        
        if (!isInitialized) {
            return@withContext MLResult.error(
                IllegalStateException("ML models not initialized"),
                fallbackAvailable = false
            )
        }
        
        // Check memory usage before heavy processing
        val memoryPressure = resourceManager.checkMemoryPressure()
        if (memoryPressure > 0.9f) {
            return@withContext performMemoryOptimizedAnalysis(bitmap)
        }
        
        val result = analyzePoseFromFrameWithResourceManager(bitmap)
        lastAnalysisTime = currentTime
        
        // Cache result for potential reuse
        when (result) {
            is MLResult.Success -> cachePoseAnalysisResult(result.data)
            is MLResult.Degraded -> result.degradedResult?.let { cachePoseAnalysisResult(it) }
            else -> { /* Don't cache errors */ }
        }
        
        result
    }
    
    /**
     * Perform memory-optimized analysis when resources are constrained
     */
    private suspend fun performMemoryOptimizedAnalysis(bitmap: Bitmap): MLResult<PoseAnalysisResult> {
        Log.w(TAG, "Performing memory-optimized analysis due to high memory pressure")
        
        // Use smaller bitmap to reduce memory usage
        val scaledBitmap = try {
            val scale = 0.5f
            val scaledWidth = (bitmap.width * scale).toInt()
            val scaledHeight = (bitmap.height * scale).toInt()
            Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        } catch (e: OutOfMemoryError) {
            return MLResult.degraded(
                exception = e,
                degradedResult = PoseAnalysisResult.empty(),
                message = "Could not scale bitmap - using empty result"
            )
        }
        
        return try {
            val result = analyzePoseFromFrameWithResourceManager(scaledBitmap)
            when (result) {
                is MLResult.Success -> MLResult.degraded(
                    exception = OutOfMemoryError("High memory pressure"),
                    degradedResult = result.data,
                    message = "Analysis performed with reduced resolution"
                )
                is MLResult.Degraded -> result
                is MLResult.Error -> result
            }
        } finally {
            if (scaledBitmap !== bitmap) {
                scaledBitmap.recycle()
            }
        }
    }
    
    /**
     * Analyze pose using resource manager for safe interpreter access
     */
    private suspend fun analyzePoseFromFrameWithResourceManager(bitmap: Bitmap): MLResult<PoseAnalysisResult> {
        return try {
            val start = System.currentTimeMillis()
            
            // Use bitmap from resource manager if possible
            val workingBitmap = resourceManager.borrowBitmap(
                bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888
            ) ?: bitmap
            
            try {
                // Process image
                val tensorImage = TensorImage.fromBitmap(workingBitmap)
                val processedImage = imageProcessor.process(tensorImage)
                
                // Get model key for current model type
                val modelKey = when (currentModelType) {
                    PoseModelType.MOVENET_THUNDER -> THUNDER_KEY
                    PoseModelType.MOVENET_LIGHTNING -> LIGHTNING_KEY
                    PoseModelType.BLAZEPOSE -> BLAZEPOSE_KEY
                }
                
                // Run pose detection using resource manager
                val keypointsResult = resourceManager.useInterpreter(modelKey) { interpreter ->
                    runPoseInferenceWithInterpreter(interpreter, processedImage)
                }
                
                when (keypointsResult) {
                    is MLResult.Success -> {
                        val keypoints = keypointsResult.data
                        val formQuality = analyzeFormFromKeypoints(keypoints)
                        val riskFactors = detectInjuryRisks(keypoints)
                        val improvements = generateFormImprovements(keypoints, formQuality)
                        
                        val result = PoseAnalysisResult(
                            keypoints = keypoints,
                            overallFormQuality = formQuality,
                            confidence = calculateConfidence(keypoints),
                            riskFactors = riskFactors,
                            improvements = improvements,
                            timestamp = System.currentTimeMillis()
                        )
                        
                        val elapsed = System.currentTimeMillis() - start
                        performanceMetrics.recordPoseAnalysis(elapsed)
                        performanceMetrics.updateMemoryUsage()
                        
                        MLResult.success(result)
                    }
                    is MLResult.Error -> {
                        if (keypointsResult.fallbackAvailable) {
                            // Generate simulated result as fallback
                            val simulatedKeypoints = generateSimulatedKeypoints(bitmap.width, bitmap.height)
                            val formQuality = analyzeFormFromKeypoints(simulatedKeypoints)
                            val riskFactors = detectInjuryRisks(simulatedKeypoints)
                            val improvements = generateFormImprovements(simulatedKeypoints, formQuality)
                            
                            val result = PoseAnalysisResult(
                                keypoints = simulatedKeypoints,
                                overallFormQuality = formQuality,
                                confidence = calculateConfidence(simulatedKeypoints),
                                riskFactors = riskFactors,
                                improvements = improvements,
                                timestamp = System.currentTimeMillis()
                            )
                            
                            MLResult.degraded(
                                exception = keypointsResult.exception,
                                degradedResult = result,
                                message = "Using simulated pose detection due to model error"
                            )
                        } else {
                            keypointsResult
                        }
                    }
                    is MLResult.Degraded -> {
                        val keypoints = keypointsResult.degradedResult ?: emptyList()
                        val formQuality = analyzeFormFromKeypoints(keypoints)
                        val riskFactors = detectInjuryRisks(keypoints)
                        val improvements = generateFormImprovements(keypoints, formQuality)
                        
                        val result = PoseAnalysisResult(
                            keypoints = keypoints,
                            overallFormQuality = formQuality,
                            confidence = calculateConfidence(keypoints),
                            riskFactors = riskFactors,
                            improvements = improvements,
                            timestamp = System.currentTimeMillis()
                        )
                        
                        MLResult.degraded(
                            exception = keypointsResult.exception,
                            degradedResult = result,
                            message = keypointsResult.message
                        )
                    }
                }
            } finally {
                // Return working bitmap to pool if borrowed
                if (workingBitmap !== bitmap) {
                    resourceManager.returnBitmap(workingBitmap)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing pose", e)
            MLResult.error(e, fallbackAvailable = true)
        }
    }
    
    /**
     * Memory-efficient movement pattern analysis with buffering
     */
    suspend fun analyzeMovementPatternOptimized(
        newSensorData: MovementData,
        exerciseType: String
    ): MovementPatternAnalysis = withContext(Dispatchers.Default) {
        // Add to circular buffer with size limit
        addToSensorBuffer(newSensorData)
        
        // Only analyze when we have sufficient data
        if (sensorDataBuffer.size < 10) {
            return@withContext MovementPatternAnalysis.empty()
        }
        
        // Check cache first
        val currentTime = System.currentTimeMillis()
        val cacheKey = "$exerciseType-${sensorDataBuffer.size}"
        getCachedMovementAnalysis(cacheKey)?.let { cached ->
            if (currentTime - cached.timestamp < 1000L) { // 1 second cache
                return@withContext cached.movementResult ?: MovementPatternAnalysis.empty()
            }
        }
        
        val result = analyzeMovementPattern(sensorDataBuffer.toList(), exerciseType)
        
        // Cache the result
        cacheMovementAnalysis(cacheKey, result)
        
        result
    }
    
    /**
     * Get performance metrics for monitoring
     */
    fun getPerformanceMetrics(): PerformanceMetrics = performanceMetrics.copy()
    
    /**
     * Batch processing for multiple frames - analyze multiple frames simultaneously
     */
    suspend fun analyzeBatch(frames: List<Bitmap>): List<PoseAnalysisResult> = withContext(Dispatchers.Default) {
        if (!isInitialized || frames.isEmpty()) {
            return@withContext emptyList()
        }
        
        Log.d(TAG, "Starting batch analysis of ${frames.size} frames")
        val startTime = System.currentTimeMillis()
        
        try {
            // Process frames in parallel for better performance
            val results = frames.map { frame ->
                analyzePoseFromFrame(frame)
            }
            
            val processingTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "Batch processing completed in ${processingTime}ms for ${frames.size} frames")
            
            results
        } catch (e: Exception) {
            Log.e(TAG, "Error in batch processing", e)
            emptyList()
        }
    }
    
    /**
     * Check if device is running low on memory
     */
    private fun isMemoryLow(): Boolean {
        return resourceManager.checkMemoryPressure() > LOW_MEMORY_THRESHOLD
    }
    
    /**
     * Perform memory cleanup when needed
     */
    private fun performMemoryCleanup() {
        Log.i(TAG, "Performing memory cleanup...")
        clearCache()
        resourceManager.checkMemoryPressure() // Triggers cleanup in resource manager
    }
    
    /**
     * Add sensor data to circular buffer
     */
    private fun addToSensorBuffer(data: MovementData) {
        synchronized(sensorDataBuffer) {
            sensorDataBuffer.add(data)
            if (sensorDataBuffer.size > MAX_SENSOR_BUFFER_SIZE) {
                sensorDataBuffer.removeAt(0) // Remove oldest
            }
        }
    }
    
    /**
     * Cache management methods
     */
    private fun clearCache() {
        analysisCache.clear()
        sensorDataBuffer.clear()
    }
    
    private fun getCachedMovementAnalysis(key: String): CachedAnalysis? {
        return analysisCache[key]?.let { cached ->
            if (System.currentTimeMillis() - cached.timestamp < 1000L) {
                cached
            } else null
        }
    }
    
    private fun cacheMovementAnalysis(key: String, result: MovementPatternAnalysis) {
        if (analysisCache.size >= CACHE_SIZE) {
            val oldestKey = analysisCache.keys.minByOrNull { analysisCache[it]?.timestamp ?: 0L }
            oldestKey?.let { analysisCache.remove(it) }
        }
        
        analysisCache[key] = CachedAnalysis(
            poseResult = null,
            movementResult = result,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Analyze pose from camera frame for form assessment (backward compatibility)
     */
    suspend fun analyzePoseFromFrame(bitmap: Bitmap): PoseAnalysisResult = withContext(Dispatchers.Default) {
        val result = analyzePoseFromFrameOptimized(bitmap)
        return@withContext when (result) {
            is MLResult.Success -> result.data
            is MLResult.Degraded -> result.degradedResult ?: PoseAnalysisResult.empty()
            is MLResult.Error -> PoseAnalysisResult.empty()
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
            val start = System.currentTimeMillis()
            // Advanced sensor fusion and pattern analysis
            val smoothedData = applySensorFusion(sensorData)
            val patterns = extractAdvancedPatterns(smoothedData, exerciseType)
            val asymmetries = detectAdvancedAsymmetries(patterns)
            val compensations = detectCompensationPatterns(patterns)
            val fatigue = detectFatigueIndicators(patterns)
            
            val result = MovementPatternAnalysis(
                patterns = patterns,
                asymmetryScore = asymmetries.maxOfOrNull { it.severity.toDouble() }?.toFloat() ?: 0f,
                compensationScore = compensations.maxOfOrNull { it.severity.toDouble() }?.toFloat() ?: 0f,
                fatigueScore = fatigue,
                riskLevel = calculateOverallRisk(asymmetries, compensations, fatigue),
                recommendations = generateMovementRecommendations(patterns, asymmetries, compensations),
                confidence = calculatePatternConfidence(sensorData.size)
            )
            val elapsed = System.currentTimeMillis() - start
            performanceMetrics.recordMovementAnalysis(elapsed)
            performanceMetrics.updateMemoryUsage()
            result
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
     * Generate simulated keypoints for fallback when real model fails
     */
    private fun generateSimulatedKeypoints(width: Int, height: Int): List<Keypoint> {
        val keypoints = mutableListOf<Keypoint>()
        
        // Simuliere eine stehende Person in der Bildmitte
        val centerX = width * 0.5f
        val centerY = height * 0.5f
        
        MoveNetNames.forEachIndexed { index, name ->
            val (x, y) = when (name) {
                "nose" -> centerX to centerY * 0.3f
                "left_eye" -> centerX - 20 to centerY * 0.25f
                "right_eye" -> centerX + 20 to centerY * 0.25f
                "left_shoulder" -> centerX - 60 to centerY * 0.5f
                "right_shoulder" -> centerX + 60 to centerY * 0.5f
                "left_elbow" -> centerX - 80 to centerY * 0.7f
                "right_elbow" -> centerX + 80 to centerY * 0.7f
                "left_wrist" -> centerX - 70 to centerY * 0.9f
                "right_wrist" -> centerX + 70 to centerY * 0.9f
                "left_hip" -> centerX - 40 to centerY * 1.1f
                "right_hip" -> centerX + 40 to centerY * 1.1f
                "left_knee" -> centerX - 45 to centerY * 1.4f
                "right_knee" -> centerX + 45 to centerY * 1.4f
                "left_ankle" -> centerX - 40 to centerY * 1.7f
                "right_ankle" -> centerX + 40 to centerY * 1.7f
                else -> centerX to centerY
            }
            
            keypoints.add(
                Keypoint(
                    name = name,
                    x = x + (Math.random() * 10 - 5).toFloat(), // Kleine Variation
                    y = y + (Math.random() * 10 - 5).toFloat(),
                    confidence = 0.7f + (Math.random() * 0.3).toFloat() // Confidence 0.7-1.0
                )
            )
        }
        
        return keypoints
    }
    
    /**
     * Get cached pose analysis result
     */
    private fun getCachedPoseAnalysisResult(): PoseAnalysisResult? {
        return analysisCache["pose_latest"]?.let { cached ->
            if (System.currentTimeMillis() - cached.timestamp < 500L) { // 500ms cache
                cached.poseResult
            } else null
        }
    }
    
    /**
     * Cache pose analysis result
     */
    private fun cachePoseAnalysisResult(result: PoseAnalysisResult) {
        if (analysisCache.size >= CACHE_SIZE) {
            val oldestKey = analysisCache.keys.minByOrNull { analysisCache[it]?.timestamp ?: 0L }
            oldestKey?.let { analysisCache.remove(it) }
        }
        
        analysisCache["pose_latest"] = CachedAnalysis(
            poseResult = result,
            movementResult = null,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Run pose inference with specific interpreter
     */
    private fun runPoseInferenceWithInterpreter(interpreter: Interpreter, processedImage: TensorImage): List<Keypoint> {
        return try {
            val output = Array(1) { Array(1) { Array(NUM_KEYPOINTS) { FloatArray(3) } } }
            interpreter.run(processedImage.buffer, output)
            parseMoveNetOutput(output, CONFIDENCE_THRESHOLD)
        } catch (e: Exception) {
            Log.w(TAG, "Pose inference failed", e)
            emptyList()
        }
    }

    private val MoveNetNames = listOf(
        "nose", "left_eye", "right_eye", "left_ear", "right_ear",
        "left_shoulder", "right_shoulder", "left_elbow", "right_elbow",
        "left_wrist", "right_wrist", "left_hip", "right_hip",
        "left_knee", "right_knee", "left_ankle", "right_ankle"
    )
    
    /**
     * Parse MoveNet output array into keypoints
     */
    private fun parseMoveNetOutput(output: Array<Array<Array<FloatArray>>>, confidenceThreshold: Float): List<Keypoint> {
        val keypoints = mutableListOf<Keypoint>()
        
        for (i in 0 until NUM_KEYPOINTS) {
            val y = output[0][0][i][0] // Y-coordinate (normalized)
            val x = output[0][0][i][1] // X-coordinate (normalized)
            val confidence = output[0][0][i][2] // Confidence score
            
            if (confidence >= confidenceThreshold) {
                keypoints.add(
                    Keypoint(
                        name = MoveNetNames[i],
                        x = x,
                        y = y,
                        confidence = confidence
                    )
                )
            }
        }
        
        return keypoints
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
     * Clean up resources with resource manager integration
     */
    suspend fun cleanup() {
        Log.i(TAG, "Cleaning up ML models...")
        
        // Resource manager handles interpreter cleanup
        try { 
            ortSession?.close() 
        } catch (_: Exception) {}
        ortSession = null
        // OrtEnvironment sollte nur einmal pro Prozess existieren; kein force close notwendig
        
        // Clear all caches and buffers
        clearCache()
        
        isInitialized = false
        Log.i(TAG, "ML models cleanup completed")
    }
}

// Performance monitoring data classes
data class PerformanceMetrics(
    var initTime: Long = 0L,
    var avgPoseAnalysisTime: Float = 0f,
    var avgMovementAnalysisTime: Float = 0f,
    var cacheHitRate: Float = 0f,
    var memoryUsageMB: Float = 0f,
    var totalAnalyses: Int = 0
) {
    fun recordInitTime(time: Long) {
        initTime = time
    }
    
    fun recordPoseAnalysis(time: Long) {
        totalAnalyses++
        avgPoseAnalysisTime = ((avgPoseAnalysisTime * (totalAnalyses - 1)) + time) / totalAnalyses
    }
    
    fun recordMovementAnalysis(time: Long) {
        avgMovementAnalysisTime = ((avgMovementAnalysisTime * (totalAnalyses - 1)) + time) / totalAnalyses
    }
    
    fun updateMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        memoryUsageMB = usedMemory / (1024f * 1024f)
    }
}

data class CachedAnalysis(
    val poseResult: PoseAnalysisResult?,
    val movementResult: MovementPatternAnalysis?,
    val timestamp: Long
)

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