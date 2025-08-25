package com.example.fitapp.ui.screens

import android.graphics.BitmapFactory
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fitapp.ai.AiCore
import com.example.fitapp.ai.AiProvider
import com.example.fitapp.ai.AppAi
import kotlinx.coroutines.launch

@Composable
fun FoodScanScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<AiCore.CalorieEstimation?>(null) }
    var selectedProvider by remember { mutableStateOf(AiProvider.OpenAI) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
        analysisResult = null
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Food Scan",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        item {
            Text(
                "Fotografiere dein Essen und erhalte eine AI-basierte Kalorienanalyse",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // AI Provider Selection
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "AI Provider auswählen",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AiProvider.entries.forEach { provider ->
                            FilterChip(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
                                label = { Text(provider.name) }
                            )
                        }
                    }
                }
            }
        }
        
        // Photo Selection
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Foto auswählen",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📷 Foto aus Galerie wählen")
                    }
                    
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Ausgewähltes Essen",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    selectedImageUri?.let { uri ->
                                        isAnalyzing = true
                                        try {
                                            val inputStream = context.contentResolver.openInputStream(uri)
                                            val bitmap = BitmapFactory.decodeStream(inputStream)
                                            inputStream?.close()
                                            
                                            analysisResult = AppAi.analyzeFood(bitmap, selectedProvider)
                                        } catch (e: Exception) {
                                            // Handle error
                                            analysisResult = null
                                        } finally {
                                            isAnalyzing = false
                                        }
                                    }
                                }
                            },
                            enabled = !isAnalyzing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Analysiere...")
                            } else {
                                Text("🤖 Kalorien analysieren")
                            }
                        }
                    }
                }
            }
        }
        
        // Analysis Results
        analysisResult?.let { result ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "🍽️ Analyse Ergebnis",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Geschätzte Kalorien",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    "${result.estimated_calories} kcal",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Vertrauen",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    "${(result.confidence * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        
                        if (result.food_items.isNotEmpty()) {
                            Text(
                                "Erkannte Lebensmittel:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            result.food_items.forEach { item ->
                                Text(
                                    "• $item",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        if (result.portion_size.isNotBlank()) {
                            Text(
                                "Portionsgröße: ${result.portion_size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (result.notes.isNotBlank()) {
                            Text(
                                "Hinweise: ${result.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
