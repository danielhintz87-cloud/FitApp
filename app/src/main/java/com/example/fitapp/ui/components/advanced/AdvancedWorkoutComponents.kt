package com.example.fitapp.ui.components.advanced

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.CoachingTip
import com.example.fitapp.ai.CoachingTipType
import com.example.fitapp.ai.HeartRateZone
import com.example.fitapp.ai.Priority
import com.example.fitapp.ai.ProgressionSuggestion
import com.example.fitapp.ai.SessionMetrics
import com.example.fitapp.services.VideoResource
import com.example.fitapp.services.VideoQuality
import com.example.fitapp.services.RestTimerState
import com.example.fitapp.services.RestSuggestion
import com.example.fitapp.services.NextSetRecommendation
import android.net.Uri

/**
 * Advanced Workout UI Components for Phase 1 Enhancement
 * Real-time performance monitoring and AI-powered coaching interface
 * Enhanced with video guidance and smart rest timer integration
 */

@Composable
fun LivePerformanceDashboard(
    heartRate: Int?,
    heartRateZone: HeartRateZone?,
    repCount: Int,
    volume: Float,
    efficiency: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 Live Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Heart Rate
                PerformanceMetric(
                    icon = Icons.Filled.Favorite,
                    value = heartRate?.toString() ?: "--",
                    unit = "BPM",
                    color = heartRateZone?.color ?: MaterialTheme.colorScheme.outline,
                    label = heartRateZone?.displayName ?: "Herzfrequenz"
                )
                
                // Rep Count
                PerformanceMetric(
                    icon = Icons.Filled.FitnessCenter,
                    value = repCount.toString(),
                    unit = "REPS",
                    color = MaterialTheme.colorScheme.secondary,
                    label = "Wiederholungen"
                )
                
                // Volume
                PerformanceMetric(
                    icon = Icons.Filled.ShowChart,
                    value = "${volume.toInt()}",
                    unit = "KG",
                    color = MaterialTheme.colorScheme.tertiary,
                    label = "Volumen"
                )
                
                // Efficiency
                PerformanceMetric(
                    icon = Icons.Filled.Speed,
                    value = "${(efficiency * 100).toInt()}",
                    unit = "%",
                    color = when {
                        efficiency > 0.8f -> Color(0xFF4CAF50)
                        efficiency > 0.6f -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    label = "Effizienz"
                )
            }
        }
    }
}

