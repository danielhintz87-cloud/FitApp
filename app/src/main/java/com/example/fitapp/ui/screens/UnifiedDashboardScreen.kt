package com.example.fitapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.data.repo.PersonalMotivationRepository
import com.example.fitapp.ui.util.applyContentPadding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

/**
 * 🚀 UNIFIED DASHBOARD - Revolutionary Fitness Experience
 * 
 * Central hub that intelligently connects ALL FitApp features:
 * - BMI & Weight Loss Journey
 * - Intervallfasten Progress
 * - Nutrition Tracking with AI insights
 * - Training Plans & Achievements
 * - Health Data Integration
 * - AI Personal Trainer
 * - Recipe Recommendations
 * - Social Challenges
 * 
 * Smart Cross-Feature Workflows:
 * BMI Goal → Fasting Protocol → Meal Planning → Training Adaptation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedDashboardScreen(
    contentPadding: PaddingValues,
    onNavigateToFeature: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.get(context) }
    val nutritionRepo = remember { NutritionRepository(database) }
    val motivationRepo = remember { PersonalMotivationRepository(database) }
    
    // Collect all data streams
    val today = LocalDate.now()
    val todayEpoch = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    
    val nutritionGoal by nutritionRepo.goalFlow(today).collectAsState(initial = null)
    val nutritionEntries by nutritionRepo.dayEntriesFlow(todayEpoch).collectAsState(initial = emptyList())
    val trainingPlans by nutritionRepo.plansFlow().collectAsState(initial = emptyList())
    val activeStreaks by motivationRepo.activeStreaksFlow().collectAsState(initial = emptyList())
    val achievements by motivationRepo.achievementsByCompletionFlow(false).collectAsState(initial = emptyList())
    
    // Calculate unified metrics
    val caloriesConsumed = nutritionEntries.sumOf { it.kcal }
    val caloriesTarget = nutritionGoal?.targetKcal ?: 2000
    val calorieProgress = if (caloriesTarget > 0) (caloriesConsumed.toFloat() / caloriesTarget) else 0f
    
    val longestStreak = activeStreaks.maxOfOrNull { it.currentStreak } ?: 0
    val completedAchievements = achievements.count { it.isCompleted }
    val totalAchievements = achievements.size
    
    var expandedCard by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .applyContentPadding(contentPadding)
    ) {
        // Header with dynamic greeting
        DynamicGreetingHeader(longestStreak, completedAchievements)
        
        Spacer(Modifier.height(20.dp))
        
        // Smart Health Overview Card
        SmartHealthOverviewCard(
            calorieProgress = calorieProgress,
            caloriesConsumed = caloriesConsumed,
            caloriesTarget = caloriesTarget,
            longestStreak = longestStreak,
            onNavigateToFeature = onNavigateToFeature
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Quick Action Hub
        QuickActionHub(onNavigateToFeature)
        
        Spacer(Modifier.height(16.dp))
        
        // AI Insights Card
        AIInsightsCard(
            expanded = expandedCard == "ai_insights",
            onToggle = { 
                expandedCard = if (expandedCard == "ai_insights") null else "ai_insights"
            },
            calorieProgress = calorieProgress,
            streakCount = longestStreak,
            onNavigateToFeature = onNavigateToFeature
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Cross-Feature Workflow Cards
        CrossFeatureWorkflowSection(
            expanded = expandedCard == "workflows",
            onToggle = { 
                expandedCard = if (expandedCard == "workflows") null else "workflows"
            },
            onNavigateToFeature = onNavigateToFeature
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Achievement Progress Showcase
        AchievementProgressShowcase(
            achievements = achievements.take(6),
            onNavigateToFeature = onNavigateToFeature
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Health Integration Status
        HealthIntegrationStatusCard(onNavigateToFeature)
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DynamicGreetingHeader(longestStreak: Int, completedAchievements: Int) {
    val currentHour = java.time.LocalTime.now().hour
    val greeting = when {
        currentHour < 12 -> "Guten Morgen"
        currentHour < 18 -> "Guten Tag"
        else -> "Guten Abend"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = buildString {
                        append("Deine Fitness-Journey läuft perfekt! ")
                        if (longestStreak > 0) {
                            append("🔥 $longestStreak Tage Streak ")
                        }
                        if (completedAchievements > 0) {
                            append("🏆 $completedAchievements Erfolge erreicht")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SmartHealthOverviewCard(
    calorieProgress: Float,
    caloriesConsumed: Int,
    caloriesTarget: Int,
    longestStreak: Int,
    onNavigateToFeature: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Dashboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Smart Health Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Health Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calorie Progress
                HealthMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Kalorien",
                    value = "${(calorieProgress * 100).roundToInt()}%",
                    subtitle = "$caloriesConsumed / $caloriesTarget",
                    icon = Icons.Default.LocalFireDepartment,
                    progress = calorieProgress,
                    progressColor = when {
                        calorieProgress < 0.8f -> MaterialTheme.colorScheme.primary
                        calorieProgress <= 1.2f -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.error
                    },
                    onClick = { onNavigateToFeature("nutrition") }
                )
                
                // Streak Status
                HealthMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Streak",
                    value = "$longestStreak",
                    subtitle = "Tage aktiv",
                    icon = Icons.Default.LocalFireDepartment,
                    progress = (longestStreak / 30f).coerceAtMost(1f),
                    progressColor = Color(0xFFFF9800),
                    onClick = { onNavigateToFeature("progress") }
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Quick Health Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigateToFeature("bmi_calculator") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("BMI", fontSize = 12.sp)
                }
                
                OutlinedButton(
                    onClick = { onNavigateToFeature("fasting") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Fasten", fontSize = 12.sp)
                }
                
                OutlinedButton(
                    onClick = { onNavigateToFeature("ai_personal_trainer") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("AI Coach", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun HealthMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    progress: Float,
    progressColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = progressColor
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun QuickActionHub(onNavigateToFeature: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    QuickActionCard(
                        title = "Barcode Scanner",
                        icon = Icons.Default.QrCodeScanner,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { onNavigateToFeature("barcode_scanner") }
                    )
                }
                item {
                    QuickActionCard(
                        title = "Rezepte",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        color = Color(0xFF4CAF50),
                        onClick = { onNavigateToFeature("recipes") }
                    )
                }
                item {
                    QuickActionCard(
                        title = "Training",
                        icon = Icons.Default.FitnessCenter,
                        color = Color(0xFFFF9800),
                        onClick = { onNavigateToFeature("today_training") }
                    )
                }
                item {
                    QuickActionCard(
                        title = "Analytics",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = Color(0xFF9C27B0),
                        onClick = { onNavigateToFeature("enhanced_analytics") }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AIInsightsCard(
    expanded: Boolean,
    onToggle: () -> Unit,
    calorieProgress: Float,
    streakCount: Int,
    onNavigateToFeature: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "🤖 AI Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    
                    val insights = generateSmartInsights(calorieProgress, streakCount)
                    
                    insights.forEach { insight ->
                        AIInsightItem(insight, onNavigateToFeature)
                        Spacer(Modifier.height(8.dp))
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Button(
                        onClick = { onNavigateToFeature("ai_personal_trainer") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Zum AI Personal Trainer")
                    }
                }
            }
        }
    }
}

@Composable
private fun AIInsightItem(insight: AIInsight, onNavigateToFeature: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { insight.action?.let { onNavigateToFeature(it) } },
        shape = RoundedCornerShape(8.dp),
        color = insight.color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, insight.color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                insight.icon,
                fontSize = 20.sp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    insight.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (insight.action != null) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = insight.color
                )
            }
        }
    }
}

@Composable
private fun CrossFeatureWorkflowSection(
    expanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToFeature: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "🔗 Smart Workflows",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    
                    val workflows = listOf(
                        WorkflowItem(
                            title = "Gewichtsverlust Journey",
                            description = "BMI → Intervallfasten → Rezepte → Training",
                            icon = "⚖️",
                            color = MaterialTheme.colorScheme.primary,
                            steps = listOf("bmi_calculator", "fasting", "recipes", "today_training")
                        ),
                        WorkflowItem(
                            title = "Optimale Ernährung",
                            description = "Barcode Scanner → KI Analyse → Rezept Empfehlungen",
                            icon = "🥗",
                            color = Color(0xFF4CAF50),
                            steps = listOf("barcode_scanner", "ai_personal_trainer", "recipes")
                        ),
                        WorkflowItem(
                            title = "Performance Tracking",
                            description = "Training → Health Sync → Analytics → AI Insights",
                            icon = "📈",
                            color = Color(0xFF9C27B0),
                            steps = listOf("today_training", "health_sync", "enhanced_analytics", "ai_personal_trainer")
                        )
                    )
                    
                    workflows.forEach { workflow ->
                        WorkflowCard(workflow, onNavigateToFeature)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkflowCard(
    workflow: WorkflowItem,
    onNavigateToFeature: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                // Navigate to first step of workflow
                workflow.steps.firstOrNull()?.let { onNavigateToFeature(it) }
            },
        shape = RoundedCornerShape(12.dp),
        color = workflow.color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, workflow.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    workflow.icon,
                    fontSize = 24.sp
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        workflow.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        workflow.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = workflow.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Workflow Steps Visualization
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(workflow.steps.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(workflow.color.copy(alpha = 0.7f))
                    )
                    
                    if (index < workflow.steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(workflow.color.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementProgressShowcase(
    achievements: List<com.example.fitapp.data.db.PersonalAchievementEntity>,
    onNavigateToFeature: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "🏆 Achievement Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { onNavigateToFeature("progress") }) {
                    Text("Alle anzeigen")
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements) { achievement ->
                    AchievementMiniCard(achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementMiniCard(
    achievement: com.example.fitapp.data.db.PersonalAchievementEntity
) {
    val progress = if ((achievement.targetValue ?: 0.0) > 0.0) {
        (achievement.currentValue / (achievement.targetValue ?: 1.0)).coerceAtMost(1.0)
    } else 0.0
    
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isCompleted) {
                Color(0xFFFFD700).copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (achievement.isCompleted) "🏆" else "🎯",
                fontSize = 24.sp
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                achievement.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            
            Spacer(Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = if (achievement.isCompleted) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                "${achievement.currentValue.toInt()}/${(achievement.targetValue ?: 0.0).toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HealthIntegrationStatusCard(onNavigateToFeature: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Health Integration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthIntegrationStatus(
                    name = "Health Connect",
                    status = "Aktiv",
                    icon = Icons.Default.Sync,
                    isConnected = true,
                    modifier = Modifier.weight(1f)
                )
                
                HealthIntegrationStatus(
                    name = "Fitness Tracker",
                    status = "Verfügbar",
                    icon = Icons.Default.Watch,
                    isConnected = false,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = { onNavigateToFeature("health_sync") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Health Settings")
            }
        }
    }
}

@Composable
private fun HealthIntegrationStatus(
    name: String,
    status: String,
    icon: ImageVector,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isConnected) {
            Color(0xFF4CAF50).copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                status,
                style = MaterialTheme.typography.labelSmall,
                color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
            )
        }
    }
}

// Data classes for the unified dashboard
private data class AIInsight(
    val title: String,
    val description: String,
    val icon: String,
    val color: Color,
    val action: String? = null
)

private data class WorkflowItem(
    val title: String,
    val description: String,
    val icon: String,
    val color: Color,
    val steps: List<String>
)

private fun generateSmartInsights(calorieProgress: Float, streakCount: Int): List<AIInsight> {
    val insights = mutableListOf<AIInsight>()
    
    when {
        calorieProgress < 0.7f -> insights.add(
            AIInsight(
                title = "Mehr Kalorien benötigt",
                description = "Du könntest heute noch ${((1.0f - calorieProgress) * 100).roundToInt()}% mehr essen",
                icon = "🍽️",
                color = Color(0xFFFF9800),
                action = "nutrition"
            )
        )
        calorieProgress > 1.3f -> insights.add(
            AIInsight(
                title = "Kalorienüberschuss",
                description = "Vielleicht ist heute ein guter Tag für extra Training?",
                icon = "🏃‍♀️",
                color = MaterialTheme.colorScheme.error,
                action = "today_training"
            )
        )
        else -> insights.add(
            AIInsight(
                title = "Perfekte Balance",
                description = "Deine Kalorienbilanz ist heute optimal!",
                icon = "✅",
                color = Color(0xFF4CAF50)
            )
        )
    }
    
    if (streakCount >= 7) {
        insights.add(
            AIInsight(
                title = "Streak Champion!",
                description = "$streakCount Tage konstante Fitness - unglaublich!",
                icon = "🔥",
                color = Color(0xFFFF5722),
                action = "progress"
            )
        )
    } else if (streakCount >= 3) {
        insights.add(
            AIInsight(
                title = "Guter Rhythmus",
                description = "Du bist auf dem richtigen Weg mit $streakCount Tagen",
                icon = "💪",
                color = Color(0xFF2196F3),
                action = "progress"
            )
        )
    }
    
    // AI recommendation based on time of day
    val currentHour = java.time.LocalTime.now().hour
    when (currentHour) {
        in 6..10 -> insights.add(
            AIInsight(
                title = "Morgen Boost",
                description = "Perfekte Zeit für Intervallfasten oder ein gesundes Frühstück",
                icon = "🌅",
                color = Color(0xFF9C27B0),
                action = "fasting"
            )
        )
        in 11..14 -> insights.add(
            AIInsight(
                title = "Mittagszeit",
                description = "Wie wäre es mit einem gesunden Rezept für heute?",
                icon = "🥗",
                color = Color(0xFF4CAF50),
                action = "recipes"
            )
        )
        in 15..19 -> insights.add(
            AIInsight(
                title = "Training Time",
                description = "Optimale Zeit für dein Workout!",
                icon = "🏋️‍♀️",
                color = Color(0xFFFF9800),
                action = "today_training"
            )
        )
        else -> insights.add(
            AIInsight(
                title = "Entspannung",
                description = "Zeit für Recovery und Fortschritts-Tracking",
                icon = "🧘‍♀️",
                color = Color(0xFF607D8B),
                action = "enhanced_analytics"
            )
        )
    }
    
    return insights
}
