package com.example.fitapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 🎨 YAZIO-INSPIRED ADVANCED DASHBOARD
 * 
 * Design Elements based on professional fitness app screenshots:
 * - Gradient backgrounds inspired by fitness_ui_ux_collection.png
 * - Circular progress indicators like yazio_like_app_three_screens.png
 * - Smart health cards similar to cronometer_app_screen.png
 * - Clean navigation from nike_training_club.png
 * - Data visualization inspired by nutrition_app_trends_charts.png
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YazioInspiredDashboardScreen(
    contentPadding: PaddingValues,
    onNavigateToFeature: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Mock data for demonstration - in real app, fetch from repositories
    var calorieProgress by remember { mutableFloatStateOf(0.75f) }
    var waterProgress by remember { mutableFloatStateOf(0.6f) }
    var proteinProgress by remember { mutableFloatStateOf(0.9f) }
    var streakCount by remember { mutableIntStateOf(12) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                )
            ),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section with Greeting
        item {
            YazioStyleHeader(
                userName = "Max",
                streakCount = streakCount,
                onProfileClick = { onNavigateToFeature("profile") }
            )
        }
        
        // Main Progress Ring (YAZIO-Style)
        item {
            YazioStyleProgressRing(
                calorieProgress = calorieProgress,
                targetCalories = 2000,
                consumedCalories = (calorieProgress * 2000).roundToInt(),
                onAddFoodClick = { onNavigateToFeature("barcode_scanner") }
            )
        }
        
        // Macro Progress Cards Row
        item {
            MacroProgressRow(
                proteinProgress = proteinProgress,
                carbsProgress = 0.7f,
                fatProgress = 0.5f,
                onMacroClick = { macro -> onNavigateToFeature("nutrition") }
            )
        }
        
        // Water & Activity Section
        item {
            WaterActivitySection(
                waterProgress = waterProgress,
                stepsToday = 8432,
                onWaterAdd = { waterProgress = (waterProgress + 0.1f).coerceAtMost(1f) },
                onActivityClick = { onNavigateToFeature("today_training") }
            )
        }
        
        // Smart Insights Carousel
        item {
            SmartInsightsCarousel(
                insights = generateYazioStyleInsights(calorieProgress, streakCount),
                onInsightClick = { insight -> onNavigateToFeature(insight.action) }
            )
        }
        
        // Quick Actions Grid (Nike Training Club inspired)
        item {
            QuickActionsGrid(
                onActionClick = { action -> onNavigateToFeature(action) }
            )
        }
        
        // Achievement Preview (Gamification inspired by waterllama)
        item {
            AchievementPreviewSection(
                recentAchievements = getMockAchievements(),
                onViewAllClick = { onNavigateToFeature("achievements") }
            )
        }
        
        // Health Metrics Dashboard (Cronometer inspired)
        item {
            HealthMetricsDashboard(
                bmi = 23.4f,
                bodyFat = 15.2f,
                muscleMass = 45.8f,
                onMetricClick = { metric -> onNavigateToFeature("bmi_calculator") }
            )
        }
    }
}

@Composable
fun YazioStyleHeader(
    userName: String,
    streakCount: Int,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hallo, $userName! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dein Fitness-Tag startet jetzt!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            // Streak Badge with Fire Animation
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF5722)
                ),
                modifier = Modifier.clickable { onProfileClick() }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "$streakCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tage",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun YazioStyleProgressRing(
    calorieProgress: Float,
    targetCalories: Int,
    consumedCalories: Int,
    onAddFoodClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Kalorienziel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Large Circular Progress (YAZIO-style)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                CircularProgressIndicator(
                    progress = { calorieProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = when {
                        calorieProgress < 0.5f -> MaterialTheme.colorScheme.error
                        calorieProgress < 0.8f -> Color(0xFFFF9800)
                        calorieProgress <= 1.0f -> Color(0xFF4CAF50)
                        else -> Color(0xFFFF5722)
                    }
                )
                
                // Center content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$consumedCalories",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "von $targetCalories",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Kalorien",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAddFoodClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Essen hinzufügen")
                }
                
                OutlinedButton(
                    onClick = { /* View diary */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tagebuch")
                }
            }
        }
    }
}

