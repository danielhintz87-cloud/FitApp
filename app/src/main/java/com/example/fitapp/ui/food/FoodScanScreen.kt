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
    onLogged: () -> Unit = {},
    onBackPressed: (() -> Unit)? = null
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
    var showFoodValidationDialog by remember { mutableStateOf(false) }
    var customPrompt by remember { mutableStateOf("") }
    var analysisAttempts by remember { mutableStateOf(0) }
    
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

    Column(Modifier.fillMaxSize()) {
        // Top App Bar - only show if onBackPressed is provided
        onBackPressed?.let { backHandler ->
            TopAppBar(
                title = { Text("Food Scan") },
                navigationIcon = {
                    IconButton(onClick = backHandler) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(if (onBackPressed != null) PaddingValues(0.dp) else contentPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Only show title if no TopAppBar
            if (onBackPressed == null) {
                Text("Food Scan", style = MaterialTheme.typography.titleLarge)
            }

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

        // Custom prompt input section - only show after image is selected
        if (picked != null || captured != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Zusätzliche Informationen für die AI",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customPrompt,
                        onValueChange = { customPrompt = it },
                        label = { Text("Was ist auf dem Bild? (optional)") },
                        placeholder = { Text("z.B. 'Ein Apfel mit 200g' oder 'Pasta mit Tomatensauce'") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    if (customPrompt.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row {
                            OutlinedButton(
                                onClick = { customPrompt = "" },
                                modifier = Modifier.size(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("×", style = MaterialTheme.typography.labelLarge)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Diese Informationen helfen der AI bei einer genaueren Analyse",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                    analysisAttempts++
                    scope.launch {
                        try {
                            val prompt = if (customPrompt.isNotBlank()) {
                                "Bitte analysiere dieses Bild und identifiziere das Essen. Zusätzliche Informationen: $customPrompt. Prüfe ob es sich um echtes Essen handelt und schätze die Kalorien."
                            } else {
                                "Analysiere dieses Bild und identifiziere das Essen. Prüfe zuerst ob es sich um echtes, essbares Essen handelt. Wenn ja, schätze die Kalorien. Wenn nein, antworte mit 'KEIN_ESSEN_ERKANNT'."
                            }
                            
                            estimate = when {
                                captured != null -> {
                                    val result = AppAi.caloriesWithOptimalProvider(ctx, captured!!, prompt)
                                    if (result.isFailure) {
                                        CaloriesEstimate(0, 30, "Analyse fehlgeschlagen: ${result.exceptionOrNull()?.message}")
                                    } else {
                                        result.getOrNull()
                                    }
                                }
                                picked != null -> {
                                    try {
                                        val bitmap = BitmapUtils.loadBitmapFromUri(ctx.contentResolver, picked!!)
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
                                    showFoodValidationDialog = true
                                } else {
                                    showConfirmDialog = true
                                    editedKcal = est.kcal.toString()
                                    editedLabel = "Essen (Foto): ${est.text.take(50)}"
                                }
                            } ?: run {
                                // If estimate is null, show error dialog
                                estimate = CaloriesEstimate(0, 30, "Unbekannter Fehler bei der Analyse")
                                showFoodValidationDialog = true
                            }
                            
                        } catch (e: Exception) {
                            estimate = CaloriesEstimate(0, 30, "Analyse fehlgeschlagen: ${e.message}")
                            showFoodValidationDialog = true
                        } finally {
                            loading = false
                        }
                    }
                }
            ) {
                Text(if (loading) "Analysiere…" else "Bild analysieren")
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
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { 
                                // Try again with current prompt
                                loading = true
                                analysisAttempts++
                                scope.launch {
                                    try {
                                        val prompt = if (customPrompt.isNotBlank()) {
                                            "Bitte analysiere dieses Bild nochmals genauer. Zusätzliche Informationen: $customPrompt. Prüfe ob es sich um echtes Essen handelt und schätze die Kalorien."
                                        } else {
                                            "Analysiere dieses Bild nochmals genauer und identifiziere das Essen. Prüfe zuerst ob es sich um echtes, essbares Essen handelt."
                                        }
                                        
                                        estimate = when {
                                            captured != null -> AppAi.caloriesWithOptimalProvider(ctx, captured!!, prompt).getOrThrow()
                                            picked != null -> {
                                                val bitmap = BitmapUtils.loadBitmapFromUri(ctx.contentResolver, picked!!)
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
                            },
                            enabled = !loading
                        ) {
                            Text("🔄 Erneut analysieren")
                        }
                        OutlinedButton(onClick = { 
                            estimate = null
                            picked = null
                            captured = null
                            customPrompt = ""
                            analysisAttempts = 0
                        }) {
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
    
    // Food Validation Dialog (when no food is detected)
    if (showFoodValidationDialog) {
        AlertDialog(
            onDismissRequest = { showFoodValidationDialog = false },
            title = { Text("❌ Kein Essen erkannt") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Die AI konnte kein essbares Lebensmittel auf dem Bild identifizieren.")
                    
                    estimate?.let { e ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Text(
                                e.text,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    
                    Text("Was möchten Sie tun?")
                    
                    OutlinedTextField(
                        value = customPrompt,
                        onValueChange = { customPrompt = it },
                        label = { Text("Beschreiben Sie das Essen") },
                        placeholder = { Text("z.B. 'Apfel, ca. 150g' oder 'Nudeln mit Soße'") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customPrompt.isNotBlank()) {
                            // Try analysis again with user input
                            loading = true
                            analysisAttempts++
                            showFoodValidationDialog = false
                            scope.launch {
                                try {
                                    val prompt = "Analysiere dieses Bild als Essen: $customPrompt. Schätze die Kalorien für diese Beschreibung."
                                    
                                    estimate = when {
                                        captured != null -> AppAi.caloriesWithOptimalProvider(ctx, captured!!, prompt).getOrThrow()
                                        picked != null -> {
                                            val bitmap = BitmapUtils.loadBitmapFromUri(ctx.contentResolver, picked!!)
                                            AppAi.caloriesWithOptimalProvider(ctx, bitmap, prompt).getOrThrow()
                                        }
                                        else -> null
                                    }
                                    
                                    estimate?.let { est ->
                                        editedKcal = est.kcal.toString()
                                        editedLabel = "Essen (manuell): $customPrompt"
                                        showConfirmDialog = true
                                    }
                                } catch (e: Exception) {
                                    estimate = CaloriesEstimate(0, 50, "Analyse mit Beschreibung fehlgeschlagen: ${e.message}")
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    },
                    enabled = customPrompt.isNotBlank()
                ) {
                    Text("Mit Beschreibung analysieren")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { 
                        showFoodValidationDialog = false
                        // Suggest taking a new photo
                        picked = null
                        captured = null
                        estimate = null
                        customPrompt = ""
                    }) {
                        Text("Neues Foto")
                    }
                    OutlinedButton(onClick = {
                        showFoodValidationDialog = false
                        // Allow manual entry
                        editedKcal = "200"
                        editedLabel = "Manueller Eintrag"
                        showConfirmDialog = true
                    }) {
                        Text("Manuell eingeben")
                    }
                }
            }
        )
        }
    }
}
