package com.example.fitapp.ui.components

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExperimentalGetImage
@Composable
fun BarcodeScannerView(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var isScanning by remember { mutableStateOf(true) }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Initialize barcode scanner
    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_QR_CODE
            )
            .build()
        BarcodeScanning.getClient(options)
    }
    
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProvider = cameraProviderFuture.get()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        cameraProvider?.let { provider ->
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context)
                    
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analyzer ->
                            analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                                if (isScanning) {
                                    processImageProxy(scanner, imageProxy) { barcode ->
                                        isScanning = false
                                        onBarcodeDetected(barcode)
                                    }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }
                    
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (exc: Exception) {
                        Log.e("BarcodeScanner", "Use case binding failed", exc)
                    }
                    
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Overlay with scanning frame and controls
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Barcode scannen",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onClose,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Scanning frame in the middle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ScanningFrame()
            }
            
            // Bottom instruction text
            Text(
                "Richten Sie den Barcode im Rahmen aus",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
        
        // Show loading overlay if not scanning
        if (!isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Barcode erkannt...")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanningFrame() {
    Box(
        modifier = Modifier.size(250.dp)
    ) {
        // Corner indicators for scanning frame
        val cornerSize = 20.dp
        
        // Top-left corner
        Box(
            modifier = Modifier
                .size(cornerSize)
                .align(Alignment.TopStart)
        )
        
        // Top-right corner
        Box(
            modifier = Modifier
                .size(cornerSize)
                .align(Alignment.TopEnd)
        )
        
        // Bottom-left corner
        Box(
            modifier = Modifier
                .size(cornerSize)
                .align(Alignment.BottomStart)
        )
        
        // Bottom-right corner
        Box(
            modifier = Modifier
                .size(cornerSize)
                .align(Alignment.BottomEnd)
        )
    }
}

@androidx.camera.core.ExperimentalGetImage
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        onBarcodeDetected(rawValue)
                        break
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("BarcodeScanner", "Barcode scanning failed", exception)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}