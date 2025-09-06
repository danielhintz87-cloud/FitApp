package com.example.fitapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitapp.data.db.AppDatabase
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Personalized Dashboard Framework
 * Allows users to create custom dashboard layouts with drag & drop widgets
 */

/**
 * Main personalized dashboard screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizedDashboardScreen(
    userRole: UserRole = UserRole.INTERMEDIATE,
    focusMode: FocusMode = FocusMode.GENERAL,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dashboardConfig by remember { mutableStateOf(getDefaultDashboardConfig(userRole, focusMode)) }
    var isEditMode by remember { mutableStateOf(false) }
    var showWidgetSelector by remember { mutableStateOf(false) }
    
    // Load user's dashboard configuration
    LaunchedEffect(userRole, focusMode) {
        // In real implementation, load from database
        dashboardConfig = getDefaultDashboardConfig(userRole, focusMode)
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Dashboard Header
        DashboardHeader(
            userRole = userRole,
            focusMode = focusMode,
            isEditMode = isEditMode,
            onEditModeToggle = { isEditMode = !isEditMode },
            onAddWidget = { showWidgetSelector = true },
            onResetLayout = { dashboardConfig = getDefaultDashboardConfig(userRole, focusMode) }
        )
        
        // Dashboard Content
        if (isEditMode) {
            EditableDashboard(
                config = dashboardConfig,
                onConfigChanged = { dashboardConfig = it },
                onWidgetRemoved = { widgetId ->
                    dashboardConfig = dashboardConfig.copy(
                        widgets = dashboardConfig.widgets.filter { it.id != widgetId }
                    )
                }
            )
        } else {
            StaticDashboard(
                config = dashboardConfig
            )
        }
    }
    
    // Widget Selector Dialog
    if (showWidgetSelector) {
        WidgetSelectorDialog(
            availableWidgets = getAvailableWidgets(),
            currentWidgets = dashboardConfig.widgets,
            onWidgetSelected = { widget ->
                dashboardConfig = dashboardConfig.copy(
                    widgets = dashboardConfig.widgets + widget.copy(
                        id = "widget_${System.currentTimeMillis()}",
                        position = DashboardPosition(0, dashboardConfig.widgets.size)
                    )
                )
                showWidgetSelector = false
            },
            onDismiss = { showWidgetSelector = false }
        )
    }
}

/**
 * Dashboard header with controls
 */
@Composable
private fun DashboardHeader(
    userRole: UserRole,
    focusMode: FocusMode,
    isEditMode: Boolean,
    onEditModeToggle: () -> Unit,
    onAddWidget: () -> Unit,
    onResetLayout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🏠 Personal Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${userRole.displayName} • ${focusMode.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isEditMode) {
                        IconButton(onClick = onAddWidget) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Widget")
                        }
                        IconButton(onClick = onResetLayout) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Reset Layout")
                        }
                    }
                    
                    FilledTonalButton(
                        onClick = onEditModeToggle
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Filled.Check else Icons.Filled.Edit,
                            contentDescription = if (isEditMode) "Save" else "Edit"
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (isEditMode) "Fertig" else "Bearbeiten")
                    }
                }
            }
            
            // Quick stats row
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStat("📊", "Progress", "87%")
                QuickStat("🔥", "Streak", "12d")
                QuickStat("⚡", "Energy", "High")
                QuickStat("🎯", "Goal", "85%")
            }
        }
    }
}

@Composable
private fun QuickStat(
    icon: String,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Static dashboard for viewing mode
 */
@Composable
private fun StaticDashboard(
    config: DashboardConfig
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(config.columnCount),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(config.widgets.sortedBy { it.position.row * 10 + it.position.column }) { widget ->
            DashboardWidget(
                widget = widget,
                modifier = Modifier.height(
                    when (widget.size) {
                        WidgetSize.SMALL -> 120.dp
                        WidgetSize.MEDIUM -> 160.dp
                        WidgetSize.LARGE -> 200.dp
                        WidgetSize.EXTRA_LARGE -> 280.dp
                    }
                )
            )
        }
    }
}

/**
 * Editable dashboard for customization mode
 */
