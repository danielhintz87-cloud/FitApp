package com.example.fitapp.ai

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CameraX-basierter Frame Provider für Pose Analyse.
 * Liefert das zuletzt aufgenommene Frame (ARGB_8888 Bitmap) über [currentFrame()].
 *
 * Design-Ziele:
 * - Thread-sichere Speicherung des letzten Frames (volatile)
 * - Schnelle YUV420 -> ARGB Konvertierung ohne externe Libs (JPEG Roundtrip, ausreichend für MVP)
 * - Dynamische Zielauflösung orientiert sich am aktuell aktiven Modell (256 oder 192 px quadratisch)
 * - Sanftes Downsampling um unnötige Skalierung im ML-Pfad zu vermeiden
 */
class CameraXPoseFrameProvider(
    private val context: Context,
    private val modelService: AdvancedMLModels
) : PoseFrameProvider {

    companion object {
        private const val TAG = "CameraXPoseFrameProvider"
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var analysis: ImageAnalysis? = null
    private var preview: Preview? = null
    private var previewView: PreviewView? = null
    private val started = AtomicBoolean(false)
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    @Volatile
    private var latestBitmap: Bitmap? = null

    override fun currentFrame(): Bitmap? = latestBitmap

    /**
     * Initialisiert CameraX & startet den Analyse-Stream.
     * @param lifecycleOwner muss ein aktives Lifecycle besitzen (z.B. Activity oder Fragment)
     */
    fun start(lifecycleOwner: LifecycleOwner, previewView: PreviewView? = null) {
        if (!started.compareAndSet(false, true)) return
        this.previewView = previewView
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                cameraProvider = future.get()
                bindUseCases(lifecycleOwner)
            } catch (e: Exception) {
                Log.e(TAG, "CameraProvider Init fehlgeschlagen", e)
                started.set(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stop() {
        if (!started.compareAndSet(true, false)) return
        try {
            cameraProvider?.unbindAll()
        } catch (_: Exception) {}
        analysis = null
        preview = null
        camera = null
        latestBitmap = null
    }

    private fun targetSize(): Int = when (modelService.getCurrentModelType()) {
        AdvancedMLModels.PoseModelType.MOVENET_LIGHTNING -> 192
        else -> 256 // Thunder / BlazePose aktuell 256
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindUseCases(lifecycleOwner: LifecycleOwner) {
        val provider = cameraProvider ?: return
        // Vorher alles lösen
        provider.unbindAll()

        val rotation = previewView?.display?.rotation ?: 0
        val size = targetSize()

        @Suppress("DEPRECATION")
        preview = Preview.Builder()
            .setTargetResolution(android.util.Size(size, size))
            .setTargetRotation(rotation)
            .build().also { p ->
                previewView?.let { pv -> p.setSurfaceProvider(pv.surfaceProvider) }
            }

        @Suppress("DEPRECATION")
        analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(android.util.Size(size, size))
            .setTargetRotation(rotation)
            .build().also { ia ->
                ia.setAnalyzer(executor) { image ->
                    try {
                        latestBitmap = image.toBitmap()
                    } catch (e: Exception) {
                        Log.w(TAG, "Frame Konvertierung fehlgeschlagen", e)
                    } finally {
                        image.close()
                    }
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysis)
        } catch (e: Exception) {
            Log.e(TAG, "UseCases Bind fehlgeschlagen", e)
        }
    }
}

// --- Hilfsfunktionen ---
private fun ImageProxy.toBitmap(): Bitmap? {
    // Nur YUV_420_888 unterstützt
    if (format != ImageFormat.YUV_420_888) return null

    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    // U & V sind vertauscht (VU Format für NV21)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 75, out)
    val jpeg = out.toByteArray()
    val bitmap = android.graphics.BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)
    
    // Use the image rotation from ImageProxy
    val rotationDegrees = imageInfo.rotationDegrees
    if (rotationDegrees != 0) {
        val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    return bitmap
}

private fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val data = ByteArray(remaining())
    get(data)
    return data
}
