package com.example.fitapp.ui.screens

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FoodScanScreen() {
    // Dummy-UI, CameraX Einrichtung folgt – verhindert Crash und bietet Platzhalter
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Food Scan (Beta)", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Bilder vom Essen aufnehmen und analysieren – folgt.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { /* TODO: Kamera & Analyse (DALL·E/vision) */ }) {
            Text("Foto aufnehmen")
        }
    }
}
