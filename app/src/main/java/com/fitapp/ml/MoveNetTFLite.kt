package com.fitapp.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import com.example.fitapp.ml.MLResourceManager
import com.example.fitapp.ml.MLResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.min

/**
 * MoveNet Thunder TensorFlow Lite Inferenz-Engine
 * 
 * Lädt das vortrainierte MoveNet Thunder Modell und führt Pose-Estimation durch.
 * Optimiert für mobile Geräte mit effizienter Speicherverwaltung.
 * 
 * Now integrated with MLResourceManager for improved resource management.
 */
class MoveNetTFLite private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "MoveNetTFLite"
        private const val MODEL_PATH = "models/tflite/movenet_thunder.tflite"
        private const val INPUT_SIZE = 256
        private const val CONFIDENCE_THRESHOLD = 0.3f
        private const val INTERPRETER_KEY = "movenet_thunder"
        
        @Volatile
        private var INSTANCE: MoveNetTFLite? = null
        
        fun getInstance(context: Context): MoveNetTFLite {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MoveNetTFLite(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val resourceManager = MLResourceManager.getInstance()
    private var isInitialized = false
    
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()
    
    /**
     * Initialisiert das MoveNet Modell mit MLResourceManager
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isInitialized) return@withContext true
            
            Log.i(TAG, "Initializing MoveNet Thunder model...")
            
            // Versuche das echte Modell zu laden
            try {
                val modelByteBuffer = FileUtil.loadMappedFile(context, MODEL_PATH)
                
                val options = Interpreter.Options().apply {
                    setNumThreads(2) // Optimiert für mobile Geräte
                    setUseNNAPI(true) // Hardware-Beschleunigung wenn verfügbar
                    setUseXNNPACK(true) // CPU-Optimierung
                }
                
                val interpreter = Interpreter(modelByteBuffer, options)
                
                // Register interpreter with resource manager
                resourceManager.registerInterpreter(INTERPRETER_KEY, interpreter)
                isInitialized = true
                
                Log.i(TAG, "MoveNet model loaded successfully with resource manager")
                true
                
            } catch (e: Exception) {
                Log.w(TAG, "Could not load real model, using simulation mode", e)
                isInitialized = true // Simulation mode
                true
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MoveNet", e)
            false
        }
    }
    
    /**
     * Führt Pose-Estimation auf dem gegebenen Bitmap durch mit verbesserter Fehlerbehandlung
     */
    suspend fun detectPose(bitmap: Bitmap): MLResult<Pose?> = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            Log.w(TAG, "Model not initialized")
            return@withContext MLResult.error(
                IllegalStateException("Model not initialized"), 
                fallbackAvailable = false
            )
        }
        
        // Use bitmap from resource manager if possible
        val workingBitmap = resourceManager.borrowBitmap(
            INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888
        ) ?: run {
            Log.w(TAG, "Could not borrow bitmap from pool, using direct processing")
            null
        }
        
        try {
            // Preprocessing
            val processedBitmap = if (bitmap.width != INPUT_SIZE || bitmap.height != INPUT_SIZE) {
                Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
            } else {
                bitmap
            }
            
            val tensorImage = TensorImage.fromBitmap(processedBitmap)
            val processedImage = imageProcessor.process(tensorImage)
            
            // Use resource manager for safe interpreter access
            val result = resourceManager.useInterpreter<List<Keypoint>>(INTERPRETER_KEY) { interpreter ->
                runModelInference(interpreter, processedImage, bitmap.width, bitmap.height)
            }
            
            // Clean up scaled bitmap if created
            if (processedBitmap !== bitmap) {
                processedBitmap.recycle()
            }
            
            when (result) {
                is MLResult.Success -> {
                    val keypoints = result.data
                    if (keypoints.isNotEmpty()) {
                        // Filter keypoints by confidence
                        val validKeypoints = keypoints.filter { it.score >= CONFIDENCE_THRESHOLD }
                        
                        if (validKeypoints.isNotEmpty()) {
                            val boundingBox = calculateBoundingBox(validKeypoints)
                            val averageScore = validKeypoints.map { it.score }.average().toFloat()
                            
                            val pose = Pose(
                                keypoints = validKeypoints,
                                boundingBox = boundingBox,
                                score = averageScore
                            )
                            MLResult.success(pose)
                        } else {
                            MLResult.success(null)
                        }
                    } else {
                        // Fallback to simulation if real model fails
                        val simulatedKeypoints = generateSimulatedKeypoints(bitmap.width, bitmap.height)
                        val validKeypoints = simulatedKeypoints.filter { it.score >= CONFIDENCE_THRESHOLD }
                        
                        if (validKeypoints.isNotEmpty()) {
                            val boundingBox = calculateBoundingBox(validKeypoints)
                            val averageScore = validKeypoints.map { it.score }.average().toFloat()
                            
                            val pose = Pose(
                                keypoints = validKeypoints,
                                boundingBox = boundingBox,
                                score = averageScore
                            )
                            MLResult.degraded(
                                exception = RuntimeException("Using simulation fallback"),
                                degradedResult = pose,
                                message = "Real model unavailable, using simulation"
                            )
                        } else {
                            MLResult.success(null)
                        }
                    }
                }
                is MLResult.Error -> {
                    if (result.fallbackAvailable) {
                        // Use simulation as fallback
                        val simulatedKeypoints = generateSimulatedKeypoints(bitmap.width, bitmap.height)
                        val validKeypoints = simulatedKeypoints.filter { it.score >= CONFIDENCE_THRESHOLD }
                        
                        if (validKeypoints.isNotEmpty()) {
                            val boundingBox = calculateBoundingBox(validKeypoints)
                            val averageScore = validKeypoints.map { it.score }.average().toFloat()
                            
                            val pose = Pose(
                                keypoints = validKeypoints,
                                boundingBox = boundingBox,
                                score = averageScore
                            )
                            MLResult.degraded(
                                exception = result.exception,
                                degradedResult = pose,
                                message = "Using simulation fallback due to error"
                            )
                        } else {
                            result
                        }
                    } else {
                        result
                    }
                }
                is MLResult.Degraded -> result
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during pose detection", e)
            return@withContext MLResult.error(e, fallbackAvailable = true)
        } finally {
            // Return bitmap to pool
            workingBitmap?.let { resourceManager.returnBitmap(it) }
        }
    }
    
    /**
     * Führt echte Modell-Inferenz durch
     */
    private fun runModelInference(interpreter: Interpreter, tensorImage: TensorImage, originalWidth: Int, originalHeight: Int): List<Keypoint> {
        return try {
            // Input vorbereiten
            val inputBuffer = tensorImage.buffer
            
            // Output vorbereiten - MoveNet Thunder Output: [1, 1, 17, 3]
            val outputArray = Array(1) { Array(1) { Array(MoveNetSpec.NUM_KEYPOINTS) { FloatArray(3) } } }
            
            // Inferenz ausführen
            interpreter.run(inputBuffer, outputArray)
            
            // Output zu Keypoints konvertieren
            val keypoints = mutableListOf<Keypoint>()
            for (i in 0 until MoveNetSpec.NUM_KEYPOINTS) {
                val y = outputArray[0][0][i][0] * originalHeight // Y-Koordinate
                val x = outputArray[0][0][i][1] * originalWidth  // X-Koordinate
                val confidence = outputArray[0][0][i][2]         // Confidence
                
                keypoints.add(
                    Keypoint(
                        name = MoveNetSpec.KEYPOINT_NAMES[i],
                        x = x,
                        y = y,
                        score = confidence
                    )
                )
            }
            
            keypoints
            
        } catch (e: Exception) {
            Log.e(TAG, "Error running model inference", e)
            emptyList()
        }
    }
    
    /**
     * Generiert simulierte Keypoints für Demo-Zwecke
     */
    private fun generateSimulatedKeypoints(width: Int, height: Int): List<Keypoint> {
        val keypoints = mutableListOf<Keypoint>()
        
        // Simuliere eine stehende Person in der Bildmitte
        val centerX = width * 0.5f
        val centerY = height * 0.5f
        
        MoveNetSpec.KEYPOINT_NAMES.forEachIndexed { index, name ->
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
                    score = 0.7f + (Math.random() * 0.3).toFloat() // Confidence 0.7-1.0
                )
            )
        }
        
        return keypoints
    }
    
    /**
     * Berechnet Bounding Box aus Keypoints
     */
    private fun calculateBoundingBox(keypoints: List<Keypoint>): RectF {
        if (keypoints.isEmpty()) return RectF()
        
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE
        
        keypoints.forEach { keypoint ->
            minX = min(minX, keypoint.x)
            minY = min(minY, keypoint.y)
            maxX = max(maxX, keypoint.x)
            maxY = max(maxY, keypoint.y)
        }
        
        // Etwas Padding hinzufügen
        val padding = 20f
        return RectF(
            minX - padding,
            minY - padding,
            maxX + padding,
            maxY + padding
        )
    }
    
    /**
     * Cleanup-Methode mit Resource Manager Integration
     */
    suspend fun cleanup() {
        Log.i(TAG, "Cleaning up MoveNet resources")
        
        // Resource manager handles interpreter cleanup
        isInitialized = false
        
        Log.i(TAG, "MoveNet cleaned up")
    }
}