package com.fitapp.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageUtils {
    /**
     * Konvertiert ein CameraX ImageProxy zu Bitmap
     */
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /**
     * Konvertiert YUV_420_888 ImageProxy zu RGB Bitmap
     */
    fun imageProxyYuvToBitmap(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer // Y
        val uBuffer = imageProxy.planes[1].buffer // U
        val vBuffer = imageProxy.planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    /**
     * Skaliert Bitmap auf gewünschte Größe unter Beibehaltung des Seitenverhältnisses
     */
    fun scaleBitmap(
        bitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
    ): Bitmap {
        val matrix = Matrix()
        val scaleX = targetWidth.toFloat() / bitmap.width
        val scaleY = targetHeight.toFloat() / bitmap.height
        val scale = kotlin.math.min(scaleX, scaleY)

        matrix.setScale(scale, scale)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Rotiert Bitmap um angegebene Grad
     */
    fun rotateBitmap(
        bitmap: Bitmap,
        degrees: Float,
    ): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Lädt Bitmap aus URI (für Galerie-Bilder)
     */
    suspend fun loadBitmapFromUri(
        context: Context,
        uri: Uri,
    ): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Komprimiert Bitmap für bessere Performance
     */
    fun compressBitmap(
        bitmap: Bitmap,
        maxSize: Int = 512,
    ): Bitmap {
        val ratio =
            kotlin.math.min(
                maxSize.toFloat() / bitmap.width,
                maxSize.toFloat() / bitmap.height,
            )

        return if (ratio < 1.0f) {
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }
    }

    /**
     * Konvertiert Keypoints von normalisierten Koordinaten zu Pixel-Koordinaten
     */
    fun denormalizeKeypoints(
        keypoints: List<Keypoint>,
        imageWidth: Int,
        imageHeight: Int,
    ): List<Keypoint> {
        return keypoints.map { keypoint ->
            keypoint.copy(
                x = keypoint.x * imageWidth,
                y = keypoint.y * imageHeight,
            )
        }
    }

    /**
     * Konvertiert Keypoints von Pixel-Koordinaten zu normalisierten Koordinaten
     */
    fun normalizeKeypoints(
        keypoints: List<Keypoint>,
        imageWidth: Int,
        imageHeight: Int,
    ): List<Keypoint> {
        return keypoints.map { keypoint ->
            keypoint.copy(
                x = keypoint.x / imageWidth,
                y = keypoint.y / imageHeight,
            )
        }
    }
}