@Composable
private fun EditableDashboard(
    config: DashboardConfig,
    onConfigChanged: (DashboardConfig) -> Unit,
    onWidgetRemoved: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(config.columnCount),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(config.widgets) { widget ->
            EditableWidget(
                widget = widget,
                onRemove = { onWidgetRemoved(widget.id) },
                onSizeChange = { newSize ->
                    val updatedWidgets = config.widgets.map { w ->
                        if (w.id == widget.id) w.copy(size = newSize) else w
                    }
                    onConfigChanged(config.copy(widgets = updatedWidgets))
                },
                modifier = Modifier.height(
                    when (widget.size) {
                        WidgetSize.SMALL -> 120.dp
                        WidgetSize.MEDIUM -> 160.dp
                        WidgetSize.LARGE -> 200.dp
                        WidgetSize.EXTRA_LARGE -> 280.dp
                    }
                )
            )
        }
    }
}

/**
 * Individual dashboard widget
 */
@Composable
fun DashboardWidget(
    widget: DashboardWidgetConfig,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        when (widget.type) {
            WidgetType.TODAY_PROGRESS -> TodayProgressWidget(widget)
            WidgetType.WORKOUT_QUICK_START -> WorkoutQuickStartWidget(widget)
            WidgetType.NUTRITION_SUMMARY -> NutritionSummaryWidget(widget)
            WidgetType.WEIGHT_TRACKER -> WeightTrackerWidget(widget)
            WidgetType.ACHIEVEMENT_SHOWCASE -> AchievementShowcaseWidget(widget)
            WidgetType.STREAKS_DISPLAY -> StreaksDisplayWidget(widget)
            WidgetType.HEART_RATE_ZONE -> HeartRateZoneWidget(widget)
            WidgetType.WEEKLY_SUMMARY -> WeeklySummaryWidget(widget)
            WidgetType.QUICK_ACTIONS -> QuickActionsWidget(widget)
            WidgetType.MOTIVATION_QUOTE -> MotivationQuoteWidget(widget)
            WidgetType.FORM_ANALYSIS -> FormAnalysisWidget(widget)
            WidgetType.INJURY_PREVENTION -> InjuryPreventionWidget(widget)
        }
    }
}

/**
 * Editable widget with controls
 */
@Composable
private fun EditableWidget(
    widget: DashboardWidgetConfig,
    onRemove: () -> Unit,
    onSizeChange: (WidgetSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                2.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box {
            // Widget content
            DashboardWidget(widget = widget)
            
            // Edit controls overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                    )
            ) {
                // Remove button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                // Size control
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WidgetSize.values().forEach { size ->
                        val isSelected = widget.size == size
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 12.dp else 8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                                .clickable { onSizeChange(size) }
                        )
                    }
                }
            }
        }
    }
}

// Widget implementations
@Composable
private fun TodayProgressWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 Heute",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (widget.size != WidgetSize.SMALL) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProgressItem("🏃", "Schritte", "8,542", 0.85f)
                ProgressItem("🔥", "Kalorien", "342", 0.68f)
            }
        }
        
        if (widget.size == WidgetSize.LARGE || widget.size == WidgetSize.EXTRA_LARGE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProgressItem("💧", "Wasser", "1.8L", 0.72f)
                ProgressItem("💪", "Training", "75%", 0.75f)
            }
        }
    }
}

@Composable
private fun ProgressItem(
    icon: String,
    label: String,
    value: String,
    progress: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(40.dp)
                .height(2.dp),
            color = when {
                progress > 0.8f -> Color(0xFF4CAF50)
                progress > 0.6f -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
        )
    }
}

@Composable
private fun WorkoutQuickStartWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🏋️ Quick Start",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (widget.size != WidgetSize.SMALL) {
            Button(
                onClick = { /* Start workout */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Push/Pull/Legs")
            }
        }
        
        if (widget.size == WidgetSize.LARGE || widget.size == WidgetSize.EXTRA_LARGE) {
            OutlinedButton(
                onClick = { /* Create custom */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Custom Workout")
            }
        }
    }
}

@Composable
private fun NutritionSummaryWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🍎 Ernährung",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (widget.size != WidgetSize.SMALL) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroProgress("C", 45, 60, Color(0xFF2196F3))
                MacroProgress("P", 120, 150, Color(0xFF4CAF50))
                MacroProgress("F", 35, 50, Color(0xFFFF9800))
            }
        }
        
        if (widget.size == WidgetSize.EXTRA_LARGE) {
            Text(
                text = "Noch 420 kcal bis zum Tagesziel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MacroProgress(
    label: String,
    current: Int,
    target: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$current",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = "/$target",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CircularProgressIndicator(
            progress = { current.toFloat() / target },
            modifier = Modifier.size(24.dp),
            color = color,
            strokeWidth = 3.dp
        )
    }
}

