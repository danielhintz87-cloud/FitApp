package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AiProvider
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.PlanRequest
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import kotlinx.coroutines.launch

@Composable
fun PlanScreen(contentPadding: PaddingValues, provider: AiProvider) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    var goal by remember { mutableStateOf("Muskelaufbau") }
    var sessions by remember { mutableStateOf("3") }
    var minutes by remember { mutableStateOf("60") }
    var equipment by remember { mutableStateOf("Hanteln, Klimmzugstange") }
    var result by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var saveStatus by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("12-Wochen-Trainingsplan", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it },
            label = { Text("Ziel") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = sessions,
            onValueChange = { sessions = it },
            label = { Text("Sessions pro Woche") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = minutes,
            onValueChange = { minutes = it },
            label = { Text("Minuten pro Session") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = equipment,
            onValueChange = { equipment = it },
            label = { Text("Verfügbare Geräte") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        Button(
            enabled = !busy,
            onClick = {
                scope.launch {
                    busy = true
                    result = try {
                        val req = PlanRequest(
                            goal = goal,
                            weeks = 12,
                            sessionsPerWeek = sessions.toIntOrNull() ?: 3,
                            minutesPerSession = minutes.toIntOrNull() ?: 60,
                            equipment = equipment.split(",").map { it.trim() }
                        )
                        val planContent = if (provider == AiProvider.Auto) {
                            AppAi.planWithOptimalProvider(ctx, req).getOrThrow()
                        } else {
                            AppAi.plan(ctx, provider, req).getOrThrow()
                        }
                        
                        // Save the plan to database
                        val planId = repo.savePlan(
                            title = "12-Wochen-Trainingsplan: $goal",
                            content = planContent,
                            goal = goal,
                            weeks = 12,
                            sessionsPerWeek = req.sessionsPerWeek,
                            minutesPerSession = req.minutesPerSession,
                            equipment = req.equipment
                        )
                        saveStatus = "✓ Plan gespeichert (ID: $planId)"
                        
                        planContent
                    } catch (e: Exception) {
                        saveStatus = ""
                        "Fehler: ${e.message}"
                    } finally {
                        busy = false
                    }
                }
            }
        ) {
            Text(if (busy) "Generiere..." else "Plan erstellen")
        }
        
        if (saveStatus.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(saveStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        
        if (result.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Card {
                Text(result, Modifier.padding(16.dp))
            }
        }
    }
}
