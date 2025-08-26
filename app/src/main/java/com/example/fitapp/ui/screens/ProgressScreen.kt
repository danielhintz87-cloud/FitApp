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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ProgressScreen(contentPadding: PaddingValues) {
    val ctx = LocalContext.current
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    
    // Get data for the last 7 days
    val last7Days = remember {
        (0..6).map { daysAgo ->
            LocalDate.now().minusDays(daysAgo.toLong())
        }.reversed()
    }
    
    val dailyCalories = remember { mutableStateMapOf<LocalDate, Int>() }
    
    LaunchedEffect(Unit) {
        last7Days.forEach { date ->
            val epochSec = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
            val total = repo.totalForDay(epochSec)
            dailyCalories[date] = total
        }
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Progress", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        // Weekly Calorie Summary
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Wöchentliche Übersicht", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                val totalWeeklyCalories = dailyCalories.values.sum()
                val avgDailyCalories = if (dailyCalories.isNotEmpty()) totalWeeklyCalories / dailyCalories.size else 0
                
                Text("Gesamtkalorien diese Woche: $totalWeeklyCalories kcal", style = MaterialTheme.typography.bodyMedium)
                Text("Durchschnitt pro Tag: $avgDailyCalories kcal", style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Daily Breakdown Card
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Letzte 7 Tage", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                last7Days.forEach { date ->
                    val calories = dailyCalories[date] ?: 0
                    val dateStr = date.format(DateTimeFormatter.ofPattern("dd.MM"))
                    val dayName = when (date.dayOfWeek.value) {
                        1 -> "Mo"
                        2 -> "Di" 
                        3 -> "Mi"
                        4 -> "Do"
                        5 -> "Fr"
                        6 -> "Sa"
                        7 -> "So"
                        else -> ""
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$dayName $dateStr", style = MaterialTheme.typography.bodyMedium)
                        Text("$calories kcal", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Training Plans Card
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Trainingspläne", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                val plans by repo.plansFlow().collectAsState(initial = emptyList())
                
                if (plans.isEmpty()) {
                    Text("Noch keine Trainingspläne erstellt.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Erstellt: ${plans.size} Pläne", style = MaterialTheme.typography.bodyMedium)
                    
                    plans.take(3).forEach { plan ->
                        val createdDate = Instant.ofEpochSecond(plan.createdAt)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                        
                        Column {
                            Text(plan.title, style = MaterialTheme.typography.bodyMedium)
                            Text("Erstellt: $createdDate", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}