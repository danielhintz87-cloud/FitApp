package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.PersonalAchievementEntity
import com.example.fitapp.data.db.PersonalStreakEntity
import com.example.fitapp.services.StreakManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealLoggingStreakCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val streakManager = remember { StreakManager(context) }
    
    var currentStreak by remember { mutableStateOf<PersonalStreakEntity?>(null) }
    var recentAchievements by remember { mutableStateOf<List<PersonalAchievementEntity>>(emptyList()) }
    
    // Load streak data
    LaunchedEffect(Unit) {
        scope.launch {
            currentStreak = streakManager.getCurrentMealLoggingStreak()
            
            // Get recent meal logging achievements
            val db = AppDatabase.get(context)
            recentAchievements = db.personalAchievementDao()
                .achievementsByCategoryFlow("nutrition")
                .first()
                .filter { it.isCompleted }
                .sortedByDescending { it.completedAt }
                .take(3)
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meal Logging Streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Current streak display
            currentStreak?.let { streak ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StreakStatCard(
                        title = "Current",
                        value = streak.currentStreak.toString(),
                        unit = "days",
                        icon = Icons.Default.LocalFireDepartment,
                        color = if (streak.currentStreak > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    
                    StreakStatCard(
                        title = "Best",
                        value = streak.longestStreak.toString(),
                        unit = "days",
                        icon = Icons.Default.MilitaryTech,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } ?: run {
                Text(
                    text = "Start logging meals to build your streak!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Recent achievements
            if (recentAchievements.isNotEmpty()) {
                Text(
                    text = "Recent Achievements",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentAchievements) { achievement ->
                        AchievementBadge(achievement = achievement)
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakStatCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AchievementBadge(
    achievement: PersonalAchievementEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (achievement.badgeType) {
                "diamond" -> Color(0xFFB9F2FF)
                "platinum" -> Color(0xFFE5E4E2)
                "gold" -> Color(0xFFFFD700).copy(alpha = 0.3f)
                "silver" -> Color(0xFFC0C0C0).copy(alpha = 0.3f)
                "bronze" -> Color(0xFFCD7F32).copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val badgeIcon = when (achievement.iconName) {
                "emoji_food_beverage" -> Icons.Default.EmojiFoodBeverage
                "local_fire_department" -> Icons.Default.LocalFireDepartment
                "military_tech" -> Icons.Default.MilitaryTech
                "workspace_premium" -> Icons.Default.WorkspacePremium
                "diamond" -> Icons.Default.Diamond
                else -> Icons.Default.Star
            }
            
            Icon(
                imageVector = badgeIcon,
                contentDescription = achievement.title,
                modifier = Modifier.size(20.dp),
                tint = when (achievement.badgeType) {
                    "diamond" -> Color(0xFF00BCD4)
                    "platinum" -> Color(0xFF9E9E9E)
                    "gold" -> Color(0xFFFFD700)
                    "silver" -> Color(0xFFC0C0C0)
                    "bronze" -> Color(0xFFCD7F32)
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            Text(
                text = achievement.targetValue?.toInt()?.toString() ?: "",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}