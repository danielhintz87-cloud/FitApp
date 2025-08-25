package com.example.fitapp.ui.screens

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
import com.example.fitapp.ai.AiGateway
import com.example.fitapp.ai.CalorieEstimate
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.ui.components.BudgetBar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScanScreen() {
    val ctx = LocalContext.current
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    val scope = rememberCoroutineScope()
    var picked by remember { mutableStateOf<Uri?>(null) }
    var estimate by remember { mutableStateOf<CalorieEstimate?>(null) }
    var loading by remember { mutableStateOf(false) }
    val todayEpoch = remember { LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond() }
    val goal by repo.goalFlow(LocalDate.now()).collectAsState(initial = null)
    val entries by repo.dayEntriesFlow(todayEpoch).collectAsState(initial = emptyList())
    val consumed = entries.sumOf { it.kcal }
    val target = goal?.targetKcal ?: 2000

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> picked = uri }
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Food Scan", style = MaterialTheme.typography.headlineSmall)
        BudgetBar(consumed = consumed, target = target)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("Foto wählen") }

            OutlinedButton(
                enabled = picked != null && !loading,
                onClick = {
                    val uri = picked ?: return@OutlinedButton
                    loading = true
                    scope.launch {
                        try {
                            val e = repo.analyzeFoodImage(ctx, uri, AiGateway.Provider.OPENAI)
                            estimate = e
                        } catch (t: Throwable) {
                            estimate = CalorieEstimate(0, "niedrig", "Analyse fehlgeschlagen: ${t.message}")
                        } finally {
                            loading = false
                        }
                    }
                }
            ) { Text(if (loading) "Analysiere…" else "Kalorien schätzen") }
        }

        picked?.let { Text("Bild gewählt: $it", style = MaterialTheme.typography.bodySmall) }

        estimate?.let { e ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Schätzung: ${e.kcal} kcal (${e.confidence})", style = MaterialTheme.typography.titleMedium)
                    Text(e.details, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            scope.launch {
                                repo.logIntake(e.kcal, label = "Essen (Foto)", source = "PHOTO")
                            }
                        }) { Text("Zu Tagesbilanz buchen") }
                        OutlinedButton(onClick = { estimate = null; picked = null }) { Text("Zurücksetzen") }
                    }
                }
            }
        }
        Spacer(Modifier.height(96.dp))
    }
}
