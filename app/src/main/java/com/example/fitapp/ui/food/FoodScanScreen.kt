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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScanScreen(
    contentPadding: PaddingValues,
    onLogged: () -> Unit = {}
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
    var analysisAttempts by remember { mutableStateOf(0) }
    var manualFoodDescription by remember { mutableStateOf("") }
    var isEstimatingManualCalories by remember { mutableStateOf(false) }
    
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

        // Daily Goal Display - Show training plan derived calories instead of manual input
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Tageskalorien (aus Trainingsplan)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                var recommendedCalories by remember { mutableStateOf<Int?>(null) }
                
                // Load recommended calories based on training plan
                LaunchedEffect(Unit) {
                    try {
                        recommendedCalories = repo.generateAICalorieRecommendation()
                    } catch (e: Exception) {
                        // Fallback to current goal if no plan exists
                        recommendedCalories = target
                    }
                }
                
                recommendedCalories?.let { recommended ->
                    val remaining = recommended - consumed
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Empfohlen: $recommended kcal", style = MaterialTheme.typography.bodyMedium)
                        Text("Verbraucht: $consumed kcal", style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        if (remaining > 0) "Noch verfügbar: $remaining kcal" else "Tagesbudget erreicht",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (remaining > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    
                    // Update daily goal to match recommendation if different
                    if (target != recommended) {
                        LaunchedEffect(recommended) {
                            repo.setDailyGoal(LocalDate.now(), recommended)
                        }
                    }
                } ?: run {
                    Text("Lade Empfehlung...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        BudgetBar(consumed = consumed, target = target)
        
        // Auto-analyze when image is captured/picked
        LaunchedEffect(picked, captured) {
            if ((picked != null || captured != null) && !loading && estimate == null) {
                loading = true
                analysisAttempts++
                try {
                    val prompt = "Analysiere dieses Bild und identifiziere das Essen. Prüfe zuerst ob es sich um echtes, essbares Essen handelt. Wenn ja, schätze die Kalorien. Wenn nein, antworte mit 'KEIN_ESSEN_ERKANNT'."
                    
                    estimate = when {
                        captured != null -> {
                            val result = AppAi.caloriesWithOptimalProvider(ctx, captured, prompt)
                            if (result.isFailure) {
                                CaloriesEstimate(0, 30, "Analyse fehlgeschlagen: ${result.exceptionOrNull()?.message}")
                            } else {
                                result.getOrNull()
                            }
                        }
                        picked != null -> {
                            try {
                                val bitmap = BitmapUtils.loadBitmapFromUri(ctx.contentResolver, picked)
                                val result = AppAi.caloriesWithOptimalProvider(ctx, bitmap, prompt)
                                if (result.isFailure) {
                                    CaloriesEstimate(0, 30, "Analyse fehlgeschlagen: ${result.exceptionOrNull()?.message}")
                                } else {
                                    result.getOrNull()
                                }
                            } catch (e: Exception) {
                                CaloriesEstimate(0, 30, "Bild konnte nicht geladen werden: ${e.message}")
                            }
                        }
                        else -> null
                    }
                    
                    // Check if food was detected or if there was an error
                    estimate?.let { est ->
                        if (est.text.contains("KEIN_ESSEN_ERKANNT", ignoreCase = true) || 
                            est.text.contains("kein essen", ignoreCase = true) ||
                            est.text.contains("fehlgeschlagen", ignoreCase = true) ||
                            est.kcal == 0) {
                            // Keep estimate to show error UI, but don't show confirm dialog
                        } else {
                            // Food was detected successfully
                            showConfirmDialog = true
                            editedKcal = est.kcal.toString()
                            editedLabel = "Essen (Foto): ${est.text.take(50)}"
                        }
                    }
                    
                } catch (e: Exception) {
                    estimate = CaloriesEstimate(0, 30, "Analyse fehlgeschlagen: ${e.message}")
                } finally {
                    loading = false
                }
            }
        }
        
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
            // Only show manual re-analyze button if analysis failed or was incorrect
            if ((picked != null || captured != null) && estimate != null) {
                OutlinedButton(
                    enabled = !loading,
                    onClick = {
                        loading = true
                        analysisAttempts++
                        scope.launch {
                            try {
                                val prompt = "Analysiere dieses Bild nochmals genauer und identifiziere das Essen. Prüfe zuerst ob es sich um echtes, essbares Essen handelt."
                                
                                estimate = when {
                                    captured != null -> AppAi.caloriesWithOptimalProvider(ctx, captured, prompt).getOrThrow()
                                    picked != null -> {
                                        val bitmap = BitmapUtils.loadBitmapFromUri(ctx.contentResolver, picked)
                                        AppAi.caloriesWithOptimalProvider(ctx, bitmap, prompt).getOrThrow()
                                    }
                                    else -> null
                                }
                            } catch (e: Exception) {
                                estimate = CaloriesEstimate(0, 30, "Erneute Analyse fehlgeschlagen: ${e.message}")
                            } finally {
                                loading = false
                            }
                        }
                    }
                ) {
                    Text("🔄 Erneut analysieren")
                }
                
                OutlinedButton(onClick = { 
                    estimate = null
                    picked = null
                    captured = null
                }) {
                    Text("📷 Neues Foto")
                }
            }
        }

        // Manual Food Entry Section
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("📝 Manueller Eintrag", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = manualFoodDescription,
                    onValueChange = { manualFoodDescription = it },
                    label = { Text("Beschreibung des Essens") },
                    placeholder = { Text("z.B. 'gekochtes Ei', 'Apfel', 'Pasta mit Tomatensauce'") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (manualFoodDescription.isNotBlank()) {
                                isEstimatingManualCalories = true
                                scope.launch {
                                    try {
                                        val estimatedKcal = AppAi.estimateCaloriesForManualEntry(ctx, manualFoodDescription).getOrThrow()
                                        editedKcal = estimatedKcal.toString()
                                        editedLabel = "Manuell: $manualFoodDescription"
                                        showConfirmDialog = true
                                    } catch (e: Exception) {
                                        // Fallback to manual input if AI estimation fails
                                        editedKcal = ""
                                        editedLabel = "Manuell: $manualFoodDescription"
                                        showConfirmDialog = true
                                    } finally {
                                        isEstimatingManualCalories = false
                                    }
                                }
                            }
                        },
                        enabled = manualFoodDescription.isNotBlank() && !isEstimatingManualCalories,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isEstimatingManualCalories) {
                            Text("🤖 Schätze...")
                        } else {
                            Text("🤖 KI-Schätzung + Buchen")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = {
                            if (manualFoodDescription.isNotBlank()) {
                                editedKcal = ""
                                editedLabel = "Manuell: $manualFoodDescription"
                                showConfirmDialog = true
                            }
                        },
                        enabled = manualFoodDescription.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("✏️ Selbst eingeben")
                    }
                }
                
                if (manualFoodDescription.isBlank()) {
                    Text(
                        "Geben Sie eine Beschreibung des Essens ein für KI-Kalorienschätzung oder manuelle Eingabe",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AI Analyse", style = MaterialTheme.typography.titleMedium)
                        Text("Versuch #$analysisAttempts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    
                    if (e.kcal > 0) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("🍽️ Erkanntes Essen", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Kalorien: ${e.kcal} kcal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Vertrauenswürdigkeit: ${e.confidence}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                    
                    Text("Beschreibung:", style = MaterialTheme.typography.titleSmall)
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
                    
                    if (e.kcal > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    editedKcal = e.kcal.toString()
                                    editedLabel = "Essen (Foto): ${e.text.take(50)}"
                                    showConfirmDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) { 
                                Text("✓ Korrekt & Buchen") 
                            }
                            OutlinedButton(
                                onClick = {
                                    editedKcal = e.kcal.toString()
                                    editedLabel = "Essen (Foto): ${e.text.take(50)}"
                                    showConfirmDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Bearbeiten")
                            }
                        }
                    } else {
                        // If food not detected, don't show redundant error input, just guide to manual entry
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("❌ Kein Essen erkannt", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("Nutzen Sie bitte den 'Manueller Eintrag' unten für eine manuelle Eingabe.", 
                                     style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
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
