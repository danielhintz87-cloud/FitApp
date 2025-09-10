package com.fitapp.ml.demo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.fitapp.ml.MLResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fitapp.ml.MoveNetTFLite
import com.fitapp.ml.Pose
import com.fitapp.ml.ImageUtils
import com.fitapp.ml.ui.MoveNetPoseOverlay
import kotlinx.coroutines.launch

/**
 * Demo Activity für MoveNet TensorFlow Lite
 * 
 * Zeigt TFLite Inferenz mit MoveNet Thunder Modell (17 COCO Keypoints)
 * und statische Bild-Analyse mit Compose Overlay.
 */
class MoveNetDemoActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MoveNetDemoContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveNetDemoContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var detectedPose by remember { mutableStateOf<Pose?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    
    val moveNet = remember { MoveNetTFLite.getInstance(context) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val bitmap = ImageUtils.loadBitmapFromUri(context, it)
                bitmap?.let { bmp ->
                    selectedBitmap = ImageUtils.compressBitmap(bmp, 512)
                    detectedPose = null
                }
            }
        }
    }
    
    // Initialize MoveNet
    LaunchedEffect(Unit) {
        isInitialized = moveNet.initialize()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            // Launch cleanup in a coroutine since it's now a suspend function
            kotlinx.coroutines.runBlocking {
                moveNet.cleanup()
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("MoveNet TFLite Demo") }
        )
        
        if (selectedBitmap != null) {
            // Image with Pose Overlay
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                selectedBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Pose Overlay
                    MoveNetPoseOverlay(
                        pose = detectedPose,
                        imageWidth = bitmap.width,
                        imageHeight = bitmap.height,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        selectedBitmap?.let { bitmap ->
                            coroutineScope.launch {
                                isProcessing = true
                                try {
                                    val result = moveNet.detectPose(bitmap)
                                    detectedPose = when (result) {
                                        is MLResult.Success -> result.data
                                        is MLResult.Degraded -> result.degradedResult
                                        is MLResult.Error -> null
                                    }
                                } finally {
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    enabled = !isProcessing && isInitialized
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Pose erkennen")
                    }
                }
                
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Text("Neues Bild")
                }
                
                Button(
                    onClick = { 
                        selectedBitmap = null
                        detectedPose = null 
                    }
                ) {
                    Text("Zurücksetzen")
                }
            }
            
            // Pose Info
            detectedPose?.let { pose ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Erkannte Pose",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Keypoints: ${pose.keypoints.size}")
                        Text("Confidence: ${(pose.score * 100).toInt()}%")
                        Text("Sichtbare Keypoints: ${pose.keypoints.count { it.score > 0.3f }}")
                    }
                }
            }
            
        } else {
            // Info Screen
            MoveNetDemoInfo(
                onSelectImage = { imagePickerLauncher.launch("image/*") },
                isInitialized = isInitialized
            )
        }
    }
}

@Composable
private fun MoveNetDemoInfo(
    onSelectImage: () -> Unit,
    isInitialized: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "MoveNet Thunder",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "TensorFlow Lite Pose-Estimation",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Features:",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("• 17 COCO Pose Keypoints")
                Text("• TensorFlow Lite Optimierung")
                Text("• Mobile-first Design")
                Text("• ONNX Konvertierung (optional)")
                Text("• Hardware-Beschleunigung")
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Verwendung:",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("1. Bild aus Galerie auswählen")
                Text("2. 'Pose erkennen' drücken")
                Text("3. Keypoints werden als Overlay angezeigt")
                Text("4. Skelett-Verbindungen visualisiert")
            }
        }
        
        if (!isInitialized) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Status:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Modell wird initialisiert...")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onSelectImage,
            modifier = Modifier.fillMaxWidth(),
            enabled = isInitialized
        ) {
            Text("Bild auswählen")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}