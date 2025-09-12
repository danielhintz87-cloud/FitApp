package com.fitapp.ml.demo

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * ML Demo Launcher
 *
 * Zentrale Übersicht für alle ML Demo Activities
 */
@Composable
fun MLDemoLauncher(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "ML Pose Estimation Demos",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Verschiedene Implementierungen für Pose Detection",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // MoveNet TFLite Demo
        DemoCard(
            title = "MoveNet Thunder (TFLite)",
            description = "17 COCO Keypoints mit TensorFlow Lite\nOptimiert für mobile Geräte",
            features =
                listOf(
                    "Statische Bildanalyse",
                    "TensorFlow Lite Inferenz",
                    "Hardware-Beschleunigung",
                    "ONNX Kompatibilität",
                ),
            onLaunch = {
                val intent = Intent(context, MoveNetDemoActivity::class.java)
                context.startActivity(intent)
            },
        )

        // BlazePose MediaPipe Demo
        DemoCard(
            title = "BlazePose (MediaPipe)",
            description = "33 hochpräzise Landmarks mit MediaPipe Tasks\nEchtzeit CameraX Integration",
            features =
                listOf(
                    "Live Camera Stream",
                    "33 Pose Landmarks",
                    "MediaPipe Tasks API",
                    "Echtzeit Overlay",
                ),
            onLaunch = {
                val intent = Intent(context, BlazePoseDemoActivity::class.java)
                context.startActivity(intent)
            },
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "💡 Hinweis",
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text =
                        "Die Demos nutzen simulierte Daten, falls die echten Modelle nicht verfügbar sind. " +
                            "Führe 'bash scripts/fetch_models.sh' aus, um die echten Modelle herunterzuladen.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DemoCard(
    title: String,
    description: String,
    features: List<String>,
    onLaunch: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Features:",
                style = MaterialTheme.typography.titleSmall,
            )

            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                ) {
                    Text("• ")
                    Text(feature, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLaunch,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Demo starten")
            }
        }
    }
}
