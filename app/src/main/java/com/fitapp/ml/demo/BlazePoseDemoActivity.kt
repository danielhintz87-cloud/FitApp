package com.fitapp.ml.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitapp.ml.ui.LivePoseDetectionScreen

/**
 * Demo Activity für BlazePose Live Detection
 *
 * Zeigt CameraX Live-Stream mit BlazePose Landmarks (33 Punkte)
 * und Echtzeit Overlay-Rendering.
 */
class BlazePoseDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BlazePoseDemoContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlazePoseDemoContent() {
    var showCameraView by remember { mutableStateOf(false) }
    var permissionsGranted by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        TopAppBar(
            title = { Text("BlazePose Live Demo") },
        )

        if (showCameraView && permissionsGranted) {
            // Live Pose Detection
            LivePoseDetectionScreen(
                modifier = Modifier.weight(1f),
            )

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = { showCameraView = false },
                ) {
                    Text("Stop Camera")
                }
            }
        } else {
            // Info Screen
            BlazePoseDemoInfo(
                onStartDemo = {
                    // In production, check camera permissions here
                    permissionsGranted = true
                    showCameraView = true
                },
            )
        }
    }
}

@Composable
private fun BlazePoseDemoInfo(onStartDemo: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "BlazePose Live Detection",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Echtzeit Pose-Estimation mit 33 Landmarks",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Features:",
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("• 33 hochpräzise Pose Landmarks")
                Text("• Echtzeit CameraX Integration")
                Text("• MediaPipe Tasks Technologie")
                Text("• Compose Overlay Rendering")
                Text("• Optimiert für mobile Geräte")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Verwendung:",
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("1. Kamera-Berechtigung erteilen")
                Text("2. Demo starten")
                Text("3. Pose vor der Kamera einnehmen")
                Text("4. Landmarks werden in Echtzeit angezeigt")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartDemo,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Demo starten")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
