package com.example.fitapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun FoodScanScreen() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var analysisResult by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }

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
                                // TODO: Implement AI vision analysis
                                analysisResult = "🍎 Beispiel: Apfel (~80 kcal)\n" +
                                        "📊 Konfidenz: 85%\n" +
                                        "🥗 Weitere Analyse kommt mit AI-Vision Integration."
                                isAnalyzing = false
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
