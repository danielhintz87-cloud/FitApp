package com.fitapp.ml.blazepose

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * BlazePose MediaPipe Tasks Implementation
 * 
 * Nutzt MediaPipe Tasks für hochpräzise 33-Punkt Pose Landmarks.
 * Optimiert für Echzeit-CameraX Integration.
 * 
 * Note: Diese Implementation arbeitet derzeit im Simulationsmodus.
 * Für echte MediaPipe Integration müssen die entsprechenden Abhängigkeiten verfügbar sein.
 */
class BlazePoseMediaPipe private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "BlazePoseMediaPipe"
        private const val MODEL_PATH = "models/tflite/blazepose.tflite"
        private const val MIN_POSE_DETECTION_CONFIDENCE = 0.5f
        private const val MIN_POSE_PRESENCE_CONFIDENCE = 0.5f
        private const val MIN_TRACKING_CONFIDENCE = 0.5f
        
        @Volatile
        private var INSTANCE: BlazePoseMediaPipe? = null
        
        fun getInstance(context: Context): BlazePoseMediaPipe {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BlazePoseMediaPipe(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private var isInitialized = false
    private var resultListener: ((BlazePoseResult, Any) -> Unit)? = null
    private var errorListener: ((RuntimeException) -> Unit)? = null
    
    // MediaPipe PoseLandmarker instances
    private var imagePoseLandmarker: PoseLandmarker? = null
    private var liveStreamPoseLandmarker: PoseLandmarker? = null
    
    /**
     * Initialisiert BlazePose für IMAGE mode (einzelne Bilder)
     */
    suspend fun initializeImageMode(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isInitialized && imagePoseLandmarker != null) return@withContext true
            
            Log.i(TAG, "Initializing BlazePose for image mode...")
            
            // Create BaseOptions for MediaPipe model
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_PATH)
                .build()
            
            // Create PoseLandmarker options for image mode
            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setMinPoseDetectionConfidence(MIN_POSE_DETECTION_CONFIDENCE)
                .setMinPosePresenceConfidence(MIN_POSE_PRESENCE_CONFIDENCE)
                .setMinTrackingConfidence(MIN_TRACKING_CONFIDENCE)
                .setNumPoses(1) // Single pose detection
                .build()
            
            // Create PoseLandmarker
            imagePoseLandmarker = PoseLandmarker.createFromOptions(context, options)
            
            isInitialized = true
            Log.i(TAG, "BlazePose initialized successfully for image mode")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize BlazePose for image mode", e)
            false
        }
    }
    
    /**
     * Initialisiert BlazePose für LIVE_STREAM mode (CameraX)
     */
    suspend fun initializeLiveStreamMode(
        resultListener: (BlazePoseResult, Any) -> Unit,
        errorListener: (RuntimeException) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isInitialized && liveStreamPoseLandmarker != null) return@withContext true
            
            Log.i(TAG, "Initializing BlazePose for live stream mode...")
            
            this@BlazePoseMediaPipe.resultListener = resultListener
            this@BlazePoseMediaPipe.errorListener = errorListener
            
            // Create BaseOptions for MediaPipe model
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_PATH)
                .build()
            
            // Create PoseLandmarker options for live stream mode
            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setMinPoseDetectionConfidence(MIN_POSE_DETECTION_CONFIDENCE)
                .setMinPosePresenceConfidence(MIN_POSE_PRESENCE_CONFIDENCE)
                .setMinTrackingConfidence(MIN_TRACKING_CONFIDENCE)
                .setNumPoses(1) // Single pose detection
                .setResultListener { result, input ->
                    // Convert MediaPipe result to our format
                    val blazePoseResult = convertMediaPipeResult(result)
                    resultListener(blazePoseResult, input)
                }
                .setErrorListener { error ->
                    Log.e(TAG, "MediaPipe error in live stream", error)
                    errorListener(error)
                }
                .build()
            
            // Create PoseLandmarker
            liveStreamPoseLandmarker = PoseLandmarker.createFromOptions(context, options)
            
            isInitialized = true
            Log.i(TAG, "BlazePose live stream mode initialized successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize BlazePose live stream", e)
            false
        }
    }
    
    /**
     * Pose Detection für einzelne Bilder
     */
    suspend fun detectPose(bitmap: Bitmap): BlazePoseResult = withContext(Dispatchers.Default) {
        if (!isInitialized || imagePoseLandmarker == null) {
            Log.w(TAG, "BlazePose not initialized for image mode")
            return@withContext BlazePoseResult.empty()
        }
        
        try {
            // Convert Bitmap to MPImage
            val mpImage = BitmapImageBuilder(bitmap).build()
            
            // Perform pose detection
            val result = imagePoseLandmarker!!.detect(mpImage)
            
            // Convert result to our format
            return@withContext convertMediaPipeResult(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during pose detection", e)
            BlazePoseResult.empty()
        }
    }
    
    /**
     * Pose Detection für Live Stream (asynchron)
     */
    suspend fun detectPoseAsync(bitmap: Bitmap, timestampMs: Long): Boolean = withContext(Dispatchers.Default) {
        if (!isInitialized || liveStreamPoseLandmarker == null) {
            Log.w(TAG, "BlazePose not initialized for live stream mode")
            return@withContext false
        }
        
        try {
            // Convert Bitmap to MPImage
            val mpImage = BitmapImageBuilder(bitmap).build()
            
            // Perform async pose detection - result will be delivered via callback
            liveStreamPoseLandmarker!!.detectAsync(mpImage, timestampMs)
            
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during async pose detection", e)
            errorListener?.invoke(RuntimeException("Async detection error", e))
            false
        }
    }
    
    /**
     * Converts MediaPipe PoseLandmarkerResult to our BlazePoseResult format
     */
    private fun convertMediaPipeResult(result: PoseLandmarkerResult): BlazePoseResult {
        if (result.landmarks().isEmpty()) {
            return BlazePoseResult.empty()
        }
        
        // Get the first pose (we're configured for single pose detection)
        val poseLandmarks = result.landmarks()[0]
        val landmarks = mutableListOf<Landmark3D>()
        
        // Convert each landmark
        for (i in 0 until minOf(poseLandmarks.size, BlazePoseSpec.COUNT)) {
            val landmark = poseLandmarks[i]
            landmarks.add(
                Landmark3D(
                    x = landmark.x(),
                    y = landmark.y(),
                    z = landmark.z(),
                    visibility = 1.0f // Use constant visibility for now
                )
            )
        }
        
        return BlazePoseResult(
            landmarks = landmarks,
            isDetected = landmarks.isNotEmpty(),
            confidence = 1.0f // MediaPipe doesn't provide overall confidence in this version
        )
    }
    
    /**
     * Generiert simulierte BlazePose Landmarks für Demo/Fallback
     */
    private fun generateSimulatedResult(): BlazePoseResult {
        val landmarks = mutableListOf<Landmark3D>()
        
        // Simuliere eine stehende Person mit allen 33 Landmarks
        BlazePoseSpec.NAMES.forEachIndexed { index, name ->
            val (x, y, z) = when (name) {
                "nose" -> Triple(0.5f, 0.2f, 0f)
                "left_eye", "left_eye_inner", "left_eye_outer" -> Triple(0.48f, 0.18f, 0f)
                "right_eye", "right_eye_inner", "right_eye_outer" -> Triple(0.52f, 0.18f, 0f)
                "left_ear" -> Triple(0.46f, 0.19f, 0f)
                "right_ear" -> Triple(0.54f, 0.19f, 0f)
                "mouth_left" -> Triple(0.49f, 0.22f, 0f)
                "mouth_right" -> Triple(0.51f, 0.22f, 0f)
                "left_shoulder" -> Triple(0.4f, 0.35f, 0f)
                "right_shoulder" -> Triple(0.6f, 0.35f, 0f)
                "left_elbow" -> Triple(0.35f, 0.5f, 0f)
                "right_elbow" -> Triple(0.65f, 0.5f, 0f)
                "left_wrist" -> Triple(0.38f, 0.65f, 0f)
                "right_wrist" -> Triple(0.62f, 0.65f, 0f)
                "left_pinky", "left_index", "left_thumb" -> Triple(0.37f, 0.67f, 0f)
                "right_pinky", "right_index", "right_thumb" -> Triple(0.63f, 0.67f, 0f)
                "left_hip" -> Triple(0.43f, 0.7f, 0f)
                "right_hip" -> Triple(0.57f, 0.7f, 0f)
                "left_knee" -> Triple(0.42f, 0.85f, 0f)
                "right_knee" -> Triple(0.58f, 0.85f, 0f)
                "left_ankle" -> Triple(0.44f, 1.0f, 0f)
                "right_ankle" -> Triple(0.56f, 1.0f, 0f)
                "left_heel" -> Triple(0.43f, 1.02f, 0f)
                "right_heel" -> Triple(0.57f, 1.02f, 0f)
                "left_foot_index" -> Triple(0.45f, 1.0f, 0f)
                "right_foot_index" -> Triple(0.55f, 1.0f, 0f)
                else -> Triple(0.5f, 0.5f, 0f)
            }
            
            landmarks.add(
                Landmark3D(
                    x = x + (Math.random() * 0.02 - 0.01).toFloat(), // Kleine Variation
                    y = y + (Math.random() * 0.02 - 0.01).toFloat(),
                    z = z + (Math.random() * 0.01 - 0.005).toFloat(),
                    visibility = 0.8f + (Math.random() * 0.2).toFloat()
                )
            )
        }
        
        return BlazePoseResult(
            landmarks = landmarks,
            isDetected = true,
            confidence = 0.85f
        )
    }
    
    /**
     * Cleanup
     */
    fun cleanup() {
        try {
            // Cleanup MediaPipe PoseLandmarker instances
            imagePoseLandmarker?.close()
            imagePoseLandmarker = null
            
            liveStreamPoseLandmarker?.close()
            liveStreamPoseLandmarker = null
            
            isInitialized = false
            resultListener = null
            errorListener = null
            
            Log.i(TAG, "BlazePose cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}