@Composable
private fun WeightTrackerWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "⚖️ Gewicht",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "72.5 kg",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (widget.size != WidgetSize.SMALL) {
            Text(
                text = "-0.3 kg diese Woche",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4CAF50)
            )
        }
        
        if (widget.size == WidgetSize.LARGE || widget.size == WidgetSize.EXTRA_LARGE) {
            Button(
                onClick = { /* Log weight */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gewicht loggen")
            }
        }
    }
}

@Composable
private fun AchievementShowcaseWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🏆 Erfolge",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (widget.size == WidgetSize.SMALL) {
            Text(
                text = "💪 Strength Beast",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AchievementBadge("💪", "Strength")
                AchievementBadge("🔥", "Streak")
                if (widget.size != WidgetSize.MEDIUM) {
                    AchievementBadge("🎯", "Goal")
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(
    icon: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StreaksDisplayWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🔥 Streaks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "12 Tage",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF5722)
        )
        
        if (widget.size != WidgetSize.SMALL) {
            Text(
                text = "Workout Streak",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(7) { index ->
                    Text(
                        text = if (index < 5) "🔥" else "⚪",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HeartRateZoneWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "❤️ HR Zone",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "145",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "BPM",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (widget.size != WidgetSize.SMALL) {
            Text(
                text = "Cardio Zone",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun WeeklySummaryWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📅 Diese Woche",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryItem("Workouts", "4/5")
            SummaryItem("Kalorien", "1,847")
        }
        
        if (widget.size == WidgetSize.LARGE || widget.size == WidgetSize.EXTRA_LARGE) {
            LinearProgressIndicator(
                progress = { 0.8f },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4CAF50)
            )
            Text(
                text = "80% Weekly Goal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickActionsWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "⚡ Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (widget.size == WidgetSize.SMALL) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(Icons.Filled.FitnessCenter, "💪")
                QuickActionButton(Icons.Filled.LocalDrink, "💧")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(Icons.Filled.FitnessCenter, "💪")
                QuickActionButton(Icons.Filled.LocalDrink, "💧")
                QuickActionButton(Icons.Filled.PhotoCamera, "📷")
                if (widget.size != WidgetSize.MEDIUM) {
                    QuickActionButton(Icons.Filled.Add, "⚖️")
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    emoji: String
) {
    FilledTonalIconButton(
        onClick = { /* Quick action */ }
    ) {
        Text(text = emoji, fontSize = 16.sp)
    }
}

@Composable
private fun MotivationQuoteWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "💭 Motivation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "\"Deine einzige Konkurrenz ist die Person, die du gestern warst.\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (widget.size == WidgetSize.LARGE || widget.size == WidgetSize.EXTRA_LARGE) {
            Text(
                text = "- AI Personal Trainer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FormAnalysisWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📊 Form Analysis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "92%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            Text(
                text = "Form Quality",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (widget.size != WidgetSize.SMALL) {
            Text(
                text = "Ausgezeichnete Technik! Bereit für Progression.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InjuryPreventionWidget(widget: DashboardWidgetConfig) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🛡️ Injury Prevention",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🟢",
                fontSize = 20.sp
            )
            Text(
                text = "Low Risk",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4CAF50)
            )
        }
        
        if (widget.size != WidgetSize.SMALL) {
            Text(
                text = "Keine Risikofaktoren erkannt. Weiter so!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Widget selector dialog
 */
@Composable
private fun WidgetSelectorDialog(
    availableWidgets: List<WidgetType>,
    currentWidgets: List<DashboardWidgetConfig>,
    onWidgetSelected: (DashboardWidgetConfig) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Widget hinzufügen")
        },
        text = {
            LazyColumn {
                items(availableWidgets) { widgetType ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onWidgetSelected(
                                    DashboardWidgetConfig(
                                        id = "",
                                        type = widgetType,
                                        size = WidgetSize.MEDIUM,
                                        position = DashboardPosition(0, 0)
                                    )
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = widgetType.icon,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = widgetType.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = widgetType.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}

// Helper functions
private fun getDefaultDashboardConfig(userRole: UserRole, focusMode: FocusMode): DashboardConfig {
    val widgets = when (userRole) {
        UserRole.BEGINNER -> listOf(
            DashboardWidgetConfig("1", WidgetType.TODAY_PROGRESS, WidgetSize.LARGE, DashboardPosition(0, 0)),
            DashboardWidgetConfig("2", WidgetType.WORKOUT_QUICK_START, WidgetSize.MEDIUM, DashboardPosition(1, 0)),
            DashboardWidgetConfig("3", WidgetType.MOTIVATION_QUOTE, WidgetSize.MEDIUM, DashboardPosition(1, 1)),
            DashboardWidgetConfig("4", WidgetType.QUICK_ACTIONS, WidgetSize.SMALL, DashboardPosition(2, 0))
        )
        UserRole.INTERMEDIATE -> listOf(
            DashboardWidgetConfig("1", WidgetType.TODAY_PROGRESS, WidgetSize.MEDIUM, DashboardPosition(0, 0)),
            DashboardWidgetConfig("2", WidgetType.NUTRITION_SUMMARY, WidgetSize.MEDIUM, DashboardPosition(0, 1)),
            DashboardWidgetConfig("3", WidgetType.WORKOUT_QUICK_START, WidgetSize.MEDIUM, DashboardPosition(1, 0)),
            DashboardWidgetConfig("4", WidgetType.STREAKS_DISPLAY, WidgetSize.MEDIUM, DashboardPosition(1, 1)),
            DashboardWidgetConfig("5", WidgetType.WEEKLY_SUMMARY, WidgetSize.LARGE, DashboardPosition(2, 0))
        )
        UserRole.ADVANCED -> listOf(
            DashboardWidgetConfig("1", WidgetType.FORM_ANALYSIS, WidgetSize.MEDIUM, DashboardPosition(0, 0)),
            DashboardWidgetConfig("2", WidgetType.HEART_RATE_ZONE, WidgetSize.MEDIUM, DashboardPosition(0, 1)),
            DashboardWidgetConfig("3", WidgetType.NUTRITION_SUMMARY, WidgetSize.MEDIUM, DashboardPosition(1, 0)),
            DashboardWidgetConfig("4", WidgetType.INJURY_PREVENTION, WidgetSize.MEDIUM, DashboardPosition(1, 1)),
            DashboardWidgetConfig("5", WidgetType.ACHIEVEMENT_SHOWCASE, WidgetSize.LARGE, DashboardPosition(2, 0)),
            DashboardWidgetConfig("6", WidgetType.QUICK_ACTIONS, WidgetSize.SMALL, DashboardPosition(3, 0))
        )
    }
    
    return DashboardConfig(
        columnCount = 2,
        widgets = widgets
    )
}

private fun getAvailableWidgets(): List<WidgetType> {
    return WidgetType.values().toList()
}

// Data classes
@Serializable
data class DashboardConfig(
    val columnCount: Int = 2,
    val widgets: List<DashboardWidgetConfig>
)

@Serializable
data class DashboardWidgetConfig(
    val id: String,
    val type: WidgetType,
    val size: WidgetSize,
    val position: DashboardPosition
)

@Serializable
data class DashboardPosition(
    val column: Int,
    val row: Int
)

enum class UserRole(val displayName: String) {
    BEGINNER("Anfänger"),
    INTERMEDIATE("Fortgeschritten"),
    ADVANCED("Experte")
}

enum class FocusMode(val displayName: String) {
    GENERAL("Allgemein"),
    WEIGHT_LOSS("Abnehmen"),
    MUSCLE_GAIN("Muskelaufbau"),
    STRENGTH("Kraft"),
    ENDURANCE("Ausdauer"),
    COMPETITION_PREP("Wettkampf")
}

enum class WidgetSize {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

enum class WidgetType(
    val displayName: String,
    val description: String,
    val icon: String
) {
    TODAY_PROGRESS("Tagesfortschritt", "Schritte, Kalorien, Wasser", "📊"),
    WORKOUT_QUICK_START("Workout Start", "Schneller Trainingsbeginn", "🏋️"),
    NUTRITION_SUMMARY("Ernährung", "Makros und Kalorien", "🍎"),
    WEIGHT_TRACKER("Gewichtsverlauf", "Aktuelle Gewichtsentwicklung", "⚖️"),
    ACHIEVEMENT_SHOWCASE("Erfolge", "Errungenschaften anzeigen", "🏆"),
    STREAKS_DISPLAY("Streaks", "Tägliche Streak-Anzeige", "🔥"),
    HEART_RATE_ZONE("Herzfrequenz", "Aktuelle HR-Zone", "❤️"),
    WEEKLY_SUMMARY("Wochenzusammenfassung", "Wöchentlicher Überblick", "📅"),
    QUICK_ACTIONS("Schnellaktionen", "Ein-Klick Aktionen", "⚡"),
    MOTIVATION_QUOTE("Motivation", "Tägliches Motivationszitat", "💭"),
    FORM_ANALYSIS("Form-Analyse", "Bewegungsqualität", "📊"),
    INJURY_PREVENTION("Verletzungsprävention", "Risiko-Assessment", "🛡️")
}