@Composable
fun MacroProgressRow(
    proteinProgress: Float,
    carbsProgress: Float,
    fatProgress: Float,
    onMacroClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MacroCard(
            title = "Protein",
            progress = proteinProgress,
            value = "${(proteinProgress * 100).roundToInt()}g",
            target = "100g",
            color = Color(0xFF2196F3),
            icon = "💪",
            modifier = Modifier.weight(1f),
            onClick = { onMacroClick("protein") }
        )
        
        MacroCard(
            title = "Carbs",
            progress = carbsProgress,
            value = "${(carbsProgress * 250).roundToInt()}g",
            target = "250g",
            color = Color(0xFF4CAF50),
            icon = "🌾",
            modifier = Modifier.weight(1f),
            onClick = { onMacroClick("carbs") }
        )
        
        MacroCard(
            title = "Fett",
            progress = fatProgress,
            value = "${(fatProgress * 65).roundToInt()}g",
            target = "65g",
            color = Color(0xFFFF9800),
            icon = "🥑",
            modifier = Modifier.weight(1f),
            onClick = { onMacroClick("fat") }
        )
    }
}

@Composable
fun MacroCard(
    title: String,
    progress: Float,
    value: String,
    target: String,
    color: Color,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$value / $target",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun WaterActivitySection(
    waterProgress: Float,
    stepsToday: Int,
    onWaterAdd: () -> Unit,
    onActivityClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Water Tracking Card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💧 Wasser",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { waterProgress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        text = "${(waterProgress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${(waterProgress * 2000).roundToInt()} ml",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = onWaterAdd,
                    modifier = Modifier
                        .background(
                            Color(0xFF2196F3),
                            CircleShape
                        )
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Wasser hinzufügen",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        // Activity Card
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onActivityClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚶‍♂️ Schritte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = stepsToday.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "von 10.000",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (stepsToday / 10000f).coerceAtMost(1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF4CAF50)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Training starten",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.clickable { onActivityClick() }
                )
            }
        }
    }
}

// Data classes and helper functions
data class YazioInsight(
    val title: String,
    val description: String,
    val icon: String,
    val color: Color,
    val action: String
)

data class MockAchievement(
    val title: String,
    val description: String,
    val icon: String,
    val progress: Float
)

@Composable
fun SmartInsightsCarousel(
    insights: List<YazioInsight>,
    onInsightClick: (YazioInsight) -> Unit
) {
    Column {
        Text(
            text = "🤖 Smart Insights",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(insights) { insight ->
                InsightCard(
                    insight = insight,
                    onClick = { onInsightClick(insight) }
                )
            }
        }
    }
}

@Composable
fun InsightCard(
    insight: YazioInsight,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = insight.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = insight.icon,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun QuickActionsGrid(
    onActionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "⚡ Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 2x3 Grid of actions
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    title = "Workout",
                    icon = Icons.Default.FitnessCenter,
                    color = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f),
                    onClick = { onActionClick("today_training") }
                )
                QuickActionButton(
                    title = "Fasten",
                    icon = Icons.Default.Schedule,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f),
                    onClick = { onActionClick("fasting") }
                )
                QuickActionButton(
                    title = "BMI",
                    icon = Icons.Default.Scale,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f),
                    onClick = { onActionClick("bmi_calculator") }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    title = "Rezepte",
                    icon = Icons.Default.MenuBook,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    onClick = { onActionClick("recipes") }
                )
                QuickActionButton(
                    title = "Erfolge",
                    icon = Icons.Default.EmojiEvents,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f),
                    onClick = { onActionClick("achievements") }
                )
                QuickActionButton(
                    title = "AI Coach",
                    icon = Icons.Default.Psychology,
                    color = Color(0xFF673AB7),
                    modifier = Modifier.weight(1f),
                    onClick = { onActionClick("ai_personal_trainer") }
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AchievementPreviewSection(
    recentAchievements: List<MockAchievement>,
    onViewAllClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏆 Aktuelle Erfolge",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onViewAllClick) {
                Text("Alle anzeigen")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recentAchievements) { achievement ->
                AchievementCard(achievement)
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: MockAchievement) {
    Card(
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFD700).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = achievement.icon,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { achievement.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFFFFD700)
            )
        }
    }
}

