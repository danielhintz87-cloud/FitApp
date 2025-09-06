package com.example.fitapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.data.repo.PersonalMotivationRepository
import com.example.fitapp.ui.components.BudgetBar
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import com.example.fitapp.R

@Composable
fun TodayScreen(contentPadding: PaddingValues, navController: NavController? = null) {
    val ctx = LocalContext.current
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    val motivationRepo = remember { PersonalMotivationRepository(AppDatabase.get(ctx)) }
    val scope = rememberCoroutineScope()
    val todayEpoch = remember { LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond() }
    
    val goal by repo.goalFlow(LocalDate.now()).collectAsState(initial = null)
    val entries by repo.dayEntriesFlow(todayEpoch).collectAsState(initial = emptyList())
    val plans by repo.plansFlow().collectAsState(initial = emptyList())
    
    // Motivation data
    val activeStreaks by motivationRepo.activeStreaksFlow().collectAsState(initial = emptyList())
    val recentAchievements by motivationRepo.achievementsByCompletionFlow(true).collectAsState(initial = emptyList())
    
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
        
        // Daily Motivation Card
        DailyMotivationCard(activeStreaks, recentAchievements)
        
        Spacer(Modifier.height(16.dp))
        
        // Today's Streaks Status Card
        TodayStreaksCard(activeStreaks)
        
        Spacer(Modifier.height(16.dp))
        
        // Calorie Summary Card (existing, enhanced)
        Card {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kalorienbilanz", style = MaterialTheme.typography.titleMedium)
                    
                    // Quick goal status
                    val goalProgress = if (target > 0) (consumed.toFloat() / target) else 0f
                    val statusColor = when {
                        goalProgress >= 0.8f -> MaterialTheme.colorScheme.primary
                        goalProgress >= 0.5f -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.outline
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }
                
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
                                repo.setAIRecommendedGoal(LocalDate.now())
                            }
                        }
                    ) {
                        Text("AI-Empfehlung für Kalorienziel anwenden")
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Today's Meals Card (existing)
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
        
        // Training Plan Card (existing, enhanced)
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
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Anpassen")
                        }
                        Button(
                            onClick = {
                                // Use plan data for daily workout
                                val planGoal = latestPlan.goal
                                val minutes = latestPlan.minutesPerSession
                                navController?.navigate("daily_workout/$planGoal/$minutes")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Training starten")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    // HIIT Builder button row
                    OutlinedButton(
                        onClick = { 
                            navController?.navigate("hiit_builder")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Speed, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("HIIT Builder")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val today = LocalDate.now()
                                        val dateIso = today.toString()
                                        
                                        // Create or update today's workout as completed
                                        repo.setWorkoutStatus(
                                            dateIso = dateIso,
                                            status = "completed",
                                            completedAt = System.currentTimeMillis() / 1000
                                        )
                                        
                                        // Trigger workout streak tracking
                                        val streakManager = com.example.fitapp.services.PersonalStreakManager(
                                            ctx,
                                            PersonalMotivationRepository(AppDatabase.get(ctx))
                                        )
                                        streakManager.trackWorkoutCompletion(today)
                                    } catch (e: Exception) {
                                        android.util.Log.e("TodayScreen", "Error marking training as completed", e)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Abgeschlossen")
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val today = LocalDate.now()
                                        val dateIso = today.toString()
                                        
                                        // Create or update today's workout as skipped
                                        repo.setWorkoutStatus(
                                            dateIso = dateIso,
                                            status = "skipped",
                                            completedAt = System.currentTimeMillis() / 1000
                                        )
                                        
                                        // Note: Skipped workouts don't trigger streak tracking
                                    } catch (e: Exception) {
                                        android.util.Log.e("TodayScreen", "Error skipping training", e)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.SkipNext, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Überspringen")
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

@Composable
private fun DailyMotivationCard(
    activeStreaks: List<com.example.fitapp.data.db.PersonalStreakEntity>,
    recentAchievements: List<com.example.fitapp.data.db.PersonalAchievementEntity>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box {
            // Background motivational image  
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(R.drawable.generated_image),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alpha = 0.3f
            )
            
            Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = "Motivation",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Deine tägliche Motivation",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            val motivationText = generateMotivationText(activeStreaks, recentAchievements)
            Text(
                motivationText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            if (activeStreaks.isNotEmpty() || recentAchievements.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (activeStreaks.isNotEmpty()) {
                        val longestStreak = activeStreaks.maxOf { it.currentStreak }
                        QuickStat(
                            icon = Icons.Default.LocalFireDepartment,
                            value = "$longestStreak",
                            label = "Tage Streak"
                        )
                    }
                    
                    if (recentAchievements.isNotEmpty()) {
                        QuickStat(
                            icon = Icons.Default.EmojiEvents,
                            value = "${recentAchievements.size}",
                            label = "Erfolge"
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun TodayStreaksCard(activeStreaks: List<com.example.fitapp.data.db.PersonalStreakEntity>) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥 Heutige Streaks", style = MaterialTheme.typography.titleMedium)
                if (activeStreaks.isNotEmpty()) {
                    Text(
                        "${activeStreaks.size} aktiv",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            if (activeStreaks.isNotEmpty()) {
                activeStreaks.take(3).forEach { streak ->
                    StreakStatusRow(streak)
                    if (streak != activeStreaks.last() && activeStreaks.indexOf(streak) < 2) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
                
                if (activeStreaks.size > 3) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "und ${activeStreaks.size - 3} weitere Streaks...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                Text(
                    "Keine aktiven Streaks. Starte heute eine neue Streak!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun StreakStatusRow(streak: com.example.fitapp.data.db.PersonalStreakEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                streak.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            // Show if today's activity is needed
            val needsTodayActivity = streak.lastActivityTimestamp?.let { timestamp ->
                val lastActivity = LocalDate.ofEpochDay(timestamp / 86400)
                lastActivity != LocalDate.now()
            } ?: true
            if (needsTodayActivity) {
                Text(
                    "Heute noch nicht erledigt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    "Heute bereits erledigt ✓",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "${streak.currentStreak}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Best: ${streak.longestStreak}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun QuickStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun generateMotivationText(
    activeStreaks: List<com.example.fitapp.data.db.PersonalStreakEntity>,
    recentAchievements: List<com.example.fitapp.data.db.PersonalAchievementEntity>
): String {
    val messages = mutableListOf<String>()
    
    if (activeStreaks.isNotEmpty()) {
        val longestStreak = activeStreaks.maxOf { it.currentStreak }
        when {
            longestStreak >= 30 -> messages.add("Unglaublich! Du hast bereits $longestStreak Tage am Stück durchgehalten! 👑")
            longestStreak >= 14 -> messages.add("Fantastisch! Deine $longestStreak-Tage Streak ist beeindruckend! 🔥")
            longestStreak >= 7 -> messages.add("Super! Du bist bereits $longestStreak Tage dabei! 💪")
            else -> messages.add("Du bist auf einem guten Weg! Halte deine Streak aufrecht! 🎯")
        }
    }
    
    if (recentAchievements.isNotEmpty()) {
        val recent = recentAchievements.take(3)
        if (recent.size == 1) {
            messages.add("Glückwunsch zu deinem Erfolg '${recent.first().title}'! 🏆")
        } else {
            messages.add("Du hast bereits ${recent.size} Erfolge freigeschaltet! Weiter so! 🌟")
        }
    }
    
    if (messages.isEmpty()) {
        messages.add("Heute ist ein perfekter Tag, um deine Fitnessziele zu verfolgen! 💪")
    }
    
    return messages.joinToString(" ")
}