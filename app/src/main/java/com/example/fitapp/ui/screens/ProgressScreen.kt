package com.example.fitapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.fitapp.data.db.PersonalAchievementEntity
import com.example.fitapp.data.db.PersonalStreakEntity
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.data.repo.PersonalMotivationRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.fitapp.R

@Composable
fun ProgressScreen(contentPadding: PaddingValues) {
    val ctx = LocalContext.current
    val nutritionRepo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    val planDao = remember { AppDatabase.get(ctx).planDao() }
    val motivationRepo = remember { PersonalMotivationRepository(AppDatabase.get(ctx)) }
    
    // Get data for the last 7 days
    val last7Days = remember {
        (0..6).map { daysAgo ->
            LocalDate.now().minusDays(daysAgo.toLong())
        }.reversed()
    }
    
    val dailyCalories = remember { mutableStateMapOf<LocalDate, Int>() }
    val achievements by motivationRepo.allAchievementsFlow().collectAsState(initial = emptyList())
    val activeStreaks by motivationRepo.activeStreaksFlow().collectAsState(initial = emptyList())
    val personalRecords by motivationRepo.allRecordsFlow().collectAsState(initial = emptyList())
    val milestones by motivationRepo.allMilestonesFlow().collectAsState(initial = emptyList())
    
    LaunchedEffect(Unit) {
        last7Days.forEach { date ->
            val epochSec = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
            val total = nutritionRepo.totalForDay(epochSec)
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
        Text("Fortschritt", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        // Achievements Showcase
        AchievementShowcase(achievements)
        
        Spacer(Modifier.height(16.dp))
        
        // Active Streaks Display
        ActiveStreaksDisplay(activeStreaks)
        
        Spacer(Modifier.height(16.dp))
        
        // Personal Records Section
        PersonalRecordsSection(personalRecords)
        
        Spacer(Modifier.height(16.dp))
        
        // Progress Milestones
        ProgressMilestonesSection(milestones)
        
        Spacer(Modifier.height(16.dp))
        
        // Weekly Calorie Summary (existing)
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
        
        // Daily Breakdown Card (existing)
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
        
        // Training Plans Card (existing)
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Trainingspläne", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                val plans by planDao.plansFlow().collectAsState(initial = emptyList())
                
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

@Composable
private fun AchievementShowcase(achievements: List<PersonalAchievementEntity>) {
    Card {
        Box {
            // Background achievement image
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(R.drawable.generated_image_5),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alpha = 0.2f
            )
            
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏆 Erfolge", style = MaterialTheme.typography.titleMedium)
                    val completedCount = achievements.count { it.isCompleted }
                    Text(
                        "$completedCount/${achievements.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(12.dp))
                
                if (achievements.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(achievements.take(6)) { achievement ->
                            AchievementCard(achievement)
                        }
                    }
                    
                    if (achievements.size > 6) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "und ${achievements.size - 6} weitere...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    Text(
                        "Erfolge werden automatisch freigeschaltet, wenn du die App nutzt.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: PersonalAchievementEntity) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isCompleted) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Achievement icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isCompleted) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.outline
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (achievement.iconName) {
                        "fitness_center" -> Icons.Default.FitnessCenter
                        "emoji_events" -> Icons.Default.EmojiEvents
                        "restaurant" -> Icons.Default.Restaurant
                        "military_tech" -> Icons.Default.Star
                        "local_fire_department" -> Icons.Default.LocalFireDepartment
                        "self_improvement" -> Icons.Default.SelfImprovement
                        "sports_gymnastics" -> Icons.Default.SportsGymnastics
                        "book" -> Icons.Default.Book
                        else -> Icons.Default.EmojiEvents
                    },
                    contentDescription = achievement.title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                achievement.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            
            if (achievement.targetValue != null && !achievement.isCompleted) {
                val progress = (achievement.currentValue / achievement.targetValue).coerceIn(0.0, 1.0)
                
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "${achievement.currentValue.toInt()}/${achievement.targetValue.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ActiveStreaksDisplay(streaks: List<PersonalStreakEntity>) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥 Aktive Streaks", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${streaks.size} aktiv",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            
            if (streaks.isNotEmpty()) {
                streaks.take(4).forEach { streak ->
                    StreakRow(streak)
                    if (streak != streaks.last()) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
                
                if (streaks.size > 4) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "und ${streaks.size - 4} weitere...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                Text(
                    "Starte deine erste Streak, indem du regelmäßig trainierst oder deine Ernährung trackst.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun StreakRow(streak: PersonalStreakEntity) {
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
            Text(
                streak.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        
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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Tage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun PersonalRecordsSection(records: List<com.example.fitapp.data.db.PersonalRecordEntity>) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text("📈 Persönliche Rekorde", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            
            if (records.isNotEmpty()) {
                Text("${records.size} Rekorde erreicht", style = MaterialTheme.typography.bodyMedium)
                
                records.take(3).forEach { record ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${record.exerciseName} (${record.recordType})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${record.value} ${record.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            } else {
                Text(
                    "Persönliche Rekorde werden automatisch erkannt und gespeichert.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ProgressMilestonesSection(milestones: List<com.example.fitapp.data.db.ProgressMilestoneEntity>) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text("🎯 Meilensteine", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            
            if (milestones.isNotEmpty()) {
                val completedCount = milestones.count { it.isCompleted }
                Text("$completedCount/${milestones.size} abgeschlossen", style = MaterialTheme.typography.bodyMedium)
                
                milestones.take(3).forEach { milestone ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                milestone.title,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${milestone.progress.toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (milestone.progress / 100.0).toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            } else {
                Text(
                    "Erstelle Meilensteine, um deine Fortschritte zu verfolgen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}