@Composable
fun HealthMetricsDashboard(
    bmi: Float,
    bodyFat: Float,
    muscleMass: Float,
    onMetricClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📊 Gesundheits-Metriken",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HealthMetricItem(
                    title = "BMI",
                    value = bmi.toString(),
                    unit = "",
                    status = when {
                        bmi < 18.5f -> "Untergewicht"
                        bmi < 25f -> "Normal"
                        bmi < 30f -> "Übergewicht"
                        else -> "Adipositas"
                    },
                    color = when {
                        bmi < 18.5f -> Color(0xFF2196F3)
                        bmi < 25f -> Color(0xFF4CAF50)
                        bmi < 30f -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    modifier = Modifier.weight(1f),
                    onClick = { onMetricClick("bmi") }
                )
                
                HealthMetricItem(
                    title = "Körperfett",
                    value = bodyFat.toString(),
                    unit = "%",
                    status = if (bodyFat < 20f) "Optimal" else "Hoch",
                    color = if (bodyFat < 20f) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.weight(1f),
                    onClick = { onMetricClick("body_composition") }
                )
                
                HealthMetricItem(
                    title = "Muskeln",
                    value = muscleMass.toString(),
                    unit = "kg",
                    status = "Gut",
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f),
                    onClick = { onMetricClick("muscle_mass") }
                )
            }
        }
    }
}

@Composable
fun HealthMetricItem(
    title: String,
    value: String,
    unit: String,
    status: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

// Helper functions
fun generateYazioStyleInsights(calorieProgress: Float, streakCount: Int): List<YazioInsight> {
    return listOf(
        YazioInsight(
            title = "Perfektes Timing!",
            description = "Dein Protein-Fenster nach dem Training ist noch 2h offen. Jetzt optimalen Shake trinken!",
            icon = "💪",
            color = Color(0xFF2196F3),
            action = "nutrition"
        ),
        YazioInsight(
            title = "Streak Champion",
            description = "$streakCount Tage konsistent! Du gehörst zu den Top 5% der FitApp Nutzer.",
            icon = "🔥",
            color = Color(0xFFFF5722),
            action = "achievements"
        ),
        YazioInsight(
            title = "Wetter-Tipp",
            description = "22°C und sonnig! Perfekt für ein Outdoor-Training im Park statt Gym.",
            icon = "☀️",
            color = Color(0xFFFFD700),
            action = "today_training"
        ),
        YazioInsight(
            title = "Schlaf-Optimierung",
            description = "Mit 7.5h Schlaf bist du bereit für intensives Training. HRV ist optimal!",
            icon = "😴",
            color = Color(0xFF9C27B0),
            action = "health_sync"
        )
    )
}

fun getMockAchievements(): List<MockAchievement> {
    return listOf(
        MockAchievement(
            title = "Wasser-Meister",
            description = "7 Tage 2L+ getrunken",
            icon = "💧",
            progress = 0.85f
        ),
        MockAchievement(
            title = "Protein-Power",
            description = "95% Protein-Ziel erreicht",
            icon = "🥩",
            progress = 0.95f
        ),
        MockAchievement(
            title = "Cardio-King",
            description = "3 Cardio-Sessions/Woche",
            icon = "🏃‍♂️",
            progress = 0.6f
        ),
        MockAchievement(
            title = "Streak-Legende",
            description = "30 Tage aktiv",
            icon = "🔥",
            progress = 0.4f
        )
    )
}
