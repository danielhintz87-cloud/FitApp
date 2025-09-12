package com.example.fitapp.ui.nutrition.barcode

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * ML Kit Barcode Analyzer for CameraX
 * Provides high-accuracy barcode detection for food products
 */
class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer {
    companion object {
        private const val TAG = "BarcodeAnalyzer"
    }

    private val scanner = BarcodeScanning.getClient()
    private var lastDetectedBarcode: String? = null
    private var lastDetectionTime = 0L
    private val debounceTime = 2000L // 2 seconds between same barcode detections

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    processBarcodes(barcodes)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Barcode scanning failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun processBarcodes(barcodes: List<Barcode>) {
        for (barcode in barcodes) {
            val rawValue = barcode.rawValue
            if (rawValue != null && isValidFoodBarcode(barcode)) {
                val currentTime = System.currentTimeMillis()

                // Debounce same barcode detection
                if (rawValue != lastDetectedBarcode ||
                    currentTime - lastDetectionTime > debounceTime
                ) {
                    lastDetectedBarcode = rawValue
                    lastDetectionTime = currentTime

                    Log.d(TAG, "Detected barcode: $rawValue, format: ${barcode.format}")
                    onBarcodeDetected(rawValue)
                    break // Only process first valid barcode
                }
            }
        }
    }

    /**
     * Check if the barcode is likely a food product barcode
     */
    private fun isValidFoodBarcode(barcode: Barcode): Boolean {
        val rawValue = barcode.rawValue ?: return false

        return when (barcode.format) {
            Barcode.FORMAT_EAN_13 -> {
                // EAN-13 barcodes are most common for food products
                rawValue.length == 13 && rawValue.all { it.isDigit() }
            }
            Barcode.FORMAT_EAN_8 -> {
                // EAN-8 barcodes also used for food products
                rawValue.length == 8 && rawValue.all { it.isDigit() }
            }
            Barcode.FORMAT_UPC_A -> {
                // UPC-A common in North America
                rawValue.length == 12 && rawValue.all { it.isDigit() }
            }
            Barcode.FORMAT_UPC_E -> {
                // UPC-E compressed format
                rawValue.length == 8 && rawValue.all { it.isDigit() }
            }
            else -> {
                // Accept other formats but check basic criteria
                rawValue.length >= 8 && rawValue.length <= 14 &&
                    rawValue.all { it.isDigit() }
            }
        }
    }
}
