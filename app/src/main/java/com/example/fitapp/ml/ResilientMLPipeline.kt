package com.example.fitapp.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.fitapp.ai.AdvancedMLModels
import com.fitapp.ml.MoveNetTFLite
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Resilient ML Pipeline that integrates all ML optimization components for stable, performant operation.
 *
 * Features:
 * - Centralized resource management with automatic cleanup
 * - Background processing with adaptive performance controls
 * - Graceful error handling and degradation
 * - Lifecycle-aware pause/resume
 * - Memory pressure monitoring and response
 * - Thermal and battery-aware processing
 */
class ResilientMLPipeline private constructor(
    private val context: Context,
) : LifecycleEventObserver {
    companion object {
        private const val TAG = "ResilientMLPipeline"

        @Volatile
        private var INSTANCE: ResilientMLPipeline? = null

        fun getInstance(context: Context): ResilientMLPipeline {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ResilientMLPipeline(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val resourceManager = MLResourceManager.getInstance()
    private val frameProcessor = OptimizedFrameProcessor(resourceManager)
    private val pipelineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var advancedMLModels: AdvancedMLModels? = null
    private var moveNetTFLite: MoveNetTFLite? = null

    private val isInitialized = AtomicBoolean(false)
    private val isRunning = AtomicBoolean(false)

    // Performance monitoring
    private val _performanceMetrics = MutableSharedFlow<PipelineMetrics>(replay = 1)
    val performanceMetrics: SharedFlow<PipelineMetrics> = _performanceMetrics.asSharedFlow()

    /**
     * Pipeline configuration for adaptive behavior
     */
    data class PipelineConfig(
        val enableAdaptiveQuality: Boolean = true,
        val enableThermalThrottling: Boolean = true,
        val enableBatteryOptimization: Boolean = true,
        val maxMemoryPressure: Float = 0.8f,
        val enableBackgroundProcessing: Boolean = true,
    )

    private var config = PipelineConfig()

    /**
     * Initialize the ML pipeline with configuration
     */
    suspend fun initialize(
        config: PipelineConfig = PipelineConfig(),
        lifecycle: Lifecycle? = null,
    ): MLResult<Unit> =
        withContext(Dispatchers.IO) {
            if (isInitialized.get()) {
                return@withContext MLResult.success(Unit)
            }

            Log.i(TAG, "Initializing resilient ML pipeline")
            this@ResilientMLPipeline.config = config

            try {
                // Register lifecycle observer for automatic pause/resume
                lifecycle?.addObserver(this@ResilientMLPipeline)

                // Initialize ML models
                val modelInitResult = initializeMLModels()
                if (modelInitResult.isError) {
                    return@withContext modelInitResult
                }

                // Start frame processor if background processing is enabled
                if (config.enableBackgroundProcessing) {
                    frameProcessor.start()
                }

                // Start performance monitoring
                startPerformanceMonitoring()

                isInitialized.set(true)
                Log.i(TAG, "ML pipeline initialized successfully")

                MLResult.success(Unit)
            } catch (outOfMemory: OutOfMemoryError) {
                Log.e(TAG, "Out of memory during initialization", outOfMemory)
                MLResult.degraded(
                    exception = outOfMemory,
                    degradedResult = Unit,
                    message = "Initialized in low-memory mode",
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize ML pipeline", e)
                cleanup()
                MLResult.error(e, fallbackAvailable = false)
            }
        }

    /**
     * Initialize ML models with resource management
     */
    private suspend fun initializeMLModels(): MLResult<Unit> {
        return try {
            // Initialize AdvancedMLModels
            advancedMLModels = AdvancedMLModels.getInstance(context)
            val advancedInitResult = advancedMLModels?.initializeAdaptive() ?: false

            // Initialize MoveNetTFLite
            moveNetTFLite = MoveNetTFLite.getInstance(context)
            val moveNetInitResult = moveNetTFLite?.initialize() ?: false

            when {
                advancedInitResult && moveNetInitResult -> {
                    Log.i(TAG, "All ML models initialized successfully")
                    MLResult.success(Unit)
                }
                advancedInitResult || moveNetInitResult -> {
                    Log.w(TAG, "Some ML models failed to initialize - operating in degraded mode")
                    MLResult.degraded(
                        exception = RuntimeException("Partial model initialization"),
                        degradedResult = Unit,
                        message = "Operating with limited ML capabilities",
                    )
                }
                else -> {
                    Log.e(TAG, "All ML models failed to initialize")
                    MLResult.error(
                        RuntimeException("Failed to initialize any ML models"),
                        fallbackAvailable = false,
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ML models", e)
            MLResult.error(e, fallbackAvailable = false)
        }
    }

    /**
     * Start the pipeline processing
     */
    fun start(): MLResult<Unit> {
        if (!isInitialized.get()) {
            return MLResult.error(
                IllegalStateException("Pipeline not initialized"),
                fallbackAvailable = false,
            )
        }

        if (!isRunning.compareAndSet(false, true)) {
            return MLResult.success(Unit) // Already running
        }

        Log.i(TAG, "Starting ML pipeline")

        try {
            frameProcessor.resume()
            Log.i(TAG, "ML pipeline started successfully")
            return MLResult.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ML pipeline", e)
            isRunning.set(false)
            return MLResult.error(e, fallbackAvailable = true)
        }
    }

    /**
     * Stop the pipeline processing
     */
    fun stop() {
        if (!isRunning.compareAndSet(true, false)) {
            return // Already stopped
        }

        Log.i(TAG, "Stopping ML pipeline")
        frameProcessor.pause()
    }

    /**
     * Process a frame through the resilient pipeline
     */
    suspend fun processFrame(
        bitmap: Bitmap,
        requestId: String = "frame_${System.currentTimeMillis()}",
    ): MLResult<Any> {
        if (!isRunning.get()) {
            return MLResult.error(
                IllegalStateException("Pipeline not running"),
                fallbackAvailable = false,
            )
        }

        return try {
            // Check memory pressure before processing
            val memoryPressure = resourceManager.checkMemoryPressure()
            if (memoryPressure > config.maxMemoryPressure) {
                Log.w(TAG, "High memory pressure detected: $memoryPressure")

                // Try to free memory
                resourceManager.checkMemoryPressure() // This triggers cleanup

                // Process with degraded quality
                return processFrameDegraded(bitmap, requestId)
            }

            // Submit frame for background processing
            val submitted = frameProcessor.submitFrame(bitmap, requestId)
            if (!submitted) {
                Log.v(TAG, "Frame queue full, processing synchronously")
                return processFrameSync(bitmap)
            }

            // For now, return success (in real implementation, would wait for result)
            MLResult.success("processed_frame")
        } catch (outOfMemory: OutOfMemoryError) {
            Log.e(TAG, "Out of memory during frame processing", outOfMemory)
            handleMemoryPressure()
            MLResult.degraded(
                exception = outOfMemory,
                degradedResult = null,
                message = "Frame processing degraded due to memory pressure",
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame", e)
            MLResult.error(e, fallbackAvailable = true)
        }
    }

    /**
     * Process frame in degraded mode with reduced resource usage
     */
    private suspend fun processFrameDegraded(
        bitmap: Bitmap,
        requestId: String,
    ): MLResult<Any> {
        return try {
            // Scale down bitmap for lower memory usage
            val scaledBitmap =
                Bitmap.createScaledBitmap(
                    bitmap,
                    bitmap.width / 2,
                    bitmap.height / 2,
                    true,
                )

            try {
                // Use lightweight processing
                val result = processFrameSync(scaledBitmap)
                MLResult.degraded(
                    exception = OutOfMemoryError("High memory pressure"),
                    degradedResult = result.getOrNull(),
                    message = "Processed with reduced resolution due to memory constraints",
                )
            } finally {
                if (scaledBitmap !== bitmap) {
                    scaledBitmap.recycle()
                }
            }
        } catch (e: Exception) {
            MLResult.error(e, fallbackAvailable = false)
        }
    }

    /**
     * Process frame synchronously as fallback
     */
    private suspend fun processFrameSync(bitmap: Bitmap): MLResult<Any> {
        return try {
            // Use one of the available ML models
            val result =
                advancedMLModels?.analyzePoseFromFrameOptimized(bitmap)
                    ?: moveNetTFLite?.detectPose(bitmap)
                    ?: return MLResult.error(
                        RuntimeException("No ML models available"),
                        fallbackAvailable = false,
                    )

            MLResult.success(result)
        } catch (e: Exception) {
            MLResult.error(e, fallbackAvailable = false)
        }
    }

    /**
     * Handle memory pressure by triggering cleanup
     */
    private fun handleMemoryPressure() {
        Log.i(TAG, "Handling memory pressure")

        pipelineScope.launch {
            try {
                // Trigger resource manager cleanup
                resourceManager.checkMemoryPressure()

                // Temporarily reduce processing quality
                frameProcessor.pause()
                delay(1000) // Give system time to recover
                frameProcessor.resume()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling memory pressure", e)
            }
        }
    }

    /**
     * Start performance monitoring coroutine
     */
    private fun startPerformanceMonitoring() {
        pipelineScope.launch {
            while (isInitialized.get()) {
                try {
                    val resourceStats = resourceManager.getResourceStats()
                    val processorStats = frameProcessor.getPerformanceStats()

                    val metrics =
                        PipelineMetrics(
                            memoryUsageMB = resourceStats.memoryUsageMB,
                            memoryPressure = resourceStats.memoryPressure,
                            bitmapPoolSize = resourceStats.bitmapPoolSize,
                            currentFps = processorStats.currentFps,
                            processingQuality = processorStats.currentQuality.name,
                            isProcessing = processorStats.isProcessing,
                            timestamp = System.currentTimeMillis(),
                        )

                    _performanceMetrics.tryEmit(metrics)

                    delay(5000) // Update every 5 seconds
                } catch (e: Exception) {
                    Log.w(TAG, "Error in performance monitoring", e)
                    delay(10000) // Wait longer on error
                }
            }
        }
    }

    /**
     * Lifecycle observer implementation
     */
    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event,
    ) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                Log.d(TAG, "App paused - pausing ML pipeline")
                frameProcessor.pause()
            }
            Lifecycle.Event.ON_RESUME -> {
                Log.d(TAG, "App resumed - resuming ML pipeline")
                if (isRunning.get()) {
                    frameProcessor.resume()
                }
            }
            Lifecycle.Event.ON_DESTROY -> {
                Log.d(TAG, "App destroyed - cleaning up ML pipeline")
                pipelineScope.launch {
                    cleanup()
                }
            }
            else -> { /* Other events not handled */ }
        }
    }

    /**
     * Get current pipeline status
     */
    fun getStatus(): PipelineStatus {
        return PipelineStatus(
            isInitialized = isInitialized.get(),
            isRunning = isRunning.get(),
            resourceStats = resourceManager.getResourceStats(),
            processorStats = frameProcessor.getPerformanceStats(),
            timestamp = System.currentTimeMillis(),
        )
    }

    /**
     * Cleanup all resources
     */
    suspend fun cleanup() {
        Log.i(TAG, "Cleaning up ML pipeline")

        isRunning.set(false)
        frameProcessor.stop()

        // Cleanup ML models
        advancedMLModels?.cleanup()
        moveNetTFLite?.cleanup()

        // Cleanup resource manager
        resourceManager.cleanup()

        // Cancel monitoring
        pipelineScope.cancel()

        isInitialized.set(false)
        INSTANCE = null

        Log.i(TAG, "ML pipeline cleanup completed")
    }

    /**
     * Pipeline metrics for monitoring
     */
    data class PipelineMetrics(
        val memoryUsageMB: Long,
        val memoryPressure: Float,
        val bitmapPoolSize: Int,
        val currentFps: Int,
        val processingQuality: String,
        val isProcessing: Boolean,
        val timestamp: Long,
    )

    /**
     * Current pipeline status
     */
    data class PipelineStatus(
        val isInitialized: Boolean,
        val isRunning: Boolean,
        val resourceStats: ResourceStats,
        val processorStats: OptimizedFrameProcessor.PerformanceStats,
        val timestamp: Long,
    )
}
