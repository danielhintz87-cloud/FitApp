package com.fitapp.ml.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.fitapp.ml.ImageUtils
import com.fitapp.ml.blazepose.BlazePoseMediaPipe
import com.fitapp.ml.blazepose.BlazePoseResult
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraX Live Pose Detection mit BlazePose
 * Composable für Echzeit Pose-Estimation mit Camera Preview und Overlay
 */
@Composable
fun LivePoseDetectionScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    var blazePoseResult by remember { mutableStateOf(BlazePoseResult.empty()) }
    var isInitialized by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // BlazePose instance
    val blazePose = remember { BlazePoseMediaPipe.getInstance(context) }
    
    // Camera executor
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Initialize BlazePose
    LaunchedEffect(Unit) {
        isInitialized = blazePose.initializeLiveStreamMode(
            resultListener = { result, _ ->
                blazePoseResult = result
                isProcessing = false
            },
            errorListener = { error ->
                errorMessage = error.message
                isProcessing = false
            }
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            blazePose.cleanup()
            cameraExecutor.shutdown()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        if (isInitialized) {
            // Camera Preview mit Pose Overlay
            CameraPreviewWithPose(
                context = context,
                lifecycleOwner = lifecycleOwner,
                cameraExecutor = cameraExecutor,
                onImageAnalyzed = { bitmap, timestamp ->
                    if (!isProcessing) {
                        isProcessing = true
                        coroutineScope.launch(Dispatchers.Default) {
                            blazePose.detectPoseAsync(bitmap, timestamp)
                        }
                    }
                },
                blazePoseResult = blazePoseResult,
                modifier = Modifier.fillMaxSize()
            )
            
            // Status Overlay
            PoseDetectionStatusOverlay(
                blazePoseResult = blazePoseResult,
                isProcessing = isProcessing,
                modifier = Modifier.align(Alignment.TopStart)
            )
            
        } else {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Initializing BlazePose...")
                    
                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Camera Preview mit integriertem Pose Overlay
 */
@Composable
private fun CameraPreviewWithPose(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    onImageAnalyzed: (Bitmap, Long) -> Unit,
    blazePoseResult: BlazePoseResult,
    modifier: Modifier = Modifier
) {
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var imageWidth by remember { mutableIntStateOf(640) }
    var imageHeight by remember { mutableIntStateOf(480) }
    
    Box(modifier = modifier) {
        // Camera Preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView = this
                }
            },
            update = { view ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    startCamera(
                        context = context,
                        cameraProvider = cameraProvider,
                        previewView = view,
                        lifecycleOwner = lifecycleOwner,
                        cameraExecutor = cameraExecutor,
                        onImageAnalyzed = { bitmap, timestamp, width, height ->
                            imageWidth = width
                            imageHeight = height
                            onImageAnalyzed(bitmap, timestamp)
                        }
                    )
                }, ContextCompat.getMainExecutor(context))
            }
        )
        
        // Pose Overlay
        BlazePoseOverlay(
            blazePoseResult = blazePoseResult,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Status Overlay für Pose Detection
 */
@Composable
private fun PoseDetectionStatusOverlay(
    blazePoseResult: BlazePoseResult,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "BlazePose Live Detection",
                style = MaterialTheme.typography.titleSmall
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing...", style = MaterialTheme.typography.bodySmall)
                } else {
                    val statusColor = if (blazePoseResult.isDetected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(statusColor, CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (blazePoseResult.isDetected) "Pose Detected" else "No Pose",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            if (blazePoseResult.isDetected) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Confidence: ${(blazePoseResult.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Landmarks: ${blazePoseResult.landmarks.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Startet die Kamera mit Image Analysis
 */
private fun startCamera(
    context: Context,
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    onImageAnalyzed: (Bitmap, Long, Int, Int) -> Unit
) {
    try {
        // Preview
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
        
        // Image Analysis
        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetResolution(android.util.Size(640, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { imageAnalysis ->
                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val bitmap = ImageUtils.imageProxyYuvToBitmap(imageProxy)
                    val timestamp = System.currentTimeMillis()
                    
                    onImageAnalyzed(bitmap, timestamp, imageProxy.width, imageProxy.height)
                    
                    imageProxy.close()
                }
            }
        
        // Camera Selector
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        // Unbind any previous use cases
        cameraProvider.unbindAll()
        
        // Bind use cases to camera
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalyzer
        )
        
    } catch (exc: Exception) {
        Log.e("CameraX", "Use case binding failed", exc)
    }
}