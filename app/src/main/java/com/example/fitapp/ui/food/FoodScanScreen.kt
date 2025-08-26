package com.example.fitapp.ui.food

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AiProvider
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.ui.components.BudgetBar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun FoodScanScreen(contentPadding: PaddingValues, provider: AiProvider) {
    val ctx = LocalContext.current
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    val scope = rememberCoroutineScope()
    var picked by remember { mutableStateOf<Uri?>(null) }
    var captured by remember { mutableStateOf<Bitmap?>(null) }
    var estimate by remember { mutableStateOf<com.example.fitapp.ai.CalorieEstimate?>(null) }
    var loading by remember { mutableStateOf(false) }
    val todayEpoch = remember { LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond() }
    val goal by repo.goalFlow(LocalDate.now()).collectAsState(initial = null)
    val entries by repo.dayEntriesFlow(todayEpoch).collectAsState(initial = emptyList())
    val consumed = entries.sumOf { it.kcal }
    val target = goal?.targetKcal ?: 2000

    val picker = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
        picked = uri
        captured = null
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        captured = bmp
        picked = null
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Food Scan", style = MaterialTheme.typography.titleLarge)
        
        // Daily Goal Setting
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Tagesziel", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    var newTarget by remember { mutableStateOf(target.toString()) }
                    OutlinedTextField(
                        value = newTarget,
                        onValueChange = { newTarget = it },
                        label = { Text("Kalorien") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val targetValue = newTarget.toIntOrNull()
                        if (targetValue != null && targetValue > 0) {
                            scope.launch {
                                repo.setDailyGoal(java.time.LocalDate.now(), targetValue)
                            }
                        }
                    }) {
                        Text("Setzen")
                    }
                }
            }
        }
        
        BudgetBar(consumed = consumed, target = target)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Foto wählen") }
            Button(onClick = { camera.launch(null) }) { Text("Foto aufnehmen") }
            OutlinedButton(enabled = (picked != null || captured != null) && !loading, onClick = {
                loading = true
                scope.launch {
                    try {
                        estimate = when {
                            captured != null -> repo.analyzeFoodBitmap(captured!!, provider)
                            picked != null -> repo.analyzeFoodImage(ctx, picked!!, provider)
                            else -> null
                        }
                    } catch (e: Exception) {
                        estimate = com.example.fitapp.ai.CalorieEstimate(0, "niedrig", "Analyse fehlgeschlagen: ${'$'}{e.message}")
                    } finally {
                        loading = false
                    }
                }
            }) { Text(if (loading) "Analysiere…" else "Kalorien schätzen") }
        }
        picked?.let { Text("Bild: $it", style = MaterialTheme.typography.bodySmall) }
        captured?.let { Text("Foto aufgenommen", style = MaterialTheme.typography.bodySmall) }
        estimate?.let { e ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Schätzung: ${'$'}{e.kcal} kcal (${e.confidence})", style = MaterialTheme.typography.titleMedium)
                    Text(e.details, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { 
                            scope.launch { 
                                repo.logIntake(e.kcal, "Essen (Foto)", "PHOTO")
                                // Trigger daily goal adjustment
                                repo.adjustDailyGoal(java.time.LocalDate.now())
                            } 
                        }) { Text("Buchen") }
                        OutlinedButton(onClick = { estimate = null; picked = null; captured = null }) { Text("Zurücksetzen") }
                    }
                }
            }
        }
        Spacer(Modifier.height(96.dp))
    }
}