@Composable
private fun PerformanceMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    unit: String,
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AdvancedExerciseDisplay(
    exercise: com.example.fitapp.ui.screens.ExerciseStep,
    performance: com.example.fitapp.data.db.WorkoutPerformanceEntity?,
    heartRate: Int?,
    heartRateZone: HeartRateZone?,
    formQuality: Float,
    coachingTips: List<CoachingTip>,
    onRPEChanged: (Int) -> Unit,
    onFormFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exercise Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = exercise.value,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Form Quality Indicator
                FormQualityIndicator(
                    quality = formQuality,
                    onClick = onFormFeedback
                )
            }
            
            // Real-time Metrics Row
            if (heartRate != null || performance != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    heartRate?.let { hr ->
                        QuickMetric(
                            icon = Icons.Filled.Favorite,
                            value = hr.toString(),
                            label = "HR",
                            color = heartRateZone?.color ?: MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    performance?.let { perf ->
                        QuickMetric(
                            icon = Icons.Filled.FitnessCenter,
                            value = "${perf.weight.toInt()}kg",
                            label = "Gewicht",
                            color = MaterialTheme.colorScheme.secondary
                        )
                        
                        QuickMetric(
                            icon = Icons.Filled.Repeat,
                            value = perf.reps.toString(),
                            label = "Reps",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            
            // RPE (Rate of Perceived Exertion) Slider
            RPESelector(
                currentRPE = performance?.perceivedExertion,
                onRPEChanged = onRPEChanged
            )
            
            // Active Coaching Tips
            AnimatedVisibility(
                visible = coachingTips.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                ActiveCoachingTips(tips = coachingTips.take(2)) // Show top 2 tips
            }
        }
    }
}

@Composable
private fun FormQualityIndicator(
    quality: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when {
        quality > 0.8f -> Color(0xFF4CAF50)
        quality > 0.6f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = color.copy(alpha = 0.2f),
            contentColor = color
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Assignment,
            contentDescription = "Form Analysis",
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${(quality * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuickMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun RPESelector(
    currentRPE: Int?,
    onRPEChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Anstrengungsgrad (RPE)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (rpe in 1..10) {
                RPEButton(
                    rpe = rpe,
                    isSelected = currentRPE == rpe,
                    onClick = { onRPEChanged(rpe) }
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Leicht",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "Maximal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun RPEButton(
    rpe: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (!isSelected) {
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
                } else Modifier
            )
    ) {
        Text(
            text = rpe.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
private fun ActiveCoachingTips(
    tips: List<CoachingTip>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "💡 Coach Tipps",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        tips.forEach { tip ->
            CoachingTipCard(tip = tip)
        }
    }
}

@Composable
private fun CoachingTipCard(
    tip: CoachingTip,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (tip.priority) {
        Priority.CRITICAL -> Color(0xFFF44336).copy(alpha = 0.1f)
        Priority.HIGH -> Color(0xFFFF9800).copy(alpha = 0.1f)
        Priority.MEDIUM -> Color(0xFF2196F3).copy(alpha = 0.1f)
        Priority.LOW -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val iconColor = when (tip.priority) {
        Priority.CRITICAL -> Color(0xFFF44336)
        Priority.HIGH -> Color(0xFFFF9800)
        Priority.MEDIUM -> Color(0xFF2196F3)
        Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val icon = when (tip.type) {
        CoachingTipType.FORM_IMPROVEMENT -> Icons.Filled.Assignment
        CoachingTipType.REST_OPTIMIZATION -> Icons.Filled.AccessTime
        CoachingTipType.PROGRESSION_SUGGESTION -> Icons.Filled.TrendingUp
        CoachingTipType.MOTIVATION -> Icons.Filled.EmojiEvents
        CoachingTipType.SAFETY_WARNING -> Icons.Filled.Warning
        CoachingTipType.TECHNIQUE_TIP -> Icons.Filled.Lightbulb
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tip.type.name,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tip.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProgressionSuggestionCard(
    suggestion: ProgressionSuggestion,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onCustomize: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚀 Progression Empfehlung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Text(
                    text = "${(suggestion.confidence * 100).toInt()}% Vertrauen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Text(
                text = suggestion.exerciseName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressionMetric(
                    label = "Gewicht",
                    current = "${suggestion.currentWeight.toInt()}kg",
                    recommended = "${suggestion.recommendedWeight.toInt()}kg",
                    isIncrease = suggestion.recommendedWeight > suggestion.currentWeight
                )
                
                ProgressionMetric(
                    label = "Wiederholungen",
                    current = suggestion.currentReps.toString(),
                    recommended = suggestion.recommendedReps.toString(),
                    isIncrease = suggestion.recommendedReps > suggestion.currentReps
                )
            }
            
            Text(
                text = suggestion.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ablehnen")
                }
                
                OutlinedButton(
                    onClick = onCustomize,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Anpassen")
                }
                
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Annehmen")
                }
            }
        }
    }
}

@Composable
private fun ProgressionMetric(
    label: String,
    current: String,
    recommended: String,
    isIncrease: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = current,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Icon(
                imageVector = if (isIncrease) Icons.Filled.ArrowForward else Icons.Filled.ArrowDownward,
                contentDescription = if (isIncrease) "Increase" else "Decrease",
                tint = if (isIncrease) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = recommended,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isIncrease) Color(0xFF4CAF50) else Color(0xFFFF9800)
            )
        }
    }
}

@Composable
fun AICoachingPanel(
    tips: List<CoachingTip>,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit,
    onTipAction: (CoachingTip) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = "AI Coach",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Coach",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (tips.isNotEmpty()) {
                        Text(
                            text = "${tips.size} Tipps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    IconButton(onClick = onToggleExpansion) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand"
                        )
                    }
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tips.isEmpty()) {
                        Text(
                            text = "Keine aktuellen Tipps verfügbar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        tips.forEach { tip ->
                            ExpandedCoachingTipCard(
                                tip = tip,
                                onAction = { onTipAction(tip) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedCoachingTipCard(
    tip: CoachingTip,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (tip.type) {
                        CoachingTipType.FORM_IMPROVEMENT -> "Form verbessern"
                        CoachingTipType.REST_OPTIMIZATION -> "Pausen optimieren"
                        CoachingTipType.PROGRESSION_SUGGESTION -> "Progression"
                        CoachingTipType.MOTIVATION -> "Motivation"
                        CoachingTipType.SAFETY_WARNING -> "Sicherheit"
                        CoachingTipType.TECHNIQUE_TIP -> "Technik"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = tip.priority.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (tip.priority) {
                        Priority.CRITICAL -> Color(0xFFF44336)
                        Priority.HIGH -> Color(0xFFFF9800)
                        Priority.MEDIUM -> Color(0xFF2196F3)
                        Priority.LOW -> MaterialTheme.colorScheme.outline
                    }
                )
            }
            
            Text(
                text = tip.message,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (tip.actionable && tip.action != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onAction) {
                        Text(tip.action)
                    }
                }
            }
        }
    }
}

/**
 * Interactive Video Player for exercise guidance
 * Supports slow-motion, pause, repeat, and multi-angle views
 */
@Composable
fun ExerciseVideoPlayer(
    videoResource: VideoResource?,
    exerciseTitle: String,
    isPlaying: Boolean = false,
    showOverlays: Boolean = true,
    currentAngle: Int = 0,
    availableAngles: List<String> = listOf("Front", "Side", "Back"),
    onPlayPause: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onAngleChange: (Int) -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Video Title and Quality
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎥 $exerciseTitle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                videoResource?.let { resource ->
                    Chip(
                        onClick = { },
                        label = { 
                            Text(
                                text = "${resource.quality.name} • ${if (resource.isLocal) "📱" else "🌐"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            }
            
            // Video Preview Area (placeholder for actual video player)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (videoResource != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isPlaying) "Playing..." else "Video Ready",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        // Form overlay indicators
                        if (showOverlays) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(3) { 
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Video wird geladen...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            // Video Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }
                
                // Speed Control
                AssistChip(
                    onClick = { onSpeedChange(0.5f) },
                    label = { Text("0.5x") }
                )
                
                AssistChip(
                    onClick = { onSpeedChange(1.0f) },
                    label = { Text("1x") }
                )
                
                // Repeat
                IconButton(onClick = onRepeat) {
                    Icon(
                        imageVector = Icons.Filled.Replay,
                        contentDescription = "Repeat"
                    )
                }
            }
            
            // Multi-Angle Selection
            if (availableAngles.size > 1) {
                Text(
                    text = "Camera Angles:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableAngles.forEachIndexed { index, angle ->
                        FilterChip(
                            selected = currentAngle == index,
                            onClick = { onAngleChange(index) },
                            label = { Text(angle) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Smart Rest Timer with AI-powered recommendations
 */
@Composable
fun SmartRestTimerDisplay(
    timerState: RestTimerState,
    restSuggestion: RestSuggestion?,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (timerState) {
                is RestTimerState.RUNNING -> MaterialTheme.colorScheme.secondaryContainer
                is RestTimerState.PAUSED -> MaterialTheme.colorScheme.errorContainer
                is RestTimerState.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer Status
            when (timerState) {
                is RestTimerState.RUNNING -> {
                    Text(
                        text = "⏱️ Pause Timer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = formatTime(timerState.remaining),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Progress indicator
                    LinearProgressIndicator(
                        progress = (timerState.total - timerState.remaining).toFloat() / timerState.total,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                is RestTimerState.PAUSED -> {
                    Text(
                        text = "⏸️ Timer Pausiert",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = formatTime(timerState.remaining),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                is RestTimerState.COMPLETED -> {
                    Text(
                        text = "✅ Pause beendet!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    timerState.nextSetRecommendation?.let { recommendation ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "📈 Nächster Satz",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = recommendation.suggestion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (recommendation.weightAdjustment != 0f || recommendation.repAdjustment != 0) {
                                    Text(
                                        text = "Anpassung: ${if (recommendation.weightAdjustment > 0) "+" else ""}${recommendation.weightAdjustment}kg, ${if (recommendation.repAdjustment > 0) "+" else ""}${recommendation.repAdjustment} Reps",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                
                else -> {
                    Text(
                        text = "Bereit für das nächste Set",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            // Motivational Message
            restSuggestion?.motivationalMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Form Tips
            restSuggestion?.formTips?.takeIf { it.isNotEmpty() }?.let { tips ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "💡 Form-Tipps",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        tips.forEach { tip ->
                            Text(
                                text = "• $tip",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Timer Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                when (timerState) {
                    is RestTimerState.RUNNING -> {
                        OutlinedButton(onClick = onPause) {
                            Icon(Icons.Filled.Pause, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Pause")
                        }
                        
                        Button(onClick = onSkip) {
                            Icon(Icons.Filled.SkipNext, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Skip")
                        }
                    }
                    
                    is RestTimerState.PAUSED -> {
                        Button(onClick = onResume) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Weiter")
                        }
                        
                        OutlinedButton(onClick = onStop) {
                            Icon(Icons.Filled.Stop, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Stop")
                        }
                    }
                    
                    is RestTimerState.COMPLETED -> {
                        Button(onClick = onStop) {
                            Text("Nächster Satz")
                        }
                    }
                    
                    else -> {
                        // Timer not active
                    }
                }
            }
        }
    }
}

/**
 * Format seconds to MM:SS
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}