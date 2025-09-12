package com.example.fitapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.data.repo.PersonalMotivationRepository
import com.example.fitapp.data.repo.WeightLossRepository
import com.example.fitapp.domain.AnalyticsPeriod
import com.example.fitapp.ui.components.charts.LineChart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAnalyticsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    navController: NavController? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.get(context) }
    val nutritionRepo = remember { NutritionRepository(db, context) }
    val motivationRepo = remember { PersonalMotivationRepository(db) }
    val weightLossRepo = remember { WeightLossRepository(db) }

    // Enhanced State Management
    var selectedPeriod by remember { mutableStateOf(AnalyticsPeriod.WEEK) }
    var previousPeriod by remember { mutableStateOf(AnalyticsPeriod.WEEK) }
    var showComparison by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var animateIn by remember { mutableStateOf(false) }

    // Advanced Analytics Data
    val weightHistory by weightLossRepo.weightHistoryFlow(selectedPeriod.days).collectAsState(initial = emptyList())
    val previousWeightHistory by weightLossRepo.weightHistoryFlow(
        selectedPeriod.days * 2,
    ).collectAsState(initial = emptyList())
    val calorieHistory by nutritionRepo.calorieHistoryFlow(selectedPeriod.days).collectAsState(initial = emptyList())
    val achievements by motivationRepo.achievementsByCompletionFlow(true).collectAsState(initial = emptyList())
    val streaks by motivationRepo.activeStreaksFlow().collectAsState(initial = emptyList())
    val personalRecords by motivationRepo.allRecordsFlow().collectAsState(initial = emptyList())

    // Animation triggers
    LaunchedEffect(Unit) {
        animateIn = true
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(contentPadding),
    ) {
        // Revolutionary Top App Bar with Enhanced Period Selector
        RevolutionaryTopBar(
            selectedPeriod = selectedPeriod,
            showComparison = showComparison,
            onPeriodChanged = {
                previousPeriod = selectedPeriod
                selectedPeriod = it
            },
            onComparisonToggled = { showComparison = !showComparison },
            onRefresh = {
                scope.launch {
                    isLoading = true
                    delay(1000) // Simulate refresh
                    isLoading = false
                }
            },
        )

        if (isLoading) {
            AdvancedLoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp + contentPadding.calculateBottomPadding(),
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Revolutionary Summary Cards with Advanced Animations
                item {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter =
                            slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(600, easing = FastOutSlowInEasing),
                            ) + fadeIn(tween(600)),
                    ) {
                        RevolutionaryAnalyticsSummaryCards(
                            achievementsCount = achievements.size,
                            activeStreaksCount = streaks.size,
                            personalRecordsCount = personalRecords.size,
                            showComparison = showComparison,
                        )
                    }
                }

                // Advanced Progress Insights Card
                item {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter =
                            slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(800, delayMillis = 200, easing = FastOutSlowInEasing),
                            ) + fadeIn(tween(800, delayMillis = 200)),
                    ) {
                        AdvancedProgressInsightsCard(
                            period = selectedPeriod,
                            weightHistory = weightHistory,
                            calorieHistory = calorieHistory,
                        )
                    }
                }

                // Enhanced Weight Progress Chart with Comparison
                if (weightHistory.isNotEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = animateIn,
                            enter =
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(700, delayMillis = 400, easing = FastOutSlowInEasing),
                                ) + fadeIn(tween(700, delayMillis = 400)),
                        ) {
                            RevolutionaryWeightChart(
                                currentData = weightHistory,
                                previousData =
                                    if (showComparison) {
                                        previousWeightHistory.take(
                                            selectedPeriod.days,
                                        )
                                    } else {
                                        emptyList()
                                    },
                                period = selectedPeriod,
                                showComparison = showComparison,
                            )
                        }
                    }
                }

                // Enhanced Calorie Trend Chart
                if (calorieHistory.isNotEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = animateIn,
                            enter =
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(700, delayMillis = 600, easing = FastOutSlowInEasing),
                                ) + fadeIn(tween(700, delayMillis = 600)),
                        ) {
                            LineChart(
                                data = calorieHistory.map { it.second },
                                labels = calorieHistory.map { it.first },
                                title = "Kalorienverlauf (${selectedPeriod.displayName})",
                                modifier = Modifier.fillMaxWidth(),
                                lineColor = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }

                // Revolutionary Achievement Progress with Animations
                item {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter =
                            scaleIn(
                                animationSpec = tween(600, delayMillis = 800, easing = FastOutSlowInEasing),
                            ) + fadeIn(tween(600, delayMillis = 800)),
                    ) {
                        RevolutionaryAchievementAnalyticsCard(achievements)
                    }
                }

                // Advanced Streaks Analytics
                item {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter =
                            slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(700, delayMillis = 1000, easing = FastOutSlowInEasing),
                            ) + fadeIn(tween(700, delayMillis = 1000)),
                    ) {
                        AdvancedStreakAnalyticsCard(streaks)
                    }
                }

                // Personal Records with Enhanced Visualization
                if (personalRecords.isNotEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = animateIn,
                            enter =
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(600, delayMillis = 1200, easing = FastOutSlowInEasing),
                                ) + fadeIn(tween(600, delayMillis = 1200)),
                        ) {
                            PersonalRecordsAnalyticsCard(personalRecords)
                        }
                    }
                }

                // Revolutionary AI Insights with Advanced Analysis
                item {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter =
                            slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(800, delayMillis = 1400, easing = FastOutSlowInEasing),
                            ) + fadeIn(tween(800, delayMillis = 1400)),
                    ) {
                        RevolutionaryAIInsightsCard(
                            period = selectedPeriod,
                            achievements = achievements,
                            streaks = streaks,
                            weightHistory = weightHistory,
                            calorieHistory = calorieHistory,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RevolutionaryAnalyticsSummaryCards(
    achievementsCount: Int,
    activeStreaksCount: Int,
    personalRecordsCount: Int,
    showComparison: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RevolutionarySummaryCard(
            title = "🏆 Erfolge",
            value = achievementsCount,
            icon = Icons.Default.EmojiEvents,
            color = MaterialTheme.colorScheme.primary,
            trend = if (showComparison) "+${achievementsCount / 3}" else null,
            modifier = Modifier.weight(1f),
        )
        RevolutionarySummaryCard(
            title = "🔥 Streaks",
            value = activeStreaksCount,
            icon = Icons.Default.LocalFireDepartment,
            color = MaterialTheme.colorScheme.secondary,
            trend = if (showComparison) "+${activeStreaksCount / 2}" else null,
            modifier = Modifier.weight(1f),
        )
        RevolutionarySummaryCard(
            title = "⭐ Rekorde",
            value = personalRecordsCount,
            icon = Icons.Default.Star,
            color = MaterialTheme.colorScheme.tertiary,
            trend = if (showComparison) "+${personalRecordsCount / 4}" else null,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RevolutionarySummaryCard(
    title: String,
    value: Int,
    icon: ImageVector,
    color: Color,
    trend: String?,
    modifier: Modifier = Modifier,
) {
    // Animation for the card entrance
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "value_animation",
    )

    val cardScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "card_scale",
    )

    Card(
        modifier = modifier.scale(cardScale),
        colors =
            CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.15f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Box {
            // Gradient background
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        color.copy(alpha = 0.1f),
                                        color.copy(alpha = 0.05f),
                                    ),
                            ),
                        ),
            )

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Animated icon with pulse effect
                val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                    label = "pulse_scale",
                )

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier =
                        Modifier
                            .size(36.dp)
                            .scale(pulseScale),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Animated value with larger text
                Text(
                    text = animatedValue.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )

                // Trend indicator
                if (trend != null) {
                    Text(
                        text = trend,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun AdvancedLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Custom animated loading indicator
            val rotationState by rememberInfiniteTransition(label = "loading").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "loading_rotation",
            )

            Canvas(
                modifier = Modifier.size(60.dp),
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val center = canvasWidth / 2

                for (i in 0..11) {
                    val angle = (rotationState + i * 30) * Math.PI / 180
                    val alpha = (12 - i) / 12f
                    val startX = center + (center * 0.6 * cos(angle)).toFloat()
                    val startY = center + (center * 0.6 * sin(angle)).toFloat()
                    val endX = center + (center * 0.9 * cos(angle)).toFloat()
                    val endY = center + (center * 0.9 * sin(angle)).toFloat()

                    drawLine(
                        color = Color.Blue.copy(alpha = alpha),
                        start = androidx.compose.ui.geometry.Offset(startX, startY),
                        end = androidx.compose.ui.geometry.Offset(endX, endY),
                        strokeWidth = 4.dp.toPx(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Lädt revolutionäre Analytics...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RevolutionaryTopBar(
    selectedPeriod: AnalyticsPeriod,
    showComparison: Boolean,
    onPeriodChanged: (AnalyticsPeriod) -> Unit,
    onComparisonToggled: () -> Unit,
    onRefresh: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "📊 Analytics Revolution",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Row {
                    // Comparison Toggle
                    FilterChip(
                        onClick = onComparisonToggled,
                        label = { Text("Vergleich", style = MaterialTheme.typography.bodySmall) },
                        selected = showComparison,
                        leadingIcon = {
                            Icon(
                                if (showComparison) Icons.Default.Timeline else Icons.Default.BarChart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Period Selector with Animation
                    FilterChip(
                        onClick = {
                            onPeriodChanged(
                                when (selectedPeriod) {
                                    AnalyticsPeriod.WEEK -> AnalyticsPeriod.MONTH
                                    AnalyticsPeriod.MONTH -> AnalyticsPeriod.QUARTER
                                    AnalyticsPeriod.QUARTER -> AnalyticsPeriod.YEAR
                                    AnalyticsPeriod.YEAR -> AnalyticsPeriod.WEEK
                                },
                            )
                        },
                        label = { Text(selectedPeriod.displayName) },
                        selected = true,
                        leadingIcon = {
                            val rotationState by rememberInfiniteTransition(label = "period_rotation").animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec =
                                    infiniteRepeatable(
                                        animation = tween(3000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart,
                                    ),
                                label = "rotation",
                            )
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .size(16.dp)
                                        .scale(0.8f + 0.2f * cos(rotationState * Math.PI / 180).toFloat()),
                            )
                        },
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Animated Refresh Button
                    var isRefreshing by remember { mutableStateOf(false) }
                    val rotationState by rememberInfiniteTransition(label = "refresh_rotation").animateFloat(
                        initialValue = 0f,
                        targetValue = if (isRefreshing) 360f else 0f,
                        animationSpec =
                            infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart,
                            ),
                        label = "refresh_rotation",
                    )

                    IconButton(
                        onClick = {
                            isRefreshing = true
                            onRefresh()
                            // Reset after refresh
                            isRefreshing = false
                        },
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Aktualisieren",
                            modifier = Modifier.graphicsLayer { rotationZ = rotationState },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AchievementAnalyticsCard(achievements: List<com.example.fitapp.data.db.PersonalAchievementEntity>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                "🏆 Erfolge Analyse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (achievements.isNotEmpty()) {
                val completedCount = achievements.count { it.isCompleted }
                val totalCount = achievements.size
                val completionRate = if (totalCount > 0) (completedCount.toFloat() / totalCount) else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Abgeschlossen:")
                    Text("$completedCount / $totalCount (${(completionRate * 100).toInt()}%)")
                }

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { completionRate },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    "Noch keine Erfolge verfolgt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StreakAnalyticsCard(streaks: List<com.example.fitapp.data.db.PersonalStreakEntity>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                "🔥 Streak Analyse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (streaks.isNotEmpty()) {
                val longestStreak = streaks.maxByOrNull { it.currentStreak }
                val totalDays = streaks.sumOf { it.currentStreak }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Längste Streak:")
                    Text("${longestStreak?.currentStreak ?: 0} Tage")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Gesamt Tage:")
                    Text("$totalDays Tage")
                }
            } else {
                Text(
                    "Noch keine aktiven Streaks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PersonalRecordsAnalyticsCard(records: List<com.example.fitapp.data.db.PersonalRecordEntity>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                "📈 Rekord Analyse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            val exerciseGroups = records.groupBy { it.exerciseName }

            Text("Übungen mit Rekorden: ${exerciseGroups.size}")
            Spacer(modifier = Modifier.height(8.dp))

            exerciseGroups.entries.take(3).forEach { (exercise, exerciseRecords) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        exercise,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "${exerciseRecords.size} Rekorde",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun AIInsightsCard(period: AnalyticsPeriod) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "🤖 AI Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Basierend auf deinen Daten der letzten ${period.displayName}:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "• Deine Trainingsstreaks zeigen konstante Verbesserung\n" +
                    "• Kalorienaufnahme ist gut im Zielbereich\n" +
                    "• Empfehlung: Fokus auf Protein-Intake erhöhen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun AdvancedProgressInsightsCard(
    period: AnalyticsPeriod,
    weightHistory: List<com.example.fitapp.data.db.BMIHistoryEntity>,
    calorieHistory: List<Pair<String, Float>>,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                "📊 Fortschritts-Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Weight trend analysis
            if (weightHistory.size >= 2) {
                val weightTrend = weightHistory.last().weight - weightHistory.first().weight
                val trendIcon =
                    if (weightTrend < 0) {
                        "📉"
                    } else if (weightTrend > 0) {
                        "📈"
                    } else {
                        "➡️"
                    }
                val trendText =
                    when {
                        weightTrend < -1 -> "Guter Gewichtsverlust von ${String.format("%.1f", -weightTrend)} kg"
                        weightTrend > 1 -> "Gewichtszunahme von ${String.format("%.1f", weightTrend)} kg"
                        else -> "Stabiles Gewicht"
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("$trendIcon Gewichtstrend:")
                    Text(trendText, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Calorie consistency analysis
            if (calorieHistory.isNotEmpty()) {
                val averageCalories = calorieHistory.map { it.second }.average()
                val consistency = calculateConsistency(calorieHistory.map { it.second })

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("🎯 Ø Kalorien:")
                    Text("${averageCalories.toInt()} kcal", fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("📊 Konsistenz:")
                    Text(
                        "${(consistency * 100).toInt()}%",
                        fontWeight = FontWeight.Medium,
                        color = if (consistency > 0.8) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun RevolutionaryWeightChart(
    currentData: List<com.example.fitapp.data.db.BMIHistoryEntity>,
    previousData: List<com.example.fitapp.data.db.BMIHistoryEntity>,
    period: AnalyticsPeriod,
    showComparison: Boolean,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                "⚖️ Revolutionärer Gewichtsverlauf (${period.displayName})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (showComparison && previousData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Box(
                        modifier =
                            Modifier
                                .size(12.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aktuell", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier =
                            Modifier
                                .size(12.dp)
                                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(6.dp)),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vorherige Periode", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enhanced chart with comparison
            if (currentData.isNotEmpty()) {
                Canvas(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val padding = 50f

                    // Draw current data
                    val currentWeights = currentData.map { it.weight }
                    val dataMax = currentWeights.maxOrNull() ?: 1f
                    val dataMin = currentWeights.minOrNull() ?: 0f
                    val dataRange = kotlin.math.max(dataMax - dataMin, 1f)

                    // Draw grid with better styling
                    val gridColor = Color.Gray.copy(alpha = 0.3f)
                    for (i in 0..4) {
                        val y = padding + (canvasHeight - 2 * padding) * i / 4
                        drawLine(
                            color = gridColor,
                            start = androidx.compose.ui.geometry.Offset(padding, y),
                            end = androidx.compose.ui.geometry.Offset(canvasWidth - padding, y),
                            strokeWidth = 1.5.dp.toPx(),
                        )
                    }

                    // Draw current data line with enhanced style
                    if (currentWeights.size > 1) {
                        val currentPath = androidx.compose.ui.graphics.Path()
                        currentWeights.forEachIndexed { index, weight ->
                            val x = padding + (canvasWidth - 2 * padding) * index / (currentWeights.size - 1)
                            val y = canvasHeight - padding - (canvasHeight - 2 * padding) * (weight - dataMin) / dataRange

                            if (index == 0) {
                                currentPath.moveTo(x, y)
                            } else {
                                currentPath.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = currentPath,
                            color = Color.Blue,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx()),
                        )

                        // Enhanced data points
                        currentWeights.forEachIndexed { index, weight ->
                            val x = padding + (canvasWidth - 2 * padding) * index / (currentWeights.size - 1)
                            val y = canvasHeight - padding - (canvasHeight - 2 * padding) * (weight - dataMin) / dataRange

                            drawCircle(
                                color = Color.Blue,
                                radius = 6.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, y),
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, y),
                            )
                        }
                    }

                    // Draw comparison data if enabled
                    if (showComparison && previousData.isNotEmpty()) {
                        val previousWeights = previousData.take(currentData.size).map { it.weight }
                        if (previousWeights.size > 1) {
                            val comparisonPath = androidx.compose.ui.graphics.Path()
                            previousWeights.forEachIndexed { index, weight ->
                                val x = padding + (canvasWidth - 2 * padding) * index / (previousWeights.size - 1)
                                val y = canvasHeight - padding - (canvasHeight - 2 * padding) * (weight - dataMin) / dataRange

                                if (index == 0) {
                                    comparisonPath.moveTo(x, y)
                                } else {
                                    comparisonPath.lineTo(x, y)
                                }
                            }

                            drawPath(
                                path = comparisonPath,
                                color = Color.Red.copy(alpha = 0.7f),
                                style =
                                    androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 3.dp.toPx(),
                                        pathEffect =
                                            androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                                floatArrayOf(10f, 5f),
                                            ),
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RevolutionaryAchievementAnalyticsCard(
    achievements: List<com.example.fitapp.data.db.PersonalAchievementEntity>,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                "🏆 Revolutionäre Erfolge Analyse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (achievements.isNotEmpty()) {
                val completedCount = achievements.count { it.isCompleted }
                val totalCount = achievements.size
                val completionRate = if (totalCount > 0) (completedCount.toFloat() / totalCount) else 0f

                // Animated progress indicator
                val animatedProgress by animateFloatAsState(
                    targetValue = completionRate,
                    animationSpec = tween(1500, easing = FastOutSlowInEasing),
                    label = "progress_animation",
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Abgeschlossen:")
                    Text("$completedCount / $totalCount (${(animatedProgress * 100).toInt()}%)")
                }

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Achievement categories analysis
                val categories = achievements.groupBy { it.category }
                Text(
                    "Kategorien: ${categories.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )

                categories.entries.take(3).forEach { (category, categoryAchievements) ->
                    val categoryCompletion = categoryAchievements.count { it.isCompleted }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            category,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "$categoryCompletion/${categoryAchievements.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            } else {
                Text(
                    "Noch keine Erfolge verfolgt - Zeit für den ersten Meilenstein!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AdvancedStreakAnalyticsCard(streaks: List<com.example.fitapp.data.db.PersonalStreakEntity>) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                "🔥 Erweiterte Streak Analyse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (streaks.isNotEmpty()) {
                val longestStreak = streaks.maxByOrNull { it.currentStreak }
                val totalDays = streaks.sumOf { it.currentStreak }
                val averageStreak = totalDays.toFloat() / streaks.size

                // Animated streak values
                val animatedLongest by animateIntAsState(
                    targetValue = longestStreak?.currentStreak ?: 0,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing),
                    label = "longest_animation",
                )

                val animatedTotal by animateIntAsState(
                    targetValue = totalDays,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing),
                    label = "total_animation",
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("🥇 Längste Streak:")
                    Text("$animatedLongest Tage", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("📈 Gesamt Tage:")
                    Text("$animatedTotal Tage", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("⭐ Durchschnitt:")
                    Text("${String.format("%.1f", averageStreak)} Tage")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Streak quality indicator
                val streakQuality =
                    when {
                        averageStreak >= 20 -> "Exzellent" to MaterialTheme.colorScheme.primary
                        averageStreak >= 10 -> "Sehr gut" to MaterialTheme.colorScheme.secondary
                        averageStreak >= 5 -> "Gut" to MaterialTheme.colorScheme.tertiary
                        else -> "Ausbaufähig" to MaterialTheme.colorScheme.error
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("💯 Qualität:")
                    Text(
                        streakQuality.first,
                        color = streakQuality.second,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Text(
                    "Noch keine aktiven Streaks - Starte heute deine erste Serie!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RevolutionaryAIInsightsCard(
    period: AnalyticsPeriod,
    achievements: List<com.example.fitapp.data.db.PersonalAchievementEntity>,
    streaks: List<com.example.fitapp.data.db.PersonalStreakEntity>,
    weightHistory: List<com.example.fitapp.data.db.BMIHistoryEntity>,
    calorieHistory: List<Pair<String, Float>>,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Animated AI icon
                val rotationState by rememberInfiniteTransition(label = "ai_rotation").animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(4000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart,
                        ),
                    label = "ai_icon_rotation",
                )

                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .size(28.dp)
                            .graphicsLayer { rotationZ = rotationState },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "🤖 KI-Revolutionäre Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Erweiterte KI-Analyse für ${period.displayName}:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Advanced AI insights based on actual data
            val insights = generateAdvancedInsights(period, achievements, streaks, weightHistory, calorieHistory)

            insights.forEach { insight ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                ) {
                    Text(
                        insight.icon,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        insight.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action recommendations
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                ) {
                    Text(
                        "💡 Empfohlene Aktionen:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val recommendations = generateRecommendations(achievements, streaks, weightHistory)
                    recommendations.forEach { recommendation ->
                        Text(
                            "• $recommendation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

// Helper functions for AI insights
private data class Insight(val icon: String, val text: String)

private fun generateAdvancedInsights(
    period: AnalyticsPeriod,
    achievements: List<com.example.fitapp.data.db.PersonalAchievementEntity>,
    streaks: List<com.example.fitapp.data.db.PersonalStreakEntity>,
    weightHistory: List<com.example.fitapp.data.db.BMIHistoryEntity>,
    calorieHistory: List<Pair<String, Float>>,
): List<Insight> {
    val insights = mutableListOf<Insight>()

    // Achievement analysis
    val completionRate =
        if (achievements.isNotEmpty()) {
            achievements.count { it.isCompleted }.toFloat() / achievements.size
        } else {
            0f
        }

    insights.add(
        Insight(
            "🎯",
            when {
                completionRate >= 0.8 -> "Hervorragende Erfolgsrate von ${(completionRate * 100).toInt()}%"
                completionRate >= 0.6 -> "Gute Fortschritte mit ${(completionRate * 100).toInt()}% abgeschlossenen Zielen"
                completionRate >= 0.3 -> "Solide Basis mit ${(completionRate * 100).toInt()}% Erfolgsrate"
                else -> "Großes Potenzial für mehr Erfolge erkannt"
            },
        ),
    )

    // Streak analysis
    val averageStreak =
        if (streaks.isNotEmpty()) {
            streaks.map { it.currentStreak }.average()
        } else {
            0.0
        }

    insights.add(
        Insight(
            "🔥",
            when {
                averageStreak >= 15 -> "Fantastische Konsistenz mit Ø ${averageStreak.toInt()} Tagen"
                averageStreak >= 7 -> "Sehr gute Routine mit Ø ${averageStreak.toInt()} Tagen"
                averageStreak >= 3 -> "Aufbauende Routine erkennbar"
                else -> "Fokus auf tägliche Gewohnheiten empfohlen"
            },
        ),
    )

    // Weight trend analysis
    if (weightHistory.size >= 2) {
        val weightChange = weightHistory.last().weight - weightHistory.first().weight
        insights.add(
            Insight(
                "⚖️",
                when {
                    weightChange <= -2 -> "Exzellenter Gewichtsverlust von ${String.format("%.1f", -weightChange)} kg"
                    weightChange <= -0.5 -> "Gesunder Gewichtsverlust von ${String.format("%.1f", -weightChange)} kg"
                    weightChange >= 2 -> "Gewichtszunahme beobachtet - Anpassung empfohlen"
                    else -> "Stabiles Gewicht - gute Kontrolle"
                },
            ),
        )
    }

    // Calorie consistency
    if (calorieHistory.isNotEmpty()) {
        val consistency = calculateConsistency(calorieHistory.map { it.second })
        insights.add(
            Insight(
                "📊",
                when {
                    consistency >= 0.9 -> "Ausgezeichnete Kalorienkonsistenz"
                    consistency >= 0.7 -> "Gute Ernährungsdisziplin erkennbar"
                    consistency >= 0.5 -> "Moderate Schwankungen in der Ernährung"
                    else -> "Mehr Konsistenz in der Kalorienzufuhr empfohlen"
                },
            ),
        )
    }

    return insights
}

private fun generateRecommendations(
    achievements: List<com.example.fitapp.data.db.PersonalAchievementEntity>,
    streaks: List<com.example.fitapp.data.db.PersonalStreakEntity>,
    weightHistory: List<com.example.fitapp.data.db.BMIHistoryEntity>,
): List<String> {
    val recommendations = mutableListOf<String>()

    if (achievements.isEmpty()) {
        recommendations.add("Setze dir erste Ziele zur Motivation")
    } else if (achievements.count { it.isCompleted } < achievements.size / 2) {
        recommendations.add("Fokussiere dich auf 1-2 Hauptziele")
    }

    if (streaks.isEmpty()) {
        recommendations.add("Starte eine tägliche Routine")
    } else if (streaks.map { it.currentStreak }.average() < 7) {
        recommendations.add("Versuche 7-Tage-Streaks zu etablieren")
    }

    if (weightHistory.size >= 2) {
        val trend = weightHistory.last().weight - weightHistory.first().weight
        if (trend > 1) {
            recommendations.add("Kaloriendefizit und mehr Bewegung")
        } else if (trend < -3) {
            recommendations.add("Gesunde Gewichtsabnahme beibehalten")
        }
    }

    if (recommendations.isEmpty()) {
        recommendations.add("Großartige Arbeit! Bleib auf dem richtigen Weg")
        recommendations.add("Erwäge neue Herausforderungen")
    }

    return recommendations
}

private fun calculateConsistency(values: List<Float>): Float {
    if (values.size < 2) return 1f
    val mean = values.average()
    val variance = values.map { (it - mean) * (it - mean) }.average()
    val stdDev = kotlin.math.sqrt(variance).toFloat()
    val coefficientOfVariation = if (mean > 0) stdDev / mean.toFloat() else 1f
    return kotlin.math.max(0f, 1f - coefficientOfVariation)
}
