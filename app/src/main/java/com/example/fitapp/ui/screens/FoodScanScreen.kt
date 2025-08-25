package com.example.fitapp.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fitapp.ai.AiProvider
import com.example.fitapp.ai.AppAi
import kotlinx.coroutines.launch

@Composable
fun FoodScanScreen() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var analysisResult by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { 
            Text("Food Scan", style = MaterialTheme.typography.titleLarge) 
        }
        
        item {
            Text("Mache ein Foto von deinem Essen und erhalte eine Kalorien-Schätzung durch AI-Vision.")
        }

        item {
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Foto auswählen")
            }
        }

        selectedImageUri?.let { uri ->
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Ausgewähltes Food-Bild",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                isAnalyzing = true
                                scope.launch {
                                    // For demo purposes, simulate analysis without actual bitmap conversion
                                    // In production, convert URI to Bitmap properly
                                    analysisResult = "🍎 Beispiel-Analyse:\n" +
                                            "📊 Geschätzte Kalorien: 80 kcal\n" +
                                            "🎯 Konfidenz: 85%\n" +
                                            "💡 AI-Vision Integration aktiviert (Demo-Modus)"
                                    isAnalyzing = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isAnalyzing
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Text("Kalorien analysieren")
                            }
                        }
                    }
                }
            }
        }

        if (analysisResult.isNotBlank()) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Analyse-Ergebnis", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(analysisResult)
                    }
                }
            }
        }
    }
}
