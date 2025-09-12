package com.example.fitapp.ui.components.adaptive

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.*

/**
 * UI Components for Freeletics-style Adaptive Training
 *
 * Provides real-time workout adaptation, coaching feedback, and performance insights
 * following the Freeletics training personalization model.
 */

@Composable
fun FreeleticsAdaptiveTrainingPanel(
    realTimePerformance: RealTimePerformance?,
    workoutAdaptation: WorkoutAdaptation?,
    difficultyAdjustment: DifficultyAdjustment?,
    exerciseSubstitution: ExerciseSubstitution?,
    adaptiveCoachingFeedback: AdaptiveCoachingFeedback?,
    adaptiveRestCalculation: AdaptiveRestCalculation?,
    onAcceptAdaptation: (WorkoutAdaptation) -> Unit,
    onDeclineAdaptation: () -> Unit,
    onAcceptSubstitution: (ExerciseSubstitution) -> Unit,
    onDeclineSubstitution: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .animateContentSize(),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🚀 Adaptive Training",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = "Adaptive Training",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-time Performance Summary
            realTimePerformance?.let { performance ->
                PerformanceSummaryCard(performance = performance)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Workout Adaptation Alert
            workoutAdaptation?.let { adaptation ->
                WorkoutAdaptationAlert(
                    adaptation = adaptation,
                    onAccept = { onAcceptAdaptation(adaptation) },
                    onDecline = onDeclineAdaptation,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Difficulty Adjustment Indicator
            difficultyAdjustment?.let { adjustment ->
                DifficultyAdjustmentCard(adjustment = adjustment)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Exercise Substitution Suggestion
            exerciseSubstitution?.let { substitution ->
                ExerciseSubstitutionAlert(
                    substitution = substitution,
                    onAccept = { onAcceptSubstitution(substitution) },
                    onDecline = onDeclineSubstitution,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Adaptive Rest Time Info
            adaptiveRestCalculation?.let { restCalc ->
                AdaptiveRestTimeCard(restCalculation = restCalc)
            }
        }
    }
}

@Composable
private fun PerformanceSummaryCard(
    performance: RealTimePerformance,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PerformanceMetric(
                label = "Form",
                value = "${(performance.formQuality * 100).toInt()}%",
                color =
                    when {
                        performance.formQuality > 0.8f -> MaterialTheme.colorScheme.primary
                        performance.formQuality > 0.6f -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    },
            )

            PerformanceMetric(
                label = "RPE",
                value = "${performance.rpe}/10",
                color =
                    when {
                        performance.rpe <= 6 -> MaterialTheme.colorScheme.primary
                        performance.rpe <= 8 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    },
            )

            performance.heartRate?.let { hr ->
                PerformanceMetric(
                    label = "HR",
                    value = "$hr",
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            PerformanceMetric(
                label = "Rep",
                value = "${performance.currentRep}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PerformanceMetric(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WorkoutAdaptationAlert(
    adaptation: WorkoutAdaptation,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when (adaptation.type) {
                        AdaptationType.INCREASE_INTENSITY -> MaterialTheme.colorScheme.primaryContainer
                        AdaptationType.REDUCE_INTENSITY -> MaterialTheme.colorScheme.errorContainer
                        AdaptationType.SUBSTITUTE_EXERCISE -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector =
                        when (adaptation.type) {
                            AdaptationType.INCREASE_INTENSITY -> Icons.AutoMirrored.Filled.TrendingUp
                            AdaptationType.REDUCE_INTENSITY -> Icons.AutoMirrored.Filled.TrendingDown
                            AdaptationType.SUBSTITUTE_EXERCISE -> Icons.Default.SwapHoriz
                            else -> Icons.Default.AutoFixHigh
                        },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = getAdaptationTitle(adaptation.type),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = adaptation.reasoning,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (adaptation.modifications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                adaptation.modifications.first().reason.takeIf { it.isNotEmpty() }?.let { reason ->
                    Text(
                        text = "• $reason",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Ablehnen")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Anwenden")
                }
            }
        }
    }
}

@Composable
private fun DifficultyAdjustmentCard(
    adjustment: DifficultyAdjustment,
    modifier: Modifier = Modifier,
) {
    if (kotlin.math.abs(adjustment.adjustmentFactor - 1.0f) < 0.05f) return // No significant adjustment

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector =
                        if (adjustment.adjustmentFactor > 1.0f) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                    contentDescription = null,
                    tint =
                        if (adjustment.adjustmentFactor > 1.0f) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Schwierigkeit angepasst",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${((adjustment.adjustmentFactor - 1.0f) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (adjustment.adjustmentFactor > 1.0f) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                )
            }

            if (adjustment.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                adjustment.reasons.forEach { reason ->
                    Text(
                        text = "• $reason",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseSubstitutionAlert(
    substitution: ExerciseSubstitution,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Alternative Übung vorgeschlagen",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aktuell:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = substitution.originalExercise.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vorschlag:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = substitution.substitution.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = substitution.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Behalten")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Wechseln")
                }
            }
        }
    }
}

@Composable
private fun AdaptiveRestTimeCard(
    restCalculation: AdaptiveRestCalculation,
    modifier: Modifier = Modifier,
) {
    val isAdjusted = kotlin.math.abs(restCalculation.adjustedRestTime - restCalculation.baseRestTime) > 5f

    if (!isAdjusted) return // Don't show if no significant adjustment

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Adaptive Pause",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = restCalculation.reasoning,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = "${restCalculation.adjustedRestTime.toInt()}s",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
fun AdaptiveCoachingFeedbackCard(
    feedback: AdaptiveCoachingFeedback,
    onActionTaken: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when (feedback.priority) {
                        FeedbackPriority.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                        FeedbackPriority.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
                        FeedbackPriority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer
                        FeedbackPriority.LOW -> MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector =
                        when (feedback.priority) {
                            FeedbackPriority.CRITICAL -> Icons.Default.Warning
                            FeedbackPriority.HIGH -> Icons.Default.PriorityHigh
                            else -> Icons.Default.Psychology
                        },
                    contentDescription = null,
                    tint =
                        when (feedback.priority) {
                            FeedbackPriority.CRITICAL -> MaterialTheme.colorScheme.error
                            FeedbackPriority.HIGH -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        },
                    modifier = Modifier.size(24.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "🤖 AI Coach",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Immediate messages
            feedback.immediateMessages.forEach { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Adaptive actions
            if (feedback.adaptiveActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Empfohlene Anpassungen:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))

                feedback.adaptiveActions.forEach { action ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "• $action",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )

                        TextButton(
                            onClick = { onActionTaken(action) },
                        ) {
                            Text("Anwenden", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealTimePerformanceInsights(
    performance: RealTimePerformance,
    adaptationHistory: List<WorkoutAdaptation>,
    sessionProgress: Float,
    onToggleAdaptiveTraining: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "📊 Performance Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Switch(
                    checked = true, // Would be adaptiveTrainingEnabled
                    onCheckedChange = { onToggleAdaptiveTraining() },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Session progress bar
            LinearProgressIndicator(
                progress = { sessionProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Session Fortschritt: ${sessionProgress.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Adaptation summary
            if (adaptationHistory.isNotEmpty()) {
                Text(
                    text = "Anpassungen diese Session:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(4.dp))

                val adaptationCounts = adaptationHistory.groupingBy { it.type }.eachCount()
                adaptationCounts.forEach { (type, count) ->
                    Text(
                        text = "• ${getAdaptationTitle(type)}: ${count}x",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun getAdaptationTitle(type: AdaptationType): String {
    return when (type) {
        AdaptationType.NO_CHANGE -> "Keine Änderung"
        AdaptationType.INCREASE_INTENSITY -> "Intensität erhöhen"
        AdaptationType.REDUCE_INTENSITY -> "Intensität reduzieren"
        AdaptationType.MODIFY_TECHNIQUE -> "Technik anpassen"
        AdaptationType.SUBSTITUTE_EXERCISE -> "Übung wechseln"
        AdaptationType.ADJUST_REST_TIME -> "Pause anpassen"
        AdaptationType.CHANGE_TEMPO -> "Tempo ändern"
    }
}
