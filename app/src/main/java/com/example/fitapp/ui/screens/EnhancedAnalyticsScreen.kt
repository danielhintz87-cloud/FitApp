package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.data.repo.PersonalMotivationRepository
import com.example.fitapp.data.repo.WeightLossRepository
import com.example.fitapp.domain.AnalyticsPeriod
import com.example.fitapp.ui.components.charts.LineChart
import com.example.fitapp.ui.components.charts.PieChart
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAnalyticsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    navController: NavController? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.get(context) }
    val nutritionRepo = remember { NutritionRepository(db) }
    val motivationRepo = remember { PersonalMotivationRepository(db) }
    val weightLossRepo = remember { WeightLossRepository(db) }
    
    // State für verschiedene Analytics-Daten
    var selectedPeriod by remember { mutableStateOf(AnalyticsPeriod.WEEK) }
    val weightHistory by weightLossRepo.weightHistoryFlow(selectedPeriod.days).collectAsState(initial = emptyList())
    val calorieHistory by nutritionRepo.calorieHistoryFlow(selectedPeriod.days).collectAsState(initial = emptyList())
    val achievements by motivationRepo.achievementsByCompletionFlow(true).collectAsState(initial = emptyList())
    val streaks by motivationRepo.activeStreaksFlow().collectAsState(initial = emptyList())
    val personalRecords by motivationRepo.allRecordsFlow().collectAsState(initial = emptyList())
    
    var isLoading by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        // Modern Top App Bar mit Period Selector
        TopAppBar(
            title = { 
                Text(
                    "Analytics Dashboard",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                // Period Filter
                FilterChip(
                    onClick = { 
                        selectedPeriod = when(selectedPeriod) {
                            AnalyticsPeriod.WEEK -> AnalyticsPeriod.MONTH
                            AnalyticsPeriod.MONTH -> AnalyticsPeriod.QUARTER
                            AnalyticsPeriod.QUARTER -> AnalyticsPeriod.YEAR
                            AnalyticsPeriod.YEAR -> AnalyticsPeriod.WEEK
                        }
                    },
                    label = { 
                        Text(selectedPeriod.displayName) 
                    },
                    selected = true,
                    leadingIcon = {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { 
                    scope.launch { 
                        isLoading = true
                        // Refresh data logic here
                        isLoading = false
                    }
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren")
                }
            }
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Cards Row
                item {
                    AnalyticsSummaryCards(
                        achievementsCount = achievements.size,
                        activeStreaksCount = streaks.size,
                        personalRecordsCount = personalRecords.size
                    )
                }
                
                // Weight Progress Chart
                if (weightHistory.isNotEmpty()) {
                    item {
                        LineChart(
                            data = weightHistory.map { it.weight },
                            labels = weightHistory.map { it.date },
                            title = "Gewichtsverlauf (${selectedPeriod.displayName})",
                            modifier = Modifier.fillMaxWidth(),
                            lineColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Calorie Trend Chart
                if (calorieHistory.isNotEmpty()) {
                    item {
                        LineChart(
                            data = calorieHistory.map { it.second },
                            labels = calorieHistory.map { it.first },
                            title = "Kalorienverlauf (${selectedPeriod.displayName})",
                            modifier = Modifier.fillMaxWidth(),
                            lineColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                // Achievement Progress
                item {
                    AchievementAnalyticsCard(achievements)
                }
                
                // Active Streaks Analytics
                item {
                    StreakAnalyticsCard(streaks)
                }
                
                // Personal Records Summary
                if (personalRecords.isNotEmpty()) {
                    item {
                        PersonalRecordsAnalyticsCard(personalRecords)
                    }
                }
                
                // AI Insights Placeholder
                item {
                    AIInsightsCard(selectedPeriod)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsSummaryCards(
    achievementsCount: Int,
    activeStreaksCount: Int,
    personalRecordsCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Erfolge",
            value = achievementsCount.toString(),
            icon = Icons.Default.EmojiEvents,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Streaks",
            value = activeStreaksCount.toString(),
            icon = Icons.Default.LocalFireDepartment,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Rekorde",
            value = personalRecordsCount.toString(),
            icon = Icons.Default.Star,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AchievementAnalyticsCard(achievements: List<com.example.fitapp.data.db.PersonalAchievementEntity>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "🏆 Erfolge Analyse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (achievements.isNotEmpty()) {
                val completedCount = achievements.count { it.isCompleted }
                val totalCount = achievements.size
                val completionRate = if (totalCount > 0) (completedCount.toFloat() / totalCount) else 0f
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Abgeschlossen:")
                    Text("$completedCount / $totalCount (${(completionRate * 100).toInt()}%)")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { completionRate },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    "Noch keine Erfolge verfolgt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StreakAnalyticsCard(streaks: List<com.example.fitapp.data.db.PersonalStreakEntity>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "🔥 Streak Analyse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (streaks.isNotEmpty()) {
                val longestStreak = streaks.maxByOrNull { it.currentStreak }
                val totalDays = streaks.sumOf { it.currentStreak }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Längste Streak:")
                    Text("${longestStreak?.currentStreak ?: 0} Tage")
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Gesamt Tage:")
                    Text("$totalDays Tage")
                }
            } else {
                Text(
                    "Noch keine aktiven Streaks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PersonalRecordsAnalyticsCard(records: List<com.example.fitapp.data.db.PersonalRecordEntity>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "📈 Rekord Analyse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            val exerciseGroups = records.groupBy { it.exerciseName }
            
            Text("Übungen mit Rekorden: ${exerciseGroups.size}")
            Spacer(modifier = Modifier.height(8.dp))
            
            exerciseGroups.entries.take(3).forEach { (exercise, exerciseRecords) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        exercise,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${exerciseRecords.size} Rekorde",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AIInsightsCard(period: AnalyticsPeriod) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "🤖 AI Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "Basierend auf deinen Daten der letzten ${period.displayName}:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "• Deine Trainingsstreaks zeigen konstante Verbesserung\n" +
                "• Kalorienaufnahme ist gut im Zielbereich\n" +
                "• Empfehlung: Fokus auf Protein-Intake erhöhen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}