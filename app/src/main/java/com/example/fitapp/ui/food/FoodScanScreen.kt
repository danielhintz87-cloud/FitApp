package com.example.fitapp.ui.food

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.CaloriesEstimate
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.ui.components.BudgetBar
import com.example.fitapp.util.BitmapUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun FoodScanScreen(
    contentPadding: PaddingValues,
    onLogged: () -> Unit
) {
    val ctx = LocalContext.current
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    val scope = rememberCoroutineScope()
    var picked by remember { mutableStateOf<Uri?>(null) }
    var captured by remember { mutableStateOf<Bitmap?>(null) }
    var estimate by remember { mutableStateOf<CaloriesEstimate?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var editedKcal by remember { mutableStateOf("") }
    var editedLabel by remember { mutableStateOf("") }
    
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
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) camera.launch(null)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                repo.setDailyGoal(LocalDate.now(), targetValue)
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
            Button(onClick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Text("Foto wählen")
            }
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    camera.launch(null)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }) {
                Text("Foto aufnehmen")
            }
            OutlinedButton(
                enabled = (picked != null || captured != null) && !loading,
                onClick = {
                    loading = true
                    scope.launch {
                        try {
                            estimate = when {
                                captured != null -> AppAi.caloriesWithOptimalProvider(ctx, captured!!, "").getOrThrow()
                                picked != null -> {
                                    val bitmap = BitmapUtils.loadBitmapFromUri(ctx.contentResolver, picked!!)
                                    AppAi.caloriesWithOptimalProvider(ctx, bitmap, "").getOrThrow()
                                }
                                else -> null
                            }
                        } catch (e: Exception) {
                            estimate = CaloriesEstimate(0, 60, "Analyse fehlgeschlagen: ${e.message}")
                        } finally {
                            loading = false
                        }
                    }
                }
            ) {
                Text(if (loading) "Analysiere…" else "Kalorien schätzen")
            }
        }

        picked?.let {
            AsyncImage(
                model = it,
                contentDescription = "Selected food image for analysis",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }
        captured?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        picked?.let { Text("Bild: $it", style = MaterialTheme.typography.bodySmall) }
        captured?.let { Text("Foto aufgenommen", style = MaterialTheme.typography.bodySmall) }

        estimate?.let { e ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Schätzung: ${e.kcal} kcal (${e.confidence}%)", style = MaterialTheme.typography.titleMedium)
                    Text(e.text, style = MaterialTheme.typography.bodyMedium)
                    
                    // Show uncertainty warning if confidence is low
                    if (e.confidence < 70) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Column(Modifier.padding(8.dp)) {
                                Text("⚠️ Unsichere Schätzung", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("Die AI ist unsicher bei dieser Analyse. Bitte prüfen Sie die Kalorien und korrigieren Sie sie wenn nötig.", 
                                     style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            editedKcal = e.kcal.toString()
                            editedLabel = "Essen (Foto)"
                            showConfirmDialog = true
                        }) { Text("Bestätigen & Buchen") }
                        OutlinedButton(onClick = { estimate = null; picked = null; captured = null }) {
                            Text("Zurücksetzen")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(96.dp))
    }
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Kalorien bestätigen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Bitte bestätigen oder korrigieren Sie die erkannten Werte:")
                    
                    // Show detected food description if available
                    estimate?.let { e ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(Modifier.padding(8.dp)) {
                                Text("Erkanntes Essen:", style = MaterialTheme.typography.labelMedium)
                                Text(e.text.take(100) + if (e.text.length > 100) "..." else "", 
                                     style = MaterialTheme.typography.bodySmall)
                                Text("Vertrauen: ${e.confidence}%", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = editedKcal,
                        onValueChange = { editedKcal = it },
                        label = { Text("Kalorien") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Korrigieren Sie die Kalorienzahl falls nötig") }
                    )
                    OutlinedTextField(
                        value = editedLabel,
                        onValueChange = { editedLabel = it },
                        label = { Text("Bezeichnung") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Beschreibung für das Ernährungstagebuch") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val kcal = editedKcal.toIntOrNull() ?: 0
                            repo.logIntake(kcal, editedLabel, "PHOTO")
                            repo.adjustDailyGoal(LocalDate.now())
                            showConfirmDialog = false
                            estimate = null
                            picked = null
                            captured = null
                            onLogged()
                        }
                    }
                ) {
                    Text("Buchen")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
