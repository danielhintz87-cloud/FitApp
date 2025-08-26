package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.ui.components.BudgetBar
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TodayScreen(contentPadding: PaddingValues, navController: NavController? = null) {
    val ctx = LocalContext.current
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    val scope = rememberCoroutineScope()
    val todayEpoch = remember { LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond() }
    
    val goal by repo.goalFlow(LocalDate.now()).collectAsState(initial = null)
    val entries by repo.dayEntriesFlow(todayEpoch).collectAsState(initial = emptyList())
    val plans by repo.plansFlow().collectAsState(initial = emptyList())
    
    val consumed = entries.sumOf { it.kcal }
    val target = goal?.targetKcal ?: 2000
    val latestPlan = plans.firstOrNull()

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Heute", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        // Calorie Summary Card
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Kalorienbilanz", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                BudgetBar(consumed = consumed, target = target)
                Spacer(Modifier.height(8.dp))
                Text("Gegessen: $consumed kcal", style = MaterialTheme.typography.bodyMedium)
                Text("Ziel: $target kcal", style = MaterialTheme.typography.bodyMedium)
                Text("Verbleibend: ${maxOf(0, target - consumed)} kcal", style = MaterialTheme.typography.bodyMedium)
                
                if (latestPlan != null) {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                repo.setAIRecommendedGoal(ctx, LocalDate.now())
                            }
                        }
                    ) {
                        Text("AI-Empfehlung für Kalorienziel anwenden")
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Today's Meals Card
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Heutige Mahlzeiten", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (entries.isEmpty()) {
                    Text("Noch keine Mahlzeiten eingetragen.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    entries.take(5).forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(entry.label, style = MaterialTheme.typography.bodyMedium)
                            Text("${entry.kcal} kcal", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Training Plan Card  
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Heutiges Training", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (latestPlan != null) {
                    Text("Aktueller Plan: ${latestPlan.title}", style = MaterialTheme.typography.bodyMedium)
                    Text("Ziel: ${latestPlan.goal}", style = MaterialTheme.typography.bodySmall)
                    Text("${latestPlan.sessionsPerWeek}x pro Woche, ${latestPlan.minutesPerSession} Min", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { 
                                navController?.navigate("todaytraining")
                            }
                        ) {
                            Text("Heutiges Training anpassen")
                        }
                        OutlinedButton(
                            onClick = {
                                // TODO: Mark today's training as completed
                            }
                        ) {
                            Text("Training abgeschlossen")
                        }
                    }
                } else {
                    Text(
                        "Erstelle einen Trainingsplan im Plan-Bereich, um hier dein heutiges Workout zu sehen.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}